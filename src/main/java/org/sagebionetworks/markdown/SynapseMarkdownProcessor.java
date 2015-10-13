package org.sagebionetworks.markdown;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.sagebionetworks.markdown.parsers.BlockQuoteParser;
import org.sagebionetworks.markdown.parsers.BoldParser;
import org.sagebionetworks.markdown.parsers.BookmarkTargetParser;
import org.sagebionetworks.markdown.parsers.CenterTextParser;
import org.sagebionetworks.markdown.parsers.CodeParser;
import org.sagebionetworks.markdown.parsers.CodeSpanParser;
import org.sagebionetworks.markdown.parsers.DoiAutoLinkParser;
import org.sagebionetworks.markdown.parsers.EscapedBacktickParser;
import org.sagebionetworks.markdown.parsers.EscapedDashParser;
import org.sagebionetworks.markdown.parsers.EscapedUnderscoreParser;
import org.sagebionetworks.markdown.parsers.EscapedVerticalLineParser;
import org.sagebionetworks.markdown.parsers.HeadingParser;
import org.sagebionetworks.markdown.parsers.HorizontalLineParser;
import org.sagebionetworks.markdown.parsers.ImageParser;
import org.sagebionetworks.markdown.parsers.ItalicsParser;
import org.sagebionetworks.markdown.parsers.LinkParser;
import org.sagebionetworks.markdown.parsers.ListParser;
import org.sagebionetworks.markdown.parsers.MarkdownElementParser;
import org.sagebionetworks.markdown.parsers.MarkdownElements;
import org.sagebionetworks.markdown.parsers.MathParser;
import org.sagebionetworks.markdown.parsers.ReferenceParser;
import org.sagebionetworks.markdown.parsers.RowColumnParser;
import org.sagebionetworks.markdown.parsers.StrikeoutParser;
import org.sagebionetworks.markdown.parsers.SubscriptParser;
import org.sagebionetworks.markdown.parsers.SuperscriptParser;
import org.sagebionetworks.markdown.parsers.SynapseAutoLinkParser;
import org.sagebionetworks.markdown.parsers.SynapseMarkdownWidgetParser;
import org.sagebionetworks.markdown.parsers.TableParser;
import org.sagebionetworks.markdown.parsers.TildeParser;
import org.sagebionetworks.markdown.parsers.UrlAutoLinkParser;
import org.sagebionetworks.markdown.utils.ServerMarkdownUtils;


public class SynapseMarkdownProcessor {
	private static SynapseMarkdownProcessor singleton = null;
	private List<MarkdownElementParser> allElementParsers = new ArrayList<MarkdownElementParser>();
	private Pattern blockquotePatternProtector;
	private CodeParser codeParser;
	private MathParser mathParser;
	public static SynapseMarkdownProcessor getInstance() {
		if (singleton == null) {
			singleton = new SynapseMarkdownProcessor();
		}
		return singleton;
	}

	
	private SynapseMarkdownProcessor() {
		init();
	}
	
	private void init() {
		//protect widget syntax
		allElementParsers.add(new ReferenceParser());
		allElementParsers.add(new BookmarkTargetParser());
		allElementParsers.add(new SynapseMarkdownWidgetParser());
		
		//parsers that handle escaping
		allElementParsers.add(new TildeParser());
		allElementParsers.add(new EscapedUnderscoreParser());
		allElementParsers.add(new EscapedBacktickParser());
		allElementParsers.add(new EscapedVerticalLineParser());
		allElementParsers.add(new EscapedDashParser());
		//other parsers should not affect code spans
		allElementParsers.add(new CodeSpanParser());
		//parsers protecting urls go before other simple parsers
		allElementParsers.add(new ImageParser());
		allElementParsers.add(new DoiAutoLinkParser());
		allElementParsers.add(new LinkParser());
		allElementParsers.add(new UrlAutoLinkParser());
		
		//initialize other markdown element parsers
		allElementParsers.add(new BlockQuoteParser());
		allElementParsers.add(new BoldParser());	
		codeParser = new CodeParser();
		allElementParsers.add(codeParser);
		mathParser = new MathParser();
		allElementParsers.add(mathParser);
		allElementParsers.add(new HeadingParser());
		allElementParsers.add(new HorizontalLineParser());
		allElementParsers.add(new ItalicsParser());
		allElementParsers.add(new ListParser());
		allElementParsers.add(new StrikeoutParser());
		allElementParsers.add(new SubscriptParser());
		allElementParsers.add(new SuperscriptParser());
		allElementParsers.add(new CenterTextParser());
		allElementParsers.add(new SynapseAutoLinkParser());
		allElementParsers.add(new TableParser());
		allElementParsers.add(new RowColumnParser());
		blockquotePatternProtector = Pattern.compile("^&gt;", Pattern.MULTILINE);
	}
	
	/**
	 * This converts the given markdown to html using the given markdown processor.
	 * It also post processes the output html, including:
	 * *sending all links to a new window.
	 * *applying the markdown css classname to entities supported by the markdown.
	 * *auto detects Synapse IDs (and creates links out of them)
	 * *auto detects generic urls (and creates links out of them)
	 * *resolve Widgets!
	 * @param panel
	 * @throws IOException 
	 */
	public String markdown2Html(String markdown, String suffix, String clientHostString) throws IOException {
		String originalMarkdown = markdown;
		if (markdown == null || markdown.equals("")) return "";
		
		//To enable different html levels, we should change the Whitelist.  that's it!
		markdown = Jsoup.clean(markdown, "", Whitelist.none(),  new Document.OutputSettings().prettyPrint(false));
		markdown = blockquotePatternProtector.matcher(markdown).replaceAll(">");
		
		String html = processMarkdown(markdown, allElementParsers, suffix, clientHostString);
		if (html == null) {
			//if the markdown processor fails to convert the md to html (will return null in this case), return the raw markdown instead. (as ugly as it might be, it's better than no information).
			return originalMarkdown; 
		}
		//URLs are automatically resolved from the markdown processor
		html = "<div class=\"markdown\">" + postProcessHtml(html) + "</div>";
		
		return html;
	}
	
