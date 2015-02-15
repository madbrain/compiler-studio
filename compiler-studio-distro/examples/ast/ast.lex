lexer Ast;

DIGIT		= [0-9];
LETTER		= [a-zA-Z_];
IDENT		= {LETTER}({LETTER}|{DIGIT})*;
COMMENT     = "//" [^\n]*;

<INITIAL> {

	COMMENT = {COMMENT} -> comment;
	[ \t\f\r\b\n]+;

	LBRC  = "{";
	RBRC  = "}";
	COLO  = ":";
	SEMI  = ";";
	SLASH = "/";
	STAR  = "*";

	IDENT = {IDENT} -> value;
}
