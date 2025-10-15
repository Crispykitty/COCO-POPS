package backend.codeGen;

import org.antlr.v4.runtime.tree.ParseTree;
import frontend.parser.SymbolTable;
import frontend.parser.antlr.SPLParser;
import frontend.parser.antlr.SPLBaseVisitor;

import java.util.*;

public class SPLCodeGenerator extends SPLBaseVisitor<String> {
    private SymbolTable symbolTable;
    private int labelCounter = 0;
    private Map<String, String> internalNames;
    private StringBuilder targetCode;

    public SPLCodeGenerator(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.internalNames = new HashMap<>();
        this.targetCode = new StringBuilder();
        initializeInternalNames();
    }

    private String newLabel(String prefix) {
        return prefix + (++labelCounter);
    }

    private void initializeInternalNames() {
        for (SymbolTable.SymbolEntry entry : symbolTable.getTable().values()) {
            if (entry.type.equals("variable") || entry.type.equals("parameter")) {
                internalNames.put(entry.name, entry.name + "_internal");
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
        return null; // To be handled in inlining phase
    }

    @Override
    public String visitFdef(SPLParser.FdefContext ctx) {
        return null; // To be handled in inlining phase
    }

    @Override
    public String visitAlgo(SPLParser.AlgoContext ctx) {
        StringBuilder code = new StringBuilder();
        if (ctx.instr() != null) {
            String instrCode = visit(ctx.instr());
            if (instrCode != null) {
                code.append(instrCode).append("\n");
            }
        }
        if (ctx.algo() != null) {
            String algoCode = visit(ctx.algo());
            if (algoCode != null) {
                code.append(algoCode).append("\n");
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

    @Override
    public String visitAssign(SPLParser.AssignContext ctx) {
        String varName = ctx.var().IDENT().getText();
        String internalName = getInternalName(varName, ctx.var());
        if (ctx.name() != null) {
            String funcName = ctx.name().IDENT().getText();
            String params = visit(ctx.input());
            return internalName + " = CALL " + funcName + (params != null && !params.isEmpty() ? " " + params : "");
        } else {
            String termCode = visit(ctx.term());
            return internalName + " = " + termCode;
        }
    }

    @Override
    public String visitBranch(SPLParser.BranchContext ctx) {
        StringBuilder code = new StringBuilder();
        String termCode = visit(ctx.term());
        String tLabel = newLabel("T");
        String exitLabel = newLabel("Exit");

        String[] termParts = parseTerm(termCode);
        String t1 = termParts[0];
        String op = termParts[1];
        String t2 = termParts[2];

        boolean isNot = ctx.term().unop() != null && ctx.term().unop().NOT() != null;
        SPLParser.AlgoContext thenAlgo = isNot && ctx.algo().size() > 1 ? ctx.algo(1) : ctx.algo(0);
        SPLParser.AlgoContext elseAlgo = isNot && ctx.algo().size() > 1 ? ctx.algo(0) : (ctx.algo().size() > 1 ? ctx.algo(1) : null);

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

        String thenCode = visit(thenAlgo);
        if (thenCode != null && !thenCode.isEmpty()) {
            code.append(thenCode).append("\n");
        }

        code.append("REM ").append(exitLabel);
        return code.toString().trim();
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
        return null;
    }

    private String[] parseTerm(String termCode) {
        String[] parts = termCode.split("\\s+");
        if (parts.length == 3) {
            return parts;
        }
        return new String[]{termCode, "=", "0"};
    }

    private String handleBooleanOp(String left, String op, String right) {
        String tLabel = newLabel("T");
        String exitLabel = newLabel("Exit");
        StringBuilder code = new StringBuilder();
        String[] leftParts = parseTerm(left);
        String[] rightParts = parseTerm(right);

        if (op.equals("or")) {
            code.append("IF ").append(leftParts[0]).append(" ").append(leftParts[1])
                .append(" ").append(leftParts[2]).append(" THEN ").append(tLabel).append("\n");
            code.append("IF ").append(rightParts[0]).append(" ").append(rightParts[1])
                .append(" ").append(rightParts[2]).append(" THEN ").append(tLabel).append("\n");
            code.append("GOTO ").append(exitLabel).append("\n");
            code.append("REM ").append(tLabel).append("\n");
            code.append("REM ").append(exitLabel);
        } else if (op.equals("and")) {
            String fLabel = newLabel("F");
            code.append("IF ").append(leftParts[0]).append(" ").append(leftParts[1])
                .append(" ").append(leftParts[2]).append(" THEN ").append(tLabel).append("\n");
            code.append("GOTO ").append(fLabel).append("\n");
            code.append("REM ").append(tLabel).append("\n");
            code.append("IF ").append(rightParts[0]).append(" ").append(rightParts[1])
                .append(" ").append(rightParts[2]).append(" THEN ").append(tLabel).append("\n");
            code.append("GOTO ").append(fLabel).append("\n");
            code.append("REM ").append(tLabel).append("\n");
            code.append("REM ").append(fLabel);
        }
        return code.toString().trim();
    }
}