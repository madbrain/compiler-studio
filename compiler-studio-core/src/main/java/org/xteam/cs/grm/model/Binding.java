package org.xteam.cs.grm.model;

import org.xteam.cs.ast.model.AstField;

public class Binding {

	private AstField field;
	private int index;
	private String value;

	public Binding(AstField field) {
		this(field, null);
	}

	public Binding(AstField field, String value) {
		this.field = field;
		this.value = value;
		this.index = -1;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public AstField getField() {
		return field;
	}

	public int getIndex() {
		return index;
	}

	public String getValue() {
		return value;
	}

}
