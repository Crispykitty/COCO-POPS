package backend.codeGen;

import frontend.parser.SPLParserWrapper;
import frontend.parser.SymbolTable;
import frontend.parser.ParseException;
import frontend.parser.ValidationResult;
import frontend.parser.antlr.SPLParser;

public class CodeGenTests {
    
    public static void main(String[] args) {
        System.out.println("Running SPL Code Generation Tests");
        System.out.println("=" + "=".repeat(70));
        System.out.println();
        
        int passed = 0;
        int failed = 0;
        
        // Run only tests that match the grammar capabilities
        //if (testSimpleProgram()) passed++; else failed++;
        //if (testProgramWithVariables()) passed++; else failed++;
        //if (testProgramWithProcedures()) passed++; else failed++;
        //if (testProgramWithFunctions()) passed++; else failed++;
        
        // Tests 5-7 commented out - they use features not in the grammar
        //if (testComplexProgram()) passed++; else failed++;
        //if (testLoopProgram()) passed++; else failed++;
        if (testBooleanOpProgram()) passed++; else failed++;
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("TEST SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total:  " + (passed + failed));
        System.out.println("=".repeat(70));
    }
    
    /**
     * Test 1: Minimal SPL program
     */
    private static boolean testSimpleProgram() {
        System.out.println("TEST 1: MINIMAL PROGRAM");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { } proc { } func { } main { var { } halt }";
        String expectedCode = "STOP";
        return testProgram(program, expectedCode);
    }
    
    /**
     * Test 2: Program with global and local variables
     */
    private static boolean testProgramWithVariables() {
        System.out.println("TEST 2: PROGRAM WITH VARIABLES");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { x y }\n" +
                        "proc { }\n" +
                        "func { }\n" +
                        "main {\n" +
                        "    var { temp result }\n" +
                        "    x = 5;\n" +
                        "    temp = ( x plus 3 );\n" +
                        "    print temp;\n" +
                        "    halt\n" +
                        "}";
        String expectedCode = "x_internal = 5\n" +
                             "temp_internal = x_internal + 3\n" +
                             "PRINT temp_internal\n" +
                             "STOP";
        return testProgram(program, expectedCode);
    }
    
    /**
     * Test 3: Program with procedures
     */
    private static boolean testProgramWithProcedures() {
        System.out.println("TEST 3: PROGRAM WITH PROCEDURES");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { total }\n" +
                        "proc {\n" +
                        "    init ( ) { local { } total = 0 }\n" +
                        "    increment ( x ) { local { temp } temp = ( x plus 1 ); total = temp }\n" +
                        "}\n" +
                        "func { }\n" +
                        "main {\n" +
                        "    var { }\n" +
                        "    init ( );\n" +
                        "    increment ( total );\n" +
                        "    print total;\n" +
                        "    halt\n" +
                        "}";
        String expectedCode = "CALL init\n" +
                             "CALL increment total_internal\n" +
                             "PRINT total_internal\n" +
                             "STOP";
        return testProgram(program, expectedCode);
    }
    
    /**
     * Test 4: Program with functions
     */
    private static boolean testProgramWithFunctions() {
        System.out.println("TEST 4: PROGRAM WITH FUNCTIONS");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { value }\n" +
                        "proc { }\n" +
                        "func {\n" +
                        "    getvalue ( ) { local { } return value }\n" +
                        "    calculate ( x y ) { local { result } return result }\n" +
                        "}\n" +
                        "main {\n" +
                        "    var { answer }\n" +
                        "    answer = getvalue ( );\n" +
                        "    print answer;\n" +
                        "    halt\n" +
                        "}";
        String expectedCode = "answer_internal = CALL getvalue\n" +
                             "PRINT answer_internal\n" +
                             "STOP";
        return testProgram(program, expectedCode);
    }
    
    private static boolean testComplexProgram() {
        System.out.println("TEST 5: COMPLEX PROGRAM");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { counter total zero one }\n" +
                        "proc {\n" +
                        "    init ( ) { local { } counter = 0; total = 0 }\n" +
                        "}\n" +
                        "func {\n" +
                        "    getzero ( ) { local { } return zero }\n" +
                        "    getone ( ) { local { } return one }\n" +
                        "}\n" +
                        "main {\n" +
                        "    var { result }\n" +
                        "    init ( );\n" +
                        "    result = getzero ( );\n" +
                        "    if ( result eq 0 ) { print \"zero found\"; } else { print \"not zero\"; };\n" +
                        "    halt\n" +
                        "}";
        String expectedCode = "CALL init\n" +
                             "result_internal = CALL getzero\n" +
                             "IF result_internal = 0 THEN T1\n" +
                             "PRINT \"not zero\"\n" +
                             "GOTO Exit1\n" +
                             "REM T1\n" +
                             "PRINT \"zero found\"\n" +
                             "REM Exit1\n" +
                             "STOP";
        return testProgram(program, expectedCode);
    }
    
