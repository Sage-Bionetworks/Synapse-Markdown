package org.sagebionetworks.markdown.parsers;

public class UnorderedMarkdownList extends MarkdownList {
	public UnorderedMarkdownList(int depth) {
		super(depth);
	}
	@Override
	public String getStartListHtml() {
		return "<ul>";
	}
	@Override
	public String getEndListHtml() {
		return "</ul>";
	}

}
