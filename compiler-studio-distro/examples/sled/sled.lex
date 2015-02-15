
lexer Sled;

DIGIT		= [0-9];
LETTER		= [a-zA-Z_.];
HEXA		= [a-fA-F0-9];
IDENT		= {LETTER}({LETTER}|{DIGIT})*;
INT			= {DIGIT}+ | "0" [xX] {HEXA}+;
COMMENT		= "#" [^\n]*;

<INITIAL> {

	COMMENT      = {COMMENT} -> comment;
	[ \n\t\f\b]+;

	ANY          = "any";
	BIT          = "bit";
	CHECKED      = "checked";
	COLUMNS      = "columns";
	CONSTRUCTORS = "constructors";
	EPSILON      = "epsilon";
	FIELDINFO    = "fieldinfo";
	FIELDS       = "fields";
	FOR          = "for";
	GUARANTEED   = "guaranteed";
	IS           = "is";
	KEEP         = "keep";
	NAMES        = "names";
	OF           = "of";
	OTHERWISE    = "otherwise";
	PATTERNS     = "patterns";
	PLACEHOLDER  = "placeholder";
	RELOCATABLE  = "relocatable";
	SIGNIFICANT  = "significant";
	SOME         = "some";
	SPARSE       = "sparse";
	TO           = "to";
	UNCHECKED    = "unchecked";
	WHEN         = "when";
	WHICH        = "which";

	LPAR         = "(";
	RPAR         = ")";
	LBRC         = "{";
	RBRC         = "}";
	LBRT         = "[";
	RBRT         = "]";
	COLO         = ":";
	COMA         = ",";
	EXCL         = "!";
	AND          = "&";
	OR           = "|";
	SEMI         = ";";
	PLUS         = "+";
	MINUS        = "-";
	TIMES        = "*";
	DIV          = "/";
	AT           = "@";
	EQUALS       = "=";
	HAT          = "^";
	LT           = "<";
	LE           = "<=";
	GT           = ">";
	GE           = ">=";
	NE           = "!=";
	DOTS         = "...";
	"\""         -> keep, next(STR);

	INT          = {INT}   -> value(int, decodeInt);
	IDENT        = {IDENT} -> value;
	// . -> error("bad character \'"+yycharat(0)+"\'");
}

<OPERAND,NL> {
	{COMMENT};
	NEWLINE   = "\n"        -> next(INITIAL);
	NEWLINE   = <<EOF>>     -> next(INITIAL);
	COLO      = ":"         -> next(INITIAL);
	LBRC      = "{"         -> next(INITIAL);
	IS        = "is"        -> next(INITIAL);
	OTHERWISE = "otherwise"	-> next(INITIAL);
	WHEN      = "when"      -> next(INITIAL);
	IDENT     = {IDENT}		-> value;
	"\""      -> keep, next(STR);
}

<NL> {
	[ \t\f\b]+;
	NOISE = [,()[\]]+ -> value;
	HAT   = "^";
	// . -> error("bad character \'"+yycharat(0)+"\' in opcode");
}

<OPERAND> {
	NOISE = [,()[\] \t\f\b]+ -> value;
	EXCL  = "!";
	// .     -> error("bad character \'"+yycharat(0)+"\' in operand");
}

<STR> {
	STRING   = "\"" -> return, value;
	[^\"\\]+ -> keep;
	"\\\""   -> keep;
	"\\"     -> keep;
}
