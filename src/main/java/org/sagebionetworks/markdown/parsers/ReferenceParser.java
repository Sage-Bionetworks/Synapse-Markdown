package org.sagebionetworks.markdown.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;
import org.sagebionetworks.markdown.utils.WidgetEncodingUtil;


public class ReferenceParser extends BasicMarkdownElementParser {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.REFERENCE_REGEX);
	ArrayList<String> footnotes;
	List<MarkdownElementParser> parsersOnCompletion;
	int footnoteNumber;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		footnotes = new ArrayList<String>();
		footnoteNumber = 1;
		parsersOnCompletion = simpleParsers;
	}

	@Override
	public void processLine(MarkdownElements line) {
		String input = line.getMarkdown();
		Matcher m = p1.matcher(input);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//Expression has 4 groupings (2 parameter/value pairs.)
			//Store the reference text
			for(int i = 1; i < 4; i += 2) {
				String param = input.substring(m.start(i), m.end(i));
				if(param.contains("text")) {
					footnotes.add(input.substring(m.start(i + 1), m.end(i + 1)));
				}
			}
			
			/*
			 * Insert:
			 * 1) Bookmark target so that footnotes can link back to the reference
			 * 2) add a footnoteId param to the original syntax to tell the renderer which footnote to link to
			 */
			String referenceId = WidgetConstants.REFERENCE_ID_WIDGET_PREFIX + footnoteNumber;
			String footnoteParameter = WidgetConstants.REFERENCE_FOOTNOTE_KEY + "=" + footnoteNumber;
			
			String updated = "<span id=\"" + referenceId + "\">&nbsp;</span>" + input.substring(m.start(), m.end() - 1) + "&amp;" + footnoteParameter + "}";
			updated = Matcher.quoteReplacement(updated);	//Escapes the replacement string for appendReplacement
			m.appendReplacement(sb, updated);
			footnoteNumber++;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		if (footnotes.size() > 0)
			html.append("<hr>");
		StringBuilder footnoteMarkdown = new StringBuilder();
		for(int i = 0; i < footnotes.size(); i++) {
			String footnoteText = WidgetEncodingUtil.decodeValue(footnotes.get(i));
			String targetReferenceId = WidgetConstants.REFERENCE_ID_WIDGET_PREFIX + (i + 1);
			String footnoteId = WidgetConstants.FOOTNOTE_ID_WIDGET_PREFIX + (i + 1);
			
			//Insert bookmark to link back to the reference.
			//SWC-2453: instead of relying on the link parser to insert the bookmark widget, do it here.
			footnoteMarkdown.append(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.BOOKMARK_CONTENT_TYPE + "?");
			footnoteMarkdown.append(WidgetConstants.TEXT_KEY + "=" + "[" + (i + 1) + "]&");
			footnoteMarkdown.append(WidgetConstants.INLINE_WIDGET_KEY + "=true&");
			footnoteMarkdown.append(WidgetConstants.BOOKMARK_KEY + "=" + targetReferenceId);
			footnoteMarkdown.append(WidgetConstants.WIDGET_END_MARKDOWN);			

			//Assign id to the element so that the reference can link to this footnote
			footnoteMarkdown.append("<span id=\"" + footnoteId + "\" class=\"margin-left-5\">" + footnoteText + "</span>");
			footnoteMarkdown.append("<br>");
		}
		String parsedFootnotes = runSimpleParsers(footnoteMarkdown.toString(), parsersOnCompletion);
		html.append(parsedFootnotes);
	}
}
