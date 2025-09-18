package frontend.lexer;

import java.util.regex.Pattern;

import frontend.tokenizer.ILexer;
import frontend.tokens.Token;
import frontend.tokens.TokenKind;

/**
 * Production-quality lexer for the Students' Programming Language (SPL) 2025
 * Implements SPL vocabulary rules and grammar terminals
 */
public class SPLLexer implements ILexer {
    private String[] tokens;
    private int index = 0;
    private int currentLine = 1;
    private int currentCol = 1;

    // SPL vocabulary regex patterns
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-z][a-z0-9]*$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");
    private static final Pattern STRING_PATTERN = Pattern.compile("^\"[a-zA-Z0-9]*\"$");

    public SPLLexer(String input) {
        tokenize(input);
    }

    /**
     * Main tokenization method - converts input string to tokens
     * Handles SPL-specific tokenization rules including string literals
     */
    private void tokenize(String input) {
        java.util.List<String> tokenList = new java.util.ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                if (inString) {
                    // End of string - include closing quote
                    currentToken.append(c);
                    addToken(tokenList, currentToken.toString());
                    currentToken = new StringBuilder();
                    inString = false;
                } else {
                    // Start of string - save any pending token first
                    addToken(tokenList, currentToken.toString());
                    currentToken = new StringBuilder();
                    currentToken.append(c);
                    inString = true;
                }
            } else if (inString) {
                // Inside string - collect everything including spaces
                currentToken.append(c);
            } else if (Character.isWhitespace(c)) {
                // Outside string, whitespace - finish current token
                addToken(tokenList, currentToken.toString());
                currentToken = new StringBuilder();
                handleWhitespace(c);
            } else if (isDelimiter(c)) {
                // Delimiter character - save current token and delimiter
                addToken(tokenList, currentToken.toString());
                currentToken = new StringBuilder();
                tokenList.add(String.valueOf(c));
            } else {
                // Regular character
                currentToken.append(c);
            }
        }

        // Add final token if any
        addToken(tokenList, currentToken.toString());

        tokens = tokenList.toArray(new String[0]);
    }

    /**
     * Helper method to add non-empty tokens to the list
     */
    private void addToken(java.util.List<String> tokenList, String token) {
        String trimmed = token.trim();
        if (!trimmed.isEmpty()) {
            tokenList.add(trimmed);
        }
    }

    /**
     * Checks if character is a SPL delimiter
     */
    private boolean isDelimiter(char c) {
        return "{}();=>".indexOf(c) >= 0;
    }

    /**
     * Handles whitespace for line/column tracking
     */
    private void handleWhitespace(char c) {
        if (c == '\n') {
            currentLine++;
            currentCol = 1;
        } else {
            currentCol++;
        }
    }

    @Override
    public Token nextToken() {
        if (index >= tokens.length) {
            return new Token(TokenKind.EOF, "", currentLine, currentCol);
        }

        String lexeme = tokens[index++];
        TokenKind kind = recognizeTokenKind(lexeme);

        // Create token with position info
        Token token = new Token(kind, lexeme, currentLine, currentCol);
        currentCol += lexeme.length();

        return token;
    }

    /**
     * Core token recognition method - implements SPL vocabulary rules
     * Order matters: keywords before identifiers, strings before identifiers
     */
    private TokenKind recognizeTokenKind(String lexeme) {
        // Rule 1: Keywords take precedence over user-defined names
        TokenKind keyword = recognizeKeyword(lexeme);
        if (keyword != null) {
            return keyword;
        }

        // Symbols and operators
        TokenKind symbol = recognizeSymbol(lexeme);
        if (symbol != null) {
            return symbol;
        }

        // Rule 4: String literals (quoted sequences)
        if (STRING_PATTERN.matcher(lexeme).matches()) {
            return TokenKind.STRING;
        }

        // Rule 3: Numbers (0 | [1-9][0-9]*)
        if (NUMBER_PATTERN.matcher(lexeme).matches()) {
            return TokenKind.NUMBER;
        }

        // Rule 2: User-defined names [a-z][a-z0-9]*
        if (IDENTIFIER_PATTERN.matcher(lexeme).matches()) {
            return TokenKind.IDENT;
        }

        // Fallback - real implementation might throw lexical error
        System.err.println("Warning: Unrecognized token '" + lexeme + "' - treating as identifier");
        return TokenKind.IDENT;
    }

    /**
     * Recognizes SPL keywords (green terminals from grammar)
     */
    private TokenKind recognizeKeyword(String lexeme) {
        switch (lexeme) {
            // Structure keywords
            case "glob":
                return TokenKind.GLOB;
            case "proc":
                return TokenKind.PROC;
            case "func":
                return TokenKind.FUNC;
            case "main":
                return TokenKind.MAIN;
            case "local":
                return TokenKind.LOCAL;
            case "var":
                return TokenKind.VAR;

            // Control flow keywords
            case "while":
                return TokenKind.WHILE;
            case "do":
                return TokenKind.DO;
            case "until":
                return TokenKind.UNTIL;
            case "if":
                return TokenKind.IF;
            case "else":
                return TokenKind.ELSE;

            // Statement keywords
            case "halt":
                return TokenKind.HALT;
            case "print":
                return TokenKind.PRINT;
            case "return":
                return TokenKind.RETURN;

            // Unary operators
            case "neg":
                return TokenKind.NEG;
            case "not":
                return TokenKind.NOT;

            // Binary operators
            case "eq":
                return TokenKind.EQ;
            case "gt":
                return TokenKind.GT;
            case "or":
                return TokenKind.OR;
            case "and":
                return TokenKind.AND;
            case "plus":
                return TokenKind.PLUS;
            case "minus":
                return TokenKind.MINUS;
            case "mult":
                return TokenKind.MULT;
            case "div":
                return TokenKind.DIV;

            default:
                return null;
        }
    }

    /**
     * Recognizes SPL symbols and punctuation
     */
    private TokenKind recognizeSymbol(String lexeme) {
        switch (lexeme) {
            case "{":
                return TokenKind.LBRACE;
            case "}":
                return TokenKind.RBRACE;
            case "(":
                return TokenKind.LPAREN;
            case ")":
                return TokenKind.RPAREN;
            case ";":
                return TokenKind.SEMI;
            case "=":
                return TokenKind.ASSIGN;
            case ">":
                return TokenKind.GT;
            default:
                return null;
        }
    }

    /**
     * Utility method for debugging - shows all tokens
     */
    public void debugPrintAllTokens() {
        System.out.println("=== SPL Lexer Debug Output ===");
        for (int i = 0; i < tokens.length; i++) {
            String lexeme = tokens[i];
            TokenKind kind = recognizeTokenKind(lexeme);
            System.out.printf("[%d] %s ('%s')%n", i, kind, lexeme);
        }
        System.out.println("=== End Debug Output ===");
    }
}