package org.xteam.cs.lex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xteam.cs.lex.ast.AnyCharExprAst;
import org.xteam.cs.lex.ast.CharclassExprAst;
import org.xteam.cs.lex.ast.ConcatExprAst;
import org.xteam.cs.lex.ast.EofExprAst;
import org.xteam.cs.lex.ast.ExprAst;
import org.xteam.cs.lex.ast.ILexerAstVisitor;
import org.xteam.cs.lex.ast.LexerActionAst;
import org.xteam.cs.lex.ast.LexerDefinitionAst;
import org.xteam.cs.lex.ast.LexerFileAst;
import org.xteam.cs.lex.ast.LexerIdentAst;
import org.xteam.cs.lex.ast.LexerRuleAst;
import org.xteam.cs.lex.ast.LexerStateDefinitionAst;
import org.xteam.cs.lex.ast.MacroExprAst;
import org.xteam.cs.lex.ast.PlusExprAst;
import org.xteam.cs.lex.ast.QuestionExprAst;
import org.xteam.cs.lex.ast.StarExprAst;
import org.xteam.cs.lex.ast.StringExprAst;
import org.xteam.cs.lex.ast.UnionExprAst;
import org.xteam.cs.lex.ast.UpToExprAst;
import org.xteam.cs.lex.model.Action;
import org.xteam.cs.lex.model.ActionCode;
import org.xteam.cs.lex.model.AnyCharExpr;
import org.xteam.cs.lex.model.CharClassExpr;
import org.xteam.cs.lex.model.ConcatExpr;
import org.xteam.cs.lex.model.Expr;
import org.xteam.cs.lex.model.IExprVisitor;
import org.xteam.cs.lex.model.IntCharSet;
import org.xteam.cs.lex.model.InverseCharClassExpr;
import org.xteam.cs.lex.model.LexerModel;
import org.xteam.cs.lex.model.LexicalState;
import org.xteam.cs.lex.model.PlusExpr;
import org.xteam.cs.lex.model.QuestionExpr;
import org.xteam.cs.lex.model.RegularExpression;
import org.xteam.cs.lex.model.StarExpr;
import org.xteam.cs.lex.model.StringExpr;
import org.xteam.cs.lex.model.UnionExpr;
import org.xteam.cs.lex.model.UpToExpr;
import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.runtime.semantic.Error;
import org.xteam.cs.runtime.semantic.Ok;
import org.xteam.cs.runtime.semantic.Result;
import org.xteam.cs.types.PrimitiveType;

public class LexSemantic {
	
	private static class Definition {

		LexerDefinitionAst def;
		Expr expr;
		boolean hasError = false;;
		Set<Definition> uses = new HashSet<Definition>();
		boolean isUsed = false;

		public Definition(LexerDefinitionAst def) {
			this.def = def;
		}
		
		public void markUsed() {
			isUsed = true;
			for (Definition def : uses) {
				def.markUsed();
			}
		}
		
	}

	private IErrorReporter reporter;
	private Map<String, Definition> definitions = new HashMap<String, Definition>();
	private Stack<Definition> buildingStack = new Stack<Definition>();
	private Map<String, LexicalState> stateMap = new HashMap<String, LexicalState>();
	private int priority;

	public LexSemantic(IErrorReporter reporter) {
		this.reporter = reporter;
	}

	public LexerModel analyse(LexerFileAst ast) {
		priority = 0;
		LexerModel model = new LexerModel(ast.getHeader().getName().getValue());
		for (LexerDefinitionAst def : ast.getDefinitions()) {
			definitions.put(def.getName().getValue(), new Definition(def));
		}
		for (LexerDefinitionAst def : ast.getDefinitions()) {
			Definition d = definitions.get(def.getName().getValue());
			getDefinitionExpr(d);
		}
		if (ast.getStateDefinitions().size() == 0) {
			reportError(ast.getHeader().span(), "lexer contains no rules");
			return model;
		}
		for (LexerStateDefinitionAst def : ast.getStateDefinitions()) {
			for (LexerIdentAst name : def.getNames()) {
				LexicalState state = stateMap.get(name.getValue());
				if (state == null) {
					 stateMap.put(name.getValue(), state = new LexicalState(name.getValue()));
					 model.addState(state);
				}
			}
		}
		LexicalState initialState = stateMap.get("INITIAL");
		if (initialState == null) {
			LexerStateDefinitionAst def = ast.getStateDefinitions().get(0);
			initialState = stateMap.get(def.getNames().get(0).getValue());
			reportError(def.getNames().get(0).span(), "lexer doesn't contain 'INITIAL' state, choosing '"
					+initialState.getName()+"' as initial state");
		}
		model.setInitialState(initialState);
		for (LexerStateDefinitionAst def : ast.getStateDefinitions()) {
			List<LexicalState> states = new ArrayList<LexicalState>();
			for (LexerIdentAst name : def.getNames()) {
				states.add(stateMap.get(name.getValue()));
			}
			for (LexerRuleAst rule : def.getRules()) {
				Action action = makeAction(rule.getName(), rule.getActions());
				Expr expr = makeExpr(rule.getExpression());
				boolean isEOF = false;
				if (expr instanceof EOFExpr) {
					isEOF = true;
					expr = null;
				}
				Expr lookahead = null;
				if (rule.getLookahead() != null) {
					if (isEOF) {
						reportError(rule.getLookahead().span(),
								"EOF rule cannot have lookahead");
					} else {
						lookahead = makeExpr(rule.getLookahead());
					}
				}
				model.addExpression(new RegularExpression(states,
						rule.getBol() != null, isEOF, expr, lookahead, action));
			}
		}
		for (Definition def : definitions.values()) {
			if (! def.isUsed && ! def.hasError) {
				reportWarning(def.def.span(), "macro '" + def.def.getName().getValue() + "' is unused");
			}
		}
		return model;
	}