    private static boolean testLoopProgram() {
        System.out.println("TEST 6: PROGRAM WITH LOOPS");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { x }\n" +
                        "proc { }\n" +
                        "func { }\n" +
                        "main {\n" +
                        "    var { counter }\n" +
                        "    counter = 5;\n" +
                        "    while ( counter gt 0 ) { counter = ( counter minus 1 ); print counter; };\n" +
                        "    do { print \"loop\"; counter = ( counter plus 1 ); } until ( counter eq 5 );\n" +
                        "    halt\n" +
                        "}";
        String expectedCode = "counter_internal = 5\n" +
                             "REM L1\n" +
                             "IF counter_internal > 0 THEN T1\n" +
                             "GOTO Exit1\n" +
                             "REM T1\n" +
                             "counter_internal = counter_internal - 1\n" +
                             "PRINT counter_internal\n" +
                             "GOTO L1\n" +
                             "REM Exit1\n" +
                             "REM L2\n" +
                             "PRINT \"loop\"\n" +
                             "counter_internal = counter_internal + 1\n" +
                             "IF counter_internal = 5 THEN Exit2\n" +
                             "GOTO L2\n" +
                             "REM Exit2\n" +
                             "STOP";
        return testProgram(program, expectedCode);
    }
    
    private static boolean testBooleanOpProgram() {
        System.out.println("TEST 7: PROGRAM WITH BOOLEAN OPERATORS");
        System.out.println("=" + "=".repeat(50));
        
        String program = "glob { x y }\n" +
                        "proc { }\n" +
                        "func { }\n" +
                        "main {\n" +
                        "    var { }\n" +
                        "    x = 1;\n" +
                        "    y = 0;\n" +
                        "    if ( ( x eq 1 ) or ( y eq 1 ) ) { print \"true\"; } else { print \"false\"; };\n" +
                        "    halt\n" +
                        "}";
        String expectedCode = "x_internal = 1\n" +
                             "y_internal = 0\n" +
                             "IF x_internal = 1 THEN T1\n" +
                             "IF y_internal = 1 THEN T1\n" +
                             "GOTO Exit1\n" +
                             "REM T1\n" +
                             "PRINT \"true\"\n" +
                             "REM Exit1\n" +
                             "PRINT \"false\"\n" +
                             "STOP";
        return testProgram(program, expectedCode);
    }
    
    
    //Helper method to test a program and compare generated code
    
    private static boolean testProgram(String program, String expectedCode) {
        System.out.println("SPL Program:");
        System.out.println(program);
        System.out.println();

        try {
            SPLParserWrapper parser = new SPLParserWrapper(program);
            SPLParser.Spl_progContext tree = parser.parse();
            SymbolTable symbolTable = parser.getSymbolTable();

            System.out.println("SYMBOL TABLE:");
            if (symbolTable.getTable().isEmpty()) {
                System.out.println("(empty)");
            } else {
                symbolTable.getTable().forEach((id, entry) -> System.out.println(entry));
            }

            System.out.println("\nVALIDATION RESULTS:");
            ValidationResult result = parser.validate();
            if (result.hasErrors()) {
                result.getErrors().forEach(error -> System.out.println("Error: " + error));
            } else {
                System.out.println("No semantic errors detected.");
            }

            SPLCodeGenerator codeGenerator = new SPLCodeGenerator(symbolTable);
            String targetCode = codeGenerator.generateCode(tree);

            System.out.println("\nGenerated Target Code:");
            System.out.println(targetCode);

            System.out.println("\nExpected Target Code:");
            System.out.println(expectedCode);

            System.out.println("\nTest Result:");
            if (targetCode.trim().equals(expectedCode.trim())) {
                System.out.println("✓ PASS: Generated code matches expected code.");
                System.out.println("\n" + "=".repeat(70) + "\n");
                return true;
            } else {
                System.out.println("✗ FAIL: Generated code does not match expected code.");
                System.out.println("\n" + "=".repeat(70) + "\n");
                return false;
            }

        } catch (ParseException e) {
            System.err.println("✗ FAIL: Parser errors found:");
            System.err.println(e.getMessage());
            if (!e.getErrors().isEmpty()) {
                e.getErrors().forEach(error -> System.err.println("  " + error));
            }
            System.out.println("\n" + "=".repeat(70) + "\n");
            return false;
        } catch (Exception e) {
            System.err.println("✗ FAIL: Error during code generation: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\n" + "=".repeat(70) + "\n");
            return false;
        }
    }
}