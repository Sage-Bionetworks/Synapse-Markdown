package org.sagebionetworks.markdown.parsers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;
import org.sagebionetworks.markdown.utils.ServerMarkdownUtils;
import org.sagebionetworks.markdown.utils.SharedMarkdownUtils;


public class UrlAutoLinkParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.LINK_URL);
	MarkdownExtractor extractor;

	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_AUTOLINK_PREFIX + extractor.getCurrentContainerId() + suffix;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String url = m.group(1).trim();
			StringBuilder html = new StringBuilder();
			html.append(ServerMarkdownUtils.getStartLink(getClientHostString(), url));
			html.append(url + "\">");
			html.append(url + ServerMarkdownUtils.END_LINK);
			extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
			
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			m.appendReplacement(sb, containerElement);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}
	
}
