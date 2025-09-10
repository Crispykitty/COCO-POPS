package frontend.tokenizer;
import frontend.tokens.Token;

public interface ILexer {
    Token nextToken();
}