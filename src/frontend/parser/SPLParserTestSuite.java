package frontend.parser;

/**
 * Fixed SPL Parser Test Suite with correct SPL syntax
 * Updated to follow SPL specification exactly
 */
public class SPLParserTestSuite {

    public static void main(String[] args) {
        System.out.println("SPL Parser Comprehensive Test Suite");
        System.out.println("===================================");

        // Basic structure tests
        runBasicTests();
        
        // Advanced feature tests
        runAdvancedTests();
        
        // Error handling tests
        runErrorTests();
        
        // Integration tests
        runIntegrationTests();
        
        System.out.println("\nTest Suite Complete!");
    }
    
    private static void runBasicTests() {
        System.out.println("\n--- BASIC STRUCTURE TESTS (FIXED) ---");
        
        // Test 1: Minimal valid program (FIXED - must have var block in main)
        testProgram("Minimal Program", 
            "glob { } proc { } func { } main { var { } halt }", true);
        
        // Test 2: Global variables (FIXED)
        testProgram("Global Variables", 
            "glob { x y z } proc { } func { } main { var { } halt }", true);
        
        // Test 3: Main with local variables (already correct)
        testProgram("Main Local Variables",
            "glob { } proc { } func { } main { var { temp count } halt }", true);
        
        // Test 4: All sections populated (FIXED)
        testProgram("All Sections",
            "glob { x } proc { test ( ) { local { } halt } } " +
            "func { getx ( ) { local { } return x } } " +
            "main { var { } halt }", true);
    }
    
    private static void runAdvancedTests() {
        System.out.println("\n--- ADVANCED FEATURE TESTS (FIXED) ---");
        
        // Test 5: Procedures with parameters (FIXED)
        testProgram("Procedure with Parameters",
            "glob { } proc { greet ( name ) { local { } print name } } " +
            "func { } main { var { x } greet ( x ) }", true);
        
        // Test 6: Functions with return values (FIXED)
        testProgram("Function with Return",
            "glob { five } proc { } " +
            "func { getfive ( ) { local { } return five } } " +
            "main { var { x } x = getfive ( ) ; halt }", true);
        
        // Test 7: Control structures - IF (FIXED)
        testProgram("IF Statement",
            "glob { x } proc { } func { } " +
            "main { var { } if ( x gt 0 ) { print x } ; halt }", true);
        
        // Test 8: Control structures - IF-ELSE (FIXED)
        testProgram("IF-ELSE Statement",
            "glob { x } proc { } func { } " +
            "main { var { } if ( x eq 0 ) { halt } else { print x } }", true);
        
        // Test 9: WHILE loop (FIXED)
        testProgram("WHILE Loop",
            "glob { x } proc { } func { } " +
            "main { var { } while ( x gt 0 ) { x = ( x minus 1 ) } ; halt }", true);
        
        // Test 10: DO-UNTIL loop (FIXED)
        testProgram("DO-UNTIL Loop",
            "glob { x } proc { } func { } " +
            "main { var { } do { x = ( x plus 1 ) } until ( x gt 10 ) ; halt }", true);
        
        // Test 11: Complex expressions (FIXED)
        testProgram("Complex Expressions",
            "glob { x y a b } proc { } func { } " +
            "main { var { result } result = ( ( x plus y ) mult ( a minus b ) ) ; halt }", true);
        
        // Test 12: All unary operators (FIXED)
        testProgram("Unary Operators",
            "glob { y flag } proc { } func { } " +
            "main { var { x z } x = ( neg y ) ; z = ( not flag ) ; halt }", true);
        
        // Test 13: All binary operators (FIXED)
        testProgram("All Binary Operators",
            "glob { x y } proc { } func { } " +
            "main { var { a b c d e f g h } " +
            "a = ( x plus y ) ; b = ( x minus y ) ; c = ( x mult y ) ; " +
            "d = ( x div y ) ; e = ( x eq y ) ; f = ( x gt y ) ; " +
            "g = ( x or y ) ; h = ( x and y ) ; halt }", true);
        
        // Test 14: String output (FIXED)
        testProgram("String Output",
            "glob { } proc { } func { } " +
            "main { var { } print \"hello\" ; halt }", true);
        
        // Test 15: Max parameters (3) (FIXED)
        testProgram("Maximum Parameters",
            "glob { result } proc { test ( a b c ) { local { x y z } halt } } " +
            "func { getresult ( x y z ) { local { } return result } } " +
            "main { var { } test ( 1 2 3 ) }", true);
    }
    
