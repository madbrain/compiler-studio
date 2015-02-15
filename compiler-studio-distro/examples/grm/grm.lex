lexer Grm;

DIGIT		= [0-9];
LETTER		= [a-zA-Z_];
IDENT		= {LETTER}({LETTER}|{DIGIT})*;

<INITIAL> {

	COMMENT = "//" [^\n]* -> comment;
	[ \t\f\r\b\n]+;

	GRAMMAR  = "grammar";
	AST      = "ast";
	TERMINAL = "terminal";
	START    = "start";
	LEXER    = "lexer";
	ACTION   = "action";
	LPAR     = "(";
	RPAR     = ")";
	COLO     = ":";
	SEMI     = ";";
	COMA     = ",";
	ASGN     = "=";
	EQUALS   = "::=";
	ARROW    = "->";
	"\"" -> keep, next(STR);
	IDENT    = {IDENT} -> value;
}

<STR> {
	STRING = "\"" -> next(INITIAL), value;
	[^\"\\]+ -> keep;
	"\\\""   -> keep;
	"\\"     -> keep;
	//"\n"     ->	error("Unterminated string"), next(INITIAL), value;
	//<<EOF>>	 ->	error("Unterminated string"), next(INITIAL), value;
}
