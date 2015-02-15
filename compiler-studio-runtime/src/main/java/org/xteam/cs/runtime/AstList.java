package org.xteam.cs.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AstList<T extends AstNode> extends AstNode implements Iterable<T> {

	private List<T> elements = new ArrayList<T>();
	
	public AstList() {
		super(Span.NULL);
	}
	
	public AstList(T element) {
		super(Span.NULL);
		add(element);
	}

	public void add(T element) {
		span = span.merge(element.span);
		this.elements.add(element);
	}

	public int size() {
		return elements.size();
	}

	public T get(int index) {
		return elements.get(index);
	}

	public Iterator<T> iterator() {
		return elements.iterator();
	}

	public static <T extends AstNode> AstList<T> add(AstList<T> astList, T astNode) {
		astList.add(astNode);
		return astList;
	}

}
