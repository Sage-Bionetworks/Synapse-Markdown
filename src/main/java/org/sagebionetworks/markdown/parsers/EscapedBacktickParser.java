package org.sagebionetworks.markdown.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;

public class EscapedBacktickParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.BACKTICK_ESCAPED_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("&#96;"));
	}

}
