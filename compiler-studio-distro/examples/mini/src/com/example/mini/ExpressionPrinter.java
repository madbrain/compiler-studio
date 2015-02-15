package com.example.mini;

import com.example.mini.cmodel.CBinary;
import com.example.mini.cmodel.CExpr;
import com.example.mini.cmodel.CFunctionCall;
import com.example.mini.cmodel.CInteger;
import com.example.mini.cmodel.CString;
import com.example.mini.cmodel.CVariable;

public class ExpressionPrinter implements IExprVisitor {

	private StringBuffer buffer = new StringBuffer();

	public String print(CExpr expr) {
		buffer .setLength(0);
		expr.visit(this);
		return buffer.toString();
	}

	@Override
	public void visitBinary(CBinary cBinary) {
		buffer.append("(");
		cBinary.getLeft().visit(this);
		buffer.append(makeOperator(cBinary.getOp()));
		cBinary.getRight().visit(this);
		buffer.append(")");
	}

	private String makeOperator(int op) {
		if (op == CBinary.ADD)
			return "+";
		if (op == CBinary.MUL)
			return "*";
		throw new RuntimeException();
	}

	@Override
	public void visitFunctionCall(CFunctionCall cFunctionCall) {
		buffer.append(cFunctionCall.getName()).append("(");
		boolean isFirst = true;
		for (CExpr expr : cFunctionCall.getArguments()) {
			if (! isFirst)
				buffer.append(", ");
			buffer.append(new ExpressionPrinter().print(expr));
			isFirst = false;
		}
		buffer.append(")");
	}

	@Override
	public void visitInteger(CInteger cInteger) {
		buffer.append(String.valueOf(cInteger.getValue()));
	}

	@Override
	public void visitVariable(CVariable cVariable) {
		buffer.append(cVariable.getName());
	}

	@Override
	public void visitString(CString cString) {
		buffer.append("\"" + cString.getValue() + "\"");
	}

}
