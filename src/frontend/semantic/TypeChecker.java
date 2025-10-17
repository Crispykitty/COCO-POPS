package frontend.semantic;

import frontend.parser.SymbolTable;
import frontend.parser.antlr.*;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.*;

/**
 * Type Checker for SPL
 * Implements semantic type analysis according to SPL_Types.pdf
 * Fixed to match actual SPL.g4 grammar structure
 */
public class TypeChecker extends SPLBaseVisitor<TypeSystem.SPLType> {
    
    private SymbolTable symbolTable;
    private TypeSystem typeSystem;
    private List<String> typeErrors;
    private Map<ParseTree, TypeSystem.SPLType> nodeTypes;
    
    public TypeChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.typeSystem = new TypeSystem();
        this.typeErrors = new ArrayList<>();
        this.nodeTypes = new HashMap<>();
    }
    
    /**
     * Perform type checking on the entire program
     */
    public boolean check(SPLParser.Spl_progContext tree) {
        typeErrors.clear();
        nodeTypes.clear();
        
        // Visit the tree and perform type checking
        visit(tree);
        
        // Return true if no type errors
        return typeErrors.isEmpty();
    }
    
    /**
     * Get all type errors found
     */
    public List<String> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
    
    /**
     * Get the inferred type of a node
     */
    public TypeSystem.SPLType getNodeType(ParseTree node) {
        return nodeTypes.getOrDefault(node, TypeSystem.SPLType.UNKNOWN);
    }
    
    /**
     * Record an error
     */
    private void addError(String message) {
        typeErrors.add(message);
        System.err.println("TYPE ERROR: " + message);
    }
    
    /**
     * Record the type of a node
     */
    private void recordType(ParseTree node, TypeSystem.SPLType type) {
        nodeTypes.put(node, type);
    }
    
    // ========== VISITOR METHODS ==========
    
    /**
     * SPL_PROG is correctly typed if all sections are correctly typed
     */
    @Override
    public TypeSystem.SPLType visitSpl_prog(SPLParser.Spl_progContext ctx) {
        visit(ctx.variables());     // Global variables
        visit(ctx.procdefs());      // Procedures
        visit(ctx.funcdefs());      // Functions
        visit(ctx.mainprog());      // Main program
        
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * VARIABLES is correctly typed if all VARs are numeric
     */
    @Override
    public TypeSystem.SPLType visitVariables(SPLParser.VariablesContext ctx) {
        // All variables in SPL are of type "numeric" (fact)
        visitChildren(ctx);
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * VAR - user-defined name
     */
    @Override
    public TypeSystem.SPLType visitVar(SPLParser.VarContext ctx) {
        // All variables are numeric in SPL
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * NAME - identifier for procedures/functions
     */
    @Override
    public TypeSystem.SPLType visitName(SPLParser.NameContext ctx) {
        // Names are typeless
        return TypeSystem.SPLType.TYPELESS;
    }
    
    /**
     * PROCDEFS is correctly typed if all PDEFs are correctly typed
     */
    @Override
    public TypeSystem.SPLType visitProcdefs(SPLParser.ProcdefsContext ctx) {
        visitChildren(ctx);
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * PDEF is correctly typed if:
     * - NAME is type-less (has no type in symbol table)
     * - PARAM is correctly typed
     * - BODY is correctly typed
     */
    @Override
    public TypeSystem.SPLType visitPdef(SPLParser.PdefContext ctx) {
        String procName = ctx.name().getText();
        
        // Check that procedure name is not a variable
        if (isVariableInSymbolTable(procName)) {
            addError("Procedure name '" + procName + "' conflicts with a variable");
        }
        
        // Check parameters
        visit(ctx.param());
        
        // Check body
        visit(ctx.body());
        
        return TypeSystem.SPLType.TYPELESS;
    }
    
    /**
     * FUNCDEFS is correctly typed if all FDEFs are correctly typed
     */
    @Override
    public TypeSystem.SPLType visitFuncdefs(SPLParser.FuncdefsContext ctx) {
        visitChildren(ctx);
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * FDEF is correctly typed if:
     * - NAME is type-less
     * - PARAM is correctly typed
     * - Local variables are correctly typed
     * - return ATOM is of type "numeric"
     * 
     * NOTE: In your grammar, fdef has maxthree directly, not body
     */
    @Override
    public TypeSystem.SPLType visitFdef(SPLParser.FdefContext ctx) {
        String funcName = ctx.name().getText();
        
        // Check that function name is not a variable
        if (isVariableInSymbolTable(funcName)) {
            addError("Function name '" + funcName + "' conflicts with a variable");
        }
        
        // Check parameters
        visit(ctx.param());
        
        // Check body (which contains local variables and algorithm)
        visit(ctx.body());
        
        // Check return value is numeric
        TypeSystem.SPLType returnType = visit(ctx.atom());
        if (returnType != TypeSystem.SPLType.NUMERIC) {
            addError("Function '" + funcName + "' must return a numeric value, found: " + 
                    typeSystem.typeToString(returnType));
        }
        
        return TypeSystem.SPLType.TYPELESS;
    }
    
    /**
     * BODY is correctly typed if MAXTHREE and ALGO are correctly typed
     */
    @Override
    public TypeSystem.SPLType visitBody(SPLParser.BodyContext ctx) {
        visit(ctx.maxthree());
        visit(ctx.algo());
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * PARAM is correctly typed if maxthree is correctly typed
     */
    @Override
    public TypeSystem.SPLType visitParam(SPLParser.ParamContext ctx) {
        visit(ctx.maxthree());
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * MAXTHREE is correctly typed if all VARs are numeric
     */
    @Override
    public TypeSystem.SPLType visitMaxthree(SPLParser.MaxthreeContext ctx) {
        visitChildren(ctx);
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * MAINPROG is correctly typed if VARIABLES and ALGO are correctly typed
     */
    @Override
    public TypeSystem.SPLType visitMainprog(SPLParser.MainprogContext ctx) {
        visit(ctx.variables());
        visit(ctx.algo());
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * ALGO is correctly typed if all INSTRs are correctly typed
     */
    @Override
    public TypeSystem.SPLType visitAlgo(SPLParser.AlgoContext ctx) {
        visitChildren(ctx);
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * INSTR - various instruction types
     */
    @Override
    public TypeSystem.SPLType visitInstr(SPLParser.InstrContext ctx) {
        if (ctx.HALT() != null) {
            return TypeSystem.SPLType.NUMERIC; // halt is always valid
        }
        
        // Check for procedure call: NAME ( INPUT )
        if (ctx.name() != null && ctx.input() != null) {
            String procName = ctx.name().getText();
            int providedArgs = countInputArgs(ctx.input());
            int expectedParams = getProcedureParamCount(procName);
            
            if (expectedParams != -1 && providedArgs != expectedParams) {
                addError("Procedure '" + procName + "' expects " + expectedParams + 
                        " parameter(s), but " + providedArgs + " were provided");
            }
            
            visit(ctx.input());
            return TypeSystem.SPLType.NUMERIC;
        }
        
        // Delegate to child nodes
        visitChildren(ctx);
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * OUTPUT - print statement output
     */
    @Override
    public TypeSystem.SPLType visitOutput(SPLParser.OutputContext ctx) {
        if (ctx.STRING() != null) {
            // String output is always valid
            return TypeSystem.SPLType.STRING;
        }
        
        if (ctx.atom() != null) {
            TypeSystem.SPLType atomType = visit(ctx.atom());
            if (atomType != TypeSystem.SPLType.NUMERIC) {
                addError("print statement requires numeric value, found: " + 
                         typeSystem.typeToString(atomType));
            }
            return atomType;
        }
        
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * ASSIGN - VAR = TERM or VAR = NAME ( INPUT )
     */
    @Override
    public TypeSystem.SPLType visitAssign(SPLParser.AssignContext ctx) {
        String varName = ctx.var().getText();
        
        // Check that variable is declared and numeric
        if (!isVariableInSymbolTable(varName)) {
            addError("Undeclared variable: " + varName);
            return TypeSystem.SPLType.UNKNOWN;
        }
        
        // Function call assignment: VAR = NAME ( INPUT )
        if (ctx.name() != null) {
            String funcName = ctx.name().getText();
            
            if (isVariableInSymbolTable(funcName)) {
                addError("'" + funcName + "' is a variable, cannot be called as function");
            }
            
            if (ctx.input() != null) {
                int providedArgs = countInputArgs(ctx.input());
                int expectedParams = getFunctionParamCount(funcName);
                
                if (expectedParams != -1 && providedArgs != expectedParams) {
                    addError("Function '" + funcName + "' expects " + expectedParams + 
                            " parameter(s), but " + providedArgs + " were provided");
                }
                
                visit(ctx.input());
            }
            return TypeSystem.SPLType.NUMERIC;
        }
        
        // Term assignment: VAR = TERM
        if (ctx.term() != null) {
            TypeSystem.SPLType termType = visit(ctx.term());
            
            if (termType != TypeSystem.SPLType.NUMERIC) {
                addError("Cannot assign " + typeSystem.typeToString(termType) + 
                         " to numeric variable '" + varName + "'");
            }
        }
        
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * LOOP - while TERM { ALGO } or do { ALGO } until TERM
     */
    @Override
    public TypeSystem.SPLType visitLoop(SPLParser.LoopContext ctx) {
        if (ctx.WHILE() != null) {
            // while loop
            TypeSystem.SPLType condType = visit(ctx.term());
            
            if (condType != TypeSystem.SPLType.BOOLEAN) {
                addError("while condition must be boolean, found: " + 
                         typeSystem.typeToString(condType));
            }
            
            visit(ctx.algo());
        } else if (ctx.DO() != null) {
            // do-until loop
            visit(ctx.algo());
            
            TypeSystem.SPLType condType = visit(ctx.term());
            
            if (condType != TypeSystem.SPLType.BOOLEAN) {
                addError("until condition must be boolean, found: " + 
                         typeSystem.typeToString(condType));
            }
        }
        
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * BRANCH - if TERM { ALGO } [else { ALGO }]
     */
    @Override
    public TypeSystem.SPLType visitBranch(SPLParser.BranchContext ctx) {
        TypeSystem.SPLType condType = visit(ctx.term());
        
        if (condType != TypeSystem.SPLType.BOOLEAN) {
            addError("if condition must be boolean, found: " + 
                     typeSystem.typeToString(condType));
        }
        
        // Check all algorithm branches
        for (int i = 0; i < ctx.algo().size(); i++) {
            visit(ctx.algo(i));
        }
        
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * INPUT is correctly typed if all ATOMs are of type "numeric"
     */
    @Override
    public TypeSystem.SPLType visitInput(SPLParser.InputContext ctx) {
        for (SPLParser.AtomContext atom : ctx.atom()) {
            TypeSystem.SPLType atomType = visit(atom);
            
            if (atomType != TypeSystem.SPLType.NUMERIC) {
                addError("Input argument must be numeric, found: " + 
                         typeSystem.typeToString(atomType));
            }
        }
        
        return TypeSystem.SPLType.NUMERIC;
    }
    
    /**
     * ATOM - VAR or NUMBER
     */
    @Override
    public TypeSystem.SPLType visitAtom(SPLParser.AtomContext ctx) {
        if (ctx.var() != null) {
            String varName = ctx.var().getText();
            
            if (!isVariableInSymbolTable(varName)) {
                addError("Undeclared variable: " + varName);
                return TypeSystem.SPLType.UNKNOWN;
            }
            
            // All variables in SPL are numeric
            return TypeSystem.SPLType.NUMERIC;
        }
        
        if (ctx.NUMBER() != null) {
            // Number literals are always numeric
            return TypeSystem.SPLType.NUMERIC;
        }
        
        return TypeSystem.SPLType.UNKNOWN;
    }
    
    /**
     * TERM type inference
     */
    @Override
    public TypeSystem.SPLType visitTerm(SPLParser.TermContext ctx) {
        // TERM ::= ATOM
        if (ctx.atom() != null) {
            TypeSystem.SPLType atomType = visit(ctx.atom());
            recordType(ctx, atomType);
            return atomType;
        }
        
        // TERM ::= ( UNOP TERM )
        if (ctx.unop() != null) {
            String operator = ctx.unop().getText();
            TypeSystem.SPLType operandType = visit(ctx.term(0));
            
            TypeSystem.SPLType resultType = typeSystem.inferUnaryTermType(operator, operandType);
            
            if (resultType == TypeSystem.SPLType.UNKNOWN) {
                addError("Type mismatch for unary operator '" + operator + "' with operand type " + 
                         typeSystem.typeToString(operandType));
            }
            
            recordType(ctx, resultType);
            return resultType;
        }
        
        // TERM ::= ( TERM BINOP TERM )
        if (ctx.binop() != null) {
            String operator = ctx.binop().getText();
            TypeSystem.SPLType leftType = visit(ctx.term(0));
            TypeSystem.SPLType rightType = visit(ctx.term(1));
            
            TypeSystem.SPLType resultType = typeSystem.inferBinaryTermType(operator, leftType, rightType);
            
            if (resultType == TypeSystem.SPLType.UNKNOWN) {
                addError("Type mismatch for binary operator '" + operator + "' with operand types " + 
                         typeSystem.typeToString(leftType) + " and " + typeSystem.typeToString(rightType));
            }
            
            recordType(ctx, resultType);
            return resultType;
        }
        
        return TypeSystem.SPLType.UNKNOWN;
    }
    
    /**
     * UNOP - unary operators
     */
    @Override
    public TypeSystem.SPLType visitUnop(SPLParser.UnopContext ctx) {
        String operator = ctx.getText();
        return typeSystem.getUnaryOperatorType(operator);
    }
    
    /**
     * BINOP - binary operators
     */
    @Override
    public TypeSystem.SPLType visitBinop(SPLParser.BinopContext ctx) {
        String operator = ctx.getText();
        return typeSystem.getBinaryOperatorType(operator);
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Check if a name exists as a variable in the symbol table
     */
    private boolean isVariableInSymbolTable(String name) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(name) && 
                (entry.type.equals("variable") || entry.type.equals("parameter"))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the parameter count for a procedure
     */
    private int getProcedureParamCount(String procName) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(procName) && entry.type.equals("procedure")) {
                return entry.paramCount;
            }
        }
        return -1; // Not found
    }
    
    /**
     * Get the parameter count for a function
     */
    private int getFunctionParamCount(String funcName) {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.name.equals(funcName) && entry.type.equals("function")) {
                return entry.paramCount;
            }
        }
        return -1; // Not found
    }
    
    /**
     * Count the number of arguments in INPUT
     */
    private int countInputArgs(SPLParser.InputContext ctx) {
        if (ctx == null) {
            return 0;
        }
        return ctx.atom().size();
    }
    
    /**
     * Print type checking report
     */
    public void printReport() {
        System.out.println("\n=== TYPE CHECKING REPORT ===");
        
        if (typeErrors.isEmpty()) {
            System.out.println("✓ No type errors found - Program is correctly typed!");
        } else {
            System.out.println("✗ Found " + typeErrors.size() + " type error(s):");
            for (int i = 0; i < typeErrors.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + typeErrors.get(i));
            }
        }
        
        System.out.println("=== END REPORT ===\n");
    }
}