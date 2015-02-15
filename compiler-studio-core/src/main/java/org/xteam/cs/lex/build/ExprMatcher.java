package org.xteam.cs.lex.build;

import java.util.Stack;

import org.xteam.cs.lex.model.AnyCharExpr;
import org.xteam.cs.lex.model.CharClassExpr;
import org.xteam.cs.lex.model.ConcatExpr;
import org.xteam.cs.lex.model.Expr;
import org.xteam.cs.lex.model.IExprVisitor;
import org.xteam.cs.lex.model.IntCharSet;
import org.xteam.cs.lex.model.InverseCharClassExpr;
import org.xteam.cs.lex.model.NegationExpr;
import org.xteam.cs.lex.model.PlusExpr;
import org.xteam.cs.lex.model.QuestionExpr;
import org.xteam.cs.lex.model.StarExpr;
import org.xteam.cs.lex.model.StringExpr;
import org.xteam.cs.lex.model.UnionExpr;
import org.xteam.cs.lex.model.UpToExpr;

public class ExprMatcher implements IExprVisitor {

	private Stack<Integer> positions = new Stack<Integer>();
	private Stack<Boolean> stack = new Stack<Boolean>();
	private String value;
	private int index;

	public ExprMatcher(String value) {
		this.value = value;
		this.index = 0;
	}

	@Override
	public void visitUnion(UnionExpr unionExpr) {
		for (Expr expr : unionExpr.getExprs()) {
			positions.push(index);
			expr.visit(this);
			boolean isOk = stack.pop();
			if (isOk) {
				stack.push(true);
				return;
			}
			index = positions.pop();
		}
		stack.push(false);
	}

	@Override
	public void visitConcat(ConcatExpr concatExpr) {
		for (Expr expr : concatExpr.getExprs()) {
			expr.visit(this);
			boolean isOk = stack.pop();
			if (! isOk) {
				stack.push(false);
				return;
			}
		}
		stack.push(true);
	}

	@Override
	public void visitNegation(NegationExpr negationExpr) {
		throw new RuntimeException();
	}

	@Override
	public void visitPlus(PlusExpr plusExpr) {
		boolean ret = false;
		while (true) {
			positions.push(index);
			plusExpr.getExpr().visit(this);
			boolean isOk = stack.pop();
			if (! isOk) {
				index = positions.pop();
				stack.push(ret);
				return;
			}
			ret = true;
		}
	}

	@Override
	public void visitQuestion(QuestionExpr questionExpr) {
		positions.push(index);
		questionExpr.getExpr().visit(this);
		boolean isOk = stack.pop();
		if (! isOk) {
			index = positions.pop();
			stack.push(false);
		} else {
			stack.push(true);
		}
	}

	@Override
	public void visitStar(StarExpr starExpr) {
		while (true) {
			positions.push(index);
			starExpr.getExpr().visit(this);
			boolean isOk = stack.pop();
			if (! isOk) {
				index = positions.pop();
				stack.push(true);
				return;
			}
		}
	}
	
	@Override
	public void visitUpTo(UpToExpr upToExpr) {
		throw new RuntimeException();
	}

	@Override
	public void visitString(StringExpr stringExpr) {
		for (int i = 0; i < stringExpr.getValue().length(); ++i) {
			if (index >= value.length() || stringExpr.getValue().charAt(i) != value.charAt(index)) {
				stack.push(false);
				return;
			}
			++index;
		}
		stack.push(true);
	}
	
	@Override
	public void visitCharclass(CharClassExpr charClassExpr) {
		if (index < value.length()) {
			char c = value.charAt(index++);
			for (IntCharSet.Interval interval : charClassExpr.getIntervals()) {
				if (interval.contains(c)) {
					stack.push(true);
					return;
				}
			}
		}
		stack.push(false);
	}
	
	@Override
	public void visitAnyChar(AnyCharExpr anyCharExpr) {
		if (index < value.length()) {
			++index;
			stack.push(true);
		} else {
			stack.push(false);
		}
	}

	@Override
	public void visitInverseCharclass(InverseCharClassExpr inverseCharClassExpr) {
		throw new RuntimeException();
	}

	public static boolean match(Expr expr, String value) {
		if (expr == null)
			return false;
		ExprMatcher matcher = new ExprMatcher(value);
		expr.visit(matcher);
		return matcher.stack.peek();
	}

	
}
