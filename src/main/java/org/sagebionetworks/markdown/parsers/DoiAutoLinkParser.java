package org.sagebionetworks.markdown.parsers;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.markdown.utils.ServerMarkdownUtils;
import org.sagebionetworks.markdown.utils.SharedMarkdownUtils;

public class DoiAutoLinkParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.LINK_DOI);
	//SWC-1883: need to protect the doi output
	MarkdownExtractor extractor;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		extractor = new MarkdownExtractor();
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String updated = "<a target=\"_blank\" class=\"link\" href=\"http://dx.doi.org/" + m.group(1) + "\">" + m.group(0) +"</a>";
			
			extractor.putContainerIdToContent(getCurrentDivID(), updated);
			
			String containerElement = extractor.getNewElementStart(getCurrentDivID()) + extractor.getContainerElementEnd();
			m.appendReplacement(sb, containerElement);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	private String getCurrentDivID() {
		return WidgetConstants.DIV_ID_DOI_PREFIX + extractor.getCurrentContainerId() + SharedMarkdownUtils.getPreviewSuffix(isPreview);
	}

	@Override
	public void completeParse(Document doc) {
		ServerMarkdownUtils.insertExtractedContentToMarkdown(extractor, doc, true);
	}

}
