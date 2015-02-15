package org.xteam.cs.gui.grm;

import java.util.List;

public class ChildBinding extends GenericAST {

	private String name;
	private GenericAST ast;

	public ChildBinding(String name, GenericAST ast) {
		this.name = name;
		this.ast = ast;
	}

	@Override
	public int size() {
		if (ast == null)
			return 0;
		return ast.size();
	}
	
	@Override
	public List<? extends GenericAST> getChildren() {
		return ast.getChildren();
	}
	
	@Override
	public String toString() {
		return name + " = " + ast;
	}

}