	private Expr makeExpr(ExprAst ast) {
		if (ast == null)
			return null;
		ExprBuilder builder = new ExprBuilder();
		ast.visit(builder);
		return builder.getExpr();
	}

	private Action makeAction(LexerIdentAst name,
			AstList<LexerActionAst> actions) {
		List<ActionCode> codes = new ArrayList<ActionCode>();
		String token = null;
		if (name != null) {
			token = name.getValue();
			codes.add(ActionCode.TOKEN);
		}
		LexicalState next = null;
		int tokenType = PrimitiveType.STRING;
		String convertCode = null;
		for (LexerActionAst action : actions) {
			if (action.getName().getValue().equals("next")) {
				if (action.getArguments().size() == 0) {
					reportError(action.span(), "next action must have an argument");
				} else {
					next = stateMap.get(action.getArguments().get(0).getValue());
					if (next == null) {
						reportError(action.getArguments().get(0).span(), "unknown lexical state");
					} else {
						codes.add(ActionCode.NEXT);
					}
					if (action.getArguments().size() > 1) {
						reportError(action.span(), "too much argument in next action");
					}
				}
			} else if (action.getName().getValue().equals("keep")) {
				codes.add(ActionCode.KEEP);
				if (action.getArguments().size() > 0) {
					reportError(action.span(), "keep action doesn't take any argument");
				}
			} else if (action.getName().getValue().equals("value")) {
				codes.add(ActionCode.VALUE);
				if (action.getArguments().size() > 0) {
					if (action.getArguments().size() == 2) {
						if (! PrimitiveType.isPrimitive(action.getArguments().get(0).getValue())) {
							reportError(action.getArguments().get(0).span(),
									"'" + action.getArguments().get(0).getValue() + "' is not a valid type");
						} else {
							tokenType = PrimitiveType.get(action.getArguments().get(0).getValue());
						}
						codes.add(ActionCode.CONVERT);
						convertCode = action.getArguments().get(1).getValue();
					} else if (action.getArguments().size() != 0) {
						reportError(action.span(), "value action take only zero or one two");
					}
				}
			} else if (action.getName().getValue().equals("error")) {
				codes.add(ActionCode.ERROR);
				if (action.getArguments().size() > 0) {
					// check msg of error
				}
			} else if (action.getName().getValue().equals("return")) {
				codes.add(ActionCode.RETURN);
				if (action.getArguments().size() > 0) {
					// check msg of error
				}
			} else if (action.getName().getValue().equals("comment")) {
				codes.add(ActionCode.COMMENT);
				if (action.getArguments().size() > 0) {
					// check msg of error
				}
			} else {
				reportError(action.span(), "unknown action kind");
			}
		}
		return new Action(codes, priority++, token, next, tokenType, convertCode);
	}

	private Expr getDefinitionExpr(Definition d) {
		if (d.expr == null && ! d.hasError) {
			buildingStack.push(d);
			ExprBuilder builder = new ExprBuilder();
			d.def.getExpr().visit(builder);
			d.expr = builder.getExpr();
			d.hasError = d.expr == null;
			buildingStack.pop();
		}
		return d.expr;
	}
	
	private class ExprBuilder implements ILexerAstVisitor {

		private Stack<Result<Expr>> stack = new Stack<Result<Expr>>();

		public Expr getExpr() {
			return stack.peek().isError() ? null : ExprOptimizer.optimize(stack.peek().value());
		}

