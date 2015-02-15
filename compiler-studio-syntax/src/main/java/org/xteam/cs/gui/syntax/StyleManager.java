package org.xteam.cs.gui.syntax;

import java.util.HashMap;
import java.util.Map;

public class StyleManager {
	
	private int defaultType;
	private Map<Integer, SyntaxStyle> styles = new HashMap<Integer, SyntaxStyle>();
	
	public StyleManager(int defaultType) {
		this.defaultType = defaultType;
	}

	public SyntaxStyle getStyle(int type) {
		if (! styles.containsKey(type)) {
			type = defaultType;
		}
		return styles.get(type);
	}
	
	public SyntaxStyle getDefaultStyle() {
		return getStyle(defaultType);
	}

	public void set(int type, SyntaxStyle syntaxStyle) {
		this.styles.put(type, syntaxStyle);
	}

}
