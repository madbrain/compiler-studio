package org.xteam.cs.lex;

import java.util.Stack;

import org.xteam.cs.lex.LexSemantic.EOFExpr;
import org.xteam.cs.lex.model.AnyCharExpr;
import org.xteam.cs.lex.model.CharClassExpr;
import org.xteam.cs.lex.model.ConcatExpr;
import org.xteam.cs.lex.model.Expr;
import org.xteam.cs.lex.model.IExprVisitor;
import org.xteam.cs.lex.model.InverseCharClassExpr;
import org.xteam.cs.lex.model.NegationExpr;
import org.xteam.cs.lex.model.PlusExpr;
import org.xteam.cs.lex.model.QuestionExpr;
import org.xteam.cs.lex.model.StarExpr;
import org.xteam.cs.lex.model.StringExpr;
import org.xteam.cs.lex.model.UnionExpr;
import org.xteam.cs.lex.model.UpToExpr;

public class ExprOptimizer implements IExprVisitor {
	
	private Stack<Expr> stack = new Stack<Expr>();

	@Override
	public void visitUnion(UnionExpr unionExpr) {
		UnionExpr newUnion = new UnionExpr();
		for (Expr expr : unionExpr.getExprs()) {
			expr.visit(this);
			expr = stack.pop();
			newUnion.add(expr);
		}
		stack.push(newUnion);
	}

	@Override
	public void visitConcat(ConcatExpr concatExpr) {
		ConcatExpr newConcat = new ConcatExpr();
		StringBuffer buffer = new StringBuffer();
		boolean isOnlyString = true;
		for (Expr expr : concatExpr.getExprs()) {
			expr.visit(this);
			expr = stack.pop();
			newConcat.add(expr);
			if (expr instanceof StringExpr) {
				buffer.append(((StringExpr)expr).getValue());
			} else {
				isOnlyString = false;
			}
		}
		if (isOnlyString)
			stack.push(new StringExpr(buffer.toString()));
		else
			stack.push(newConcat);
	}

	@Override
	public void visitNegation(NegationExpr negationExpr) {
		negationExpr.getExpr().visit(this);
		stack.push(new NegationExpr(stack.pop()));
	}

	@Override
	public void visitPlus(PlusExpr plusExpr) {
		plusExpr.getExpr().visit(this);
		stack.push(new PlusExpr(stack.pop()));
	}

	@Override
	public void visitQuestion(QuestionExpr questionExpr) {
		questionExpr.getExpr().visit(this);
		stack.push(new QuestionExpr(stack.pop()));
	}

	@Override
	public void visitStar(StarExpr starExpr) {
		starExpr.getExpr().visit(this);
		stack.push(new StarExpr(stack.pop()));
	}
	
	@Override
	public void visitUpTo(UpToExpr upToExpr) {
		upToExpr.getExpr().visit(this);
		stack.push(new UpToExpr(stack.pop()));
	}

	@Override
	public void visitString(StringExpr stringExpr) {
		stack.push(stringExpr);
	}
	
	@Override
	public void visitAnyChar(AnyCharExpr anyCharExpr) {
		stack.push(anyCharExpr);
	}
	
	@Override
	public void visitCharclass(CharClassExpr charClassExpr) {
		if (charClassExpr.getIntervals().size() == 1 && charClassExpr.getIntervals().get(0).isSingle()) {
			stack.push(new StringExpr(String.valueOf(charClassExpr.getIntervals().get(0).start())));
		} else {
			stack.push(charClassExpr);
		}
	}

	@Override
	public void visitInverseCharclass(InverseCharClassExpr inverseCharClassExpr) {
		stack.push(inverseCharClassExpr);
	}

	public static Expr optimize(Expr expr) {
		if (expr == null || expr instanceof EOFExpr)
			return expr;
		ExprOptimizer optimizer = new ExprOptimizer();
		expr.visit(optimizer);
		return optimizer.stack.peek();
	}

}
