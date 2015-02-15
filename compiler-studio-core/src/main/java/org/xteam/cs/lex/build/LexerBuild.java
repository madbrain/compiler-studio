package org.xteam.cs.lex.build;

import org.xteam.cs.runtime.ILexerTables;

public class LexerBuild {

	private LexerMapping mapping;
	private ILexerTables tables;
	private String name;

	public LexerBuild(String name, LexerMapping lexerMapping, ILexerTables tables) {
		this.name = name;
		this.mapping = lexerMapping;
		this.tables = tables;
	}

	public ILexerTables getTables() {
		return tables;
	}

	public LexerMapping getMapping() {
		return mapping;
	}

	public String getName() {
		return name;
	}

}
