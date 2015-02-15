package org.xteam.cs.lex;

import java.io.IOException;
import java.io.Reader;

import org.xteam.cs.lex.ast.AnyCharExprAst;
import org.xteam.cs.lex.ast.CharclassExprAst;
import org.xteam.cs.lex.ast.ConcatExprAst;
import org.xteam.cs.lex.ast.EofExprAst;
import org.xteam.cs.lex.ast.ExprAst;
import org.xteam.cs.lex.ast.LexerActionAst;
import org.xteam.cs.lex.ast.LexerBolAst;
import org.xteam.cs.lex.ast.LexerDefinitionAst;
import org.xteam.cs.lex.ast.LexerFileAst;
import org.xteam.cs.lex.ast.LexerHeaderAst;
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
import org.xteam.cs.model.IParser;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.Span;

public class LexParser implements IParser {

	private LexLexer lexer;
	private IToken token;
	private IErrorReporter reporter;
	
	public LexParser() {
		lexer = new LexLexer();
		lexer.skipComments(true);
	}
	
	@Override
	public void setInput(Reader reader) {
		lexer.setInput(reader);
	}
	
	@Override
	public void setErrorReporter(IErrorReporter reporter) {
		this.reporter = reporter;
		lexer.setErrorReporter(reporter);
	}
	
