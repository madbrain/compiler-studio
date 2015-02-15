
//http://chronos-st.blogspot.com/2007/12/smalltalk-in-one-page.html

lexer St80;

WhitespaceCharacter    = [\n\r\t ];
DecimalDigit           = [0-9];
Letter                 = [A-Za-z];
LetterOrDigit          = {DecimalDigit} | {Letter};
Identifier             = ({Letter} | "_") ({LetterOrDigit} | "_")*;
Comment                = "\"" [^\"]* "\"";
BinarySelectorChar     = "~" | "!" | "@" | "%" | "&" | "*" | "-"
                       | "+" | "=" | "|" | "\\" | "<" | ">" | "," | "?" | "/";
BinaryMessageSelector  = {BinarySelectorChar}+;
DecimalIntegerLiteral  = {DecimalDigit}+;
Radix                  = {DecimalIntegerLiteral};
BaseNIntegerLiteral    = {LetterOrDigit}+;
IntegerLiteral         = "-"? {UnsignedIntegerLiteral};
UnsignedIntegerLiteral = {DecimalIntegerLiteral} | {Radix} "r" {BaseNIntegerLiteral};
ScaledDecimalLiteral   = "-"? {DecimalIntegerLiteral} ("." {DecimalIntegerLiteral})? "s" {DecimalIntegerLiteral}?;
Exponent               = ("e" | "d" | "q") ("-"? {DecimalIntegerLiteral})?;
FloatingPointLiteral   = "-"? {DecimalIntegerLiteral} ("." {DecimalIntegerLiteral} {Exponent}? | {Exponent});
Keyword                = {Identifier} ":";
KeywordMessageSelector = {Keyword}+;

<INITIAL> {
	{WhitespaceCharacter}+;
	COMMENT = {Comment}	-> comment;

	CHARACTER_LITERAL = "$" . -> value;
	ASSIGN            = ":=";
	START_ARRAY       = "#(";
	START_BYTE        = "#[";
	"#\'" -> keep, next(SYMSTR);
	SYMBOL_LITERAL    = "#" ({Identifier} | {KeywordMessageSelector} | {BinaryMessageSelector}) -> value;
	"\'" -> keep, next(STR);

	PIPE  = "|";
	DOT   = ".";
	HAT   = "^";
	SEMI  = ";";
	COLON = ":";
	LPAR  = "(";
	RPAR  = ")";
	LBRT  = "[";
	RBRT  = "]";

	KEYWORD         = {Keyword} -> value;
	SYMBOL_LITERAL  = {KeywordMessageSelector} -> value;
	IDENTIFIER      = {Identifier} -> value;
	BINARY_SELECTOR = {BinaryMessageSelector} -> value;
	INTEGER_LITERAL = {IntegerLiteral} -> value;
	FLOATING_POINT_LITERAL = {FloatingPointLiteral} -> value;
	SCALED_DECIMAL_LITERAL = {ScaledDecimalLiteral} -> value;
}

<STR> {
	"\'\'" -> keep;
	STRING_LITERAL = "\'"   -> next(INITIAL), value;
	[^\']+ -> keep;
}

<SYMSTR> {
	"\'\'" -> keep;
	SYMBOL_LITERAL = "\'" -> next(INITIAL), value;
	[^\']+ -> keep;
}
