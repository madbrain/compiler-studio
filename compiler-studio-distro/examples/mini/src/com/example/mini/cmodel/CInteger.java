package com.example.mini.cmodel;

import com.example.mini.IExprVisitor;


public class CInteger extends CExpr {

	private int value;

	public CInteger(int value) {
		this.value = value;
	}

	@Override
	public MiniType getType() {
		return MiniType.INTEGER;
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitInteger(this);
	}

	public int getValue() {
		return value;
	}

}
