package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SubscriptParserTest {
	SubscriptParser parser;
	
	@Before
	public void setup(){
		parser = new SubscriptParser();
	}
	
	@Test
	public void testSubscript(){
		String text = "2 in H~2~0 should be a subscript as should x / 2 in log~x / 2~.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("<sub>2</sub>"));
		assertTrue(elements.getHtml().contains("<sub>x / 2</sub>"));
	}
}
