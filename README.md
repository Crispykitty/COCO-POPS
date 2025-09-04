# SPL Compiler 2025 (COS341 Semester Project)

## 📖 Project Overview

This repository hosts the implementation of the Students' Programming Language (SPL) Compiler for the COS341 Semester Project (2025).
The compiler will be developed in phases. This repository currently focuses on the Front-End Phase (lexical analysis + parsing).

- **Language**: SPL (2025 grammar provided by lecturer)
- **Compiler strategy**: Bottom-up SLR parser, grammar used as-is
- **Input format**: SPL source programs (.txt)
- **Output (Phase 1)**: Parse tree / syntax validation (accept or reject)2025 (COS341 Semester Project)
📖 Project Overview

This repository hosts the implementation of the Students’ Programming Language (SPL) Compiler for the COS341 Semester Project (2025).
The compiler will be developed in phases. This repository currently focuses on the Front-End Phase (lexical analysis + parsing).

Language: SPL (2025 grammar provided by lecturer)

Compiler strategy: Bottom-up SLR parser, grammar used as-is

Input format: SPL source programs (.txt)

Output (Phase 1): Parse tree / syntax validation (accept or reject)

## 📂 Folder Structure

```
spl-compiler-2025/
│
├── docs/
│   ├── grammar.md       # Original SPL grammar from spec
│   ├── notes.md         # Team notes, parser/lexer decisions
│   └── design.md        # Design documentation for front-end
│
├── src/
│   ├── frontend/
│   │   ├── lexer/       # Lexical analysis (character stream → tokens)
│   │   ├── tokenizer/   # Token stream utilities (peek/next/expect)
│   │   ├── parser/      # SLR parser driver + table
│   │   ├── tokens/      # Token definitions, enums, regexes
│   │   └── init.py      # (if Python) / package-info.java (if Java)
│   │
│   └── main.(py|cpp|java) # Compiler entry point
│
├── tests/
│   ├── frontend/
│   │   ├── lexer_tests/
│   │   ├── parser_tests/
│   │   └── sample_inputs/  # Example SPL programs to test
│
├── examples/
│   ├── minimal.txt
│   ├── print_number.txt
│   ├── if_else.txt
│   ├── while_do.txt
│   └── calls.txt
│
├── .gitignore
├── LICENSE
└── README.md
```

## 🧑‍🤝‍🧑 Team Work Division

### 2 × Parser Developers
- Encode grammar, construct LR(0) item sets, FOLLOW sets, and SLR table
- Implement table-driven parser driver (stack machine, SHIFT/REDUCE/GOTO/ACCEPT)
- Error recovery

### 1 × Tokenizer Developer
- Implement peek/next/expect API
- Provide stub scanner so parser team can start before lexer is ready
- Maintain fixtures: input → token sequence JSON

### 1 × Lexer Developer
- Implement real Lexer over raw input
- Handle regex rules (identifiers, numbers, strings)
- Enforce keyword table & naming rules
- Write unit tests for edge cases (max string length, leading zeros, etc.)

## 🔑 Token Contracts

### Token Kinds
- **KEYWORDS**: `glob`, `proc`, `func`, `main`, `local`, `var`, `while`, `do`, `until`, `if`, `else`, `halt`, `print`, `return`, `neg`, `not`, `eq`, `or`, `and`, `plus`, `minus`, `mult`, `div`
- **SYMBOLS**: `{`, `}`, `(`, `)`, `;`, `=`
- **OPERATORS**: `>`
- **LITERALS**: `NUMBER`, `STRING`
- **IDENT**: `NAME` (user-defined, not a keyword)
- **SPECIAL**: `EOF`

### Token Shape
```javascript
interface Token {
  kind: TokenKind;   // e.g. IDENT, NUMBER, VAR, IF, PLUS, etc.
  lexeme: string;    // raw source substring
  line: number;      // line number for error reporting
  col: number;       // column number
}
```

## ⚙️ Parser Strategy

- **Approach**: Bottom-up SLR parser
- **Tables**: LR(0) item sets → GOTO graph → FOLLOW sets → SLR parse table
- **Driver**: Table-driven stack machine (SHIFT / REDUCE / GOTO / ACCEPT)
- **Error recovery**: Panic-mode (sync on `{`, `}`, `;`, or EOF)

## ❌ Why not LL(1)?

The SPL grammar has several LL(1) conflicts:

1. `ALGO ::= INSTR | INSTR ; ALGO` → FIRST collisions
2. `TERM ::= ATOM | ( UNOP TERM ) | ( TERM BINOP TERM )` → parenthesis ambiguity
3. `INPUT ::= ε | ATOM | ATOM ATOM | ATOM ATOM ATOM` → FIRST/FOLLOW overlap with ε
4. List-style rules (VARIABLES, PROCDEFS, FUNCDEFS) → nullable recursion conflicts
5. IDENT ambiguity (keyword vs variable name)

## 🚀 Milestones

### M1 (Setup)
- [x] Repo + folder structure
- [ ] Token contract frozen
- [ ] Tokenizer stub implemented
- [ ] Parser team can begin with dummy tokens

### M2 (Integration)
- [ ] SLR tables generated & stored (parser/slr_table.json)
- [ ] Parser driver implemented
- [ ] Lexer covers all token types

### M3 (Completion)
- [ ] Full grammar implemented
- [ ] Error recovery active
- [ ] Example programs parse successfully
- [ ] Documentation updated with LL(1) analysis & parser design

## 📘 References
- Introduction to Compiler Design – Torben Ægidius Mogensen
- COS341 Semester Project 2025 Spec (SPL Grammar)