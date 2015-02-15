package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class IdentAst extends AstNode {

	private String value;

	public IdentAst(Span span, String value) {
		super(span);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void visit(IAstVisitor visitor) {
		visitor.visitIdent(this);
	}
	
}