package org.sagebionetworks.markdown.parsers;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class BasicMarkdownElementParser implements MarkdownElementParser {
	protected String suffix="";
	protected String clientHostString="";
	
	/**
	 * Can leave alone if element parser has no state 
	 */
	public void reset(List<MarkdownElementParser> simpleParsers) {
	}
	
	/**
	 * Can leave along if parser does not need to operate on the entire html document after processing is complete (most don't).
	 */
	public void completeParse(StringBuilder html) {
	}

	/**
	 *  If all parsing is complete on a single line, then this method can be ignored. 
	 */
	public boolean isInMarkdownElement() {
		return false;
	}

	/**
	 * If output html is preformatted, the this will inform the processor.
	 */
	public boolean isBlockElement() {
		return false;
	}
	
	public boolean isInputSingleLine() {
		return true;
	}

	protected String getLineWithoutHTML(String line) {
		return Jsoup.parse(line).text();
	}
	
	/**
	 * Can leave along if parser does not need to operate on the entire html document after processing is complete (most don't).
	 */
	public void completeParse(Document doc) {
	}

	public void setSuffix(String suffix) {
		this.suffix= suffix;
	}
	
	public String getClientHostString() {
		return clientHostString;
	}
	
	public void setClientHostString(String clientHostString) {
		this.clientHostString = clientHostString;
	}
	
	public String runSimpleParsers(String line, List<MarkdownElementParser> simpleParsers) {
		MarkdownElements elements = new MarkdownElements(line);
		for (MarkdownElementParser parser : simpleParsers) {
			parser.processLine(elements);
		}
		return elements.getHtml();
	}
	
	public abstract void processLine(MarkdownElements line);
}