	public String processMarkdown(String markdown, List<MarkdownElementParser> parsers, String suffix, String clientHostString) {
		//go through the document once, and apply all markdown parsers to it
		StringBuilder output = new StringBuilder();
		if (suffix == null) {
			suffix = "";
		}
		
		//these are the parsers that only take a single line as input (element does not span across lines)
		List<MarkdownElementParser> simpleParsers = new ArrayList<MarkdownElementParser>();
		
		//these are the processors that report they are in the middle of a multiline element
		List<MarkdownElementParser> activeComplexParsers = new ArrayList<MarkdownElementParser>();
		//the rest of the multiline processors not currently in the middle of an element
		List<MarkdownElementParser> inactiveComplexParsers = new ArrayList<MarkdownElementParser>();
		
		//initialize all processors either in the simple list, or in the inactive list
		for (MarkdownElementParser parser : parsers) {
			if (parser.isInputSingleLine())
				simpleParsers.add(parser);
			else
				inactiveComplexParsers.add(parser);
		}
		
		//reset all of the parsers
		String lowerClientHostString = clientHostString == null ? "" : clientHostString.toLowerCase();
		for (MarkdownElementParser parser : parsers) {
			parser.reset(simpleParsers);
			parser.setSuffix(suffix);
			parser.setClientHostString(lowerClientHostString);
		}
		
		List<String> allLines = new ArrayList<String>();
		for (String line : markdown.split("\n")) {
			allLines.add(line);
		}
		allLines.add("");
		for (String line : allLines) {
			MarkdownElements elements = new MarkdownElements(line);
			//do parsers we're currently in the middle of
			for (MarkdownElementParser parser : activeComplexParsers) {
				parser.processLine(elements);
			}
			
			//only give the option to start new multiline element (complex parser) or process simple elements if we're not in a code block (or a math block)
			if (!codeParser.isInMarkdownElement() && !mathParser.isInMarkdownElement()){
				//then the inactive multiline parsers
				for (MarkdownElementParser parser : inactiveComplexParsers) {
					parser.processLine(elements);
				}
				
				//process the simple processors after complex parsers (the complex parsers clean up the markdown)
				for (MarkdownElementParser parser : simpleParsers) {
					parser.processLine(elements);
				}
			}
				

			
			List<MarkdownElementParser> newActiveComplexParsers = new ArrayList<MarkdownElementParser>();
			List<MarkdownElementParser> newInactiveComplexParsers = new ArrayList<MarkdownElementParser>();
			//add all from the still processing list (maintain order)
			for (MarkdownElementParser parser : activeComplexParsers) {
				if (parser.isInMarkdownElement())
					newActiveComplexParsers.add(parser);
				else
					newInactiveComplexParsers.add(parser);
			}
			
			//sort the rest
			for (MarkdownElementParser parser : inactiveComplexParsers) {
				if (parser.isInMarkdownElement()) //add to the front (reverse their order so that they can have the opportunity to be well formed)
					newActiveComplexParsers.add(0, parser);
				else
					newInactiveComplexParsers.add(parser);
			}
			
			activeComplexParsers = newActiveComplexParsers;
			inactiveComplexParsers = newInactiveComplexParsers;
			
			output.append(elements.getHtml());
			//also tack on a <br />, unless we are a block element (those parsers handle their own newlines
			boolean isInMiddleOfBlockElement = false;
			for (MarkdownElementParser parser : parsers) {
				if (parser.isInMarkdownElement() && parser.isBlockElement()) {
					isInMiddleOfBlockElement = true;
					break;
				}
			}
			if (!isInMiddleOfBlockElement)
				output.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
		
		for (MarkdownElementParser parser : parsers) {
			parser.completeParse(output);
		}
		return output.toString();
	}
	
	/**
	 * After markdown is converted into html, postprocess that html
	 * @param markdown
	 * @return
	 */
	public String postProcessHtml(String html) {
		//using jsoup, since it's already in this project!
		Document doc = Jsoup.parse(html);
		for (MarkdownElementParser parser : allElementParsers) {
			parser.completeParse(doc);
		}
		ServerMarkdownUtils.assignIdsToHeadings(doc);
		doc.outputSettings().prettyPrint(false);
		return doc.html();
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main( String[] args )
    {
		//TODO: provide method to process markdown from the command line.  The code below is a start, 
		// but still need to have maven package it up in a jar with dependencies.
		
		// Check how many arguments were passed in
	    if(args.length != 3)
	    {
	        System.out.println("Usage: java -jar markdown-<version>.jar <client_host> <div suffix> <markdown>");
	        System.exit(0);
	    }
	    
	    String clientHostString = args[0];
	    String suffix = args[1];
	    String markdown = args[2];
	    try {
			String html = getInstance().markdown2Html(markdown, suffix, clientHostString);
			System.out.println(html);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
    }
}
