package frontend.semantic;

import frontend.parser.SPLParserWrapper;
import frontend.parser.SymbolTable;
import frontend.parser.antlr.SPLParser;

public class TypeCheckerComprehensiveTests {

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SPL TYPE CHECKER - COMPREHENSIVE TEST SUITE");
        System.out.println("=".repeat(80));
        
        // Valid programs
        test1_SimpleArithmetic();
        test2_BooleanLogic();
        test3_NestedConditions();
        test4_FunctionWithParameters();
        test5_ProcedureCall();
        test6_DoUntilLoop();
        test7_ComplexExpressions();
        
        // Invalid programs (should catch type errors)
        test8_TypeMismatchInAssignment();
        test9_InvalidConditionTypes();
        test10_WrongParameterCount();
        test11_MixedBooleanNumeric();
        test12_UnaryOperatorMisuse();
    }

    // ========== VALID PROGRAMS ==========

    private static void test1_SimpleArithmetic() {
        printTestHeader("TEST 1: Simple Arithmetic Operations (VALID)");
        String program = 
            "glob { a b c } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { result } " +
            "  a = 10 ; " +
            "  b = 20 ; " +
            "  c = ( a plus b ) ; " +
            "  result = ( c mult 2 ) ; " +
            "  print result ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test2_BooleanLogic() {
        printTestHeader("TEST 2: Boolean Logic in Conditions (VALID)");
        String program = 
            "glob { x y } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  x = 5 ; " +
            "  y = 10 ; " +
            "  if ( x > y ) { " +
            "    print \"x greater\" " +
            "  } else { " +
            "    print \"y greater\" " +
            "  } ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test3_NestedConditions() {
        printTestHeader("TEST 3: Nested Conditions with AND/OR (VALID)");
        String program = 
            "glob { age income } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  age = 25 ; " +
            "  income = 50000 ; " +
            "  if ( ( age > 18 ) and ( income > 30000 ) ) { " +
            "    print \"Approved\" " +
            "  } ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test4_FunctionWithParameters() {
        printTestHeader("TEST 4: Function with Parameters (VALID)");
        String program = 
            "glob { x } " +
            "proc { } " +
            "func { " +
            "  getvalue ( ) { " +
            "    local { } " +
            "    return x " +
            "  } " +
            "} " +
            "main { " +
            "  var { m } " +
            "  x = 42 ; " +
            "  m = getvalue ( ) ; " +
            "  print m ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test5_ProcedureCall() {
        printTestHeader("TEST 5: Procedure with Side Effects (VALID)");
        String program = 
            "glob { counter } " +
            "proc { " +
            "  increment ( step ) { " +
            "    local { } " +
            "    counter = ( counter plus step ) " +
            "  } " +
            "} " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  counter = 0 ; " +
            "  increment ( 5 ) ; " +
            "  print counter ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test6_DoUntilLoop() {
        printTestHeader("TEST 6: Do-Until Loop (VALID)");
        String program = 
            "glob { count } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  count = 0 ; " +
            "  do { " +
            "    count = ( count plus 1 ) ; " +
            "    print count " +
            "  } until ( count eq 5 ) ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test7_ComplexExpressions() {
        printTestHeader("TEST 7: Complex Nested Expressions (VALID)");
        String program = 
            "glob { x y z } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { result } " +
            "  x = 10 ; " +
            "  y = 20 ; " +
            "  z = 5 ; " +
            "  result = ( ( x plus y ) mult ( z minus 2 ) ) ; " +
            "  if ( ( result > 50 ) or ( z eq 5 ) ) { " +
            "    print result " +
            "  } ; " +
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    // ========== INVALID PROGRAMS (TYPE ERRORS) ==========

    private static void test8_TypeMismatchInAssignment() {
        printTestHeader("TEST 8: Type Mismatch in Assignment (INVALID)");
        String program = 
            "glob { x } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  x = ( 5 > 3 ) ; " +  // ERROR: assigning boolean to numeric variable
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test9_InvalidConditionTypes() {
        printTestHeader("TEST 9: Invalid Condition Types (INVALID)");
        String program = 
            "glob { x y } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  x = 10 ; " +
            "  y = 20 ; " +
            "  if ( x plus y ) { halt } ; " +  // ERROR: numeric expression in condition
            "  while ( x mult 2 ) { halt } ; " +  // ERROR: numeric expression in condition
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test10_WrongParameterCount() {
        printTestHeader("TEST 10: Wrong Parameter Count (INVALID)");
        String program = 
            "glob { } " +
            "proc { " +
            "  donothing ( x y ) { " +
            "    local { } " +
            "    x = ( x plus y ) " +
            "  } " +
            "} " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  donothing ( 5 ) ; " +  // ERROR: procedure expects 2 params, given 1
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test11_MixedBooleanNumeric() {
        printTestHeader("TEST 11: Mixed Boolean and Numeric Operations (INVALID)");
        String program = 
            "glob { x y } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  x = 5 ; " +
            "  y = ( x and 10 ) ; " +  // ERROR: 'and' requires boolean operands
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    private static void test12_UnaryOperatorMisuse() {
        printTestHeader("TEST 12: Unary Operator Type Errors (INVALID)");
        String program = 
            "glob { x flag } " +
            "proc { } " +
            "func { } " +
            "main { " +
            "  var { } " +
            "  x = 10 ; " +
            "  flag = ( not x ) ; " +  // ERROR: 'not' requires boolean operand
            "  halt " +
            "}";
        runTypeCheck(program);
    }

    // ========== HELPER METHODS ==========

    private static void printTestHeader(String testName) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(testName);
        System.out.println("=".repeat(80));
    }

    private static void runTypeCheck(String program) {
        try {
            // Parse the program
            SPLParserWrapper parser = new SPLParserWrapper(program);
            SPLParser.Spl_progContext tree = parser.parse();
            SymbolTable symbolTable = parser.getSymbolTable();

            // Run semantic analysis
            SemanticAnalyzer analyzer = new SemanticAnalyzer(symbolTable);
            boolean isValid = analyzer.analyze(tree);

            if (isValid) {
                System.out.println("\n✅ PROGRAM IS SEMANTICALLY VALID\n");
            } else {
                System.out.println("\n❌ PROGRAM HAS SEMANTIC ERRORS\n");
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}