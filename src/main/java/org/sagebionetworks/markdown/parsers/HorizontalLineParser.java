package org.sagebionetworks.markdown.parsers;
import java.util.regex.Pattern;

import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;


public class HorizontalLineParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.HR_REGEX1);
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.HR_REGEX2);

	@Override
	public void processLine(MarkdownElements line) {
		String testLine = line.getMarkdown().replaceAll(" ", "");
		boolean isHr = p1.matcher(testLine).matches() || p2.matcher(testLine).matches();
		if (isHr) {
			//output hr
			line.updateMarkdown("<hr>");
		}
	}
}
