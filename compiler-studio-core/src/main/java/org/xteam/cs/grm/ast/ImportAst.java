package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public abstract class ImportAst extends AstNode {

	protected IdentAst name;

	public ImportAst(Span span, IdentAst name) {
		super(span);
		this.name = name;
	}

	public IdentAst getName() {
		return name;
	}

	public abstract void visit(IGrmVisitor visitor);

}
