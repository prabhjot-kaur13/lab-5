import java.util.Scanner;
import java.io.FileReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.util.Stack;
import java.util.Map;

public class htmlTranslator {
	private static final String titleMarker = "title: ";
	private static final String listStartMarker = "-";
	private static final int PARAGRAPH = 1;
	private static final int UNORDEREDLIST = 2;

	private static final int NoPart = 1;
	private static final int InParagraph = 2;
	private static final int InUnorderedList = 3;

	private int blockStatus = NoPart;
	private Stack<Integer> tagList = new Stack<Integer>();
	private Map<Integer, String> tagConvert = Map.of( PARAGRAPH, "p", UNORDEREDLIST, "ul");

	// Determine if a given input line matches the format for a title
	// for the document;

	private static Boolean isTitleLine( String line ) {
		Boolean titleLine = false;

		// Start with the simple requirement that the title
		// keyword must start the line.

		if ((line.length() >= titleMarker.length()) && (titleMarker.equals(line.substring(0, titleMarker.length())))) {
			titleLine = true;
		}

		return titleLine;
	}

	// Take as a precondition that we have determined this line to
	// be a proper start to the title header line.
	// The CMS only allows a single header line for the file, so
	// we can take a simple approach to printing the content.
	// We are also told that there are no formatting codes in the title
	// line, so we can take the line text as-is as the title.

	private static void translateTitleLine( String line ) {
		System.out.print("<head>\n<title>");
		System.out.print(line.substring(titleMarker.length(), line.length()));
		System.out.println("</title>\n</head>");
	}

	// Close all tags of a paragraph or an unordered list as if that block is ending.

	private void closeTextTags() {
		while (!tagList.empty()) {
			System.out.println( "</"+tagConvert.get(tagList.pop())+">");
		}
	}

	// Determine whether or not a line of text is an item for a list.

	private Boolean isListItem( String line ) {
		Boolean isList = false;

		// All list items are defined to start with a particular list marker 
		// at the start of the line.

		if (listStartMarker.equals(line.substring(0, 1))) {
			isList = true;
		}

		return isList;
	}

	// Translate a single line of a file from the CMS notation to
	// html 1.0 format.  The method uses accumulated context from
	// previous lines, which piles up in the class' attributes.

	private void translateBodyLine( String line ) {
		if (line.length() > 0) {
			// Check to see if we're starting any block of text in the body
			if (blockStatus == NoPart) {
				int toStart = PARAGRAPH; // default block of text

				blockStatus = InParagraph;
				// The only other kind of block is an unordered list.  Check for that.
				if (isListItem(line)) {
					toStart = UNORDEREDLIST;
					blockStatus = InUnorderedList;
				}

				// Start the block with appropriate tags and record of nesting.
				System.out.println("<"+tagConvert.get(toStart)+">");
				tagList.push( toStart );
			}

			// Now handle the line itself.

			System.out.println( line );
		}
	}

	// Translate the contents of a file from the CMS notation
	// to html 1.0 format.

	public void translateFile( String filename ) {
		BufferedReader userfile = null;
		String inputLine = "";

		try {
			userfile = new BufferedReader( new FileReader( filename ) );

			// The file opened, so include an opening HTML tag

			System.out.println("<html>");

			// Ignore any leading blank lines

			while ( ((inputLine = userfile.readLine()) != null) &&
			        (inputLine.length() == 0)) {
				// Do nothing but consume the line
			}

			// We have a line in memory.  Check for a title line.
			// Process it and then bring in the next line of
			// the file so the upcoming while loop always has
			// a line already loaded.

			if (isTitleLine( inputLine )) {
				translateTitleLine( inputLine );
				inputLine = userfile.readLine();
			}

			// Know that we will have some web page body, even if empty.
			// Start that part of the document.

			System.out.println("<body>");

			// Iterate through the body of the file, one line at a time.
			// Recall that we already have one line in memory.

			while ( inputLine != null) {
				// Blank lines signal a transition.  Close whatever we're in.
				// Make the choice that multiple blank lines in a row will represent a 
				// break in style, rather than multiple empty paragraphs.

				if (inputLine.length() == 0) {
					if (blockStatus != NoPart) {
						closeTextTags();
						blockStatus = NoPart;
					}
				} else {
					translateBodyLine( inputLine );
				}

				inputLine = userfile.readLine();
			}

			// Close any outstanding paragraph or list tags.

			closeTextTags();

			// Close the body and HTML tags

			System.out.println("</body>\n</html>");
		} catch (Exception e) {
			System.out.println( e.getMessage() );
		} finally {
			// Close the file, if it was opened.

			try {
				userfile.close();
			} catch (Exception error) {
				System.out.println( error.getMessage() );
			}
		}
	}
}

