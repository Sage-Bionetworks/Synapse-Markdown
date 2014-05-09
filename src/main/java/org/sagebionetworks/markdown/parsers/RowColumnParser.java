package org.sagebionetworks.markdown.parsers;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;
import org.sagebionetworks.markdown.utils.ServerMarkdownUtils;

public class RowColumnParser extends BasicMarkdownElementParser {
	Pattern rowPattern = Pattern.compile(MarkdownRegExConstants.ROW_REGEX);
	Pattern columnPattern = Pattern.compile(MarkdownRegExConstants.COLUMN_REGEX);
	private boolean isInRow;
	private boolean isInColumn;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		isInRow = false;
		isInColumn = false;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		StringBuffer sb = new StringBuffer();
		
		//detect rows
		Matcher m = rowPattern.matcher(line.getMarkdown());
		while(m.find()) {
			//found row, add html and flip isInRow
			if (!isInRow) {
				m.appendReplacement(sb, "<div class=\"row\">");	
			} else {
				m.appendReplacement(sb, "</div>");
			}
			isInRow = !isInRow;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
		
		//detect columns
		sb = new StringBuffer();
		m = columnPattern.matcher(line.getMarkdown());
		while(m.find()) {
			//found column, add html and flip isInColumn
			String parameters = m.group(1);
			StringBuilder cssClassString = new StringBuilder();
			if (parameters.trim().length() > 0) {
				Scanner s = null;
				try {
					//alternate between key and value
					boolean isKey = true;
					
					//split on whitespace or '='
					s = new Scanner(parameters).useDelimiter("[\\s=]");
					while (s.hasNext()) {
						//recognized key?
						String v = s.next();
						if (v.trim().length() > 0) {
							if (isKey) {
								//key
								if (v.equalsIgnoreCase("width")) {
									cssClassString.append(" col-sm-");
								} else if (v.equalsIgnoreCase("offset")) {
									cssClassString.append(" col-sm-offset-");
								}
								//next token will be the value
								isKey = false;
							} else {
								//value
								cssClassString.append(v + " ");
								//next token will be a key
								isKey = true;
							}
						}
					}
				} finally {
					if (s != null)
						s.close();
				}
			}
		     
			if (!isInColumn) {
				m.appendReplacement(sb, "<div class=\"" + cssClassString.toString() + "\">");	
			} else {
				m.appendReplacement(sb, "</div>");
			}
			isInColumn = !isInColumn;
		}
		
		m.appendTail(sb);
		if (isInRow && isInColumn) {
			//if we are in the row, do not output an html line break unless we're in a column too
			sb.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public boolean isBlockElement() {
		//we handle newlines
		return true;
	}
	
	@Override
	public boolean isInMarkdownElement() {
		return isInRow;
	}

}
