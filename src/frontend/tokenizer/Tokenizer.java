package frontend.tokenizer;
import frontend.tokens.Token;
import frontend.tokens.TokenKind;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final ILexer lexer;
    private final List<Token> buffer;
    private int position = 0;
    
    public Tokenizer(ILexer lexer) {
        this.lexer = lexer;
        this.buffer = new ArrayList<>();
    }
    
    public Token peek(int k) {
        ensureBufferSize(position + k);
        return buffer.get(position + k - 1);
    }
    
    public Token next() {
        ensureBufferSize(position + 1);
        return buffer.get(position++);
    }
    
    public boolean is(TokenKind kind) {
        return peek(1).getKind() == kind;
    }
    
    public Token expect(TokenKind kind) throws ParseException {
        Token token = next();
        if (token.getKind() != kind) {
            throw new ParseException(
                String.format("Expected %s but found %s at line %d, col %d", 
                    kind, token.getKind(), token.getLine(), token.getCol()), 
                token);
        }
        return token;
    }
    
    private void ensureBufferSize(int requiredSize) {
        while (buffer.size() < requiredSize) {
            buffer.add(lexer.nextToken());
        }
    }
}