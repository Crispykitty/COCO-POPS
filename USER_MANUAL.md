# SPL COMPILER - USER MANUAL

---

## PROJECT TYPE: TYPE A (Complete Compiler)

**Implementation Status:** COMPLETE  
**Marking Value:** 10 Points

**Components Implemented:**
- ✓ Lexer (Lexical Analysis)
- ✓ Parser (Syntax Analysis)
- ✓ Semantic Analyzer (Name/Scope Resolution)
- ✓ Type Checker (Type Analysis)
- ✓ Code Generator (Executable BASIC Code)

---

## 1. HOW TO RUN

### Command Syntax:
```bash
java -jar spl-compiler.jar <input.txt> [output.bas]
```

### Examples:
```bash
# Basic usage (outputs to <input>_output.bas)
java -jar spl-compiler.jar program.txt

# Specify custom output file
java -jar spl-compiler.jar program.txt my_output.bas

# Test with provided examples
java -jar spl-compiler.jar test_simple.txt
```

### Input/Output:
- **INPUT:** SPL program in `.txt` file format
- **OUTPUT:** 
  - Executable BASIC code (`.bas` file)
  - Intermediate code file (`_intermediate.txt`) for debugging
  - Inlined code file (`_inlined.txt`) for debugging

---

## 2. OUTPUT MESSAGES

### SUCCESS MESSAGES:
When compilation succeeds, you will see:
```
✓ "Source code loaded (N characters)"
✓ "Parsing successful"
✓ "Semantic analysis passed (no errors)"
✓ "Intermediate code generated (N lines)"
✓ "Inlining completed"
✓ "BASIC code generated (N lines)"
✓✓✓ COMPILATION SUCCESSFUL! ✓✓✓
```

### ERROR MESSAGES:
The compiler detects four types of errors:

**1. Lexical Errors:**
```
✗ "Lexical error: invalid character '@'"
```

**2. Syntax Errors:**
```
✗ "Syntax error: mismatched input 'x' expecting '{'"
```

**3. Naming/Scope Errors:**
```
✗ "Naming error: variable 'y' not declared in scope"
```
*(Corresponds to "Variable Naming and Function Naming accepted" when no errors)*

**4. Type Errors:**
```
✗ "Type error: cannot add string and number"
```
*(Corresponds to "Types accepted" when no errors)*

### Testing Generated BASIC Code:
The generated `.bas` file contains executable BASIC code. Test it using:
- **PCBasic** (Recommended - used for development/testing)
- https://www.calormen.com/jsbasic/
- https://www.pcjs.org/machines/pcx86/ibm/5150/mda/256kb/basic/

---

## 3. GROUP MEMBERS

1. **Tukelo Mokwena** - u22536800
2. **Nigel Mofati** - u22528084
3. **Reneiloe Brancon** - u22556771
4. **Sibusiso Zotwayo** - u22591380

---

## EXAMPLE SPL PROGRAM:

```spl
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

**Expected Output:** Generates BASIC code that prints `15`

---

**Compiler Version:** 1.0.0  
**Technologies Used:** Java 11+, ANTLR4, Maven
