package frontend;

import frontend.lexer.SPLLexer;
import frontend.tokenizer.ILexer;
import frontend.tokenizer.Tokenizer;
import frontend.tokens.Token;
import frontend.tokens.TokenKind;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String input;
        
        // Check if file argument provided
        if (args.length > 0) {
            // Read from file
            try {
                System.out.println("Attempting to read file: " + args[0]);
                input = readFile(args[0]);
                System.out.println("Successfully read " + input.length() + " characters from file");
                System.out.println("File content preview: " + input.substring(0, Math.min(50, input.length())) + "...");
            } catch (IOException e) {
                System.err.println("Error reading file '" + args[0] + "': " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            // Default test program
            input = "main { halt }";
            System.out.println("No file specified, using default test program");
        }

        // Create lexer and tokenizer
        ILexer lexer = new SPLLexer(input);
        Tokenizer tokenizer = new Tokenizer(lexer);

        // Process tokens
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
            System.err.println("Error during tokenization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reads the entire content of a file as a string
     * @param filename Path to the file to read
     * @return File contents as string
     * @throws IOException If file cannot be read
     */
    private static String readFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readString(path);
    }
}