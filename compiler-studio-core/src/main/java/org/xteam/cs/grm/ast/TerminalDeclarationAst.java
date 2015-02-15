package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.Span;

public class TerminalDeclarationAst extends DeclarationAst {

	protected AstList<IdentAst> terminals;
	protected IdentAst type;

	public TerminalDeclarationAst(Span span,
			AstList<IdentAst> terminals, IdentAst type) {
		super(span);
		this.terminals = terminals;
		this.type = type;
	}

	public AstList<IdentAst> getTerminals() {
		return terminals;
	}

	public IdentAst getType() {
		return type;
	}

	public void visit(IGrmVisitor visitor) {
		visitor.visitTerminalDeclaration(this);
	}
}
