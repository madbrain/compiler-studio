package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class GrammarFileAst extends AstNode {

	protected IdentAst name;
	protected AstList<ImportAst> astImport;
	protected AstList<DeclarationAst> declarations;
	protected AstList<RuleAst> rules;

	public GrammarFileAst(Span span, IdentAst name, AstList<ImportAst> astImport,
			AstList<DeclarationAst> declarations, AstList<RuleAst> rules) {
		super(span);
		this.name = name;
		this.astImport = astImport;
		this.declarations = declarations;
		this.rules = rules;
	}

	public IdentAst getName() {
		return name;
	}

	public AstList<ImportAst> getImports() {
		return astImport;
	}

	public AstList<DeclarationAst> getDeclarations() {
		return declarations;
	}

	public AstList<RuleAst> getRules() {
		return rules;
	}

	public void visit(IGrmVisitor visitor) {
		visitor.visitGrmFile(this);
	}
}
