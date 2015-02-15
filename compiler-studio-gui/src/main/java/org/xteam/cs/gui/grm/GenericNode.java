package org.xteam.cs.gui.grm;

import java.util.List;

public class GenericNode extends GenericAST {

	private String name;
	private List<ChildBinding> children;

	public GenericNode(String name, List<ChildBinding> children) {
		this.name = name;
		this.children = children;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public List<ChildBinding> getChildren() {
		return children;
	}

	public int size() {
		return children.size();
	}

}
