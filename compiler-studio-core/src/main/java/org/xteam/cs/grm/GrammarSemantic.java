package org.xteam.cs.grm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xteam.cs.ast.model.AstField;
import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.ast.model.AstNode;
import org.xteam.cs.grm.NonTerminalTypeBuilder.ErrorType;
import org.xteam.cs.grm.ast.ArgumentAst;
import org.xteam.cs.grm.ast.AstImportAst;
import org.xteam.cs.grm.ast.ConstructorAst;
import org.xteam.cs.grm.ast.DeclarationAst;
import org.xteam.cs.grm.ast.DefaultGrmVisitor;
import org.xteam.cs.grm.ast.GrammarFileAst;
import org.xteam.cs.grm.ast.IdentArgumentAst;
import org.xteam.cs.grm.ast.IdentAst;
import org.xteam.cs.grm.ast.ImportAst;
import org.xteam.cs.grm.ast.LexerImportAst;
import org.xteam.cs.grm.ast.RuleAst;
import org.xteam.cs.grm.ast.StartDeclarationAst;
import org.xteam.cs.grm.ast.TerminalDeclarationAst;
import org.xteam.cs.grm.ast.TupleArgumentAst;
import org.xteam.cs.grm.model.Action;
import org.xteam.cs.grm.model.AstListAction;
import org.xteam.cs.grm.model.AstNodeAction;
import org.xteam.cs.grm.model.Binding;
import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.LexerStateAction;
import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.PropagateAction;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.types.PrimitiveType;
import org.xteam.cs.types.Type;
import org.xteam.cs.types.VoidType;


public class GrammarSemantic {

	private IErrorReporter reporter;
	private Grammar currentGrammar;
	private IEvaluationContext context;
	
	private AstModel astModel;
	private LexerBuild lexerModel;
	private Map<Rule, RuleAst> ruleMap = new HashMap<Rule, RuleAst>();

	public GrammarSemantic(IErrorReporter reporter) {
		this.reporter = reporter;
	}

	public Grammar analyse(GrammarFileAst ast, IEvaluationContext context) {
		this.context = context;
		ast.visit(new NodeCreation());
		ast.visit(new LinkNodes());
		if (astModel != null) {
			NonTerminalTypeBuilder builder = new NonTerminalTypeBuilder(reporter);
			builder.build(currentGrammar, ruleMap);
			checkActionTypes();
		}
		return currentGrammar;
	}
	
	public LexerBuild getLexerBuild() {
		return lexerModel;
	}

	private Symbol getSymbol(String name) {
		for (Symbol sym : currentGrammar.getSymbols()) {
			if (sym.getName().equals(name))
				return sym;
		}
		return null;
	}
	
	private void reportError(Span span, String msg) {
		reporter.reportError(IErrorReporter.ERROR, span, msg);
	}
	
	private Symbol getDefinedSymbol(IdentAst ident) {
		Symbol sym = getSymbol(ident.getName());
		if (sym == null) {
			reportError(ident.span(),
					"symbol " + ident.getName() + " is not defined");
		}
		return sym;
	}
	
	private NonTerminal getNonTerminal(IdentAst ident) {
		Symbol sym = getSymbol(ident.getName());
		if (sym == null) {
			reportError(ident.span(),
					"non terminal " + ident.getName() + " is not defined");
		} else {
			if (! (sym instanceof NonTerminal)) {
				reportError(ident.span(),
					"symbol " + ident.getName() + " is not a non terminal");
			} else {
				return (NonTerminal) sym;
			}
		}
		return null;
	}
	
	private class NodeCreation extends DefaultGrmVisitor {
		
		@Override
		public void visitGrmFile(GrammarFileAst ast) {
			currentGrammar = new Grammar(ast.getName().getName());
			for (ImportAst imp : ast.getImports()) {
				imp.visit(this);
			}
			for (RuleAst rule : ast.getRules()) {
				Symbol sym = getSymbol(rule.getLhs().getName());
				if (sym == null) {
					NonTerminal term = new NonTerminal(rule.getLhs().getName());
					currentGrammar.getSymbols().add(term);
				} else if (sym.isTerminal()) {
					reportError(rule.getLhs().span(), "symbol '"
							+rule.getLhs().getName()+"' declared as a terminal");
				}
			}
			for (DeclarationAst dec : ast.getDeclarations()) {
				dec.visit(this);
			}
			for (RuleAst rule : ast.getRules()) {
				rule.visit(this);
			}
		}
		
		@Override
		public void visitLexerImport(LexerImportAst aLexerImportAst) {
			if (lexerModel != null) {
				reportError(aLexerImportAst.getName().span(), "lexer model already defined");
				return;
			}
			LexerBuild build = context.getLexerBuild(aLexerImportAst.getName().getName());
			if (build == null) {
				reportError(aLexerImportAst.getName().span(), "lexer '"
						+ aLexerImportAst.getName().getName() + "' not found or contains errors");
			} else {
				setCurrentLexer(build);
			}
		}
		
