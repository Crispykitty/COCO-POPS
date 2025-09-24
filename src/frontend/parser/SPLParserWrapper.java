package frontend.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import frontend.parser.antlr.*;  // ANTLR-generated classes
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Production-ready SPL Parser using ANTLR
 * Integrates with your existing project structure
 */
public class SPLParserWrapper {
    private String input;
    private SPLParser.Spl_progContext parseTree;
    private ParseTreeWalker walker;
    private List<String> errors;
    
    public SPLParserWrapper(String input) {
        this.input = input;
        this.walker = new ParseTreeWalker();
        this.errors = new ArrayList<>();
    }
    
    /**
     * Constructor to read from file
     */
    public static SPLParserWrapper fromFile(String filename) throws IOException {
        String content = Files.readString(Paths.get(filename));
        return new SPLParserWrapper(content);
    }
    
    /**
     * Main parsing method using ANTLR
     */
    public SPLParser.Spl_progContext parse() throws ParseException {
        try {
            // Create ANTLR input stream
            ANTLRInputStream inputStream = new ANTLRInputStream(input);
            
            // Create ANTLR-generated lexer
            SPLLexer lexer = new SPLLexer(inputStream);
            
            // Create token stream
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            
            // Create ANTLR-generated parser
            SPLParser parser = new SPLParser(tokens);
            
            // Add custom error handling
            parser.removeErrorListeners();
            SPLErrorListener errorListener = new SPLErrorListener();
            parser.addErrorListener(errorListener);
            
            // Parse starting from spl_prog rule
            parseTree = parser.spl_prog();
            
            // Check for parse errors
            if (parser.getNumberOfSyntaxErrors() > 0) {
                throw new ParseException("Parsing failed with " + 
                    parser.getNumberOfSyntaxErrors() + " syntax errors", 
                    errorListener.getErrors());
            }
            
            return parseTree;
            
        } catch (Exception e) {
            if (e instanceof ParseException) {
                throw e;
            }
            throw new ParseException("Unexpected parsing error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse and extract basic symbol information
     * Returns a simple data structure with program components
     */
    public ProgramInfo parseAndExtractInfo() throws ParseException {
        parse();
        
        ProgramInfo info = new ProgramInfo();
        
        // Extract information using ANTLR visitor pattern
        SPLInfoExtractor extractor = new SPLInfoExtractor();
        extractor.visit(parseTree);
        
        return extractor.getProgramInfo();
    }
    
    /**
     * Validate that the program follows SPL semantics
     */
    public ValidationResult validate() throws ParseException {
        ProgramInfo info = parseAndExtractInfo();
        ValidationResult result = new ValidationResult();
        
        // Basic semantic checks
        validateVariableUsage(info, result);
        validateProcedureUsage(info, result);
        validateFunctionUsage(info, result);
        
        return result;
    }
    
    private void validateVariableUsage(ProgramInfo info, ValidationResult result) {
        // Check if all used variables are declared
        Set<String> declaredVars = new HashSet<>(info.globalVariables);
        declaredVars.addAll(info.mainLocalVariables);
        
        for (String usedVar : info.usedVariables) {
            if (!declaredVars.contains(usedVar)) {
                result.addError("Undeclared variable: " + usedVar);
            }
        }
    }
    
    private void validateProcedureUsage(ProgramInfo info, ValidationResult result) {
        for (String usedProc : info.procedureCalls) {
            if (!info.procedures.containsKey(usedProc)) {
                result.addError("Undeclared procedure: " + usedProc);
            }
        }
    }
    
    private void validateFunctionUsage(ProgramInfo info, ValidationResult result) {
        for (String usedFunc : info.functionCalls) {
            if (!info.functions.containsKey(usedFunc)) {
                result.addError("Undeclared function: " + usedFunc);
            }
        }
    }
    
    /**
     * Pretty print the parse tree for debugging
     */
    public void printParseTree() {
        if (parseTree != null) {
            System.out.println("Parse Tree Structure:");
            System.out.println(parseTree.toStringTree(new SPLParser(null)));
        }
    }
    
    /**
     * Get detailed parsing statistics
     */
    public ParsingStats getStats() {
        if (parseTree == null) return null;
        
        return new ParsingStats(
            input.length(),
            getTreeDepth(parseTree),
            getNodeCount(parseTree),
            errors.size()
        );
    }
    
    // Helper methods
    private int getTreeDepth(ParseTree tree) {
        if (tree.getChildCount() == 0) return 1;
        int maxDepth = 0;
        for (int i = 0; i < tree.getChildCount(); i++) {
            maxDepth = Math.max(maxDepth, getTreeDepth(tree.getChild(i)));
        }
        return maxDepth + 1;
    }
    
    private int getNodeCount(ParseTree tree) {
        int count = 1;
        for (int i = 0; i < tree.getChildCount(); i++) {
            count += getNodeCount(tree.getChild(i));
        }
        return count;
    }
}

/**
 * Enhanced error listener with detailed error collection
 */
class SPLErrorListener extends BaseErrorListener {
    private List<String> errors = new ArrayList<>();
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                           int line, int charPositionInLine, String msg,
                           RecognitionException e) {
        
        String errorMsg = String.format(
            "Line %d:%d - %s", line, charPositionInLine, msg
        );
        
        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            errorMsg += String.format(" (found '%s')", token.getText());
        }
        
        errors.add(errorMsg);
        System.err.println("SPL Parse Error: " + errorMsg);
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
}

/**
 * Custom exception for parsing errors
 */
class ParseException extends Exception {
    private List<String> errors;
    
    public ParseException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }
    
    public ParseException(String message, List<String> errors) {
        super(message);
        this.errors = new ArrayList<>(errors);
    }
    
    public ParseException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new ArrayList<>();
    }
    
