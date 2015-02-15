package com.example.mini.cmodel;

import com.example.mini.IExprVisitor;


public class CVariable extends CExpr {

	private String name;
	private MiniType type;

	public CVariable(String name, MiniType type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public MiniType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitVariable(this);
	}

}
