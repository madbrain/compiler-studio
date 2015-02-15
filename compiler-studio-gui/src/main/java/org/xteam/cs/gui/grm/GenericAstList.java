package org.xteam.cs.gui.grm;

import java.util.ArrayList;
import java.util.List;

public class GenericAstList extends GenericAST {

	private List<GenericAST> elements = new ArrayList<GenericAST>();

	public void add(GenericAST ast) {
		elements.add(ast);
	}

	public void prepend(GenericAST ast) {
		elements.add(0, ast);
	}
	
	@Override
	public int size() {
		return elements.size();
	}
	
	@Override
	public List<GenericAST> getChildren() {
		return elements;
	}
	
	@Override
	public String toString() {
		return "AstList";
	}

}
