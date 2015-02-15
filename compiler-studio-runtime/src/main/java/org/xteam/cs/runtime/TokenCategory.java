package org.xteam.cs.runtime;

import java.util.HashMap;
import java.util.Map;

public class TokenCategory {
	
	public static final int NORMAL_ID  = 0;
	public static final int LAYOUT_ID  = 1;
	public static final int COMMENT_ID = 2;
	public static final int SPECIAL_ID = 3;
	public static final int EOF_ID     = 4;

	public static final TokenCategory NORMAL = new TokenCategory(NORMAL_ID);
	public static final TokenCategory LAYOUT = new TokenCategory(LAYOUT_ID);
	public static final TokenCategory COMMENT = new TokenCategory(COMMENT_ID);
	public static final TokenCategory SPECIAL = new TokenCategory(SPECIAL_ID);
	public static final TokenCategory EOF     = new TokenCategory(EOF_ID);
	
	private static final Map<String, TokenCategory> categoryMap = new HashMap<String, TokenCategory>();
	
	public static TokenCategory getCategory(String name) {
		return (TokenCategory) categoryMap.get(name);
	}
	
	public static TokenCategory getCategory(int id) {
		switch (id) {
		case NORMAL_ID:
			return NORMAL;
		case LAYOUT_ID:
			return LAYOUT;
		case COMMENT_ID:
			return COMMENT;
		case SPECIAL_ID:
			return SPECIAL;
		case EOF_ID:
			return EOF;
		}
		throw new RuntimeException("Unknown category " + id);
	}
	
	static {
		categoryMap.put("layout",  LAYOUT);
		categoryMap.put("comment", COMMENT);
		categoryMap.put("normal",  NORMAL);
		categoryMap.put("special", SPECIAL);
		categoryMap.put("eof", EOF);
	}

	private int id;
	
	private TokenCategory(int id) {
		this.id = id;
	}
	
	public int id() {
		return id;
	}
	
	public boolean equals(Object other) {
		return this == other;
	}
}
