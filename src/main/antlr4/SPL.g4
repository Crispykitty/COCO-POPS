grammar SPL;

// ===== PARSER RULES =====
// Following SPL specification exactly with corrections

spl_prog : GLOB LBRACE variables RBRACE 
           PROC LBRACE procdefs RBRACE 
           FUNC LBRACE funcdefs RBRACE 
           MAIN LBRACE mainprog RBRACE ;

// Variables (nullable)
variables : // empty
          | var variables ;

var : IDENT ;
name : IDENT ;

// Procedure Definitions (nullable)
procdefs : // empty
         | pdef procdefs ;

pdef : name LPAREN param RPAREN LBRACE body RBRACE ;

// Function Definitions (nullable) 
funcdefs : // empty
         | fdef funcdefs ;

// FIXED: Functions have simpler structure - no algorithm before return
fdef : name LPAREN param RPAREN LBRACE LOCAL LBRACE maxthree RBRACE RETURN atom RBRACE ;

// Function/Procedure Body (for procedures only)
body : LOCAL LBRACE maxthree RBRACE algo ;

// Parameters (max 3)
param : maxthree ;

maxthree : // empty
         | var
         | var var  
         | var var var ;

// Main Program - CORRECTED to match SPL spec
mainprog : VAR LBRACE variables RBRACE algo ;

// Atoms (variables or numbers)
atom : var
     | NUMBER ;

// Algorithm (sequence of instructions) - CORRECTED
algo : instr
     | instr SEMI algo ;

// Instructions - CORRECTED order and structure
instr : HALT
      | PRINT output
      | name LPAREN input RPAREN    // procedure call
      | assign
      | loop
      | branch ;

// Assignment - CORRECTED
assign : var ASSIGN name LPAREN input RPAREN  // function call assignment
       | var ASSIGN term ;                     // term assignment

// Loops
loop : WHILE term LBRACE algo RBRACE
     | DO LBRACE algo RBRACE UNTIL term ;

// Branching
branch : IF term LBRACE algo RBRACE
       | IF term LBRACE algo RBRACE ELSE LBRACE algo RBRACE ;

// Output
output : atom
       | STRING ;

// Input (max 3 parameters)
input : // empty
      | atom
      | atom atom
      | atom atom atom ;

// Terms (expressions)
term : atom
     | LPAREN unop term RPAREN
     | LPAREN term binop term RPAREN ;

// Unary operators
unop : NEG
     | NOT ;

// Binary operators  
binop : EQ
      | GT
      | OR
      | AND
      | PLUS
      | MINUS
      | MULT
      | DIV ;

// ===== LEXER RULES =====
// Following SPL vocabulary rules exactly

// Structure keywords (Rule 1: keywords take precedence)
GLOB   : 'glob' ;
PROC   : 'proc' ;
FUNC   : 'func' ;
MAIN   : 'main' ;
LOCAL  : 'local' ;
VAR    : 'var' ;

// Control flow keywords
WHILE  : 'while' ;
DO     : 'do' ;
UNTIL  : 'until' ;
IF     : 'if' ;
ELSE   : 'else' ;

// Statement keywords
HALT   : 'halt' ;
PRINT  : 'print' ;
RETURN : 'return' ;

// Unary operators
NEG    : 'neg' ;
NOT    : 'not' ;

// Binary operators
EQ     : 'eq' ;
GT     : 'gt' ;
OR     : 'or' ;
AND    : 'and' ;
PLUS   : 'plus' ;
MINUS  : 'minus' ;
MULT   : 'mult' ;
DIV    : 'div' ;

// Symbols
LBRACE : '{' ;
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
SEMI   : ';' ;
ASSIGN : '=' ;

// Rule 2: user-defined-name [a-z][a-z0-9]*
// Must come AFTER keywords due to precedence
IDENT : [a-z][a-z0-9]* ;

// Rule 3: numbers (0 | [1-9][0-9]*)
NUMBER : '0' | [1-9][0-9]* ;

// Rule 4: strings "..." max length 15
STRING : '"' ( [a-zA-Z0-9 ] )* '"' ;

// Whitespace handling (skip blanks, tabs, newlines)
WS : [ \t\r\n]+ -> skip ;

// Optional: Comments for debugging
COMMENT : '//' ~[\r\n]* -> skip ;