package org.xteam.cs.grm;

public interface IGrammarTokens {

	public static final int GRAMMAR  =  0;
	public static final int TERMINAL =  1;
	public static final int START    =  2;
	public static final int LEXER    =  3;
	public static final int AST      =  4;
	public static final int ASGN     =  5;
	public static final int SEMI     =  6;
	public static final int COMA     =  7;
	public static final int EQUALS   =  8;
	public static final int ARROW    =  9;
	public static final int LPAR     = 10;
	public static final int RPAR     = 11;
	public static final int COLO     = 12;
	public static final int STRING   = 13;
	public static final int IDENT    = 14;
	public static final int COMMENT  = 15;
	public static final int NONASSOC = 16;
	public static final int LEFT     = 17;
	public static final int RIGHT    = 18;
	public static final int PREC     = 19;
	public static final int EOF      = -1;
}
