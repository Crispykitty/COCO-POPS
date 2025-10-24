package backend.codeGen;

import org.antlr.v4.runtime.tree.ParseTree;
import frontend.parser.SymbolTable;
import frontend.parser.antlr.SPLParser;
import frontend.parser.antlr.SPLBaseVisitor;

import java.util.*;

/**
 * FIXED VERSION with proper temporary variable generation per Figure 6.3
 * 
 * Key improvements:
 * 1. Removed _internal suffix (uses symbol table names as-is)
 * 2. Added temporary variable generation for complex expressions
 * 3. Proper code emission pattern following textbook
 */
public class SPLCodeGenerator extends SPLBaseVisitor<String> {
    private SymbolTable symbolTable;
    private int labelCounter = 0;
    private int tempCounter = 0;  // NEW: Counter for temporary variables
    private Map<String, String> internalNames;
    private StringBuilder targetCode;
    private List<String> pendingCode;  // NEW: Accumulates code during expression evaluation

    public SPLCodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.internalNames = new HashMap<>();
        this.targetCode = new StringBuilder();
        this.pendingCode = new ArrayList<>();
        initializeInternalNames();
    }

    private String newLabel(String prefix) {
        return prefix + (++labelCounter);
    }
    
    /**
     * NEW: Generate temporary variable names (T1, T2, T3, ...)
     * Following Figure 6.3: place = newvar()
     */
    private String newTemp() {
        return "T" + (++tempCounter);
    }

    /**
     * FIXED: Use symbol table names as-is (no _internal suffix)
     */
    private void initializeInternalNames() {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.type.equals("variable") || entry.type.equals("parameter")) {
                internalNames.put(entry.name, entry.name);  // FIXED: No suffix
            }
        }
    }

    private String getInternalName(String varName, ParseTree ctx) {
        String internalName = internalNames.get(varName);
        if (internalName == null) {
            throw new RuntimeException("Undeclared variable '" + varName + "' at node " + ctx.getText());
        }
        return internalName;
    }

    public String generateCode(SPLParser.Spl_progContext ctx) {
        targetCode.setLength(0);
        visitSpl_prog(ctx);
        return targetCode.toString();
    }

    @Override
    public String visitSpl_prog(SPLParser.Spl_progContext ctx) {
        for (ParseTree child : ctx.children) {
            String code = visit(child);
            if (code != null && !code.isEmpty()) {
                targetCode.append(code).append("\n");
            }
        }
        return targetCode.toString();
    }

    @Override
    public String visitMainprog(SPLParser.MainprogContext ctx) {
        return visit(ctx.algo());
    }

    @Override
    public String visitPdef(SPLParser.PdefContext ctx) {
        return null;
    }

    @Override
    public String visitFdef(SPLParser.FdefContext ctx) {
        return null;
    }

    @Override
    public String visitAlgo(SPLParser.AlgoContext ctx) {
        StringBuilder code = new StringBuilder();
        for (SPLParser.InstrContext instrCtx : ctx.instr()) {
            String instrCode = visit(instrCtx);
            if (instrCode != null && !instrCode.isEmpty()) {
                code.append(instrCode).append("\n");
            }
        }
        return code.toString().trim();
    }

    @Override
    public String visitInstr(SPLParser.InstrContext ctx) {
        if (ctx.HALT() != null) {
            return "STOP";
        } else if (ctx.PRINT() != null) {
            String outputCode = visitOutput(ctx.output());
            return "PRINT " + outputCode;
        } else if (ctx.assign() != null) {
            return visit(ctx.assign());
        } else if (ctx.branch() != null) {
            return visit(ctx.branch());
        } else if (ctx.loop() != null) {
            return visit(ctx.loop());
        } else if (ctx.name() != null && ctx.input() != null) {
            String procName = ctx.name().IDENT().getText();
            String params = visit(ctx.input());
            return "CALL " + procName + (params != null && !params.isEmpty() ? " " + params : "");
        }
        return null;
    }

    /**
     * IMPROVED: Assignment with proper expression evaluation
     * Following Figure 6.5: Trans(Stat → id := Exp)
     */
    @Override
    public String visitAssign(SPLParser.AssignContext ctx) {
        String varName = ctx.var().IDENT().getText();
        String internalName = getInternalName(varName, ctx.var());
        
        if (ctx.name() != null) {
            // Function call: x = f(args)
            String funcName = ctx.name().IDENT().getText();
            String params = visit(ctx.input());
            return internalName + " = CALL " + funcName + (params != null && !params.isEmpty() ? " " + params : "");
        } else {
            // Regular assignment: x = TERM
            // NEW: Clear pending code before evaluating term
            pendingCode.clear();
            
            // Evaluate the term (generates temporaries and code)
            TermResult result = evaluateTerm(ctx.term());
            
            // Build final code: temp assignments + final assignment
            StringBuilder code = new StringBuilder();
            for (String line : pendingCode) {
                code.append(line).append("\n");
            }
            code.append(internalName).append(" = ").append(result.place);
            
            return code.toString().trim();
        }
    }

    /**
     * NEW: Evaluate a term and return result with generated code
     * Following Figure 6.3 pattern
     */
    private TermResult evaluateTerm(SPLParser.TermContext ctx) {
        if (ctx.atom() != null) {
            // Base case: ATOM
            return new TermResult(visitAtom(ctx.atom()));
        } 
        else if (ctx.unop() != null) {
            // Unary operation: (UNOP TERM)
            TermResult operand = evaluateTerm(ctx.term(0));
            String temp = newTemp();
            String op = ctx.unop().NEG() != null ? "-" : "";
            pendingCode.add(temp + " = " + op + operand.place);
            return new TermResult(temp);
        }
        else if (ctx.binop() != null) {
            String op = visitBinop(ctx.binop());
            
            // Special handling for boolean operators (no temporaries needed - handled in conditions)
            if (op.equals("or") || op.equals("and")) {
                // For boolean ops in non-condition context, just return string representation
                TermResult left = evaluateTerm(ctx.term(0));
                TermResult right = evaluateTerm(ctx.term(1));
                return new TermResult("(" + left.place + " " + op + " " + right.place + ")");
            }
            
            // Binary operation: (TERM BINOP TERM)
            // Following Figure 6.3:
            //   place₁ = newvar()
            //   place₂ = newvar()
            //   code₁ = Trans_Exp(Exp₁, place₁)
            //   code₂ = Trans_Exp(Exp₂, place₂)
            //   result = code₁ ++ code₂ ++ [place := place₁ op place₂]
            
            TermResult left = evaluateTerm(ctx.term(0));
            TermResult right = evaluateTerm(ctx.term(1));
            
            // Generate result temporary
            String resultTemp = newTemp();
            pendingCode.add(resultTemp + " = " + left.place + " " + op + " " + right.place);
            
            return new TermResult(resultTemp);
        }
        
        return new TermResult("0");
    }

    /**
     * Helper class to hold term evaluation result
     */
    private static class TermResult {
        String place;  // The variable/temp holding the result
        
        TermResult(String place) {
            this.place = place;
        }
    }

    /**
     * OLD visitTerm - kept for branch/loop conditions where we need inline expressions
     * This version doesn't generate temporaries (used in IF conditions)
     */
    @Override
    public String visitTerm(SPLParser.TermContext ctx) {
        if (ctx.atom() != null) {
            return visitAtom(ctx.atom());
        } else if (ctx.unop() != null) {
            String op = ctx.unop().NEG() != null ? "-" : "";
            return op + visit(ctx.term(0));
        } else if (ctx.binop() != null) {
            String left = visit(ctx.term(0));
            String right = visit(ctx.term(1));
            String op = visitBinop(ctx.binop());
            if (op.equals("or") || op.equals("and")) {
                return handleBooleanOp(left, op, right);
            }
            return left + " " + op + " " + right;
        }
        return null;
    }

    /**
     * Branch handling - uses visitTerm for inline conditions
     * (Conditions don't need temporaries - they're evaluated inline in IF statements)
     */
    @Override
    public String visitBranch(SPLParser.BranchContext ctx) {
        StringBuilder code = new StringBuilder();
        
        SPLParser.TermContext termCtx = ctx.term();
        boolean hasElse = ctx.algo().size() > 1;
        
        // Check if the term has a NOT operator at the top level
        boolean isNot = false;
        if (termCtx.unop() != null && termCtx.unop().NOT() != null) {
            isNot = true;
            termCtx = termCtx.term(0);
        }
        
        // Determine which algo is "then" and which is "else" based on NOT
        SPLParser.AlgoContext thenAlgo = isNot && hasElse ? ctx.algo(1) : ctx.algo(0);
        SPLParser.AlgoContext elseAlgo = isNot && hasElse ? ctx.algo(0) : (hasElse ? ctx.algo(1) : null);
        
        if (isNot && !hasElse) {
            elseAlgo = ctx.algo(0);
            thenAlgo = null;
        }
        
        return generateBranchCode(termCtx, thenAlgo, elseAlgo);
    }

    private String generateBranchCode(SPLParser.TermContext termCtx, 
                                     SPLParser.AlgoContext thenAlgo, 
                                     SPLParser.AlgoContext elseAlgo) {
        StringBuilder code = new StringBuilder();
        
        // Check if this is a boolean operator at the top level
        if (termCtx.binop() != null) {
            String op = visitBinop(termCtx.binop());
            
            if (op.equals("or")) {
                // OR: Generate labels for this level
                int currentLabel = ++labelCounter;
                String tLabel = "T" + currentLabel;
                String exitLabel = "Exit" + currentLabel;
                
                // Recursively get conditions for left and right
                List<String> leftConditions = extractConditions(termCtx.term(0));
                List<String> rightConditions = extractConditions(termCtx.term(1));
                
                // Generate IF statements for all left conditions
                for (String cond : leftConditions) {
                    String[] parts = parseTerm(cond);
                    code.append("IF ").append(parts[0]).append(" ").append(parts[1])
                        .append(" ").append(parts[2]).append(" THEN ").append(tLabel).append("\n");
                }
                
                // Generate IF statements for all right conditions
                for (String cond : rightConditions) {
                    String[] parts = parseTerm(cond);
                    code.append("IF ").append(parts[0]).append(" ").append(parts[1])
                        .append(" ").append(parts[2]).append(" THEN ").append(tLabel).append("\n");
                }
                
                // Else branch (when all conditions false)
                if (elseAlgo != null) {
                    String elseCode = visit(elseAlgo);
                    if (elseCode != null && !elseCode.isEmpty()) {
                        code.append(elseCode).append("\n");
                    }
                }
                
                code.append("GOTO ").append(exitLabel).append("\n");
                code.append("REM ").append(tLabel).append("\n");
                
                // Then branch (when any condition true)
                if (thenAlgo != null) {
                    String thenCode = visit(thenAlgo);
                    if (thenCode != null && !thenCode.isEmpty()) {
                        code.append(thenCode).append("\n");
                    }
                }
                
                code.append("REM ").append(exitLabel);
                return code.toString().trim();
                
            } else if (op.equals("and")) {
                // AND: Generate sequential checks
                int currentLabel = ++labelCounter;
                String tLabel = "T" + currentLabel;
                String exitLabel = "Exit" + currentLabel;
                int fLabelNum = ++labelCounter;
                String fLabel = "F" + fLabelNum;
                
                // Get all conditions that must be true
                List<String> allConditions = new ArrayList<>();
                collectAndConditions(termCtx, allConditions);
                
                // Check each condition sequentially
                for (int i = 0; i < allConditions.size(); i++) {
                    String[] parts = parseTerm(allConditions.get(i));
                    if (i == 0) {
                        code.append("IF ").append(parts[0]).append(" ").append(parts[1])
                            .append(" ").append(parts[2]).append(" THEN ").append(tLabel).append("\n");
                        code.append("GOTO ").append(fLabel).append("\n");
                        code.append("REM ").append(tLabel).append("\n");
                    } else if (i == allConditions.size() - 1) {
                        // Last condition
                        code.append("IF ").append(parts[0]).append(" ").append(parts[1])
                            .append(" ").append(parts[2]).append(" THEN ").append(tLabel).append("_OK\n");
                        code.append("GOTO ").append(fLabel).append("\n");
                    } else {
                        // Middle conditions
                        String nextLabel = "T" + (++labelCounter);
                        code.append("IF ").append(parts[0]).append(" ").append(parts[1])
                            .append(" ").append(parts[2]).append(" THEN ").append(nextLabel).append("\n");
                        code.append("GOTO ").append(fLabel).append("\n");
                        code.append("REM ").append(nextLabel).append("\n");
                    }
                }
                
                // Then branch (all conditions true)
                code.append("REM ").append(tLabel).append("_OK\n");
                if (thenAlgo != null) {
                    String thenCode = visit(thenAlgo);
                    if (thenCode != null && !thenCode.isEmpty()) {
                        code.append(thenCode).append("\n");
                    }
                }
                code.append("GOTO ").append(exitLabel).append("\n");
                
                // Else branch (any condition false)
                code.append("REM ").append(fLabel).append("\n");
                if (elseAlgo != null) {
                    String elseCode = visit(elseAlgo);
                    if (elseCode != null && !elseCode.isEmpty()) {
                        code.append(elseCode).append("\n");
                    }
                }
                
                code.append("REM ").append(exitLabel).append("\n");
                return code.toString().trim();
            }
        }
        
        // Simple condition (comparison operator)
        String termCode = evaluateTermForCondition(termCtx);
        
        int currentLabel = ++labelCounter;
        String tLabel = "T" + currentLabel;
        String exitLabel = "Exit" + currentLabel;
        
        String[] termParts = parseTerm(termCode);
        String t1 = termParts[0];
        String op = termParts[1];
        String t2 = termParts[2];
        
        code.append("IF ").append(t1).append(" ").append(op).append(" ").append(t2)
            .append(" THEN ").append(tLabel).append("\n");
        
        if (elseAlgo != null) {
            String elseCode = visit(elseAlgo);
            if (elseCode != null && !elseCode.isEmpty()) {
                code.append(elseCode).append("\n");
            }
        }
        
        code.append("GOTO ").append(exitLabel).append("\n");
        code.append("REM ").append(tLabel).append("\n");
        
        if (thenAlgo != null) {
            String thenCode = visit(thenAlgo);
            if (thenCode != null && !thenCode.isEmpty()) {
                code.append(thenCode).append("\n");
            }
        }
        
        code.append("REM ").append(exitLabel);
        return code.toString().trim();
    }

    // Helper methods for branch handling (keep existing implementation)
    private List<String> extractConditions(SPLParser.TermContext termCtx) {
        List<String> conditions = new ArrayList<>();
        
        if (termCtx.binop() != null) {
            String op = visitBinop(termCtx.binop());
            
            if (op.equals("or")) {
                conditions.addAll(extractConditions(termCtx.term(0)));
                conditions.addAll(extractConditions(termCtx.term(1)));
            } else if (op.equals("and")) {
                List<String> andConditions = new ArrayList<>();
                collectAndConditions(termCtx, andConditions);
                conditions.addAll(andConditions);
            } else {
                conditions.add(evaluateTermForCondition(termCtx));
            }
        } else {
            conditions.add(evaluateTermForCondition(termCtx));
        }
        
        return conditions;
    }

    private void collectAndConditions(SPLParser.TermContext termCtx, List<String> conditions) {
        if (termCtx.binop() != null && visitBinop(termCtx.binop()).equals("and")) {
            collectAndConditions(termCtx.term(0), conditions);
            collectAndConditions(termCtx.term(1), conditions);
        } else {
            conditions.add(evaluateTermForCondition(termCtx));
        }
    }

    private String evaluateTermForCondition(SPLParser.TermContext termCtx) {
        if (termCtx.binop() != null) {
            String op = visitBinop(termCtx.binop());
            if (op.equals("=") || op.equals(">")) {
                String left = visit(termCtx.term(0));
                String right = visit(termCtx.term(1));
                return left + " " + op + " " + right;
            }
        }
        return visit(termCtx);
    }

    private String[] parseTerm(String term) {
        String[] parts = term.split("\\s+");
        if (parts.length >= 3) {
            return new String[]{parts[0], parts[1], parts[2]};
        }
        return new String[]{term, "=", "0"};
    }

    private String handleBooleanOp(String left, String op, String right) {
        return "(" + left + " " + op + " " + right + ")";
    }

    @Override
    public String visitLoop(SPLParser.LoopContext ctx) {
        StringBuilder code = new StringBuilder();
        if (ctx.WHILE() != null) {
            String loopLabel = newLabel("L");
            String tLabel = newLabel("T");
            String exitLabel = newLabel("Exit");
            String termCode = visit(ctx.term());
            String[] termParts = parseTerm(termCode);
            String t1 = termParts[0];
            String op = termParts[1];
            String t2 = termParts[2];

            code.append("REM ").append(loopLabel).append("\n");
            code.append("IF ").append(t1).append(" ").append(op).append(" ").append(t2)
                .append(" THEN ").append(tLabel).append("\n");
            code.append("GOTO ").append(exitLabel).append("\n");
            code.append("REM ").append(tLabel).append("\n");
            String algoCode = visit(ctx.algo());
            if (algoCode != null && !algoCode.isEmpty()) {
                code.append(algoCode).append("\n");
            }
            code.append("GOTO ").append(loopLabel).append("\n");
            code.append("REM ").append(exitLabel);
        } else if (ctx.DO() != null) {
            String loopLabel = newLabel("L");
            String exitLabel = newLabel("Exit");
            String termCode = visit(ctx.term());
            String[] termParts = parseTerm(termCode);
            String t1 = termParts[0];
            String op = termParts[1];
            String t2 = termParts[2];

            code.append("REM ").append(loopLabel).append("\n");
            String algoCode = visit(ctx.algo());
            if (algoCode != null && !algoCode.isEmpty()) {
                code.append(algoCode).append("\n");
            }
            code.append("IF ").append(t1).append(" ").append(op).append(" ").append(t2)
                .append(" THEN ").append(exitLabel).append("\n");
            code.append("GOTO ").append(loopLabel).append("\n");
            code.append("REM ").append(exitLabel);
        }
        return code.toString().trim();
    }

    @Override
    public String visitAtom(SPLParser.AtomContext ctx) {
        if (ctx.var() != null) {
            return getInternalName(ctx.var().IDENT().getText(), ctx.var());
        } else if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText();
        }
        return null;
    }

    @Override
    public String visitOutput(SPLParser.OutputContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        } else if (ctx.atom() != null) {
            return visitAtom(ctx.atom());
        }
        return null;
    }

    @Override
    public String visitInput(SPLParser.InputContext ctx) {
        if (ctx.atom() == null || ctx.atom().isEmpty()) return "";
        StringBuilder params = new StringBuilder();
        for (SPLParser.AtomContext atom : ctx.atom()) {
            if (params.length() > 0) params.append(" ");
            params.append(visitAtom(atom));
        }
        return params.toString();
    }

    @Override
    public String visitBinop(SPLParser.BinopContext ctx) {
        if (ctx.EQ() != null) return "=";
        if (ctx.GT() != null) return ">";
        if (ctx.OR() != null) return "or";
        if (ctx.AND() != null) return "and";
        if (ctx.PLUS() != null) return "+";
        if (ctx.MINUS() != null) return "-";
        if (ctx.MULT() != null) return "*";
        if (ctx.DIV() != null) return "/";
        return "";
    }
}