		private void setCurrentLexer(LexerBuild build) {
			lexerModel = build;
			for (String name : build.getMapping().getTokens()) {
				PrimitiveType type = null;
				if (build.getMapping().hasValue(name))
					type = new PrimitiveType(null, build.getMapping().getTokenType(name));
				Terminal term = new Terminal(name, type);
				currentGrammar.getSymbols().add(term);
			}
		}

		@Override
		public void visitAstImport(AstImportAst aAstImportAst) {
			if (astModel != null) {
				reportError(aAstImportAst.getName().span(), "ast model already defined");
				return;
			}
			astModel = context.getAstModel(aAstImportAst.getName().getName());
			if (astModel == null) {
				reportError(aAstImportAst.getName().span(), "ast '"
						+ aAstImportAst.getName().getName() + "' not found or contains errors");
			}
		}
		
		@Override
		public void visitTerminalDeclaration(
				TerminalDeclarationAst terminalDeclaration) {
			for (IdentAst ident : terminalDeclaration.getTerminals()) {
				if (getSymbol(ident.getName()) != null) {
					reportError(ident.span(),
							"symbol " + ident.getName() + " already defined");
				} else {
					Type type = null;
					if (terminalDeclaration.getType() != null) {
						String t = terminalDeclaration.getType().getName();
						if (! PrimitiveType.isPrimitive(t)) {
							reportError(terminalDeclaration.getType().span(), "primitive type '"
									+ terminalDeclaration.getType().getName() + "' is not valid");
							return;
						}
						type = new PrimitiveType(terminalDeclaration.getType().span(), PrimitiveType.get(t));
					}
					Terminal term = new Terminal(ident.getName(), type);
					currentGrammar.getSymbols().add(term);
				}
			}
		}
		
		@Override
		public void visitRule(RuleAst rule) {
			NonTerminal lhs = getNonTerminal(rule.getLhs());
			if (lhs != null) {
				List<Symbol> rhs = new ArrayList<Symbol>();
				for (IdentAst ident : rule.getRhs()) {
					Symbol sym = getDefinedSymbol(ident);
					if (sym != null) {
						rhs.add(sym);
					}
				}
				Action action = null;
				if (rule.getConstructor() != null) {
					action = makeConstructor(rule.getConstructor(), rhs);
				} else if (astModel != null) {
					action = makePropagate(lhs, rhs, rule.span());
				}
				Rule r = new Rule(lhs, rhs, action);
				ruleMap.put(r, rule);
				currentGrammar.addRule(r);
			}
		}
		
		private PropagateAction makePropagate(NonTerminal lhs, List<Symbol> rhs, Span span) {
			if (rhs.isEmpty())
				return new PropagateAction(-1);
			
			int index = 0;
			int elementIndex = -1;
			boolean hasError = false;
			for (Symbol element : rhs) {
				if (! (element.isConstant() && element.isTerminal())) {
					if (elementIndex >= 0) {
						hasError = true;
						break;
					}
					elementIndex = index;
				}
				++index;
			}
			if (! hasError) {
				return new PropagateAction(elementIndex);
			} else {
				// this action will further be processed by type builder
				return new PropagateAction(-2);
			}
		}

		private Action makeConstructor(ConstructorAst constructor, List<Symbol> rhs) {
			if (constructor.getName().getName().equals("AstList")) {
				return new AstListAction();
			}
			
			if (constructor.getName().getName().equals("lexer")) {
				if (constructor.getArguments().size() != 1) {
					reportError(constructor.span(),
							"lexer action require one argument");
					return null;
				}
				if (lexerModel == null) {
					reportError(constructor.span(),
							"no lexer reference to define state '" + constructor.getName().getName() + "'");
				}
				int target;
				int condition = -1;
				ArgumentAst arg = constructor.getArguments().get(0);
				if (arg instanceof TupleArgumentAst) {
					TupleArgumentAst ta = (TupleArgumentAst) arg;
					condition = makeState(ta.getFirst().span(), ta.getFirst().getName());
					target = makeState(ta.getSecond().span(), ta.getSecond().getName());
				} else {
					IdentArgumentAst ia = (IdentArgumentAst) arg;
					target = makeState(ia.span(), ia.getName());
				}
				return new LexerStateAction(target, condition);
			}
			
			if (astModel == null) {
				reportError(constructor.span(),
						"no ast reference to define node '" + constructor.getName().getName() + "'");
				return null;
			}
			String name = constructor.getName().getName();
			AstNode node = astModel.getNode(name);
			if (node == null) {
				reportError(constructor.getName().span(),
						"ast node '" + name + "' not defined");
				return null;
			}
			List<Binding> fields = new ArrayList<Binding>();
			
			for (ArgumentAst arg : constructor.getArguments()) {
				if (arg instanceof IdentArgumentAst) {
					IdentArgumentAst ia = (IdentArgumentAst) arg;
					AstField field = node.getField(ia.getName());
					if (field == null) {
						reportError(constructor.getName().span(), "no field '"
								+ ia.getName() + "' in ast node '" + name
								+ "'");
						return null;
					}
					fields.add(new Binding(field));
				} else {
					TupleArgumentAst ta = (TupleArgumentAst) arg;
					AstField field = node.getField(ta.getFirst().getName());
					if (field == null) {
						reportError(constructor.getName().span(), "no field '"
								+ ta.getFirst().getName() + "' in ast node '" + name
								+ "'");
						return null;
					}
					fields.add(new Binding(field, ta.getSecond().getName()));
				}
			}
			return new AstNodeAction(node, fields);
		}
		
	}
	
