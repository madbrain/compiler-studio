/**
 * 
 */
package org.xteam.cs.types;

import org.xteam.cs.runtime.Span;

public class VoidType extends Type {

	public VoidType(Span span) {
		super(span);
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof VoidType;
	}
	
	@Override
	public boolean isAssignableTo(Type type) {
		return type.equals(this);
	}
	
	public String toString() {
		return "Void";
	}
	
}