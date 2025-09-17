package frontend.tokens;

public enum TokenKind {
    // Literals (from vocabulary rules)
    IDENT,          // user-defined-name [a...z]{a...z}*{0...9}*
    NUMBER,         // constant number (0 | [1...9][0...9]*)
    STRING,         // string literals "..."
    
    // Symbols (terminals from grammar)
    LBRACE,         // {
    RBRACE,         // }
    LPAREN,         // (
    RPAREN,         // )
    SEMI,           // ;
    ASSIGN,         // =
    GT,             // >
    
    // Keywords (green terminals from SPL grammar)
    GLOB,           // glob
    PROC,           // proc  
    FUNC,           // func
    MAIN,           // main
    LOCAL,          // local
    VAR,            // var
    WHILE,          // while
    DO,             // do
    UNTIL,          // until
    IF,             // if
    ELSE,           // else
    HALT,           // halt
    PRINT,          // print
    RETURN,         // return
    
    // Unary operators (green terminals)
    NEG,            // neg
    NOT,            // not
    
    // Binary operators (green terminals)
    EQ,             // eq
    OR,             // or
    AND,            // and
    PLUS,           // plus
    MINUS,          // minus
    MULT,           // mult
    DIV,            // div
    
    // Special
    EOF             // End of file
}