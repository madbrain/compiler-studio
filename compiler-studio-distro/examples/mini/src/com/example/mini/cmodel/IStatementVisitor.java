package com.example.mini.cmodel;


public interface IStatementVisitor {

	void visitReturn(CReturn cReturn);

	void visitFunctionStatement(CFunctionStatement cFunctionStatement);

	void visitAssignment(CAssignment cAssignment);

}
