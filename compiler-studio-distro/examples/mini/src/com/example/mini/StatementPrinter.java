package com.example.mini;

import com.example.mini.cmodel.CAssignment;
import com.example.mini.cmodel.CExpr;
import com.example.mini.cmodel.CFunctionStatement;
import com.example.mini.cmodel.CReturn;
import com.example.mini.cmodel.CStatement;
import com.example.mini.cmodel.IStatementVisitor;

public class StatementPrinter implements IStatementVisitor {

	private StringBuffer buffer = new StringBuffer();

	public String print(CStatement statement) {
		buffer.setLength(0);
		statement.visit(this);
		return buffer .toString();
	}

	@Override
	public void visitFunctionStatement(CFunctionStatement cFunctionStatement) {
		buffer.append(cFunctionStatement.getName()).append("(");
		boolean isFirst = true;
		for (CExpr expr : cFunctionStatement.getArguments()) {
			if (! isFirst)
				buffer.append(", ");
			buffer.append(new ExpressionPrinter().print(expr));
			isFirst = false;
		}
		buffer.append(")");
	}

	@Override
	public void visitReturn(CReturn cReturn) {
		buffer.append("return ").append(new ExpressionPrinter().print(cReturn.getExpr()));
	}

	@Override
	public void visitAssignment(CAssignment cAssignment) {
		buffer.append(cAssignment.getVar()).append(" = ").append(new ExpressionPrinter().print(cAssignment.getExpr()));
	}
	
}
