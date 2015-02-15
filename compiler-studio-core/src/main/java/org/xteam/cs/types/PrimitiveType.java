/**
 * 
 */
package org.xteam.cs.types;

import java.util.HashMap;
import java.util.Map;

import org.xteam.cs.runtime.Span;

public class PrimitiveType extends Type {
	
	public static final int STRING = 0;
	public static final int INT    = 1;
	
	private static Map<String, Integer> primitives = new HashMap<String, Integer>();
	
	static {
		primitives.put("string", STRING);
		primitives.put("int", INT);
	}
	
	public static boolean isPrimitive(String name) {
		return primitives.containsKey(name);
	}
	
	public static int get(String name) {
		return primitives.get(name);
	}
	
	private int type;

	public PrimitiveType(Span span, int type) {
		super(span);
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	@Override
	public boolean isPrimitive() {
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof PrimitiveType && ((PrimitiveType)o).type == type; 
	}
	
	@Override
	public boolean isAssignableTo(Type type) {
		return type.equals(this);
	}

	public String toString() {
		return (type == STRING ? "string" : "int");
	}
	
}