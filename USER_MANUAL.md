# SPL COMPILER - USER MANUAL

## PROJECT INFORMATION

**Project Type:** Type A - Complete Compiler  
**Components:** Lexer, Parser, Semantic Analyzer, Type Checker, Code Generator (Executable BASIC)

**Group Members:**
1. Tukelo Mokwena - u22536800
2. Nigel Mofati - u22528084
3. Reneiloe Brancon - u22556771
4. Sibusiso Zotwayo - u22591380

---

## HOW TO RUN

**Command:**
```
java -jar spl-compiler.jar <input.txt> [output.bas]
```

**Examples:**
```
java -jar spl-compiler.jar program.txt
java -jar spl-compiler.jar program.txt output.bas
```

**Input:** SPL program in `.txt` file  
**Output:** Executable BASIC code in `.bas` file

---

## OUTPUT MESSAGES

**Success:**
- "Tokens accepted" (no lexical errors)
- "Syntax accepted" (no syntax errors)
- "Variable Naming and Function Naming accepted" (no scope errors)
- "Types accepted" (no type errors)
- "COMPILATION SUCCESSFUL" + generates .bas file

**Errors:**
- "Lexical error: ..." 
- "Syntax error: ..."
- "Naming error: ..." (scope/declaration errors)
- "Type error: ..."
- "COMPILATION FAILED"

---

## TESTING BASIC OUTPUT

Generated `.bas` files can be tested with:
- PCBasic
- https://www.calormen.com/jsbasic/

**Example SPL Program:**
```
glob { x y }
proc { }
func { }
main {
    var { }
    x = 5;
    y = 10;
    print (x plus y)
}
```
Output: BASIC code that prints `15`
