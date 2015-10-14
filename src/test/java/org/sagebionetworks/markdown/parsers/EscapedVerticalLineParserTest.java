package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class EscapedVerticalLineParserTest {
	EscapedVerticalLineParser parser;
	ItalicsParser italicsParser;
	
	@Before
	public void setup() {
		parser = new EscapedVerticalLineParser();
		italicsParser = new ItalicsParser();
	}
	
	@Test
	public void testEscaping() {
		String text = "file\\|number\\|1";
		MarkdownElements elements = new MarkdownElements(text);
		//Escape first, then use other parsers
		parser.processLine(elements);
		italicsParser.processLine(elements);
		assertEquals(elements.getHtml(), "file&#124;number&#124;1");
		assertFalse(elements.getHtml().contains("<em>number</em>"));
	}
}