    private static void runErrorTests() {
        System.out.println("\n--- ERROR HANDLING TESTS ---");
        
        // Test 16: Missing main section (should fail)
        testProgram("Missing Main (Should Fail)",
            "glob { } proc { } func { }", false);
        
        // Test 17: Invalid identifier (should fail) - keep this test as-is since it should fail
        testProgram("Invalid Identifier (Should Fail)",
            "glob { 123var } proc { } func { } main { var { } halt }", false);
        
        // Test 18: More than 3 parameters (should fail)
        testProgram("Too Many Parameters (Should Fail)",
            "glob { } proc { test ( a b c d ) { local { } halt } } " +
            "func { } main { var { } halt }", false);
        
        // Test 19: Missing semicolon between instructions (should fail)
        testProgram("Missing Semicolon (Should Fail)",
            "glob { x y } proc { } func { } " +
            "main { var { } print x print y ; halt }", false);
        
        // Test 20: Unmatched braces (should fail)
        testProgram("Unmatched Braces (Should Fail)",
            "glob { proc { } func { } main { var { } halt }", false);
    }
    
    private static void runIntegrationTests() {
        System.out.println("\n--- INTEGRATION TESTS (FIXED) ---");
        
        // Test 21: Complex realistic program (FIXED)
        testProgram("Complex Program",
            "glob { counter total zero one } " +
            "proc { init ( ) { local { } counter = 0 ; total = 0 } } " +
            "func { " +
                "getzero ( ) { local { } return zero } " +
                "getone ( ) { local { } return one } " +
            "} " +
            "main { var { result } " +
            "  init ( ) ; " +
            "  result = getzero ( ) ; " +
            "  print result ; " +
            "  result = getone ( ) ; " +
            "  print result ; " +
            "  halt }", true);
        
        // Test 22: Nested control structures (FIXED)
        testProgram("Nested Control Structures",
            "glob { x y z } proc { } func { } " +
            "main { var { } " +
            "  if ( x gt 0 ) { " +
            "    while ( y gt 0 ) { " +
            "      if ( z eq 0 ) { " +
            "        do { z = ( z plus 1 ) } until ( z gt 5 ) " +
            "      } else { y = ( y minus 1 ) } " +
            "    } " +
            "  } else { print \"done\" } ; " +
            "  halt }", true);
            
        // Test 23: Simple assignment test
        testProgram("Simple Assignment",
            "glob { x } proc { } func { } " +
            "main { var { } x = 42 ; halt }", true);
            
        // Test 24: Simple procedure call
        testProgram("Simple Procedure Call", 
            "glob { } proc { hello ( ) { local { } print \"world\" } } " +
            "func { } main { var { } hello ( ) ; halt }", true);
    }
    
    private static void testProgram(String testName, String input, boolean shouldPass) {
        System.out.printf("%-35s: ", testName);
        
        try {
            SPLParserWrapper parser = new SPLParserWrapper(input);
            
            if (shouldPass) {
                // Test parsing
                parser.parse();
                
                // Test information extraction
                ProgramInfo info = parser.parseAndExtractInfo();
                
                // Test validation
                ValidationResult validation = parser.validate();
                
                if (validation.hasErrors() && shouldPass) {
                    System.out.println("FAIL (Validation errors: " + validation.getErrors() + ")");
                } else {
                    System.out.println("PASS");
                }
                
            } else {
                // Should fail - expect exception
                try {
                    parser.parse();
                    System.out.println("FAIL (Expected error but parsing succeeded)");
                } catch (ParseException e) {
                    System.out.println("PASS (Expected failure: " + e.getMessage() + ")");
                }
            }
            
        } catch (ParseException e) {
            if (shouldPass) {
                System.out.println("FAIL (Unexpected error: " + e.getMessage() + ")");
            } else {
                System.out.println("PASS (Expected failure)");
            }
        } catch (Exception e) {
            System.out.println("ERROR (Unexpected exception: " + e.getMessage() + ")");
        }
    }
}