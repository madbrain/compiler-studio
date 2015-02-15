package org.xteam.cs.ast.ast;

import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.AstNode;
import org.xteam.cs.runtime.Span;

public class NodeAst extends AstNode {

	protected AbstractFlagAst abstractFlag;
	
	protected IdentAst name;

	protected IdentAst superNode;

	protected AstList<NodeItemAst> items;

	public NodeAst(Span span, AbstractFlagAst abstractFlag, IdentAst name,
			IdentAst superNode, AstList<NodeItemAst> items) {
		super(span);
		this.abstractFlag = abstractFlag;
		this.name = name;
		this.superNode = superNode;
		this.items = items;
	}

	public AbstractFlagAst getAbstractFlag() {
		return abstractFlag;
	}
	
	public IdentAst getName() {
		return name;
	}

	public IdentAst getSuperNode() {
		return superNode;
	}

	public AstList<NodeItemAst> getItems() {
		return items;
	}

	public void visit(IAstVisitor visitor) {
		visitor.visitNode(this);
	}
	
}