package frontend;

import frontend.tokenizer.ILexer;
import frontend.tokenizer.LexerStub;
import frontend.tokenizer.Tokenizer;
import frontend.tokens.Token;
import frontend.tokens.TokenKind;

public class Main {
    public static void main(String[] args) {
        // Test the tokenizer with a simple SPL program
        String input = "main { halt }";

        // Create a lexer stub for testing
        ILexer lexer = new LexerStub(input);
        Tokenizer tokenizer = new Tokenizer(lexer);

        // Test the tokenizer
        System.out.println("Testing SPL Compiler Tokenizer:");
        System.out.println("Input: " + input);
        System.out.println("\nTokens:");

        try {
            Token token;
            do {
                token = tokenizer.next();
                System.out.println("  " + token);
            } while (token.getKind() != TokenKind.EOF);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
