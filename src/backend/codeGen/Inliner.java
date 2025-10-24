package backend.codeGen;

import frontend.parser.SymbolTable;
import frontend.parser.antlr.SPLParser;
import java.util.*;

/**
 * Implements function/procedure inlining as per lecture notes
 * Replaces CALL commands with actual function/procedure body code
 * 
 * Preconditions:
 * 1. Type checking completed
 * 2. All names are uniquely renamed (via  suffix)
 * 3. No recursive call cycles
 */
public class Inliner {
    private SymbolTable symbolTable;
    private SPLParser.Spl_progContext parseTree;
    private Map<String, FunctionDef> functionDefs;
    private Map<String, ProcedureDef> procedureDefs;
    private Set<String> recursiveFunctions;
    private SPLCodeGenerator codeGenerator;
    
    public Inliner(SymbolTable symbolTable, SPLParser.Spl_progContext parseTree) {
        this.symbolTable = symbolTable;
        this.parseTree = parseTree;
        this.functionDefs = new HashMap<>();
        this.procedureDefs = new HashMap<>();
        this.recursiveFunctions = new HashSet<>();
        this.codeGenerator = new SPLCodeGenerator(symbolTable);
    }
    
    /**
     * Main entry point - performs inlining on intermediate code
     */
    public String inline(String intermediateCode) {
        // Step 1: Extract all function/procedure definitions
        extractDefinitions();
        
        // Step 2: Detect recursive call cycles
        detectRecursion();
        
        // Step 3: Perform inlining
        String inlinedCode = performInlining(intermediateCode);
        
        return inlinedCode;
    }
    
    /**
     * Extract function and procedure definitions from parse tree
     */
    private void extractDefinitions() {
        // Extract procedures
        extractProcedures(parseTree.procdefs());
        
        // Extract functions
        extractFunctions(parseTree.funcdefs());
    }
    
    /**
     * Extract procedure definitions recursively
     */
    private void extractProcedures(SPLParser.ProcdefsContext ctx) {
        if (ctx.pdef() != null) {
            SPLParser.PdefContext pdef = ctx.pdef();
            String name = pdef.name().IDENT().getText();
            List<String> params = extractParameters(pdef.param());
            
            ProcedureDef procDef = new ProcedureDef(name, params, pdef);
            procedureDefs.put(name, procDef);
            
            // Recursively process remaining procedures
            if (ctx.procdefs() != null) {
                extractProcedures(ctx.procdefs());
            }
        }
    }
    
    /**
     * Extract function definitions recursively
     */
    private void extractFunctions(SPLParser.FuncdefsContext ctx) {
        if (ctx.fdef() != null) {
            SPLParser.FdefContext fdef = ctx.fdef();
            String name = fdef.name().IDENT().getText();
            List<String> params = extractParameters(fdef.param());
            
            FunctionDef funcDef = new FunctionDef(name, params, fdef);
            functionDefs.put(name, funcDef);
            
            // Recursively process remaining functions
            if (ctx.funcdefs() != null) {
                extractFunctions(ctx.funcdefs());
            }
        }
    }
    
    /**
     * Extract parameter names from param context
     */
    private List<String> extractParameters(SPLParser.ParamContext param) {
        List<String> params = new ArrayList<>();
        SPLParser.MaxthreeContext maxthree = param.maxthree();
        
        // Handle all var declarations in maxthree
        while (maxthree != null) {
            if (maxthree.var() != null && maxthree.var().size() > 0) {
                for (SPLParser.VarContext var : maxthree.var()) {
                    params.add(var.IDENT().getText());
                }
            }
            break;
        }
        
        return params;
    }
    
    /**
     * Detect recursive call cycles
     */
    private void detectRecursion() {
        // Check each function for recursive calls
        for (String funcName : functionDefs.keySet()) {
            if (isRecursive(funcName, new HashSet<>())) {
                recursiveFunctions.add(funcName);
                System.out.println("WARNING: Recursive function detected: " + funcName + " (cannot inline)");
            }
        }
        
        // Check procedures too
        for (String procName : procedureDefs.keySet()) {
            if (isRecursiveProcedure(procName, new HashSet<>())) {
                recursiveFunctions.add(procName);
                System.out.println("WARNING: Recursive procedure detected: " + procName + " (cannot inline)");
            }
        }
    }
    
