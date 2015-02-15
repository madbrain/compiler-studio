package com.example.mini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.Span;

import com.example.mini.ast.AddExpr;
import com.example.mini.ast.Assignment;
import com.example.mini.ast.DefaultMiniVisitor;
import com.example.mini.ast.DivExpr;
import com.example.mini.ast.Expr;
import com.example.mini.ast.Function;
import com.example.mini.ast.FunctionExpr;
import com.example.mini.ast.FunctionStatement;
import com.example.mini.ast.IdentExpr;
import com.example.mini.ast.IntExpr;
import com.example.mini.ast.MulExpr;
import com.example.mini.ast.Return;
import com.example.mini.ast.Statement;
import com.example.mini.ast.StringExpr;
import com.example.mini.ast.SubExpr;
import com.example.mini.cmodel.CAssignment;
import com.example.mini.cmodel.CBinary;
import com.example.mini.cmodel.CExpr;
import com.example.mini.cmodel.CFunction;
import com.example.mini.cmodel.CFunctionCall;
import com.example.mini.cmodel.CFunctionStatement;
import com.example.mini.cmodel.CInteger;
import com.example.mini.cmodel.CReturn;
import com.example.mini.cmodel.CString;
import com.example.mini.cmodel.CVariable;
import com.example.mini.cmodel.MiniType;

public class TypeChecker extends DefaultMiniVisitor {
	
	private Stack<CExpr> stack = new Stack<CExpr>();
	private MiniCompiler compiler;
	private CFunction function;
	private IErrorReporter reporter;
	private boolean returnReached;
	private Scope scope;

	public TypeChecker(MiniCompiler compiler, CFunction function, IErrorReporter reporter) {
		this.compiler = compiler;
		this.function = function;
		this.reporter = reporter;
		this.scope = new Scope(function.getArguments());
	}
	
	public void visitFunction(Function aFunction) {
		for (Statement statement : aFunction.getStatements()) {
			if (returnReached) {
				reporter.reportError(IErrorReporter.ERROR, statement.span(), "unreachable code");
				break;
			}
			statement.visit(this);
		}
		function.addDeclarations(scope.getDeclarations());
	}

	@Override
	public void visitAssignment(Assignment aAssignment) {
		aAssignment.getExpr().visit(this);
		CExpr expr = stack.pop();
		if (expr instanceof CError)
			return;
		CVariable var = scope.put(aAssignment.getVar().getName(), expr.getType());
		function.addStatement(new CAssignment(var.getName(), expr));
	}

	@Override
	public void visitFunctionStatement(FunctionStatement aFunctionStatement) {
		List<CExpr> args = new ArrayList<CExpr>();
		for (Expr arg : aFunctionStatement.getArguments()) {
			arg.visit(this);
			CExpr e = stack.pop();
			args.add(e);
			if (e instanceof CError)
				return;
		}
		CFunction cfunc = compiler.getFunction(aFunctionStatement.getName().getName());
		if (cfunc == null)
			cfunc = compiler.getPrimitive(aFunctionStatement.getName().getName());
		if (cfunc == null) {
			reporter.reportError(IErrorReporter.ERROR, aFunctionStatement.getName().span(),
					"unknown function '"+aFunctionStatement.getName().getName()+"'");
		} else {
			if (! cfunc.isResolved() && ! compiler.inferFrom(cfunc, args) || cfunc.getArguments().size() != args.size()) {
				reporter.reportError(IErrorReporter.ERROR, aFunctionStatement.getName().span(),
					"wrong number of arguments in call to '"+aFunctionStatement.getName().getName()+"'");
			} else {
				for (int i = 0; i < args.size(); ++i) {
					args.set(i, convert(args.get(i), cfunc.getArguments().get(i).getType(),
							aFunctionStatement.getArguments().get(i).span()));
				}
				function.addStatement(new CFunctionStatement(cfunc, args));
			}
		}
	}

	private CExpr convert(CExpr expr, MiniType type, Span span) {
		if (expr.getType() == type) {
			return expr;
		}
		if (type == MiniType.STRING) {
			if (expr.getType() == MiniType.INTEGER) {
				return new CFunctionCall(compiler.getPrimitive("i_to_s"), Arrays.asList(expr));
			}
		}
		reporter.reportError(IErrorReporter.ERROR, span, "cannot convert to " + type);
		return new CError();
	}

