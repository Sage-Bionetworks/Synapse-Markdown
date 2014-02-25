package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class StrikeoutParserTest {
	StrikeoutParser parser;
	
	@Before
	public void setup(){
		parser = new StrikeoutParser();
	}
	
	@Test
	public void testStrikeout(){
		String text = "This is correct --not this part--.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		assertTrue(elements.getHtml().contains("<del>not this part</del>"));
	}
}
