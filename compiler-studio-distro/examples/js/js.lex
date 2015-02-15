lexer JS;

DIGIT		= [0-9];
HEXDIGIT	= [0-9a-fA-F];

HEX_ESCAPE   = "x" {HEXDIGIT} {HEXDIGIT} | "u" {HEXDIGIT} {HEXDIGIT} {HEXDIGIT} {HEXDIGIT};
LETTER		 = [a-zA-Z_$];
IDENT_LETTER = {LETTER} | "\\" {HEX_ESCAPE};
IDENT		 = {IDENT_LETTER}({IDENT_LETTER}|{DIGIT})*;

HEX_INTEGER     = "0" [xX] {HEXDIGIT}+;
DECIMAL_INTEGER = "0" | [1-9] {DIGIT}*;
MANTISSA        = {DECIMAL_INTEGER} ("." {DIGIT}*)? | "." {DIGIT}+;
NUMERIC         = {HEX_INTEGER} | {MANTISSA} | {MANTISSA} [eE] ("+"|"-")? {DIGIT}+;

CONTROL_ESCAPE  = [bfnrtv];
STRING_ESCAPE   = {HEX_ESCAPE} | {CONTROL_ESCAPE} | "0";
STRING          =
	  "\"" ([^\"\n\r\\] | "\\" ({STRING_ESCAPE}|"\"") )* "\""
	| "\'" ([^\'\n\r\\] | "\\" ({STRING_ESCAPE}|"\'") )* "\'";

COMMENT     = "//" [^\n]* | "/*" [^*] ~ "*/";

<INITIAL> {
	COMMENT = {COMMENT} -> comment;
	[ \t\f\r\b\n]+;
	
	EXCL           = "!";
	NE             = "!=";
	NE_ID          = "!==";
	SHARP          = "#";
	MOD            = "%";
	MOD_ASSIGN	   = "%=";
	AND_BIT        = "&";
	AND_LOG        = "&&";
	AND_LOG_ASSIGN = "&&=";
	AND_BIT_ASSIGN = "&=";
	LPAR           = "(";
	RPAR           = ")";
	MUL            = "*";
	MUL_ASSIGN     = "*=";
	PLUS           = "+";
	INC            = "++";
	ADD_ASSIGN     = "+=";
	COMA           = ",";
	MINUS          = "-";
	DEC            = "--";
	SUB_ASSIGN     = "-=";
	ARROW          = "->";
	DOT            = ".";
	DOT_DOT        = "..";
	DOT_DOT_DOT    = "...";
	COLON          = ":";
	COLON_COLON    = "::";
	SEMI           = ";";
	LT             = "<";
	SHL            = "<<";
	SHL_ASSIGN     = "<<=";
	LE             = "<=";
	ASSIGN         = "=";
	EQ             = "==";
	EQ_ID          = "===";
	GT             = ">";
	GE             = ">=";
	SHR            = ">>";
	SHR_ASSIGN     = ">>=";
	ASHR           = ">>>";
	ASHR_ASSIGN    = ">>>";
	COND           = "?";
	AROBA          = "@";
	LBRT           = "[";
	RBRT           = "]";
	XOR            = "^";
	XOR_ASSIGN     = "^=";
	POWER          = "^^";
	POWER_ASSIGN   = "^^=";
	LBRC           = "{";
	OR_BIT         = "|";
	OR_BIT_ASSIGN  = "|=";
	OR_LOG         = "||";
	OR_LOG_ASSIGN  = "||=";
	RBRC           = "}";
	TILDE          = "~";
	
	ABSTRACT       = "abstract";
	BREAK          = "break";
	CASE           = "case";
	CATCH          = "catch";
	CLASS          = "class";
	CONST          = "const";
	CONTINUE       = "continue";
	DEBUGGER       = "debugger";
	DEFAULT        = "default";
	DELETE         = "delete";
	DO             = "do";
	ELSE           = "else";
	ENUM           = "enum";
	EVAL           = "eval";
	EXPORT         = "export";
	EXTENDS        = "extends";
	FALSE          = "false";
	FINAL          = "final";
	FINALLY        = "finally";
	FOR            = "for";
	FUNCTION       = "function";
	GOTO           = "goto";
	IF             = "if";
	IMPLEMENTS     = "implements";
	IMPORT         = "import";
	IN             = "in";
	INSTANCEOF     = "instanceof";
	NATIVE         = "native";
	NEW            = "new";
	NULL           = "null";
	PACKAGE        = "package";
	PRIVATE        = "private";
	PROTECTED      = "protected";
	PUBLIC         = "public";
	RETURN         = "return";
	STATIC         = "static";
	SUPER          = "super";
	SWITCH         = "switch";
	SYNCHRONIZED   = "synchronized";
	THIS           = "this";
	THROW          = "throw";
	THROWS         = "throws";
	TRANSIENT      = "transient";
	TRUE           = "true";
	TRY            = "try";
	TYPEOF         = "typeof";
	VAR            = "var";
	VOLATILE       = "volatile";
	WHILE          = "while";
	WITH           = "with";
	
	IDENT   = {IDENT}   -> value;
	NUMERIC = {NUMERIC} -> value;
	STRING  = {STRING}  -> value;
}