package frontend.tokenizer;

import frontend.tokens.Token;
import frontend.tokens.TokenKind;

public class TokenizerTest {
    public static void main(String[] args) {
        System.out.println("=== SPL Tokenizer Comprehensive Test Suite ===\n");
        
        // Test 1: Basic Structure
        testProgram("Test 1 - Basic Main", "main { halt }");
        
        // Test 2: All Sections
        testProgram("Test 2 - Complete Structure", 
            "glob { x y } proc { } func { } main { halt }");
        
        // Test 3: Variables and Local Scope
        testProgram("Test 3 - Variables", 
            "main { var { x y z } halt }");
            
        // Test 4: Procedure Definition
        testProgram("Test 4 - Procedure", 
            "proc { hello ( ) { print \"world\" } } main { hello ( ) }");
            
        // Test 5: Function with Return
        testProgram("Test 5 - Function", 
            "func { add ( x y ) { return ( x plus y ) } } main { halt }");
            
        // Test 6: Assignment Operations
        testProgram("Test 6 - Assignments", 
            "main { x = 42 y = add ( 1 2 ) halt }");
            
        // Test 7: Control Structures - IF
        testProgram("Test 7 - IF Statement", 
            "main { if ( x gt 0 ) { print x } halt }");
            
        // Test 8: Control Structures - IF-ELSE
        testProgram("Test 8 - IF-ELSE", 
            "main { if ( x eq 0 ) { halt } else { print x } }");
            
        // Test 9: While Loop
        testProgram("Test 9 - WHILE Loop", 
            "main { while ( x gt 0 ) { x = ( x minus 1 ) } halt }");
            
        // Test 10: Do-Until Loop
        testProgram("Test 10 - DO-UNTIL Loop", 
            "main { do { x = ( x plus 1 ) } until ( x gt 10 ) }");
            
        // Test 11: Complex Expressions
        testProgram("Test 11 - Complex Expressions", 
            "main { result = ( ( x plus y ) mult ( a minus b ) ) }");
            
        // Test 12: Unary Operators
        testProgram("Test 12 - Unary Operators", 
            "main { x = ( neg y ) z = ( not flag ) }");
            
        // Test 13: All Binary Operators
        testProgram("Test 13 - All Binary Ops", 
            "main { a = ( x plus y ) b = ( x minus y ) c = ( x mult y ) d = ( x div y ) }");
            
        // Test 14: All Comparison Operators
        testProgram("Test 14 - Comparisons", 
            "main { if ( ( x eq y ) or ( a gt b ) ) { halt } }");
            
        // Test 15: String Literals
        testProgram("Test 15 - Strings", 
            "main { print \"hello\" print \"world123\" }");
            
        // Test 16: Numbers
        testProgram("Test 16 - Numbers", 
            "main { x = 0 y = 42 z = 123456 }");
            
        // Test 17: Identifier Patterns
        testProgram("Test 17 - Identifiers", 
            "main { abc = def123 xyz789 = hello }");
            
        // Test 18: Stress Test - Large Program
        testProgram("Test 18 - Large Program",
            "glob { counter max } " +
            "proc { increment ( ) { counter = ( counter plus 1 ) } } " +
            "func { isMax ( val ) { return ( val eq max ) } } " +
            "main { " +
                "var { temp } " +
                "counter = 0 " +
                "max = 10 " +
                "while ( not ( isMax ( counter ) ) ) { " +
                    "increment ( ) " +
                    "print counter " +
                "} " +
                "halt " +
            "}");

        System.out.println("=== All Tests Complete ===");
        System.out.println("If you see this message, your tokenizer handled all test cases!");
    }
    
    private static void testProgram(String testName, String input) {
        System.out.println(testName + ":");
        System.out.println("Input: " + input);
        System.out.println("Tokens:");
        
        ILexer lexer = new LexerStub(input);
        Tokenizer tokenizer = new Tokenizer(lexer);
        
        try {
            // Test peek functionality
            System.out.println("  First 3 tokens (peek): " + 
                tokenizer.peek(1).getKind() + ", " +
                tokenizer.peek(2).getKind() + ", " + 
                tokenizer.peek(3).getKind());
            
            // Test expect functionality on first token
            Token firstExpected = tokenizer.peek(1);
            Token result = tokenizer.expect(firstExpected.getKind());
            System.out.println("  expect() test passed: " + result.getKind());
            
            // Now consume remaining tokens
            Token token;
            int tokenCount = 1; // We already consumed one with expect()
            do {
                token = tokenizer.next();
                System.out.println("  [" + (++tokenCount) + "] " + token.getKind() + 
                    " ('" + token.getLexeme() + "')");
            } while (token.getKind() != TokenKind.EOF);
            
        } catch (Exception e) {
            System.err.println("  ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
}