package frontend.semantic;

import frontend.parser.SPLParserWrapper;
import frontend.parser.SymbolTable;
import frontend.parser.antlr.SPLParser;

public class TypeCheckerDemo {

    public static void main(String[] args) {
        testValidProgram();
        testTypeErrors();
    }

    private static void testValidProgram() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST 1: VALID PROGRAM - Function with numeric return");
        System.out.println("=".repeat(60));
        
        // ✅ FIXED: Functions must return ATOM (variable or number), not expressions
        String program = 
            "glob { x y result } " +
            "proc { } " +
            "func { " +
            "  getx ( ) { local { } return x } " +  // ✅ Returns variable (ATOM)
            "} " +
            "main { " +
            "  var { } " +
            "  result = getx ( ) ; " +
            "  if ( result > 0 ) { print result } ; " +
            "  halt " +
            "}";
        
        runTypeCheck(program);
    }

    private static void testTypeErrors() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST: PROGRAM WITH TYPE ERRORS");
        System.out.println("=".repeat(60));

        String program = "glob { x } " +
                "proc { } " +
                "func { } " +
                "main { " +
                "  var { flag } " +
                "  if ( x plus 5 ) { halt } ; " + // ERROR: condition not boolean
                "  while 10 { halt } ; " + // ERROR: condition not boolean
                "  halt " +
                "}";

        runTypeCheck(program);
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
                System.out.println("\n✓ PROGRAM IS SEMANTICALLY VALID\n");
            } else {
                System.out.println("\n✗ PROGRAM HAS SEMANTIC ERRORS\n");
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}