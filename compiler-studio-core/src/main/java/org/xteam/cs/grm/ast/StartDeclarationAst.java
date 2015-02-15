package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.Span;

public class StartDeclarationAst extends DeclarationAst {

	protected IdentAst nonTerminal;

	public StartDeclarationAst(Span span, IdentAst nonTerminal) {
		super(span);
		this.nonTerminal = nonTerminal;
	}

	public IdentAst getNonTerminal() {
		return nonTerminal;
	}

	public void setNonTerminal(IdentAst nonTerminal) {
		this.nonTerminal = nonTerminal;
	}

	public void visit(IGrmVisitor visitor) {
		visitor.visitStartDeclaration(this);
	}
}
