package org.sagebionetworks.markdown.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;

public class EscapedVerticalLineParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.VERTICAL_LINE_ESCAPED_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("&#124;"));
	}
}
