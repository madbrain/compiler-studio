/**
 * 
 */
package org.xteam.cs.types;

import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.ast.model.AstNode;
import org.xteam.cs.runtime.Span;

public class NodeType extends Type {

	private AstNode node;

	public NodeType(Span span, AstNode node) {
		super(span);
		this.node = node;
	}
	
	public AstNode getAstNode() {
		return node;
	}
	
	public boolean equals(Object o) {
		return o instanceof NodeType && ((NodeType)o).node == node;
	}

	public Type merge(NodeType nodeAction) {
		AstNode ancestor = findCommonAncestor(node, nodeAction.node);
		if (ancestor == null)
			return null;
		return new NodeType(span, ancestor);
	}
	
	@Override
	public boolean isAssignableTo(Type type) {
		if (! (type instanceof NodeType))
			return false;
		return findCommonAncestor(node, ((NodeType)type).node) != null;
	}

	private static AstNode findCommonAncestor(AstNode n1, AstNode n2) {
		List<AstNode> path1 = makePath(n1);
		List<AstNode> path2 = makePath(n2);
		AstNode ancestor = null;
		int i;
		for (i = 0; i < path1.size() && i < path2.size(); ++i) {
			if (path1.get(i) != path2.get(i)) {
				return ancestor;
			}
			ancestor = path1.get(i);
		}
		if (i == path1.size())
			return path1.get(i-1);
		return path2.get(i-1);
	}

	private static List<AstNode> makePath(AstNode n1) {
		List<AstNode> path = new ArrayList<AstNode>();
		do {
			path.add(0, n1);
			n1 = n1.getSuper();
		} while (n1 != null);
		return path;
	}

	@Override
	public String toString() {
		return "Node(" + node.getName() + ")";
	}
	
}