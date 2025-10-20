// NO package declaration (default package)

import frontend.parser.SPLParserWrapper;
import frontend.parser.SymbolTable;
import frontend.parser.ParseException;
import frontend.parser.ValidationResult;
import frontend.parser.antlr.SPLParser;
import backend.codeGen.SPLCodeGenerator;
import backend.codeGen.Inliner;
import backend.codeGen.BASICGenerator;

import java.io.IOException;

/**
 * Main entry point for SPL Compiler
 * Reads SPL source from .txt file and outputs executable BASIC
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("=" + "=".repeat(70));
            System.out.println("SPL COMPILER - Usage");
            System.out.println("=" + "=".repeat(70));
            System.out.println("  java -jar spl-compiler.jar <input.txt> [output.txt]");
            System.out.println("\nOr with Maven:");
            System.out.println("  mvn exec:java -Dexec.args=\"input.txt\"");
            System.out.println("  mvn exec:java -Dexec.args=\"input.txt output.bas\"");
            System.out.println("\nExample:");
            System.out.println("  mvn exec:java -Dexec.args=\"program.txt\"");
            System.out.println("\nIf no output file specified, will use: <input>_output.bas");
            System.out.println("=" + "=".repeat(70));
            return;
        }
        
        String inputFile = args[0];
        String outputFile = args.length > 1 ? args[1] : 
                            inputFile.replace(".txt", "_output.bas");
        
        System.out.println("\n" + "=" + "=".repeat(70));
        System.out.println("SPL COMPILER - COMPLETE PIPELINE");
        System.out.println("=" + "=".repeat(70));
        System.out.println("Input:  " + inputFile);
        System.out.println("Output: " + outputFile);
        System.out.println("=" + "=".repeat(70));
        
        try {
            // Step 1: Read SPL source code
            System.out.println("\n[1/7] Reading source file...");
            String sourceCode = BASICGenerator.readFromFile(inputFile);
            System.out.println("      ✓ Source code loaded (" + sourceCode.length() + " characters)");
            
            // Step 2: Parse the program
            System.out.println("\n[2/7] Parsing SPL program...");
            SPLParserWrapper parser = new SPLParserWrapper(sourceCode);
            SPLParser.Spl_progContext tree = parser.parse();
            SymbolTable symbolTable = parser.getSymbolTable();
            System.out.println("      ✓ Parsing successful");
            
            // Display symbol table
            System.out.println("\n      Symbol Table:");
            if (symbolTable.getTable().isEmpty()) {
                System.out.println("        (empty)");
            } else {
                symbolTable.getTable().forEach((id, entry) -> 
                    System.out.println("        - " + entry));
            }
            
            // Step 3: Validate semantics
            System.out.println("\n[3/7] Running semantic analysis...");
            ValidationResult validation = parser.validate();
            if (validation.hasErrors()) {
                System.err.println("\n      ✗ SEMANTIC ERRORS FOUND:");
                validation.getErrors().forEach(error -> 
                    System.err.println("        - " + error));
                System.err.println("\n" + "=" + "=".repeat(70));
                System.err.println("COMPILATION FAILED");
                System.err.println("=" + "=".repeat(70));
                return;
            }
            System.out.println("      ✓ Semantic analysis passed (no errors)");
            
            // Step 4: Generate intermediate code
            System.out.println("\n[4/7] Generating intermediate code...");
            SPLCodeGenerator codeGen = new SPLCodeGenerator(symbolTable);
            String intermediateCode = codeGen.generateCode(tree);
            System.out.println("      ✓ Intermediate code generated (" + 
                             intermediateCode.split("\n").length + " lines)");
            
            // Step 5: Perform inlining
            System.out.println("\n[5/7] Performing function/procedure inlining...");
            Inliner inliner = new Inliner(symbolTable, tree);
            String inlinedCode = inliner.inline(intermediateCode);
            System.out.println("      ✓ Inlining completed");
            
            // Step 6: Generate executable BASIC
            System.out.println("\n[6/7] Generating executable BASIC code...");
            BASICGenerator basicGen = new BASICGenerator();
            String basicCode = basicGen.generateBASIC(inlinedCode);
            System.out.println("      ✓ BASIC code generated (" + 
                             basicCode.split("\n").length + " lines)");
            
            // Step 7: Save output files
            System.out.println("\n[7/7] Saving output files...");
            String baseName = outputFile.replace(".bas", "").replace(".txt", "");
            
            // Save main output
            basicGen.saveToFile(basicCode, outputFile);
            
            // Save intermediate stages for debugging
            basicGen.saveIntermediateToFile(intermediateCode, baseName + "_intermediate.txt");
            basicGen.saveInlinedToFile(inlinedCode, baseName + "_inlined.txt");
            
            // Display final summary
            System.out.println("\n" + "=" + "=".repeat(70));
            System.out.println("✓✓✓ COMPILATION SUCCESSFUL! ✓✓✓");
            System.out.println("=" + "=".repeat(70));
            System.out.println("\nGenerated files:");
            System.out.println("  1. " + outputFile + " (executable BASIC)");
            System.out.println("  2. " + baseName + "_intermediate.txt (intermediate code)");
            System.out.println("  3. " + baseName + "_inlined.txt (inlined code)");
            
            System.out.println("\nPreview of BASIC output:");
            System.out.println("─".repeat(70));
            String[] lines = basicCode.split("\n");
            int previewLines = Math.min(10, lines.length);
            for (int i = 0; i < previewLines; i++) {
                System.out.println(lines[i]);
            }
            if (lines.length > 10) {
                System.out.println("... (" + (lines.length - 10) + " more lines)");
            }
            System.out.println("─".repeat(70));
            
            System.out.println("\nYou can now run the BASIC code online at:");
            System.out.println("  → https://www.calormen.com/jsbasic/");
            System.out.println("  → https://www.pcjs.org/machines/pcx86/ibm/5150/mda/256kb/basic/");
            System.out.println("\n" + "=" + "=".repeat(70));
            
        } catch (ParseException e) {
            System.err.println("\n✗ PARSING ERRORS:");
            if (e.getErrors().isEmpty()) {
                System.err.println("  - " + e.getMessage());
            } else {
                e.getErrors().forEach(error -> System.err.println("  - " + error));
            }
            System.err.println("\n" + "=" + "=".repeat(70));
            System.err.println("COMPILATION FAILED");
            System.err.println("=" + "=".repeat(70));
        } catch (IOException e) {
            System.err.println("\n✗ FILE I/O ERROR: " + e.getMessage());
            System.err.println("\nPlease check:");
            System.err.println("  - Input file exists and is readable");
            System.err.println("  - Output directory has write permissions");
            System.err.println("\n" + "=" + "=".repeat(70));
            System.err.println("COMPILATION FAILED");
            System.err.println("=" + "=".repeat(70));
        } catch (Exception e) {
            System.err.println("\n✗ UNEXPECTED ERROR: " + e.getMessage());
            System.err.println("\nStack trace:");
            e.printStackTrace();
            System.err.println("\n" + "=" + "=".repeat(70));
            System.err.println("COMPILATION FAILED");
            System.err.println("=" + "=".repeat(70));
        }
    }
}