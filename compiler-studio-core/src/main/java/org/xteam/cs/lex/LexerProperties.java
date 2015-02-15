package org.xteam.cs.lex;

import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.Group;
import org.xteam.cs.model.Property;

public class LexerProperties extends BaseProperties {

	@Group(id="analysis", display="Lexer Analysis")
	@Property(display="Keyword Optimize")
	public boolean doKeywordOptimize = false;
	
	@Group(id="generator", display="Generator")
	@Property(display="Lexer Package")
	public String lexerPackage = "lexer";
	
}
