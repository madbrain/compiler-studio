package org.xteam.cs.grm.build;

import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.runtime.IParseTables;

public class ParserBuild {

	private GrammarMapping mapping;
	private IParseTables table;
	private String name;
	private LexerBuild lexerBuild;
	private boolean isValid = false;
	private boolean isValidComputed = false;
	private AstModel astModel;

	public ParserBuild(String name, GrammarMapping mapping,
			LexerBuild lexerBuild, AstModel astModel, IParseTables table) {
		this.name = name;
		this.mapping = mapping;
		this.table = table;
		this.lexerBuild = lexerBuild;
		this.astModel = astModel;
	}

	public IParseTables getTables() {
		return table;
	}

	public GrammarMapping getMapping() {
		return mapping;
	}

	public String getName() {
		return name;
	}

	public LexerBuild getLexerBuild() {
		return lexerBuild;
	}

	public boolean isValidForConcrete() {
		return lexerBuild != null;
	}

	public boolean isValidForAST() {
		if (! isValidForConcrete())
			return false;
		if (!isValidComputed) {
			isValid = true;
			for (int r = 0; r < table.productionTable().length; ++r) {
				if (mapping.getRuleFor(r).getAction() == null) {
					isValid = false;
					break;
				}
			}
			isValidComputed = true;
		}
		return isValid;
	}

	public AstModel getAstModel() {
		return astModel;
	}
	
}
