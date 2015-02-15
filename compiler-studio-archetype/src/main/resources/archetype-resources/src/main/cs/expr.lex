lexer Expr;

LETTER = [a-zA-Z];
DIGIT  = [0-9];

<INITIAL> {

	[ \r\n\t]+;
	IDENTIFIER = {LETTER}({LETTER}|{DIGIT})* -> value;
	INTEGER    = {DIGIT}+ -> value;
	PLUS       = "+";
	MINUS      = "-";
	STAR       = "*";
	SLASH      = "/";
	ASSIGN     = ":=";
	LPAR       = "(";
	RPAR       = ")";
}