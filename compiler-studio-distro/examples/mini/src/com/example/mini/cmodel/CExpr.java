package com.example.mini.cmodel;

import com.example.mini.IExprVisitor;


public abstract class CExpr {

	public abstract MiniType getType();

	public abstract void visit(IExprVisitor visitor);

}