		@Override
		public void visitCharclassExpr(CharclassExprAst charclassExprAst) {
			String value = charclassExprAst.getValue();
			
			if (value.isEmpty()) {
				reportError(charclassExprAst.span(),
						"empty charclass");
				stack.push(new Error<Expr>());
				return;
			}
			int index = 0;
			CharClassExpr expr = new CharClassExpr();
			if (value.charAt(0) == '^') {
				++index;
				expr = new InverseCharClassExpr();
			}
			for (; index < value.length(); ++index) {
				char start = value.charAt(index);
				if (start == '\\') {
					start = escape(value.charAt(++index));
				}
				char end = start;
				if (index < value.length()-2 && value.charAt(index+1) == '-') {
					index += 2;
					end = value.charAt(index);
					if (end == '\\' && index < value.length()-3) {
						end = escape(value.charAt(++index));
					}
				}
				expr.add(new IntCharSet.Interval(start, end));
			}
			stack.push(new Ok<Expr>(expr));
		}

		private char escape(char c) {
			if (c == 'n')
				return '\n';
			if (c == 'f')
				return '\f';
			if (c == 'b')
				return '\b';
			if (c == 't')
				return '\t';
			if (c == 'r')
				return '\r';
			return c;
		}
		
		private String escape(String value) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < value.length(); ++i) {
				char c = value.charAt(i);
				if (c == '\\' && i < value.length()-1) {
					c = escape(value.charAt(++i));
				}
				buffer.append(c);
			}
			return buffer.toString();
		}
		
		@Override
		public void visitUnionExpr(UnionExprAst unionExprAst) {
			UnionExpr expr = new UnionExpr();
			for (ExprAst ast : unionExprAst.getExprs()) {
				ast.visit(this);
				if (stack.peek().isError()) {
					return;
				}
				expr.add(stack.pop().value());
			}
			stack.push(new Ok<Expr>(expr));
		}

		@Override
		public void visitConcatExpr(ConcatExprAst concatExprAst) {
			ConcatExpr expr = new ConcatExpr();
			for (ExprAst ast : concatExprAst.getExprs()) {
				ast.visit(this);
				if (stack.peek().isError()) {
					return;
				}
				expr.add(stack.pop().value());
			}
			stack.push(new Ok<Expr>(expr));
		}

		@Override
		public void visitMacroExpr(MacroExprAst macroExprAst) {
			Definition def = definitions.get(macroExprAst.getName());
			if (def == null) {
				reportError(macroExprAst.span(),
						"unknown macro '" + macroExprAst.getName() + "'");
				stack.push(new Error<Expr>());
			} else {
				if (buildingStack.size() > 0) {
					buildingStack.peek().uses.add(def);
				} else {
					def.markUsed();
				}
				if (buildingStack.contains(def)) {
					reportError(macroExprAst.span(),
							"recursive definition of macro '"
									+ macroExprAst.getName() + "'");
					stack.push(new Error<Expr>());
				} else {
					stack.push(new Ok<Expr>(getDefinitionExpr(def)));
				}
			}
		}

		@Override
		public void visitStarExpr(StarExprAst starExprAst) {
			starExprAst.getExpr().visit(this);
			if (stack.peek().isError())
				return;
			stack.push(new Ok<Expr>(new StarExpr(stack.pop().value())));
		}

		@Override
		public void visitPlusExpr(PlusExprAst plusExprAst) {
			plusExprAst.getExpr().visit(this);
			if (stack.peek().isError())
				return;
			stack.push(new Ok<Expr>(new PlusExpr(stack.pop().value())));
		}
		
		@Override
		public void visitQuestion(QuestionExprAst questionExprAst) {
			questionExprAst.getExpr().visit(this);
			if (stack.peek().isError())
				return;
			stack.push(new Ok<Expr>(new QuestionExpr(stack.pop().value())));
		}
		
		@Override
		public void visitUpToExpr(UpToExprAst upToExprAst) {
			upToExprAst.getExpr().visit(this);
			if (stack.peek().isError())
				return;
			stack.push(new Ok<Expr>(new UpToExpr(stack.pop().value())));
		}

		@Override
		public void visitStringExpr(StringExprAst stringExprAst) {
			stack.push(new Ok<Expr>(new StringExpr(escape(stringExprAst.getValue()))));
		}

		@Override
		public void visitEof(EofExprAst eofExprAst) {
			stack.push(new Ok<Expr>(new EOFExpr()));
		}

		@Override
		public void visitAnyChar(AnyCharExprAst anyCharExprAst) {
			stack.push(new Ok<Expr>(new AnyCharExpr()));
		}
		
	}
	
	private void reportError(Span span, String msg) {
		reporter.reportError(IErrorReporter.ERROR, span, msg);
	}
	
	private void reportWarning(Span span, String msg) {
		reporter.reportError(IErrorReporter.WARNING, span, msg);
	}
	
	protected static class EOFExpr extends Expr {

		@Override
		public void visit(IExprVisitor visitor) {
			
		}
		
	}

}
