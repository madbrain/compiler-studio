package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class NodeItemAst extends AstNode {
	
	protected IdentAst name;
	private TypeAst type;

	public NodeItemAst(Span span, IdentAst name, TypeAst type) {
		super(span);
		this.name = name;
		this.type = type;
	}
	
	public IdentAst getName() {
		return name;
	}
	
	public TypeAst getType() {
		return type;
	}

	public void visit(IAstVisitor visitor) {
		visitor.visitNodeItem(this);
	}
}