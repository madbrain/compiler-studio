package org.xteam.cs.runtime;

import java.util.Map;

public class LexerAction {

	public static final int TOKEN    =   1;
	public static final int VALUE    =   2;
	public static final int KEEP     =   4;
	public static final int NEXT     =   8;
	public static final int ERROR    =  16;
	public static final int RETURN   =  32;
	public static final int COMMENT  =  64;
	public static final int CONVERT  = 128;
	public static final int KEYWORDS = 256;

	public int code;
	public String error;
	public int returnValue;
	public int nextValue;
	public int convertCode;
	public Map<String, Integer> keywordMap;
	
	public LexerAction(int code, int returnValue, int nextValue, int convertCode) {
		this.code = code;
		this.returnValue = returnValue;
		this.nextValue = nextValue;
		this.convertCode = convertCode;
	}
	
	public LexerAction(int code, String error) {
		this.code = code;
		this.error = error;
	}
	
	public void setKeywordMap(Map<String, Integer> keywordMap) {
		this.keywordMap = keywordMap;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if ((code & ERROR) != 0)
			return "error("+error+")";
		if ((code & TOKEN) != 0)
			buffer.append("token(").append(returnValue).append(") ");
		if ((code & NEXT) != 0)
			buffer.append("next(").append(nextValue).append(") ");
		if ((code & KEEP) != 0)
			buffer.append("keep ");
		if ((code & VALUE) != 0)
			buffer.append("value ");
		if ((code & RETURN) != 0)
			buffer.append("return ");
		if ((code & COMMENT) != 0)
			buffer.append("comment ");
		if ((code & CONVERT) != 0)
			buffer.append("convert ");
		if ((code & KEYWORDS) != 0)
			buffer.append("keywords(").append(keywordMap).append(") ");
		return buffer.toString();
	}

}
