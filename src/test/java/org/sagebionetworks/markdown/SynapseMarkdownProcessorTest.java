package org.sagebionetworks.markdown;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.markdown.parsers.MarkdownElements;

public class SynapseMarkdownProcessorTest {

	SynapseMarkdownProcessor processor = SynapseMarkdownProcessor.getInstance();
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void testMarkdown2HtmlEscapeControlCharacters() throws IOException{
		String testString = "& ==> &amp;\" ==> &quot;> ==> &gt;< ==> &lt;' =";
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(actualResult.contains("&amp;"));
		assertTrue(actualResult.contains("==&gt;"));
		assertTrue(actualResult.contains("&amp;&quot;"));
		assertTrue(actualResult.contains("==&gt;"));
		assertTrue(actualResult.contains("&quot;&gt;"));
		assertTrue(actualResult.contains("&lt;'"));
		assertTrue(actualResult.contains("="));
	}
	
	@Test
	public void testWhitespacePreservation() throws IOException{
		String codeBlock = " spaces  and\nnewline    -  test\n  preservation in preformatted  code blocks";
		String testString = "```\n"+codeBlock+"\n```";
		String actualResult = processor.markdown2Html(testString, null, "");
		//it should contain the code block, exactly as written
		assertTrue(actualResult.contains(codeBlock));
	}
	
	@Test
	public void testRemoveAllHTML() throws IOException{
		String testString = "<table><tr><td>this is a test</td><td>column 2</td></tr></table><iframe width=\"420\" height=\"315\" src=\"http://www.youtube.com/embed/AOjaQ7Vl7SM\" frameborder=\"0\" allowfullscreen></iframe><embed>";
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(!actualResult.contains("<table>"));
		assertTrue(!actualResult.contains("<iframe>"));
		assertTrue(!actualResult.contains("<embed>"));
	}

	@Test
	public void testReference() throws IOException{
		//Integration level test for reference widget
		String text = "This is a ref ${reference?text=very simple&inlineWidget=true}.";
		String actualResult = processor.markdown2Html(text, null, "");
		//should find evidence in the output that the reference was identified and processed
		assertTrue(actualResult.contains(WidgetConstants.REFERENCE_FOOTNOTE_KEY));
		assertTrue(actualResult.contains(WidgetConstants.FOOTNOTE_ID_WIDGET_PREFIX));
	}
	
	@Test
	public void testRAssign() throws IOException{
		//testing R assignment operator (html stripping should not alter)
		String testString = "DemoClinicalOnlyModel <- setRefClass(Class  = \"CINModel\",...";
		String actualResult = processor.markdown2Html(testString, null, "");
		//there should be no space between the less than and the dash:
		assertTrue(actualResult.contains("&lt;-"));
	}
	
	@Test
	public void testTableSupport() throws IOException{
		String testString = 
				"|             |          Grouping           ||\nFirst Header  | Second Header | Third Header |\n ------------ | :-----------: | -----------: |\nContent       |          *Long Cell*        ||\nContent       |   **Cell**    |         Cell |\n";
		
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(actualResult.contains("<table"));
		assertTrue(actualResult.contains("<tr>"));
		assertTrue(actualResult.contains("<td>"));
	}
	
	@Test
	public void testTableClassSupport() throws IOException{
		String testString = 
				"{| class=\"border text-align-center\" \n Row 1 Content Cell 1 | Row 1 Content Cell 2 | Row 1 Content Cell 3 \n |} ";
		
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(actualResult.contains("<table"));
		assertTrue(actualResult.contains("class=\"tablesorter markdowntable border text-align-center\""));
		assertTrue(actualResult.contains("<tr>"));
		assertTrue(actualResult.contains("<td>"));
	}
	
