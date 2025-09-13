import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer implements ILexer {
    private String input;
    private int currentPosition;
    private int line;
    private int column;
    
    // Define SPL keywords
    private static final Set<String> KEYWORDS = new HashSet<>();
    static {
        KEYWORDS.add("neg");
        KEYWORDS.add("not");
        KEYWORDS.add("eq");
        KEYWORDS.add("or");
        KEYWORDS.add("and");
        KEYWORDS.add("plus");
        KEYWORDS.add("minus");
        KEYWORDS.add("mult");
        KEYWORDS.add("div");
        KEYWORDS.add("if");
        KEYWORDS.add("else");
        KEYWORDS.add("halt");
        KEYWORDS.add("print");
        KEYWORDS.add("var");
        KEYWORDS.add("local");
        KEYWORDS.add("return");
        KEYWORDS.add("glob");
        KEYWORDS.add("proc");
        KEYWORDS.add("func");
        KEYWORDS.add("main");
    }

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+");
    private static final Pattern STRING_PATTERN = Pattern.compile("^\"([^\"\\\\]|\\\\.)*\"");
    
    public Lexer(String input) {
        this.input = input;
        this.currentPosition = 0;
        this.line = 1;
        this.column = 1;
    }
    
    @Override
    public Token nextToken() {
        skipWhitespace();
        
        if (currentPosition >= input.length()) {
            return new Token(Token.TokenType.EOF, "");
        }
        
        Token bestMatch = null;
        int longestLength = 0;

        Token identifierToken = tryIdentifier();
        if (identifierToken != null && identifierToken.getValue().length() > longestLength) {
            bestMatch = identifierToken;
            longestLength = identifierToken.getValue().length();
        }

        Token numberToken = tryNumber();
        if (numberToken != null && numberToken.getValue().length() > longestLength) {
            bestMatch = numberToken;
            longestLength = numberToken.getValue().length();
        }

        Token stringToken = tryString();
        if (stringToken != null && stringToken.getValue().length() > longestLength) {
            bestMatch = stringToken;
            longestLength = stringToken.getValue().length();
        }

        Token operatorToken = tryOperator();
        if (operatorToken != null && operatorToken.getValue().length() > longestLength) {
            bestMatch = operatorToken;
            longestLength = operatorToken.getValue().length();
        }

        Token punctuationToken = tryPunctuation();
        if (punctuationToken != null && punctuationToken.getValue().length() > longestLength) {
            bestMatch = punctuationToken;
            longestLength = punctuationToken.getValue().length();
        }
        
        if (bestMatch != null) {
            advance(longestLength);
            return bestMatch;
        }

        char unknownChar = input.charAt(currentPosition);
        advance(1);
        throw new RuntimeException("Unknown character: '" + unknownChar + "' at line " + line + ", column " + column);
    }
    
    private Token tryIdentifier() {
        Matcher matcher = IDENTIFIER_PATTERN.matcher(input.substring(currentPosition));
        
        if (matcher.find()) {
            String value = matcher.group();

            if (KEYWORDS.contains(value)) {
                return new Token(Token.TokenType.KEYWORD, value);
            } else {
                return new Token(Token.TokenType.IDENTIFIER, value);
            }
        }
        
        return null;
    }
    
    private Token tryNumber() {
        Matcher matcher = NUMBER_PATTERN.matcher(input.substring(currentPosition));
        
        if (matcher.find()) {
            String value = matcher.group();
            return new Token(Token.TokenType.LITERAL, value);
        }
        
        return null;
    }
    
    private Token tryString() {
        Matcher matcher = STRING_PATTERN.matcher(input.substring(currentPosition));
        
        if (matcher.find()) {
            String value = matcher.group();
            return new Token(Token.TokenType.LITERAL, value);
        }
        
        return null;
    }
    
    private Token tryOperator() {
        String remaining = input.substring(currentPosition);

        if (remaining.startsWith("==")) {
            return new Token(Token.TokenType.OPERATOR, "==");
        }
        if (remaining.startsWith("!=")) {
            return new Token(Token.TokenType.OPERATOR, "!=");
        }
        if (remaining.startsWith("<=")) {
            return new Token(Token.TokenType.OPERATOR, "<=");
        }
        if (remaining.startsWith(">=")) {
            return new Token(Token.TokenType.OPERATOR, ">=");
        }

        char currentChar = input.charAt(currentPosition);
        switch (currentChar) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '=':
            case '<':
            case '>':
            case '!':
                return new Token(Token.TokenType.OPERATOR, String.valueOf(currentChar));
            default:
                return null;
        }
    }
    
    private Token tryPunctuation() {
        char currentChar = input.charAt(currentPosition);
        
        switch (currentChar) {
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
            case ';':
            case ',':
            case '.':
                return new Token(Token.TokenType.PUNCTUATION, String.valueOf(currentChar));
            default:
                return null;
        }
    }
    
    private void skipWhitespace() {
        while (currentPosition < input.length() && Character.isWhitespace(input.charAt(currentPosition))) {
            if (input.charAt(currentPosition) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            currentPosition++;
        }
    }
    
    private void advance(int count) {
        for (int i = 0; i < count && currentPosition < input.length(); i++) {
            if (input.charAt(currentPosition) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            currentPosition++;
        }
    }
    
    public String getPositionInfo() {
        return "line " + line + ", column " + column;
    }
}