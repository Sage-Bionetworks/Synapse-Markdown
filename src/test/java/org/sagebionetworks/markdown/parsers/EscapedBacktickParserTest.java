package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class EscapedBacktickParserTest {
	EscapedBacktickParser parser;
	CodeSpanParser codeParser;
	
	@Before
	public void setup() {
		parser = new EscapedBacktickParser();
		codeParser = new CodeSpanParser();
	}
	
	@Test
	public void testEscaping() {
		String text = "\\`not code span\\`";
		MarkdownElements elements = new MarkdownElements(text);
		//Escape first, then use other parsers
		parser.processLine(elements);
		codeParser.processLine(elements);
		assertEquals(elements.getHtml(), "&#96;not code span&#96;");
		assertFalse(elements.getHtml().contains("<code>not code span</code>"));
	}
}