    public List<String> getErrors() {
        return errors;
    }
}

/**
 * Data structure to hold extracted program information
 */
class ProgramInfo {
    public List<String> globalVariables = new ArrayList<>();
    public List<String> mainLocalVariables = new ArrayList<>();
    public Map<String, Integer> procedures = new HashMap<>();  // name -> param count
    public Map<String, Integer> functions = new HashMap<>();   // name -> param count
    public Set<String> usedVariables = new HashSet<>();
    public Set<String> procedureCalls = new HashSet<>();
    public Set<String> functionCalls = new HashSet<>();
    
    public void addGlobalVariable(String name) {
        globalVariables.add(name);
    }
    
    public void addMainLocalVariable(String name) {
        mainLocalVariables.add(name);
    }
    
    public void addProcedure(String name, int paramCount) {
        procedures.put(name, paramCount);
    }
    
    public void addFunction(String name, int paramCount) {
        functions.put(name, paramCount);
    }
    
    public void recordVariableUsage(String name) {
        usedVariables.add(name);
    }
    
    public void recordProcedureCall(String name) {
        procedureCalls.add(name);
    }
    
    public void recordFunctionCall(String name) {
        functionCalls.add(name);
    }
}

/**
 * Validation result container
 */
class ValidationResult {
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    public void addError(String error) {
        errors.add(error);
    }
    
    public void addWarning(String warning) {
        warnings.add(warning);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
}

/**
 * Parsing statistics
 */
class ParsingStats {
    public final int inputLength;
    public final int treeDepth;
    public final int nodeCount;
    public final int errorCount;
    
    public ParsingStats(int inputLength, int treeDepth, int nodeCount, int errorCount) {
        this.inputLength = inputLength;
        this.treeDepth = treeDepth;
        this.nodeCount = nodeCount;
        this.errorCount = errorCount;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ParsingStats{input=%d chars, depth=%d, nodes=%d, errors=%d}", 
            inputLength, treeDepth, nodeCount, errorCount
        );
    }
}

/**
 * ANTLR visitor to extract program information
 */
class SPLInfoExtractor extends SPLBaseVisitor<Void> {
    private ProgramInfo info = new ProgramInfo();
    
    public ProgramInfo getProgramInfo() {
        return info;
    }
    
    @Override
    public Void visitVariables(SPLParser.VariablesContext ctx) {
        // Extract global variables
        if (ctx.var() != null) {
            String varName = ctx.var().IDENT().getText();
            info.addGlobalVariable(varName);
        }
        return super.visitVariables(ctx);
    }
    
    @Override
    public Void visitPdef(SPLParser.PdefContext ctx) {
        // Extract procedure definition
        String procName = ctx.name().IDENT().getText();
        int paramCount = countParameters(ctx.param());
        info.addProcedure(procName, paramCount);
        return super.visitPdef(ctx);
    }
    
    @Override
    public Void visitFdef(SPLParser.FdefContext ctx) {
        // Extract function definition  
        String funcName = ctx.name().IDENT().getText();
        int paramCount = countParameters(ctx.param());
        info.addFunction(funcName, paramCount);
        return super.visitFdef(ctx);
    }
    
    @Override
    public Void visitAssign(SPLParser.AssignContext ctx) {
        // Record variable usage and function calls
        if (ctx.var() != null) {
            info.recordVariableUsage(ctx.var().IDENT().getText());
        }
        if (ctx.name() != null) {
            info.recordFunctionCall(ctx.name().IDENT().getText());
        }
        return super.visitAssign(ctx);
    }
    
    private int countParameters(SPLParser.ParamContext param) {
        if (param == null || param.maxthree() == null) return 0;
        SPLParser.MaxthreeContext maxthree = param.maxthree();
        if (maxthree.var() == null) return 0;
        return maxthree.var().size();
    }
}