package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class AstFileAst extends AstNode {

	protected IdentAst name;
	protected AstList<NodeAst> nodes;

	public AstFileAst(Span span, IdentAst name, AstList<NodeAst> nodes) {
		super(span);
		this.name = name;
		this.nodes = nodes;
	}

	public IdentAst getName() {
		return name;
	}

	public void setName(IdentAst name) {
		this.name = name;
	}

	public AstList<NodeAst> getNodes() {
		return nodes;
	}

	public void setItems(AstList<NodeAst> items) {
		this.nodes = items;
	}

	public void visit(IAstVisitor visitor) {
		visitor.visitAstFile(this);
	}

}