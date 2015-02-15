package com.example.mini.cmodel;

import com.example.mini.IExprVisitor;

public class CString extends CExpr {

	private String value;

	public CString(String value) {
		this.value = value;
	}

	@Override
	public MiniType getType() {
		return MiniType.STRING;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitString(this);
	}

}
