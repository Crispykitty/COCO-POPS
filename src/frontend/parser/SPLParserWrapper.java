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
        VisualSyntaxTreeExtractor.NodeIdMapping nodeIdMapping = VisualSyntaxTreeExtractor.assignNodeIds(parseTree, new SPLParser(null));
        SPLInfoExtractor extractor = new SPLInfoExtractor(nodeIdMapping);
        extractor.visit(parseTree);
        
        return extractor.getProgramInfo();
    }

    public SymbolTable getSymbolTable() throws ParseException {
        parse();
        VisualSyntaxTreeExtractor.NodeIdMapping nodeIdMapping = VisualSyntaxTreeExtractor.assignNodeIds(parseTree, new SPLParser(null));
        SPLInfoExtractor extractor = new SPLInfoExtractor(nodeIdMapping);
        extractor.visit(parseTree);
        return extractor.getSymbolTable();
    }

    /**
     * Validate that the program follows SPL semantics
     */
    public ValidationResult validate() throws ParseException {
        parse();
        VisualSyntaxTreeExtractor.NodeIdMapping nodeIdMapping = VisualSyntaxTreeExtractor.assignNodeIds(parseTree, new SPLParser(null));
        SPLInfoExtractor extractor = new SPLInfoExtractor(nodeIdMapping);
        extractor.visit(parseTree);
        return extractor.getValidationResult();
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

// ... (keep all other classes in SPLParserWrapper.java unchanged) ...

class SPLInfoExtractor extends SPLBaseVisitor<Void> {
    private SymbolTable symbolTable = new SymbolTable();
    private ProgramInfo info = new ProgramInfo();
    private ValidationResult validationResult = new ValidationResult();
    private Stack<String> scopeStack = new Stack<>();
    private VisualSyntaxTreeExtractor.NodeIdMapping nodeIdMapping;
    private String currentProcedureOrFunction = null;

    public SPLInfoExtractor(VisualSyntaxTreeExtractor.NodeIdMapping nodeIdMapping) {
        this.nodeIdMapping = nodeIdMapping;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ProgramInfo getProgramInfo() {
        return info;
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }

    private int getNodeId(ParseTree ctx) {
        Integer id = nodeIdMapping.getId(ctx);
        if (id == null) {
            throw new IllegalStateException("Node ID not assigned for " + ctx.getText());
        }
        return id;
    }

    private boolean isDuplicateInScope(String name, String type, String scope) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(name) && entry.scope.equals(scope) && entry.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNameConflict(String name, String type) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(name) && !entry.type.equals(type) && entry.scope.equals("Everywhere")) {
                return true;
            }
        }
        return false;
    }

    private boolean isShadowingParam(String name, String scope) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(name) && entry.type.equals("parameter") && entry.scope.equals(scope)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVariableDeclared(String name, String currentScope) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(name)) {
                if (entry.type.equals("variable")) {
                    if (currentScope.equals("Local") && (entry.scope.equals("Local") || entry.scope.equals("Global"))) {
                        return true;
                    } else if (currentScope.equals("Main") && (entry.scope.equals("Main") || entry.scope.equals("Global"))) {
                        return true;
                    }
                } else if (entry.type.equals("parameter") && entry.scope.equals(currentScope)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkVariableUsage(String varName, ParseTree ctx) {
        System.out.println("Checking variable '" + varName + "' in scope " + scopeStack.peek() + ", Node ID: " + getNodeId(ctx));
        int nodeId = getNodeId(ctx);
        if (!isVariableDeclared(varName, scopeStack.peek())) {
            validationResult.addError("Undeclared variable '" + varName + "' at Node ID " + nodeId);
        } else {
            info.recordVariableUsage(varName);
        }
    }

    @Override
    public Void visitSpl_prog(SPLParser.Spl_progContext ctx) {
        scopeStack.push("Everywhere");
        System.out.println("Visiting spl_prog, scopeStack: " + scopeStack);
        super.visitSpl_prog(ctx);
        scopeStack.pop();
        return null;
    }

    @Override
    public Void visitMainprog(SPLParser.MainprogContext ctx) {
        scopeStack.push("Main");
        System.out.println("Visiting mainprog, scopeStack: " + scopeStack);
        super.visitMainprog(ctx);
        scopeStack.pop();
        return null;
    }

    @Override
    public Void visitVariables(SPLParser.VariablesContext ctx) {
        String currentScope = scopeStack.peek().equals("Everywhere") ? "Global" : scopeStack.peek();
        scopeStack.push(currentScope);
        System.out.println("Visiting variables in scope: " + currentScope + ", Node ID: " + getNodeId(ctx));

        // Collect all variable names and their node IDs
        List<Map.Entry<String, Integer>> varNames = new ArrayList<>();
        SPLParser.VariablesContext current = ctx;
        while (current != null && current.var() != null) {
            String varName = current.var().IDENT().getText();
            int nodeId = getNodeId(current.var());
            varNames.add(new AbstractMap.SimpleEntry<>(varName, nodeId));
            current = current.variables();
        }

        // Check for duplicates and add to symbol table
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, Integer> entry : varNames) {
            String varName = entry.getKey();
            int nodeId = entry.getValue();
            System.out.println("Processing variable: " + varName + ", Node ID: " + nodeId);
            if (seen.contains(varName)) {
                validationResult.addError("Duplicate variable '" + varName + "' in " + currentScope + " scope at Node ID " + nodeId);
                symbolTable.addEntry(nodeId, varName, currentScope, "variable", 0); // Add even if duplicate
            } else {
                seen.add(varName);
                if (!hasNameConflict(varName, "variable")) {
                    symbolTable.addEntry(nodeId, varName, currentScope, "variable", 0);
                    if (currentScope.equals("Global")) {
                        info.addGlobalVariable(varName);
                    } else if (currentScope.equals("Main")) {
                        info.addMainLocalVariable(varName);
                    }
                }
            }
        }

        super.visitVariables(ctx);
        scopeStack.pop();
        return null;
    }

    @Override
    public Void visitPdef(SPLParser.PdefContext ctx) {
        String procName = ctx.name().IDENT().getText();
        int nodeId = getNodeId(ctx.name());
        currentProcedureOrFunction = procName;
        System.out.println("Visiting procedure: " + procName + ", Node ID: " + nodeId);

        if (isDuplicateInScope(procName, "procedure", "Everywhere")) {
            validationResult.addError("Duplicate procedure '" + procName + "' at Node ID " + nodeId);
        } else if (hasNameConflict(procName, "procedure")) {
            validationResult.addError("Procedure '" + procName + "' conflicts with variable/function in Everywhere scope at Node ID " + nodeId);
        } else {
            int paramCount = countParameters(ctx.param());
            symbolTable.addEntry(nodeId, procName, "Everywhere", "procedure", paramCount);
            info.addProcedure(procName, paramCount);
        }

        scopeStack.push("Local");
        super.visitPdef(ctx);
        scopeStack.pop();
        currentProcedureOrFunction = null;
        return null;
    }

    @Override
    public Void visitFdef(SPLParser.FdefContext ctx) {
        String funcName = ctx.name().IDENT().getText();
        int nodeId = getNodeId(ctx.name());
        currentProcedureOrFunction = funcName;
        System.out.println("Visiting function: " + funcName + ", Node ID: " + nodeId);

        if (isDuplicateInScope(funcName, "function", "Everywhere")) {
            validationResult.addError("Duplicate function '" + funcName + "' at Node ID " + nodeId);
        } else if (hasNameConflict(funcName, "function")) {
            validationResult.addError("Function '" + funcName + "' conflicts with variable/procedure in Everywhere scope at Node ID " + nodeId);
        } else {
            int paramCount = countParameters(ctx.param());
            symbolTable.addEntry(nodeId, funcName, "Everywhere", "function", paramCount);
            info.addFunction(funcName, paramCount);
        }

        scopeStack.push("Local");
        super.visitFdef(ctx);
        scopeStack.pop();
        currentProcedureOrFunction = null;
        return null;
    }

    @Override
    public Void visitParam(SPLParser.ParamContext ctx) {
        if (ctx.maxthree() != null && ctx.maxthree().var() != null) {
            Set<String> seen = new HashSet<>();
            for (SPLParser.VarContext varCtx : ctx.maxthree().var()) {
                String paramName = varCtx.IDENT().getText();
                int nodeId = getNodeId(varCtx);
                System.out.println("Processing parameter: " + paramName + ", Node ID: " + nodeId);
                if (seen.contains(paramName)) {
                    validationResult.addError("Duplicate parameter '" + paramName + "' in " + scopeStack.peek() + " scope at Node ID " + nodeId);
                } else {
                    seen.add(paramName);
                    symbolTable.addEntry(nodeId, paramName, scopeStack.peek(), "parameter", 0);
                }
            }
        }
        return super.visitParam(ctx);
    }

    @Override
    public Void visitMaxthree(SPLParser.MaxthreeContext ctx) {
        if (ctx.getParent() instanceof SPLParser.BodyContext && ctx.var() != null) {
            Set<String> seen = new HashSet<>();
            for (SPLParser.VarContext varCtx : ctx.var()) {
                String varName = varCtx.IDENT().getText();
                int nodeId = getNodeId(varCtx);
                System.out.println("Processing local variable: " + varName + ", Node ID: " + nodeId);
                if (seen.contains(varName)) {
                    validationResult.addError("Duplicate local variable '" + varName + "' in " + scopeStack.peek() + " scope at Node ID " + nodeId);
                    symbolTable.addEntry(nodeId, varName, scopeStack.peek(), "variable", 0);
                } else if (isShadowingParam(varName, scopeStack.peek())) {
                    validationResult.addError("Local variable '" + varName + "' shadows parameter in " + scopeStack.peek() + " scope at Node ID " + nodeId);
                    symbolTable.addEntry(nodeId, varName, scopeStack.peek(), "variable", 0);
                } else {
                    seen.add(varName);
                    symbolTable.addEntry(nodeId, varName, scopeStack.peek(), "variable", 0);
                }
            }
        }
        return super.visitMaxthree(ctx);
    }

    @Override
    public Void visitAtom(SPLParser.AtomContext ctx) {
        if (ctx.var() != null && ctx.var().IDENT() != null) {
            String varName = ctx.var().IDENT().getText();
            checkVariableUsage(varName, ctx); // Use ATOM node
        }
        return super.visitAtom(ctx);
    }

    @Override
    public Void visitOutput(SPLParser.OutputContext ctx) {
        if (ctx.atom() != null && ctx.atom().var() != null && ctx.atom().var().IDENT() != null) {
            String varName = ctx.atom().var().IDENT().getText();
            checkVariableUsage(varName, ctx); // Use ATOM node
        }
        return super.visitOutput(ctx);
    }

    @Override
    public Void visitInput(SPLParser.InputContext ctx) {
        if (ctx.atom() != null) {
            for (SPLParser.AtomContext atomCtx : ctx.atom()) {
                if (atomCtx.var() != null && atomCtx.var().IDENT() != null) {
                    String varName = atomCtx.var().IDENT().getText();
                    checkVariableUsage(varName, atomCtx); // Use ATOM node
                }
            }
        }
        return super.visitInput(ctx);
    }

    @Override
    public Void visitAssign(SPLParser.AssignContext ctx) {
        if (ctx.var() != null && ctx.var().IDENT() != null) {
            String varName = ctx.var().IDENT().getText();
            checkVariableUsage(varName, ctx.var()); // Use ASSIGN node
        }
        if (ctx.name() != null && ctx.name().IDENT() != null) {
            String funcName = ctx.name().IDENT().getText();
            int nodeId = getNodeId(ctx.name());
            info.recordFunctionCall(funcName);
            boolean isDeclared = false;
            for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
                if (entry.name.equals(funcName) && entry.type.equals("function") && entry.scope.equals("Everywhere")) {
                    isDeclared = true;
                    break;
                }
            }
            if (!isDeclared) {
                validationResult.addError("Undeclared function '" + funcName + "' at Node ID " + nodeId);
            }
        }
        return super.visitAssign(ctx);
    }

    @Override
    public Void visitInstr(SPLParser.InstrContext ctx) {
        if (ctx.name() != null && ctx.name().IDENT() != null) {
            String procName = ctx.name().IDENT().getText();
            int nodeId = getNodeId(ctx.name());
            info.recordProcedureCall(procName);
            boolean isDeclared = false;
            for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
                if (entry.name.equals(procName) && entry.type.equals("procedure") && entry.scope.equals("Everywhere")) {
                    isDeclared = true;
                    break;
                }
            }
            if (!isDeclared) {
                validationResult.addError("Undeclared procedure '" + procName + "' at Node ID " + nodeId);
            }
        }
        return super.visitInstr(ctx);
    }

    private int countParameters(SPLParser.ParamContext param) {
        if (param == null || param.maxthree() == null) return 0;
        return param.maxthree().var() != null ? param.maxthree().var().size() : 0;
    }
}