    /**
     * Check if function is recursive (calls itself directly or indirectly)
     */
    private boolean isRecursive(String funcName, Set<String> visited) {
        if (visited.contains(funcName)) {
            return true; // Cycle detected
        }
        
        visited.add(funcName);
        FunctionDef func = functionDefs.get(funcName);
        
        if (func != null) {
            // Check if function body calls any other functions
            Set<String> calledFunctions = findCalledFunctions(func.context.body());
            
            for (String calledFunc : calledFunctions) {
                if (functionDefs.containsKey(calledFunc)) {
                    if (isRecursive(calledFunc, new HashSet<>(visited))) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if procedure is recursive
     */
    private boolean isRecursiveProcedure(String procName, Set<String> visited) {
        if (visited.contains(procName)) {
            return true;
        }
        
        visited.add(procName);
        ProcedureDef proc = procedureDefs.get(procName);
        
        if (proc != null) {
            Set<String> calledProcs = findCalledProcedures(proc.context.body());
            
            for (String calledProc : calledProcs) {
                if (procedureDefs.containsKey(calledProc)) {
                    if (isRecursiveProcedure(calledProc, new HashSet<>(visited))) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Find all function calls in a body
     */
    private Set<String> findCalledFunctions(SPLParser.BodyContext body) {
        Set<String> called = new HashSet<>();
        findFunctionCallsInAlgo(body.algo(), called);
        return called;
    }
    
    /**
     * Find all procedure calls in a body
     */
    private Set<String> findCalledProcedures(SPLParser.BodyContext body) {
        Set<String> called = new HashSet<>();
        findProcedureCallsInAlgo(body.algo(), called);
        return called;
    }
    
    /**
     * Recursively find function calls in algorithm
     */
    private void findFunctionCallsInAlgo(SPLParser.AlgoContext algo, Set<String> called) {
        for (SPLParser.InstrContext instr : algo.instr()) {
            if (instr.assign() != null && instr.assign().name() != null) {
                // Function call in assignment
                called.add(instr.assign().name().IDENT().getText());
            }
            // Check nested algorithms in branches and loops
            if (instr.branch() != null) {
                for (SPLParser.AlgoContext a : instr.branch().algo()) {
                    findFunctionCallsInAlgo(a, called);
                }
            }
            if (instr.loop() != null && instr.loop().algo() != null) {
                findFunctionCallsInAlgo(instr.loop().algo(), called);
            }
        }
    }
    
    /**
     * Recursively find procedure calls in algorithm
     */
    private void findProcedureCallsInAlgo(SPLParser.AlgoContext algo, Set<String> called) {
        for (SPLParser.InstrContext instr : algo.instr()) {
            if (instr.name() != null && instr.input() != null) {
                // Procedure call
                called.add(instr.name().IDENT().getText());
            }
            // Check nested algorithms
            if (instr.branch() != null) {
                for (SPLParser.AlgoContext a : instr.branch().algo()) {
                    findProcedureCallsInAlgo(a, called);
                }
            }
            if (instr.loop() != null && instr.loop().algo() != null) {
                findProcedureCallsInAlgo(instr.loop().algo(), called);
            }
        }
    }
    
    /**
     * Perform actual inlining on intermediate code
     */
    private String performInlining(String intermediateCode) {
        String[] lines = intermediateCode.split("\n");
        StringBuilder result = new StringBuilder();
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // Check for function call: var = CALL funcname args
            if (trimmed.matches(".*=\\s*CALL\\s+\\w+.*")) {
                String inlined = inlineFunctionCall(trimmed);
                result.append(inlined);
                if (!inlined.isEmpty() && !inlined.endsWith("\n")) {
                    result.append("\n");
                }
            }
            // Check for procedure call: CALL procname args
            else if (trimmed.startsWith("CALL ")) {
                String inlined = inlineProcedureCall(trimmed);
                result.append(inlined);
                if (!inlined.isEmpty() && !inlined.endsWith("\n")) {
                    result.append("\n");
                }
            }
            else {
                result.append(line).append("\n");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Inline a function call: result_var = CALL funcname arg1 arg2
     */
    private String inlineFunctionCall(String line) {
        // Parse line: result = CALL funcname arg1 arg2 ...
        String[] parts = line.split("=", 2);
        if (parts.length != 2) return line;
        
        String resultVar = parts[0].trim();
        String callPart = parts[1].trim();
        
        // Extract function name and arguments
        String[] tokens = callPart.split("\\s+");
        if (tokens.length < 2) return line;
        
        String funcName = tokens[1]; // After "CALL"
        List<String> arguments = new ArrayList<>();
        for (int i = 2; i < tokens.length; i++) {
            arguments.add(tokens[i]);
        }
        
        FunctionDef func = functionDefs.get(funcName);
        
        // Cannot inline if function doesn't exist or is recursive
        if (func == null || recursiveFunctions.contains(funcName)) {
            return line;
        }
        
        // Generate inlined code
        StringBuilder inlined = new StringBuilder();
        
        // Step 1: Parameter assignments (p0 = arg0, ...)
        for (int i = 0; i < func.parameters.size() && i < arguments.size(); i++) {
            String param = func.parameters.get(i);
            String arg = arguments.get(i);
            inlined.append(param).append(" = ").append(arg).append("\n");
        }
        
        // Step 2: Generate body code and replace return with assignment
        String bodyCode = generateBodyCode(func.context.body());
        
        // Step 3: Replace "return expr" with "resultVar = expr"
        String returnAtom = generateAtomCode(func.context.atom());
        bodyCode = bodyCode.trim();
        
        if (!bodyCode.isEmpty()) {
            inlined.append(bodyCode).append("\n");
        }
        
        // Add the assignment for return value
        inlined.append(resultVar).append(" = ").append(returnAtom);
        
        return inlined.toString();
    }
    
    /**
     * Inline a procedure call: CALL procname arg1 arg2
     */
    private String inlineProcedureCall(String line) {
        // Parse: CALL procname arg1 arg2 ...
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length < 2) return line;
        
        String procName = tokens[1]; // After "CALL"
        List<String> arguments = new ArrayList<>();
        for (int i = 2; i < tokens.length; i++) {
            arguments.add(tokens[i]);
        }
        
        ProcedureDef proc = procedureDefs.get(procName);
        
        // Cannot inline if procedure doesn't exist or is recursive
        if (proc == null || recursiveFunctions.contains(procName)) {
            return line;
        }
        
        // Generate inlined code
        StringBuilder inlined = new StringBuilder();
        
        // Step 1: Parameter assignments
        for (int i = 0; i < proc.parameters.size() && i < arguments.size(); i++) {
            String param = proc.parameters.get(i);
            String arg = arguments.get(i);
            inlined.append(param).append(" = ").append(arg).append("\n");
        }
        
        // Step 2: Generate body code
        String bodyCode = generateBodyCode(proc.context.body());
        if (!bodyCode.isEmpty()) {
            inlined.append(bodyCode);
        }
        
        return inlined.toString();
    }
    
    /**
     * Generate code for a body context
     */
    private String generateBodyCode(SPLParser.BodyContext body) {
        // Use a fresh code generator instance to generate body code
        SPLCodeGenerator bodyGen = new SPLCodeGenerator(symbolTable);
        String code = bodyGen.visitAlgo(body.algo());
        return code != null ? code : "";
    }
    
    /**
     * Generate code for an atom (return value)
     */
    private String generateAtomCode(SPLParser.AtomContext atom) {
        if (atom.var() != null) {
            return atom.var().IDENT().getText();
        } else if (atom.NUMBER() != null) {
            return atom.NUMBER().getText();
        }
        return "0";
    }
    
    // Inner classes for storing definitions
    private static class FunctionDef {
        String name;
        List<String> parameters;
        SPLParser.FdefContext context;
        
        FunctionDef(String name, List<String> parameters, SPLParser.FdefContext context) {
            this.name = name;
            this.parameters = parameters;
            this.context = context;
        }
    }
    
    private static class ProcedureDef {
        String name;
        List<String> parameters;
        SPLParser.PdefContext context;
        
        ProcedureDef(String name, List<String> parameters, SPLParser.PdefContext context) {
            this.name = name;
            this.parameters = parameters;
            this.context = context;
        }
    }
}