package org.xteam.cs.lex.model;

import org.xteam.cs.runtime.LexerAction;

public class ActionCode {
	
	public static final ActionCode TOKEN    = new ActionCode("TOKEN", LexerAction.TOKEN);
	public static final ActionCode VALUE    = new ActionCode("VALUE", LexerAction.VALUE);
	public static final ActionCode KEEP     = new ActionCode("KEEP", LexerAction.KEEP);
	public static final ActionCode NEXT     = new ActionCode("NEXT", LexerAction.NEXT);
	public static final ActionCode ERROR    = new ActionCode("ERROR", LexerAction.ERROR);
	public static final ActionCode RETURN   = new ActionCode("RETURN", LexerAction.RETURN);
	public static final ActionCode COMMENT  = new ActionCode("COMMENT", LexerAction.COMMENT);
	public static final ActionCode CONVERT  = new ActionCode("CONVERT", LexerAction.CONVERT);
	public static final ActionCode KEYWORDS = new ActionCode("KEYWORDS", LexerAction.KEYWORDS);;
	
	private String name;
	private int code;

	private ActionCode(String name, int code) {
		this.name = name;
		this.code = code;
	}

	public int getValue() {
		return code;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
