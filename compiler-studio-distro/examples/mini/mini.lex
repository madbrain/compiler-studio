
lexer Mini;

DIGIT   = [0-9];
LETTER  = [a-zA-Z_];
COMMENT = "//" [^\n]*;

<INITIAL> {
	COMMENT = {COMMENT} -> comment;
	[ \n\r\t]+;

	DEF    = "def";
	RETURN = "return";
	
	LPAR  = "(";
	RPAR  = ")";
	LBRC  = "{";
	RBRC  = "}";
	COMA  = ",";
	SEMI  = ";";
	EQUAL = "=";
	ADD   = "+";
	SUB   = "-";
	MUL   = "*";
	DIV   = "/";

	IDENT  = {LETTER}({LETTER}|{DIGIT})* -> value;
	INT    = {DIGIT}+ -> value;
	"\"" -> next(STR), keep;
}

<STR> {
	STRING = "\"" -> value, return;
	[^\\\"] -> keep;
	"\\" . -> keep;
}