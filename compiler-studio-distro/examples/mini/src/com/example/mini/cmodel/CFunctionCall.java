package com.example.mini.cmodel;

import java.util.List;

import com.example.mini.IExprVisitor;


public class CFunctionCall extends CExpr {

	private CFunction cfunc;
	private List<CExpr> args;

	public CFunctionCall(CFunction cfunc, List<CExpr> args) {
		this.cfunc = cfunc;
		this.args = args;
	}

	@Override
	public MiniType getType() {
		return cfunc.getReturnType();
	}

	@Override
	public void visit(IExprVisitor visitor) {
		visitor.visitFunctionCall(this);
	}

	public String getName() {
		return cfunc.getName();
	}

	public List<CExpr> getArguments() {
		return args;
	}

}
