package frontend.semantic;

import frontend.parser.SymbolTable;
import frontend.parser.antlr.SPLParser;

/**
 * Semantic Analyzer for SPL
 * Orchestrates scope checking (via SymbolTable) and type checking
 */
public class SemanticAnalyzer {
    
    private SymbolTable symbolTable;
    private TypeChecker typeChecker;
    
    public SemanticAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.typeChecker = new TypeChecker(symbolTable);
    }
    
    /**
     * Perform complete semantic analysis
     * Returns true if program is semantically valid
     */
    public boolean analyze(SPLParser.Spl_progContext tree) {
        System.out.println("\n=== STARTING SEMANTIC ANALYSIS ===\n");
        
        // Step 1: Symbol table already built during parsing
        System.out.println("Step 1: Scope checking (already done via SymbolTable)");
        
        // Step 2: Type checking
        System.out.println("Step 2: Performing type checking...");
        boolean typeCheckPassed = typeChecker.check(tree);
        
        // Print results
        typeChecker.printReport();
        
        return typeCheckPassed;
    }
    
    /**
     * Get the type checker (for accessing type information)
     */
    public TypeChecker getTypeChecker() {
        return typeChecker;
    }
}