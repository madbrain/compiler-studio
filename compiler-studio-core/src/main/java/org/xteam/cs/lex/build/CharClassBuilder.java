package org.xteam.cs.lex.build;

import org.xteam.cs.lex.model.CaselessStringExpr;
import org.xteam.cs.lex.model.CharClassExpr;
import org.xteam.cs.lex.model.DefaultExprVisitor;
import org.xteam.cs.lex.model.Expr;
import org.xteam.cs.lex.model.InverseCharClassExpr;
import org.xteam.cs.lex.model.StringExpr;

public class CharClassBuilder extends DefaultExprVisitor {

	private CharClasses charClasses;

	public CharClassBuilder() {
		this.charClasses = new CharClasses(255); // XXX hardcoded max char
	}

	public void caseCaselessStringExpr(CaselessStringExpr exp) {
		charClasses.makeClass(exp.getValue(), true);
	}

	@Override
	public void visitString(StringExpr exp) {
		charClasses.makeClass(exp.getValue(), false);
	}

	@Override
	public void visitCharclass(CharClassExpr exp) {
		charClasses.makeClass(exp.getIntervals(), false);
	}
	
	@Override
	public void visitInverseCharclass(InverseCharClassExpr exp) {
		charClasses.makeClassNot(exp.getIntervals(), false);
	}
	
	public CharClasses getCharClasses() {
		return charClasses;
	}

	public void visit(Expr expr) {
		expr.visit(this);
	}

}
