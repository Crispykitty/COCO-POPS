package frontend.tokenizer;

import frontend.tokens.Token;

public class ParseException extends Exception {
    private final Token token;

    public ParseException(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return String.format("ParseError: %s at line %d, col %d", 
            getMessage(), token.getLine(), token.getCol());
    }
}
