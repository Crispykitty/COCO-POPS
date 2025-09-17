package frontend.tokenizer;

import frontend.tokens.Token;
import frontend.tokens.TokenKind;
import java.util.regex.Pattern;

public class LexerStub implements ILexer {
    private String[] stubTokens;
    private int index = 0;
    private int currentLine = 1;
    private int currentCol = 1;
    
    // Regex patterns for SPL vocabulary
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-z][a-z0-9]*$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");
    private static final Pattern STRING_PATTERN = Pattern.compile("^\"[a-zA-Z0-9]*\"$");
    
    public LexerStub(String input) {
        // Enhanced tokenization that handles strings properly
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inString = false;
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '"') {
                if (inString) {
                    // End of string - include closing quote
                    currentToken.append(c);
                    tokens.add(currentToken.toString());
                    currentToken = new StringBuilder();
                    inString = false;
                } else {
                    // Start of string - save any pending token first
                    if (currentToken.length() > 0) {
                        String token = currentToken.toString().trim();
                        if (!token.isEmpty()) tokens.add(token);
                        currentToken = new StringBuilder();
                    }
                    currentToken.append(c);
                    inString = true;
                }
            } else if (inString) {
                // Inside string - collect everything
                currentToken.append(c);
            } else if (Character.isWhitespace(c)) {
                // Outside string, whitespace - finish current token
                if (currentToken.length() > 0) {
                    String token = currentToken.toString().trim();
                    if (!token.isEmpty()) tokens.add(token);
                    currentToken = new StringBuilder();
                }
            } else if ("{}();=>".indexOf(c) >= 0) {
                // Delimiter character - save current token and delimiter
                if (currentToken.length() > 0) {
                    String token = currentToken.toString().trim();
                    if (!token.isEmpty()) tokens.add(token);
                    currentToken = new StringBuilder();
                }
                tokens.add(String.valueOf(c));
            } else {
                // Regular character
                currentToken.append(c);
            }
        }
        
        // Don't forget the last token
        if (currentToken.length() > 0) {
            String token = currentToken.toString().trim();
            if (!token.isEmpty()) tokens.add(token);
        }
        
        stubTokens = tokens.toArray(new String[0]);
    }
    
    @Override
    public Token nextToken() {
        if (index >= stubTokens.length) {
            return new Token(TokenKind.EOF, "", currentLine, currentCol);
        }
        
        String lexeme = stubTokens[index++];
        TokenKind kind = recognizeToken(lexeme);
        
        // Simple position tracking (real lexer would be more sophisticated)
        Token token = new Token(kind, lexeme, currentLine, currentCol);
        currentCol += lexeme.length();
        
        return token;
    }
    
    private TokenKind recognizeToken(String lexeme) {
        // Keywords (must be checked before IDENT)
        switch (lexeme) {
            case "glob": return TokenKind.GLOB;
            case "proc": return TokenKind.PROC;
            case "func": return TokenKind.FUNC;
            case "main": return TokenKind.MAIN;
            case "local": return TokenKind.LOCAL;
            case "var": return TokenKind.VAR;
            case "while": return TokenKind.WHILE;
            case "do": return TokenKind.DO;
            case "until": return TokenKind.UNTIL;
            case "if": return TokenKind.IF;
            case "else": return TokenKind.ELSE;
            case "halt": return TokenKind.HALT;
            case "print": return TokenKind.PRINT;
            case "return": return TokenKind.RETURN;
            case "neg": return TokenKind.NEG;
            case "not": return TokenKind.NOT;
            case "eq": return TokenKind.EQ;
            case "gt": return TokenKind.GT;
            case "or": return TokenKind.OR;
            case "and": return TokenKind.AND;
            case "plus": return TokenKind.PLUS;
            case "minus": return TokenKind.MINUS;
            case "mult": return TokenKind.MULT;
            case "div": return TokenKind.DIV;
        }
        
        // Symbols
        switch (lexeme) {
            case "{": return TokenKind.LBRACE;
            case "}": return TokenKind.RBRACE;
            case "(": return TokenKind.LPAREN;
            case ")": return TokenKind.RPAREN;
            case ";": return TokenKind.SEMI;
            case "=": return TokenKind.ASSIGN;
            case ">": return TokenKind.GT;
        }
        
        // String literals (must be checked before IDENT)
        if (STRING_PATTERN.matcher(lexeme).matches()) {
            return TokenKind.STRING;
        }
        
        // Numbers
        if (NUMBER_PATTERN.matcher(lexeme).matches()) {
            return TokenKind.NUMBER;
        }
        
        // Identifiers (user-defined names)
        if (IDENTIFIER_PATTERN.matcher(lexeme).matches()) {
            return TokenKind.IDENT;
        }
        
        // If we can't recognize it, default to IDENT (real lexer would throw error)
        return TokenKind.IDENT;
    }
}