	// lexer_file ::= header definition* state_definition*
	public LexerFileAst parse() throws IOException, ParseError {
		scanToken();
		
		LexerHeaderAst header = parseHeader();
		Span span = header.span();
		AstList<LexerDefinitionAst> definitions = new AstList<LexerDefinitionAst>();
		while (token.type() == LexTokens.IDENT) {
			definitions.add(parseDefinition());
		}
		span  = span.merge(Span.listSpan(definitions));
		AstList<LexerStateDefinitionAst> stateDefinitions = new AstList<LexerStateDefinitionAst>();
		while (token.type() == LexTokens.LT) {
			stateDefinitions.add(parseStateDefinition());
		}
		span  = span.merge(Span.listSpan(stateDefinitions));
		if (token.type() != LexTokens.EOF) {
			error(tokenSpan(), "unexpected token");
		}
		return new LexerFileAst(span, header, definitions, stateDefinitions);
	}
	
	
	// header ::= "lexer" IDENT SEMI
	private LexerHeaderAst parseHeader() throws ParseError, IOException {
		if (token.type() != LexTokens.IDENT || ! token.value().equals("lexer")) {
			error(tokenSpan(), "expecting 'lexer' keyword");
		}
		Span ruleSpan = tokenSpan();
		scanToken();
		if (token.type() != LexTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		ruleSpan = ruleSpan.merge(tokenSpan());
		LexerIdentAst name = new LexerIdentAst(tokenSpan(), (String)token.value());
		scanToken();
		if (token.type() != LexTokens.SEMI) {
			error(tokenSpan(), "expecting ';'");
		}
		ruleSpan = ruleSpan.merge(tokenSpan());
		scanToken();
		return new LexerHeaderAst(ruleSpan, name);
	}
	
	// definition ::= IDENT "=" expr SEMI
	private LexerDefinitionAst parseDefinition() throws ParseError, IOException {
		Span ruleSpan = Span.NULL;
		if (token.type() != LexTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		ruleSpan = ruleSpan.merge(tokenSpan());
		LexerIdentAst name = new LexerIdentAst(tokenSpan(), (String)token.value());
		scanToken();
		if (token.type() != LexTokens.EQUALS) {
			error(tokenSpan(), "expecting '='");
		}
		ruleSpan = ruleSpan.merge(tokenSpan());
		scanToken();
		ExprAst expr = parseExpr();
		if (token.type() != LexTokens.SEMI) {
			error(tokenSpan(), "expecting ';'");
		}
		ruleSpan = ruleSpan.merge(tokenSpan());
		scanToken();
		return new LexerDefinitionAst(ruleSpan, name, expr);
	}
	
	// rule_expr = expr | LEXEOF
	private ExprAst parseRuleExpr() throws IOException, ParseError {
		if (token.type() == LexTokens.LEXEOF) {
			EofExprAst expr= new EofExprAst(tokenSpan());
			scanToken();
			return expr;
		}
		return parseExpr();
	}

	// expr = concat_expr+
	private ExprAst parseExpr() throws IOException, ParseError {
		AstList<ExprAst> exprs = new AstList<ExprAst>();
		while (true) {
			exprs.add(parseConcatExpr());
			if (token.type() == LexTokens.PIPE)
				scanToken();
			else if (token.type() == LexTokens.SEMI
					|| token.type() == LexTokens.RPAR
					|| token.type() == LexTokens.ARROW
					|| token.type() == LexTokens.SLASH)
				break;
		}
		if (exprs.size() == 1)
			return exprs.get(0);
		return new UnionExprAst(Span.listSpan(exprs), exprs);
	}

	// concat_expr = primary_expr+
	private ExprAst parseConcatExpr() throws IOException, ParseError {
		AstList<ExprAst> exprs = new AstList<ExprAst>();
		while (true) {
			exprs.add(parseSuffixExpr());
			if (token.type() == LexTokens.SEMI
					|| token.type() == LexTokens.PIPE
					|| token.type() == LexTokens.RPAR
					|| token.type() == LexTokens.ARROW
					|| token.type() == LexTokens.SLASH)
				break;
		}
		if (exprs.size() == 1)
			return exprs.get(0);
		return new ConcatExprAst(Span.listSpan(exprs), exprs);
	}
	
	// suffix_expr = ~ primaryExpr | primary_expr ('*' | '+' | '?')?
	private ExprAst parseSuffixExpr() throws IOException, ParseError {
		if (token.type() == LexTokens.TILDE) {
			Span span = tokenSpan();
			scanToken();
			ExprAst untilExpr = parsePrimaryExpr();
			span = span.merge(untilExpr.span());
			return new UpToExprAst(span, untilExpr);
		}
		ExprAst expr = parsePrimaryExpr();
		if (token.type() == LexTokens.STAR) {
			Span span = tokenSpan().merge(expr.span());
			scanToken();
			return new StarExprAst(span, expr);
		}
		if (token.type() == LexTokens.QUESTION) {
			Span span = tokenSpan().merge(expr.span());
			scanToken();
			return new QuestionExprAst(span, expr);
		}
		if (token.type() == LexTokens.PLUS) {
			Span span = tokenSpan().merge(expr.span());
			scanToken();
			return new PlusExprAst(span, expr);
		}
		return expr;
	}

	// primary_expr = CHARCLASS | '{' IDENT '}' | STRING | '(' expr ')' | LEXEOF
	private ExprAst parsePrimaryExpr() throws IOException, ParseError {
		if (token.type() == LexTokens.CHARCLASS) {
			String value = (String)token.value();
			Span span = tokenSpan();
			scanToken();
			return new CharclassExprAst(span, value);
		}
		if (token.type() == LexTokens.ANY) {
			Span span = tokenSpan();
			scanToken();
			return new AnyCharExprAst(span);
		}
		if (token.type() == LexTokens.LBRC) {
			Span span = tokenSpan();
			scanToken();
			if (token.type() != LexTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			span = span.merge(tokenSpan());
			String name = (String) token.value();
			scanToken();
			if (token.type() != LexTokens.RBRC) {
				error(tokenSpan(), "expecting '}'");
			}
			span = span.merge(tokenSpan());
			scanToken();
			return new MacroExprAst(span, name);
		}
		if (token.type() == LexTokens.STRING) {
			String value = (String)token.value();
			Span span = tokenSpan();
			scanToken();
			return new StringExprAst(span, value);
		}
		if (token.type() == LexTokens.LPAR) {
			scanToken();
			ExprAst expr = parseExpr();
			if (token.type() != LexTokens.RPAR) {
				error(tokenSpan(), "expecting ')'");
			}
			scanToken();
			return expr;
		}
		error(tokenSpan(), "expecting <CHARCLASS>, <STRING>, '(' or '{'");
		return null;
	}
	
	// state_definition ::= '<' names '> '{' rule* '}'
	private LexerStateDefinitionAst parseStateDefinition() throws ParseError, IOException {
		Span span = tokenSpan();
		if (token.type() != LexTokens.LT) {
			error(tokenSpan(), "expecting '<'");
		}
		scanToken();
		AstList<LexerIdentAst> names = new AstList<LexerIdentAst>();
		do {
			if (token.type() != LexTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			names.add(new LexerIdentAst(tokenSpan(), (String)token.value()));
			scanToken();
			if (token.type() == LexTokens.COMA)
				scanToken();
		} while (token.type() != LexTokens.GT);
		span = span.merge(tokenSpan());
		scanToken();
		if (token.type() != LexTokens.LBRC) {
			error(tokenSpan(), "expecting '{' ");
		}
		scanToken();
		AstList<LexerRuleAst> rules = new AstList<LexerRuleAst>();
		while (token.type() != LexTokens.RBRC) {
			rules.add(parseRule());
		}
		scanToken();
		return new LexerStateDefinitionAst(span, names, rules);
	}

	// rule ::= (IDENT '=')? rule_expr (-> actions)? ';'
	private LexerRuleAst parseRule() throws IOException, ParseError {
		Span span = Span.NULL;
		LexerIdentAst name = null;
		if (token.type() == LexTokens.IDENT) {
			span = span.merge(tokenSpan());
			name = new LexerIdentAst(tokenSpan(), (String)token.value());
			scanToken();
			if (token.type() != LexTokens.EQUALS) {
				error(tokenSpan(), "expecting '='");
			}
			span = span.merge(tokenSpan());
			scanToken();
		}
		LexerBolAst bol = null;
		if (token.type() == LexTokens.HAT) {
			bol = new LexerBolAst(tokenSpan());
			scanToken();
		}
		ExprAst expr = parseRuleExpr();
		ExprAst lookahead = null;
		if (token.type() == LexTokens.SLASH) {
			scanToken();
			lookahead = parseExpr();
		}
		span = span.merge(expr.span());
		AstList<LexerActionAst> actions = new AstList<LexerActionAst>();
		if (token.type() == LexTokens.ARROW) {
			scanToken();
			do {
				actions.add(parseAction());
				if (token.type() == LexTokens.COMA)
					scanToken();
			} while (token.type() != LexTokens.SEMI);
		}
		if (token.type() != LexTokens.SEMI) {
			error(tokenSpan(), "expecting ';'");
		}
		span.merge(tokenSpan());
		scanToken();
		return new LexerRuleAst(span, name, bol, expr, lookahead, actions);
	}

	// action ::= IDENT ( '(' arguments ')' )?
	private LexerActionAst parseAction() throws ParseError, IOException {
		if (token.type() != LexTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		Span span = tokenSpan();
		LexerIdentAst name = new LexerIdentAst(tokenSpan(), (String)token.value());
		scanToken();
		AstList<LexerIdentAst> arguments = new AstList<LexerIdentAst>();
		if (token.type() == LexTokens.LPAR) {
			scanToken();
			do {
				if (token.type() != LexTokens.IDENT) {
					error(tokenSpan(), "expecting <IDENT>");
				}
				arguments.add(new LexerIdentAst(tokenSpan(), (String)token.value()));
				scanToken();
				if (token.type() == LexTokens.COMA)
					scanToken();
			} while (token.type() != LexTokens.RPAR);
			span = span.merge(tokenSpan());
			scanToken();
		}
		return new LexerActionAst(span, name, arguments);
	}

	private Span tokenSpan() {
		return new Span(token.start(), token.length());
	}

	private void scanToken() throws IOException {
		token = lexer.nextToken();
	}

	private void error(Span span, String msg) throws ParseError {
		if (reporter != null)
			reporter.reportError(IErrorReporter.ERROR, span, msg);
		throw new ParseError(span, msg);
	}
	
}
