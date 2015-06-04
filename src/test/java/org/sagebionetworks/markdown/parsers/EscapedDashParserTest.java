package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class EscapedDashParserTest {
	EscapedDashParser parser;
	StrikeoutParser strikeoutParser;
	
	@Before
	public void setup() {
		parser = new EscapedDashParser();
		strikeoutParser = new StrikeoutParser();
	}
	
	@Test
	public void testEscaping() {
		String text = "\\-\\-no strikethrough\\-\\-";
		MarkdownElements elements = new MarkdownElements(text);
		//Escape first, then use other parsers
		parser.processLine(elements);
		strikeoutParser.processLine(elements);
		assertEquals(elements.getHtml(), "&#150;&#150;no strikethrough&#150;&#150;");
		assertFalse(elements.getHtml().contains("<del>"));
	}
}
