/**
 * 
 */
package org.xteam.cs.types;

import org.xteam.cs.runtime.Span;

public class ListType extends Type {
	
	private Type elementType;

	public ListType(Span span, Type elementType) {
		super(span);
		this.elementType = elementType;
	}
	
	public Type getElementType() {
		return elementType;
	}
	
	@Override
	public boolean isRepeatable() {
		return true;
	}

	public Type resolve(Type newType) {
		elementType = newType;
		return this;
	}
	
	public boolean equals(Object o) {
		return o instanceof ListType;
	}
	
	@Override
	public boolean isAssignableTo(Type type) {
		return type.equals(this);
	}
	
	public String toString() {
		return "List<"+(elementType==null?"?":elementType)+">";
	}
	
}