	private int makeState(Span span, String name) {
		if (lexerModel == null)
			return -1;
		int state = lexerModel.getMapping().getStateNumber(name);
		if (state < 0) {
			reportError(span, "lexer state '"
					+ name + "' not defined");
			return -1;
		}
		return state;
	}
	
	// Only for start declaration
	private class LinkNodes extends DefaultGrmVisitor {

		@Override
		public void visitStartDeclaration(StartDeclarationAst startDeclaration) {
			NonTerminal nt = getNonTerminal(startDeclaration.getNonTerminal());
			if (nt != null) {
				currentGrammar.setStart(nt);
			}
		}
		
		@Override
		public void visitRule(RuleAst rule) {
			if (currentGrammar.getStart() == null) {
				NonTerminal nt = getNonTerminal(rule.getLhs());
				if (nt != null) {
					currentGrammar.setStart(nt);
				}
			}
		}
		
	}
	
	private void checkActionTypes() {
		for (Rule rule : currentGrammar.getRules()) {
			if (rule.getAction() instanceof AstNodeAction) {
				checkNodeActionTypes(rule, (AstNodeAction) rule.getAction());
			} else if (rule.getAction() instanceof AstListAction) {
				checkListActionTypes(rule, (AstListAction) rule.getAction());
			} else if (rule.getAction() instanceof PropagateAction) {
				checkPropagateActionTypes(rule, (PropagateAction) rule.getAction());
			}
		}
	}

	private void checkNodeActionTypes(Rule rule, AstNodeAction action) {
		RuleAst ruleAst = ruleMap.get(rule);
		List<Integer> valuedSymbolIndex = new ArrayList<Integer>();
		int index = 0;
		for (Symbol sym : rule.getRhs()) {
			if (! (sym.isConstant() || sym.getType() instanceof VoidType)) {
				valuedSymbolIndex.add(index);
			}
			++index;
		}
		int bindings = 0;
		for (Binding binding : action.getBindings()) {
			if (binding.getValue() == null) {
				bindings++;
			}
		}
		int diff = bindings - valuedSymbolIndex.size();
		if (diff != 0) {
			reportError(ruleAst.getConstructor().span(),
					"too "+(diff>0 ? "much" : "few")+" arguments, expecting " + valuedSymbolIndex.size());
			return;
		}
		index = 0;
		for (ArgumentAst arg : ruleAst.getConstructor().getArguments()) {
			if (arg instanceof IdentArgumentAst) {
				IdentArgumentAst ia = (IdentArgumentAst) arg;
				int rhsIndex = valuedSymbolIndex.get(index);
				Binding b = action.getBindings().get(index);
				if (!b.getField().getType().isAssignableTo(
						rule.getRhs().get(rhsIndex).getType())) {
					if (!(rule.getRhs().get(rhsIndex).getType() instanceof ErrorType)
							&& !(b.getField().getType() instanceof ErrorType))
						reportError(ia.span(), "incompatible type for '"
							+ ia.getName() + "', expecting "
							+ b.getField().getType() + " found "
							+ rule.getRhs().get(rhsIndex).getType());
				}
				b.setIndex(rhsIndex);
				++index;
			} else {
				TupleArgumentAst ta = (TupleArgumentAst) arg;
				Binding b = action.getBindings().get(index);
				Type t = new PrimitiveType(Span.NULL, PrimitiveType.STRING);
				if (! t.isAssignableTo(b.getField().getType())) {
					reportError(ta.getFirst().span(), "incompatible type for '"
							+ ta.getFirst().getName() + "', expecting "
							+ b.getField().getType() + " found " + t);
				}
			}
		}
	}
	
	private void checkListActionTypes(Rule rule, AstListAction action) {
		Symbol listSymbol = rule.getLhs();
		int listIndex = -1;
		int elementIndex = -1;
		int index = 0;
		for (Symbol sym : rule.getRhs()) {
			if (sym == listSymbol) {
				listIndex = index;
			} else if (! (sym.isConstant() || sym.getType() instanceof VoidType)) {
				elementIndex = index;
			}
			++index;
		}
		action.setIndexes(listIndex, elementIndex);
	}
	
	private void checkPropagateActionTypes(Rule rule, PropagateAction action) {
		RuleAst ruleAst = ruleMap.get(rule);
		if (action.getIndex() == -2) {
			int elementIndex = -1;
			int index = 0;
			boolean hasError = false;
			for (Symbol sym : rule.getRhs()) {
				if (!(sym.isConstant() || sym.getType() instanceof VoidType)) {
					if (elementIndex >= 0) {
						hasError = true;
						break;
					}
					elementIndex = index;
				}
				++index;
			}
			if (hasError) {
				reportError(ruleAst.span(), "missing action");
			} else
				action.setIndex(elementIndex);
		}
	}

	public AstModel getAstModel() {
		return astModel;
	}

}
