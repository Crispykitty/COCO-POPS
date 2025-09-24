# Symbol Table Implementation Guide - For Dummies

## What You're Working With

### The Parser is Already Done
The ANTLR parser creates a **syntax tree** automatically when you call:
```java
SPLParserWrapper parser = new SPLParserWrapper(input);
SPLParser.Spl_progContext syntaxTree = parser.parse();
```

### What is a Syntax Tree?
Think of it like a family tree, but for code:
```
spl_prog
├── glob { variables }
├── proc { procedures }  
├── func { functions }
└── main { main program }
```

## Your Mission: Symbol Table

### What You Need to Build
1. **Symbol Table** - A hash table that stores information about variables, functions, procedures
2. **Node ID System** - Give each tree node a unique number
3. **Scope Tracker** - Know which scope you're currently in
4. **Error Detection** - Find undeclared variables, name conflicts, etc.

## Step-by-Step Implementation

### Step 1: Create Basic Data Structures
```java
// Create these classes in frontend/semantic/symboltable/
public class Symbol {
    private String name;
    private SymbolType type;  // VARIABLE, FUNCTION, PROCEDURE
    private ScopeType scope;  // GLOBAL, LOCAL, MAIN
    private int nodeId;       // Link to syntax tree node
    private int parameterCount; // For functions/procedures
}

public class SymbolTable {
    private Map<Integer, Symbol> table = new HashMap<>(); // nodeId -> Symbol
    private int nextNodeId = 1;
}
```

### Step 2: Add Node IDs to Syntax Tree
Modify `SPLParserWrapper` to assign unique IDs to each parse tree node:
```java
// Add this to SPLParserWrapper
private int nodeIdCounter = 1;

private void assignNodeIds(ParseTree node) {
    // Assign ID to current node
    node.setUserObject(nodeIdCounter++);
    
    // Recursively assign to children
    for (int i = 0; i < node.getChildCount(); i++) {
        assignNodeIds(node.getChild(i));
    }
}
```

### Step 3: Create a Tree Walker
Use ANTLR's visitor pattern to walk the syntax tree:
```java
public class SymbolTableBuilder extends SPLBaseVisitor<Void> {
    private SymbolTable symbolTable = new SymbolTable();
    private ScopeType currentScope = ScopeType.EVERYWHERE;
    
    @Override
    public Void visitSpl_prog(SPLParser.Spl_progContext ctx) {
        currentScope = ScopeType.EVERYWHERE;
        return super.visitSpl_prog(ctx);
    }
    
    @Override
    public Void visitVariables(SPLParser.VariablesContext ctx) {
        // Add global variables to symbol table
        if (ctx.var() != null) {
            String varName = ctx.var().IDENT().getText();
            int nodeId = getNodeId(ctx);
            symbolTable.addSymbol(varName, SymbolType.VARIABLE, currentScope, nodeId);
        }
        return super.visitVariables(ctx);
    }
}
```

### Step 4: Implement Scope Rules
Follow the instructor's specification document:

#### Scope Hierarchy:
- **Everywhere Scope** - Contains Global, Procedure, Function, Main scopes
- **Global Scope** - Variables declared in `glob { }`
- **Local Scopes** - Variables in procedures/functions
- **Main Scope** - Variables in `main { }`

#### Validation Rules:
```java
public class ScopeValidator {
    public void validateNoNameConflicts() {
        // Rule: No variable name = function name
        // Rule: No variable name = procedure name  
        // Rule: No function name = procedure name
    }
    
    public void validateNoDuplicateDeclarations() {
        // Rule: No duplicate variables in same scope
        // Rule: No duplicate procedures
        // Rule: No duplicate functions
    }
}
```

### Step 5: Variable Resolution
When you find a variable usage, resolve it using these rules:

```java
public Symbol resolveVariable(String varName, ScopeType currentScope) {
    // 1. Check local scope first (if in procedure/function)
    // 2. Check parameters (if in procedure/function)  
    // 3. Check global scope
    // 4. If not found -> error: "undeclared variable"
}
```

## How to Get Started

### File Structure to Create:
```
frontend/semantic/
├── symboltable/
│   ├── SymbolTable.java
│   ├── Symbol.java
│   ├── SymbolType.java (enum)
│   └── ScopeType.java (enum)
├── analyzer/
│   ├── SymbolTableBuilder.java (visitor)
│   └── ScopeValidator.java
└── errors/
    └── SemanticError.java
```

### Your First Task:
1. Create the `Symbol` and `SymbolTable` classes
2. Add node ID assignment to the parser
3. Write a simple visitor that just prints out variable names
4. Test it on a basic SPL program

### Testing Your Work:
Use this simple SPL program to start:
```spl
glob { x y }
proc { }  
func { }
main { var { z } halt }
```

Expected symbol table entries:
- x: VARIABLE, GLOBAL scope, nodeId=3
- y: VARIABLE, GLOBAL scope, nodeId=4  
- z: VARIABLE, MAIN scope, nodeId=7

## Key Resources:
- Instructor's specification: Lists all the scope rules you need to implement
- Your working parser: `SPLParserWrapper` - already creates the syntax tree
- ANTLR visitor pattern: `SPLBaseVisitor` - extend this to walk the tree

## Remember:
The syntax tree already exists. You're not creating it - you're walking through it and building a symbol table that references it via node IDs.

Start simple, test frequently, and build incrementally. The parser foundation is solid, so focus on the semantic analysis logic.