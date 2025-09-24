package frontend.parser;

import frontend.parser.antlr.SPLParser;

/**
 * Demo class to visualize SPL syntax trees
 * Run this to see how your ANTLR parser builds syntax trees
 */
public class SyntaxTreeDemo {
    
    public static void main(String[] args) {
        // Test different SPL programs to see their tree structures
        testSimpleProgram();
        testProgramWithVariables();
        testProgramWithProcedures();
        testProgramWithFunctions();
        testComplexProgram();
    }
    
    /**
     * Test 1: Minimal SPL program
     */
    private static void testSimpleProgram() {
        System.out.println("TEST 1: MINIMAL PROGRAM");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { } proc { } func { } main { var { } halt }";
        visualizeProgram(program);
    }
    
    /**
     * Test 2: Program with global variables
     */
    private static void testProgramWithVariables() {
        System.out.println("TEST 2: PROGRAM WITH VARIABLES");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { x y counter } proc { } func { } main { var { temp result } halt }";
        visualizeProgram(program);
    }
    
    /**
     * Test 3: Program with procedures
     */
    private static void testProgramWithProcedures() {
        System.out.println("TEST 3: PROGRAM WITH PROCEDURES");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { total } " +
                        "proc { init ( ) { local { } total = 0 } " +
                        "       increment ( x ) { local { temp } temp = x ; total = temp } } " +
                        "func { } " +
                        "main { var { } init ( ) ; increment ( total ) ; print total ; halt }";
        visualizeProgram(program);
    }
    
    /**
     * Test 4: Program with functions
     */
    private static void testProgramWithFunctions() {
        System.out.println("TEST 4: PROGRAM WITH FUNCTIONS");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { value } " +
                        "proc { } " +
                        "func { getvalue ( ) { local { } return value } " +
                        "       calculate ( x y ) { local { result } return result } } " +
                        "main { var { answer } answer = getvalue ( ) ; print answer ; halt }";
        visualizeProgram(program);
    }
    
    /**
     * Test 5: Complex program with all features
     */
    private static void testComplexProgram() {
        System.out.println("TEST 5: COMPLEX PROGRAM");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { counter total zero one } " +
                        "proc { init ( ) { local { } counter = 0 ; total = 0 } } " +
                        "func { getzero ( ) { local { } return zero } " +
                        "       getone ( ) { local { } return one } } " +
                        "main { var { result } " +
                        "  init ( ) ; " +
                        "  result = getzero ( ) ; " +
                        "  if result { print \"zero found\" } else { print \"not zero\" } ; " +
                        "  while result { result = getone ( ) } ; " +
                        "  halt }";
        visualizeProgram(program);
    }
    
    /**
     * Helper method to parse and visualize a program
     */
    private static void visualizeProgram(String program) {
        System.out.println("SPL Program:");
        System.out.println(program);
        System.out.println();
        
        try {
            SPLParserWrapper parser = new SPLParserWrapper(program);
            SPLParser.Spl_progContext tree = parser.parse();
            
            // Create a dummy parser for tree visualization (ANTLR requirement)
            SPLParser dummyParser = new SPLParser(null);
            
            // Display the visual tree structure
            VisualSyntaxTreeExtractor.displayTree(tree, dummyParser);
            
            // Optional: Show tree with node IDs for symbol table work
            System.out.println("TREE WITH NODE IDS (for Symbol Table):");
            VisualSyntaxTreeExtractor.displayTreeWithNodeIds(tree, dummyParser);
            
        } catch (ParseException e) {
            System.err.println("Parser errors found:");
            System.err.println(e.getMessage());
            if (!e.getErrors().isEmpty()) {
                e.getErrors().forEach(error -> System.err.println("  " + error));
            }
        } catch (Exception e) {
            System.err.println("Error parsing program: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(70) + "\n");
    }
    
    /**
     * Interactive method - test your own SPL programs
     */
    public static void testCustomProgram(String program) {
        System.out.println("CUSTOM PROGRAM TEST");
        System.out.println("=" + "=".repeat(50));
        visualizeProgram(program);
    }
}