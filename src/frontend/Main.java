package frontend;

import frontend.parser.SPLParserWrapper;
import frontend.parser.SymbolTable;
import frontend.parser.ParseException;
import frontend.parser.ValidationResult;
import frontend.parser.antlr.SPLParser;
import backend.codeGen.SPLCodeGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main <input-file.txt>");
            System.err.println("Example: java Main test.txt");
            System.err.println();
            System.err.println("The compiler will:");
            System.err.println("  1. Read the SPL program from the input file");
            System.err.println("  2. Parse and validate the program");
            System.err.println("  3. Generate target code");
            System.err.println("  4. Write the output to <input-file>_output.txt");
            return;
        }

        String inputFile = args[0];
        String outputFile = inputFile.replace(".txt", "_output.txt");

        try {
            // Read input file
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║           SPL COMPILER - Students' Programming Language  ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Reading SPL program from: " + inputFile);
            String program = readFile(inputFile);
            System.out.println("✓ Successfully read " + program.length() + " characters");
            System.out.println();

            // Display input program
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println("INPUT PROGRAM:");
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println(program);
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println();

            // Parse
            System.out.println("[ PHASE 1: PARSING ]");
            SPLParserWrapper parser = new SPLParserWrapper(program);
            SPLParser.Spl_progContext tree = parser.parse();
            System.out.println("✓ Parsing successful - syntax is valid");
            System.out.println();

            // Get symbol table
            SymbolTable symbolTable = parser.getSymbolTable();
            System.out.println("[ PHASE 2: SYMBOL TABLE ]");
            if (symbolTable.getTable().isEmpty()) {
                System.out.println("  (no variables or functions declared)");
            } else {
                symbolTable.getTable().forEach((id, entry) -> 
                    System.out.println("  • " + entry.name + " (" + entry.type + ", scope: " + entry.scope + ")"));
            }
            System.out.println();

            // Semantic validation
            System.out.println("[ PHASE 3: SEMANTIC ANALYSIS ]");
            ValidationResult result = parser.validate();
            if (result.hasErrors()) {
                System.err.println("✗ Semantic errors detected:");
                result.getErrors().forEach(error -> 
                    System.err.println("  ERROR: " + error));
                System.err.println();
                System.err.println("Compilation failed. Please fix the errors and try again.");
                System.exit(1);
            }
            System.out.println("✓ No semantic errors detected");
            System.out.println();

            // Code generation
            System.out.println("[ PHASE 4: CODE GENERATION ]");
            SPLCodeGenerator codeGenerator = new SPLCodeGenerator(symbolTable);
            String targetCode = codeGenerator.generateCode(tree);
            System.out.println("✓ Code generation successful");
            System.out.println();

            // Display generated code
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println("GENERATED TARGET CODE:");
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println(targetCode);
            System.out.println("─────────────────────────────────────────────────────────────");
            System.out.println();

            // Write to output file
            writeFile(outputFile, targetCode);
            System.out.println("✓ Target code written to: " + outputFile);
            System.out.println();
            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║              COMPILATION SUCCESSFUL!                      ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");

        } catch (ParseException e) {
            System.err.println();
            System.err.println("╔═══════════════════════════════════════════════════════════╗");
            System.err.println("║                  PARSING FAILED                           ║");
            System.err.println("╚═══════════════════════════════════════════════════════════╝");
            System.err.println();
            System.err.println("Parse errors found:");
            System.err.println(e.getMessage());
            if (!e.getErrors().isEmpty()) {
                e.getErrors().forEach(error -> 
                    System.err.println("  • " + error));
            }
            System.exit(1);
        } catch (IOException e) {
            System.err.println();
            System.err.println("✗ File I/O error: " + e.getMessage());
            System.err.println("  Make sure the file exists and is readable.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println();
            System.err.println("✗ Unexpected compilation error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static String readFile(String filename) throws IOException {
        Path path = Paths.get(filename);
        return Files.readString(path);
    }

    private static void writeFile(String filename, String content) throws IOException {
        Path path = Paths.get(filename);
        Files.writeString(path, content);
    }
}