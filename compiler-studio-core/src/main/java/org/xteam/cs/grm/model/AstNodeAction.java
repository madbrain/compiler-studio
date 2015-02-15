package org.xteam.cs.grm.model;

import java.util.List;

import org.xteam.cs.ast.model.AstNode;

public class AstNodeAction extends Action {

	private AstNode node;
	private List<Binding> fields;

	public AstNodeAction(AstNode node, List<Binding> fields) {
		this.node = node;
		this.fields = fields;
	}

	public AstNode getNode() {
		return node;
	}

	public List<Binding> getBindings() {
		return fields;
	}

}
