package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SynapseAutoLinkParserTest {
	SynapseAutoLinkParser parser;
	
	@Before
	public void setup() {
		parser = new SynapseAutoLinkParser();
		parser.reset(null);
	}
	
	@Test
	public void testAutoLink() {
		String line = "Go to syn123";
		MarkdownElements elements = new MarkdownElements(line);
		parser.processLine(elements);
		String result = elements.getHtml();
		assertTrue(result.contains("href=\"#!Synapse:syn123\""));
		assertTrue(result.contains("<a"));
		assertTrue(result.contains("</a>"));
		
		//with version
		line = "Go to syn123.10 test";
		elements = new MarkdownElements(line);
		parser.processLine(elements);
		result = elements.getHtml();
		assertTrue(result.contains("href=\"#!Synapse:syn123/version/10\""));
		assertTrue(result.contains("<a"));
		assertTrue(result.contains("</a>"));
		
		line = "synergy should not match";
		elements = new MarkdownElements(line);
		parser.processLine(elements);
		result = elements.getHtml();
		assertFalse(result.contains("href"));
		assertFalse(result.contains("<a"));
		assertFalse(result.contains("</a>"));
	}
}
