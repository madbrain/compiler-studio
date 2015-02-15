package com.example.mini.cmodel;

import java.util.List;


public class CFunctionStatement extends CStatement {

	private CFunction cfunc;
	private List<CExpr> args;

	public CFunctionStatement(CFunction cfunc, List<CExpr> args) {
		this.cfunc = cfunc;
		this.args = args;
	}

	@Override
	public void visit(IStatementVisitor visitor) {
		visitor.visitFunctionStatement(this);
	}

	public String getName() {
		return cfunc.getName();
	}

	public List<CExpr> getArguments() {
		return args;
	}

}