	@Override
	public void visitReturn(Return aReturn) {
		aReturn.getExpr().visit(this);
		CExpr e = stack.pop();
		if (e instanceof CError)
			return;
		function.addStatement(new CReturn(e));
		function.setType(e.getType());
		returnReached = true;
	}
	
	@Override
	public void visitAddExpr(AddExpr aAddExpr) {
		aAddExpr.getLeft().visit(this);
		CExpr left = stack.pop();
		aAddExpr.getRight().visit(this);
		CExpr right = stack.pop();
		if (left instanceof CError || right instanceof CError)
			return;
		if (left.getType() == MiniType.INTEGER) {
			if (right.getType() == MiniType.INTEGER) {
				stack.push(new CBinary(CBinary.ADD, MiniType.INTEGER, left, right));
			} else {
				throw new RuntimeException();
			}
		} else if (left.getType() == MiniType.STRING) {
			if (right.getType() != MiniType.STRING) {
				right = convert(right, MiniType.STRING, aAddExpr.getRight().span());
			}
			stack.push(new CFunctionCall(compiler.getPrimitive("str_concat"), Arrays.asList(left, right)));
		} else {
			throw new RuntimeException();
		}
	}
	
	@Override
	public void visitSubExpr(SubExpr aSubExpr) {
		throw new RuntimeException();
	}
	
	@Override
	public void visitMulExpr(MulExpr aMulExpr) {
		aMulExpr.getLeft().visit(this);
		CExpr left = stack.pop();
		aMulExpr.getRight().visit(this);
		CExpr right = stack.pop();
		if (left instanceof CError || right instanceof CError)
			return;
		if (left.getType() == MiniType.INTEGER) {
			if (right.getType() == MiniType.INTEGER) {
				stack.push(new CBinary(CBinary.MUL, MiniType.INTEGER, left, right));
			} else {
				throw new RuntimeException();
			}
		} else {
			throw new RuntimeException();
		}
	}
	
	@Override
	public void visitDivExpr(DivExpr aDivExpr) {
		throw new RuntimeException();
	}

	@Override
	public void visitFunctionExpr(FunctionExpr aFunctionExpr) {
		List<CExpr> args = new ArrayList<CExpr>();
		for (Expr arg : aFunctionExpr.getArguments()) {
			arg.visit(this);
			CExpr e = stack.pop();
			args.add(e);
			if (e instanceof CError)
				return;
		}
		CFunction cfunc = compiler.getFunction(aFunctionExpr.getName().getName());
		if (cfunc == null) {
			reporter.reportError(IErrorReporter.ERROR, aFunctionExpr.getName().span(),
					"unknown function '"+aFunctionExpr.getName().getName()+"'");
			stack.push(new CError());
		} else {
			if (! cfunc.isResolved() && ! compiler.inferFrom(cfunc, args) || cfunc.getArguments().size() != args.size()) {
				reporter.reportError(IErrorReporter.ERROR, aFunctionExpr.getName().span(),
					"wrong number of arguments in call to '"+aFunctionExpr.getName().getName()+"'");
				stack.push(new CError());
			} else {
				stack.push(new CFunctionCall(cfunc, args));
			}
		}
	}
	
	@Override
	public void visitIdentExpr(IdentExpr aIdentExpr) {
		CVariable var = scope.get(aIdentExpr.getName());
		if (var == null) {
			reporter.reportError(IErrorReporter.ERROR, aIdentExpr.span(),
					"undefined variable '"+aIdentExpr.getName()+"'");
			stack.push(new CError());
		} else {
			stack.push(var);
		}
	}

	@Override
	public void visitIntExpr(IntExpr aIntExpr) {
		stack.push(new CInteger(Integer.decode(aIntExpr.getValue())));
	}
	
	@Override
	public void visitStringExpr(StringExpr aStringExpr) {
		stack.push(new CString(aStringExpr.getValue().substring(1, aStringExpr.getValue().length()-1)));
	}
	
	private static class CError extends CExpr {

		@Override
		public MiniType getType() {
			return null;
		}

		@Override
		public void visit(IExprVisitor visitor) {
			throw new RuntimeException();
		}
		
	}

}
