public class Token {

    public enum TokenType {
        KEYWORD, IDENTIFIER, LITERAL, OPERATOR, PUNCTUATION, WHITESPACE, COMMENT, UNKNOWN, EOF
    }

    private TokenType type;
    private String value;
    private int line;
    private int column;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
        this.line = -1; 
        this.column = -1;
    }

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        if (line != -1 && column != -1) {
            return "Token{" + "type=" + type + ", value='" + value + "', line=" + line + ", column=" + column + '}';
        } else {
            return "Token{" + "type=" + type + ", value='" + value + '\'' + '}';
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Token token = (Token) obj;
        return type == token.type && value.equals(token.value);
    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + value.hashCode();
    }
}