	@Test
	public void testListAndHeaderInBlockquote() throws IOException{
		//complicated integration test of all parsers
		String testString = 
			"> * Item 1\n" +
			"> * Item 2\n" +
			">   1. #### SubItem 2a\n" +
			">   2. SubItem 2b\n" +
			"> ``` r\n" +
			"> Then a code block!\n" +
			"> ```";
		
		String actualResult = processor.markdown2Html(testString, null, "");
		String expectedResult = "<blockquote><ul><li><p>Item 1</p></li>";
		String expectedResult2 = "<li><p> Item 2</p><ol start=\"1\"><li><p> </p><h4 id=\"synapseheading0\" level=\"h4\" toc-style=\"toc-indent0\">SubItem 2a</h4><p></p></li><li><p> SubItem 2b</p></li></ol></li></ul>";
		String expectedResult3 = "<pre><code class=\"r\"> Then a code block! </code></pre>";
		String expectedResult4 = "</blockquote>"; 
		assertTrue(actualResult.contains(expectedResult));
		assertTrue(actualResult.contains(expectedResult2));
		assertTrue(actualResult.contains(expectedResult3));
		assertTrue(actualResult.contains(expectedResult4));
		
		String testString2 =
			"1. First\n" +
			"2. Second\n" +
			"\n" +
			"> * Item 1\n" +
			"> * Item 2\n" +
			">   1. #### SubItem 2a\n" +
			">   2. SubItem 2b\n" +
			"> ``` r\n" +
			"> Then a code block!\n" +
			"> ```";

		String expectedResult5 = "<ol start=\"1\"><li><p>First</p></li><li><p>Second</p></li></ol><br />";
		String expectedResult6 = "<blockquote> <ul><li><p>Item 1</p></li>";
		String expectedResult7 = "<li><p>Item 2</p><ol start=\"1\"><li><p></p><h4 id=\"synapseheading0\" level=\"h4\" toc-style=\"toc-indent0\">SubItem 2a</h4><p></p></li><li><p>SubItem 2b</p></li></ol></li></ul>";
		String expectedResult8 = "<pre><code class=\"r\"> Then a code block! </code></pre><br />";
		String expectedResult9 = "</blockquote>";
		
		String actualResult2 = processor.markdown2Html(testString2, null, "");
		assertTrue(actualResult2.contains(expectedResult5));
		assertTrue(actualResult2.contains(expectedResult6));
		assertTrue(actualResult2.contains(expectedResult7));
		assertTrue(actualResult2.contains(expectedResult8));
		assertTrue(actualResult2.contains(expectedResult9));
	}
	
	@Test
	public void testTableThenHR() throws IOException{
		//complicated integration test of all parsers
		String testString = "Tau | MAPT | MCF7,BT20\nVASP | VASP | MCF7,BT20\nXIAP | XIAP | MCF7,BT20\n--------------------------------\n## Additional Data Details";
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(actualResult.contains("<hr"));
		assertFalse(actualResult.contains("<del"));
	}
	
	@Test
	public void testMarkdownInFencedCode() throws IOException{
		String markdown1 = "**bold**";
		String markdown2 = "_italicized_ ";
		String testString = "```java\nString s = \"should not be "+markdown1+"\";\n```\n"+markdown2;
		String actualResult = processor.markdown2Html(testString, null, "");
		//verify that it still contains raw markdown in the code block, but not raw markdown from outside the code block
		assertTrue(actualResult.contains(markdown1));
		assertFalse(actualResult.contains(markdown2));
	}
	
	@Test
	public void testHtmlStripping() throws IOException{
		String testString = "Configure s3cmd by executing\n`python s3cmd --configure s3://&lt;your_bucket_name&gt;`";
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(actualResult.contains("&lt;"));
		assertTrue(actualResult.contains("&gt;"));
		
	}
	
	@Test
	public void testCenterHeadlineText() throws IOException{
		String testString = "##->Centered Headline<-";
		String actualResult = processor.markdown2Html(testString, null, "");
		assertTrue(actualResult.contains("h2"));
		assertTrue(actualResult.contains("text-align-center"));
		
	}
	
	@Test
	public void testSpaces() throws IOException{
		String testString = "The hi in t**hi**s is bold. No spaces in H~2~O.";
		String result = processor.markdown2Html(testString, null, "");
		assertTrue(result.contains("<strong>hi</strong>"));
		assertTrue(result.contains("No spaces in H<sub>2</sub>O."));
	}
	
	
	@Test
	public void testRowColumnMultiline() throws IOException{
		
		String testString =
			"{row}\n"+
			"{column width=6}\n"+
			"### Hello World\n"+
			"{column}\n"+
			"{column width=6}\n"+ 
			"### How are you?\n"+
			"{column}\n"+
			"{row}";
		String result = processor.markdown2Html(testString, null, "");
		
		assertTrue(result.contains("</div><div"));
	}
	
	@Test
	public void testDoiLink() throws IOException {
		//integration test for Synapse doi link
		String exampleDoi="doi:10.7303/syn2699915";
		String testString = "link to the "+exampleDoi+" for the challenge";
		String result = processor.markdown2Html(testString, null, "");
		
		//link should go refer to the doi
		assertTrue(result.contains("href=\"https://doi.org/"+exampleDoi+"\""));
		//and the text itself should contain "doi"
		assertTrue(result.contains(">"+exampleDoi+"<"));

	}
	
	@Test
	public void testSuffix() throws IOException{
		String text = "Contains a Synapse widget ${fakewidget?param1=a} in the markdown.";
		String suffix = "-suffix-test";
		//suffix is used in the div id, the caller can find each element container (to inject a Synapse widget).
		String actualResult = processor.markdown2Html(text, suffix, "");
		assertTrue(actualResult.contains(suffix));
	}
	
	@Test
	public void testSWC2854() throws IOException{
		String text = "Text in this line after (0.01<LALA<0.05)";
		String text2= " is not included in the output html";
		//suffix is used in the div id, the caller can find each element container (to inject a Synapse widget).
		String actualResult = processor.markdown2Html(text + text2, "", "");
		assertTrue(actualResult.contains(text2));
	}
	
}
