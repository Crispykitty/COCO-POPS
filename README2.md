# SPL Compiler - Front-End Components

## Overview
This repository contains the completed lexer and tokenizer components for the COS341 SPL (Students' Programming Language) compiler project. The front-end is production-ready and provides a clean interface for parser integration.

## Project Structure
```
COCO-POPS/
├── src/frontend/
│   ├── lexer/
│   │   └── SPLLexer.java        # Production SPL lexer
│   ├── tokenizer/
│   │   ├── ILexer.java          # Lexer interface
│   │   ├── Tokenizer.java       # Buffering tokenizer with lookahead
│   │   ├── ParseException.java  # Error handling
│   │   └── TokenizerTest.java   # Comprehensive test suite
│   ├── tokens/
│   │   ├── Token.java           # Token data structure
│   │   └── TokenKind.java       # All SPL token types
│   └── Main.java                # Entry point with file reading
├── test/
│   ├── simple.spl               # Basic test program
│   ├── complex.spl              # Advanced test program
│   └── examples/                # Additional test cases
└── bin/                         # Compiled classes (ignored by git)
```

## Components

### 1. SPLLexer
- **Location**: `src/frontend/lexer/SPLLexer.java`
- **Purpose**: Converts input text into SPL tokens
- **Features**:
  - Implements all SPL vocabulary rules
  - Handles keywords, identifiers, numbers, strings, operators
  - Proper string literal parsing with quotes
  - Position tracking for error reporting

### 2. Tokenizer
- **Location**: `src/frontend/tokenizer/Tokenizer.java`
- **Purpose**: Provides buffered access to tokens with lookahead
- **Interface Methods**:
  - `peek(k)` - Look ahead k tokens without consuming
  - `next()` - Consume and return next token
  - `expect(TokenKind)` - Enforce expected token type (throws ParseException)
  - `is(TokenKind)` - Check if next token matches type

### 3. Token System
- **TokenKind**: Enum containing all SPL terminal symbols
- **Token**: Data class with kind, lexeme, line, column information

## Quick Start

### Compilation
```bash
# Clean compilation from project root
cd C:\Users\mokwe\OneDrive\Documentos\COS 341 Project\COCO-POPS
rmdir /s bin
mkdir bin
javac -d bin -cp src src\frontend\lexer\*.java src\frontend\tokenizer\*.java src\frontend\tokens\*.java src\frontend\Main.java
```

### Testing

#### Run Basic Test
```bash
java -cp bin frontend.Main
```

#### Test with SPL Files
```bash
java -cp bin frontend.Main test\simple.spl
java -cp bin frontend.Main test\complex.spl
```

#### Run Comprehensive Test Suite
```bash
javac -d bin -cp bin src\frontend\tokenizer\TokenizerTest.java
java -cp bin frontend.tokenizer.TokenizerTest
```

## SPL Language Support

### Supported Constructs
- **Structure**: `glob`, `proc`, `func`, `main` sections
- **Variables**: Local and global variable declarations
- **Control Flow**: `if/else`, `while`, `do/until` loops
- **Operations**: Arithmetic (`plus`, `minus`, `mult`, `div`)
- **Logic**: Boolean operations (`and`, `or`, `not`, `eq`)
- **I/O**: `print` statements, `halt` instruction
- **Functions**: User-defined functions with return values
- **Procedures**: User-defined procedures without return

### Vocabulary Rules
1. **Keywords**: Reserved words take precedence over identifiers
2. **Identifiers**: Pattern `[a-z][a-z0-9]*` (lowercase only)
3. **Numbers**: Pattern `(0|[1-9][0-9]*)` (no leading zeros except 0)
4. **Strings**: Quoted sequences, max length 15 characters

## Parser Integration

### Usage Example
```java
import frontend.lexer.SPLLexer;
import frontend.tokenizer.ILexer;
import frontend.tokenizer.Tokenizer;
import frontend.tokens.Token;
import frontend.tokens.TokenKind;

// Initialize
String input = "main { halt }";  // or read from file
ILexer lexer = new SPLLexer(input);
Tokenizer tokenizer = new Tokenizer(lexer);

// Parse using tokenizer methods
if (tokenizer.is(TokenKind.MAIN)) {
    Token main = tokenizer.expect(TokenKind.MAIN);
    Token lbrace = tokenizer.expect(TokenKind.LBRACE);
    // ... continue parsing
}
```

### Interface Methods Details

#### `peek(int k)`
- Returns the k-th token ahead without consuming it
- Useful for lookahead decisions
- `peek(1)` returns next token, `peek(2)` returns token after that

#### `next()`
- Consumes and returns the next token
- Advances internal position

#### `expect(TokenKind kind)`
- Consumes next token and verifies it matches expected type
- Throws `ParseException` if token doesn't match
- Use for enforcing grammar requirements

#### `is(TokenKind kind)`
- Checks if next token matches given type without consuming
- Returns boolean
- Use for conditional parsing decisions

## Test Results
The comprehensive test suite validates:
- ✅ All SPL keywords recognized correctly
- ✅ String literals with proper quote handling
- ✅ Number patterns following SPL specification
- ✅ Complex nested expressions
- ✅ All binary and unary operators
- ✅ Complete SPL program structures
- ✅ Error-free processing of 18 comprehensive test cases

## File Reading
The system supports reading SPL programs from `.spl` files:
```bash
java -cp bin frontend.Main path\to\your\program.spl
```

## Error Handling
- Lexical errors result in warnings but continue processing
- Parse errors throw `ParseException` with token location information
- File reading errors provide clear error messages

## Development Notes
- All components follow SPL 2025 specification exactly
- Clean separation between lexer (character processing) and tokenizer (parser interface)
- Comprehensive test coverage ensures reliability
- Ready for parser team integration

## Next Steps for Parser Team
1. Review the tokenizer interface methods above
2. Test with provided SPL files in `test/` directory
3. Build your parser using the tokenizer methods
4. Contact tokenizer team if interface changes needed

## Contact
For questions about the lexer/tokenizer components, contact the tokenizer development team.