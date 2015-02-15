
lexer Cxx;

WS								= [ \f\v\t];

DIGIT							= [0-9];
HEX								= [0-9A-Fa-f];
LETTER							= [A-Z_a-z];
SIMPLE_ESCAPE_SEQUENCE			= "\\\'" | "\\\"" | "\\\?" | "\\\\" | "\\a" | "\\b" | "\\f" | "\\n" | "\\r" | "\\t" | "\\v";
OCTAL_ESCAPE_SEQUENCE			= "\\" [0-7] | "\\" [0-7][0-7] | "\\" [0-7][0-7][0-7];
HEXADECIMAL_ESCAPE_SEQUENCE		= "\\x" {HEX}+;
ESCAPE_SEQUENCE					= {SIMPLE_ESCAPE_SEQUENCE}|{OCTAL_ESCAPE_SEQUENCE}|{HEXADECIMAL_ESCAPE_SEQUENCE};
UNIVERSAL_CHARACTER_NAME		= "\\u" {HEX}{HEX}{HEX}{HEX} | "\\U" {HEX}{HEX}{HEX}{HEX}{HEX}{HEX}{HEX}{HEX};
NON_DIGIT						= {LETTER}|{UNIVERSAL_CHARACTER_NAME};
IDENTIFIER						= {NON_DIGIT}({NON_DIGIT}|{DIGIT})*;

CHARACTER_LIT					= "L"? "\'" ([^\'\\\n] | "\\" .)*;
CHARACTER_LITERAL				= {CHARACTER_LIT} "\'";

STRING_LIT						= "L"? "\"" ([^\"\\\n] | "\\" .)*;
STRING_LITERAL					= {STRING_LIT} "\"";

PP_NUMBER						= "\."? {DIGIT}({DIGIT}|{NON_DIGIT}|[eE][-+] | "\.")*;

TRADITIONAL_COMMENT				= "/*" [^*] ~ "*/";
END_OF_LINE_COMMENT				= "//" [^\r\n]* [\r\n]; 
COMMENT							= {TRADITIONAL_COMMENT}|{END_OF_LINE_COMMENT};
 
<INITIAL> {

	^{WS}* "#" .* "\n";
	[ \t\f\r\n\b]+;
	{COMMENT} -> comment;
	CHARACTER = {CHARACTER_LITERAL} -> value;
	//CHARACTER = {CHARACTER_LIT} "\\" -> value, error("End of line assumed to terminate character with trailing escape.");
	//CHARACTER = {CHARACTER_LIT} -> value, error("End of line assumed to terminate character.");

	STRING = {STRING_LITERAL} -> value;
	//{STRING_LIT} "\\" -> value, error("End of line assumed to terminate string with trailing escape.");
	//{STRING_LIT} -> value, error("End of line assumed to terminate string.");

	ASM          = "asm";
	AUTO         = "auto";
	BOOL         = "bool";
	BREAK        = "break";
	CASE         = "case";
	CATCH        = "catch";
	CHAR         = "char";
	CLASS        = "class";
	CONST        = "const";
	CONST_CAST   = "const_cast";
	CONTINUE     = "continue";
	DEAULT       = "default";
	DELETE       = "delete";
	DO           = "do";
	DOUBLE       = "double";
	DYNAMIC_CAST = "dynamic_cast";
	ELSE         = "else";
	ENUM         = "enum";
	EXPLICIT     = "explicit";
	EXPORT       = "export";
	EXTERN       = "extern";
	FALSE        = "false";
	FLOAT        = "float";
	FOR          = "for";
	FRIEND       = "friend";
	GOTO         = "goto";
	IF           = "if";
	INLINE       = "inline";
	INT          = "int";
	LONG         = "long";
	MUTABLE      = "mutable";
	NAMESPACE    = "namespace";
	NEW          = "new";
	OPERATOR     = "operator";
	PRIVATE      = "private";
	PROTECTED    = "protected";
	PUBLIC       = "public";
	REGISTER     = "register";
	REINTERPRET_CAST = "reinterpret_cast";
	RETURN       = "return";
	SHORT        = "short";
	SIGNED       = "signed";
	SIZEOF       = "sizeof";
	STATIC       = "static";
	STATIC_CAST  = "static_cast";
	STRUCT       = "struct";
	SWITCH       = "switch";
	TEMPLATE     = "template";
	THIS         = "this";
	THROW        = "throw";
	TRUE         = "true";
	TRY          = "try";
	TYPEDEF      = "typedef";
	TYPEID       = "typeid";
	TYPENAME     = "typename";
	UNION        = "union";
	UNSIGNED     = "unsigned";
	USING        = "using";
	VIRTUAL      = "virtual";
	VOID         = "void";
	VOLATILE     = "volatile";
	WCHAR_T      = "wchar_t";
	WHILE        = "while";

	SCOPE        = "::";
	ELLIPSIS     = "...";
	SHL          = "<<";
	SHR          = ">>";
	EQ           = "==";
	NE           = "!=";
	LE           = "<=";
	GE           = ">=";
	LOG_aND      = "&&";
	LOG_OR       = "||";
	INC          = "++";
	DEC          = "--";
	ARROW_STAR   = "->*";
	ARROW        = "->";
	DOT_STAR     = ".*";
	ASS_ADD      = "+=";
	ASS_SUB      = "-=";
	ASS_MUL      = "*=";
	ASS_DIV      = "/=";
	ASS_MOD      = "%=";
	ASS_XOR      = "^=";
	ASS_AND      = "&=";
	ASS_OR       = "|=";
	ASS_SHR      = ">>=";
	ASS_SHL      = "<<=";

	NUMBER       = {PP_NUMBER} -> value;

	IDENTIFIER   = {IDENTIFIER} -> value;

	ESCAPE       = {ESCAPE_SEQUENCE}|{UNIVERSAL_CHARACTER_NAME} -> value;

	SEMI         = ";";
	COMA         = ",";
	LPAR         = "(";
	RPAR         = ")";
	STAR         = "*";
	LBRT         = "[";
	RBRT         = "]";
	LBRC         = "{";
	RBRC         = "}";
	ASSIGN       = "=";
	LT_OP        = "<";
	GT_OP        = ">";
	XOR_OP       = "^";
	PIPE         = "|";
	COND         = "?";
	COLO         = ":";
	DOT          = ".";
	AMP          = "&";
	DIV          = "/";
	MOD          = "%";
	PLUS         = "+";
	MINUS        = "-";
	EXCL         = "!";
	TILDE        = "~";
}
