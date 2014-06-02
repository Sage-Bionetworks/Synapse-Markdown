package org.sagebionetworks.markdown.parsers;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ReferenceParserTest {
	ReferenceParser parser;
	List<MarkdownElementParser> simpleParsers;
	
	@Before
	public void setup(){
		SynapseMarkdownWidgetParser widgetParser = new SynapseMarkdownWidgetParser();
		widgetParser.reset(null);
		simpleParsers = new ArrayList<MarkdownElementParser>();
		simpleParsers.add(widgetParser);		
		LinkParser linkParser = new LinkParser();
		linkParser.reset(simpleParsers);
		simpleParsers.add(linkParser);	
		parser = new ReferenceParser();
		parser.reset(simpleParsers);
	}
	
	@Test
	public void testReference() throws IOException {
		//Test different ordering of parameters
		String text = "The statement was from here ${reference?inlineWidget=true&amp;text=Smith John%2E Cooking book%2E August 2 2013}.";
		String text2 = "The statement was from here ${reference?text=Smith John%2E Cooking book%2E August 2 2013&amp;inlineWidget=true}.";
		MarkdownElements elements = new MarkdownElements(text);
		MarkdownElements elements2 = new MarkdownElements(text2);
		StringBuilder output = new StringBuilder();
		parser.processLine(elements);
		output.append(elements.getHtml());
		parser.processLine(elements2);
		output.append(elements2.getHtml());
		String result = output.toString();
		assertTrue(result.contains("${reference?inlineWidget=true&amp;text=Smith John%2E Cooking book%2E August 2 2013&amp;footnoteId=1}."));
		assertTrue(result.contains("${reference?text=Smith John%2E Cooking book%2E August 2 2013&amp;inlineWidget=true&amp;footnoteId=2}."));
		//Check for footnotes at end of document
		assertTrue(result.contains("<span id=\"wikiReference1\"></span>"));
		assertTrue(result.contains("${reference?inlineWidget=true&amp;text=Smith John%2E Cooking book%2E August 2 2013&amp;footnoteId=1}"));
		assertTrue(result.contains("<span id=\"wikiReference2\"></span>"));
		assertTrue(result.contains("${reference?text=Smith John%2E Cooking book%2E August 2 2013&amp;inlineWidget=true&amp;footnoteId=2}"));
	}
	
	@Test
	public void testReferenceWithUrl() throws IOException {
		String text = "The statement was from here ${reference?text=So et al%2E %5BYahoo%5D%28http%3A%2F%2Fwww%2Eyahoo%2Ecom%29%2E July 2013&amp;inlineWidget=true}.";
		MarkdownElements elements = new MarkdownElements(text);
		StringBuilder output = new StringBuilder();
		parser.processLine(elements);
		output.append(elements.getHtml());
		
		elements = new MarkdownElements("");
		parser.processLine(elements);
		output.append(elements.getHtml());
		parser.completeParse(output);
		
		String result = output.toString();
		//After complete parse, this is footnote section
		assertTrue(result.contains("<hr>")); 
		//See if link regex is detected. Check for container of link.
		assertTrue(result.contains("<span id=\"link-0\"></span>"));
		
	}
	
}