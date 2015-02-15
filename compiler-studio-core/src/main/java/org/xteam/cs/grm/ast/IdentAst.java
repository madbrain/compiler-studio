package org.xteam.cs.grm.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class IdentAst extends AstNode {

	protected String name;

	public IdentAst(Span span, String name) {
		super(span);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void visit(IGrmVisitor visitor) {
		visitor.visitIdent(this);
	}
}
