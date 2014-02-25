package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BoldParserTest {
	BoldParser parser;
	
	@Before
	public void setup(){
		parser = new BoldParser();
	}
	
	@Test
	public void testBold(){
		String text = "**this** should be bold, and so should __that__";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("<strong>this</strong>"));
		assertTrue(elements.getHtml().contains("<strong>that</strong>"));
	}
}
