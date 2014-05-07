package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CenterTextParserTest {
	CenterTextParser parser;
	
	@Before
	public void setup(){
		parser = new CenterTextParser();
	}
	
	@Test
	public void testCenterText(){
		String text = "I want -&gt;this text&lt;- to have the center text style.";
		MarkdownElements elements = new MarkdownElements(text);
		parser.processLine(elements);
		//verify the correct text is centered
		assertTrue(elements.getHtml().contains(">this text</"));
		//and the center text style is there
		assertTrue(elements.getHtml().contains("text-align-center"));
	}
}
