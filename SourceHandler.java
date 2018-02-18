
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.util.ArrayList;


public class SourceHandler {
    ArrayList<String> sourceCode;
    String currentLine; // Contents of the current line.
    int cursor = 0; // The index of the cursor on the current lineNumber.
    int lineNumber = 0;   // The current lineNumber number. The line number is
                          // the sourceCode index + 1.
    int totalLines = 0;
    char peek;      // The char at the cursor.
    public static final String INTEGER = "^[0-9]+";
    public static final String IDENTIFIER = "^[a-zA-Z][a-zA-Z0-9_]*";
    Pattern integer;
    Pattern identifier;

    SourceHandler() {
        integer = Pattern.compile(INTEGER);
        identifier = Pattern.compile(IDENTIFIER);
        this.sourceCode = new ArrayList<String>();
    }

    SourceHandler(String s) {
        this();
        currentLine = s;
    }

    // Add a new line to the source code.
    // This is for loading files.
    public void addLine(String s) {
        sourceCode.add(s);
        this.totalLines++;
    }

    // Set the current line to the next available line of code.
    // Skip blank lines.
    // Return false if there is no more lines left.
    public boolean nextLine() {
        while(this.lineNumber <= this.totalLines) {
            this.lineNumber++;
            this.cursor = 0;
            this.currentLine = sourceCode.get(lineNumber-1);
            if(!this.currentLine.equals("")) {
                this.depad(); // Set the cursor to the first character and set peek.
                return true;
            }
        }
        return false;
    }
    // Match the string at cursor.
    // Return true if it matches. Update the cursor.
    public void match(String s) {
        boolean success = currentLine.substring(cursor).startsWith(s);
        if(!success)
            jpawcl.abort(this, "Expected: "+s);
        cursor += s.length();
        if(cursor >= currentLine.length())
            peek = '\0';
        else {
            peek = currentLine.charAt(cursor);
            depad();
        }
    }

    // See if there is anything remaining on the current lineNumber.
    public boolean endOfLine() {
        depad();
        return peek == '\0';
    }

    // If there is an identifier return it, otherwise return null.
    public String getIdentifier() {
        Matcher matchIdentifier = identifier.matcher(currentLine.substring(cursor));
        if(matchIdentifier.find())
            return matchIdentifier.group();
        return null;
    }

    // If there is an integer return it, otherwise return null.
    public String getInteger() {
        Matcher matchInteger = integer.matcher(currentLine.substring(cursor));
        if(matchInteger.find())
            return matchInteger.group();
        return null;
    }

    // Move the cursor to the first non-whitespace character.
    public void depad() {
        while(currentLine.charAt(cursor)==' ' || currentLine.charAt(cursor) == '\t')
            cursor++;
        // Check to see if we have hit the end of the current lineNumber.
        if(cursor >= currentLine.length())
            peek = '\0';
        else
            peek = currentLine.charAt(cursor);
    }

    // Point to where an error occurred.
    public void error() {
        System.out.println(currentLine);
        // Ugly way to create a string of cursor number of spaces.
        String pointer = new String(new char[cursor]).replace('\0', ' ')+"^";
        System.out.println(pointer);
    }

    public String rest() { // For debugging.
        return currentLine.substring(cursor);
    }
}
