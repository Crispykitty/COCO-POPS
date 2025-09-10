package frontend.tokens;

public class Token {
    private final TokenKind kind;
    private final String lexeme;
    private final int line;
    private final int col;

    public Token(TokenKind kind, String lexeme, int line, int col) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    public TokenKind getKind() { return kind; }
    public String getLexeme() { return lexeme; }
    public int getLine() { return line; }
    public int getCol() { return col; }

    @Override
    public String toString() {
        return String.format("%s('%s') at %d:%d", kind, lexeme, line, col);
    }
}
