package org.xteam.cs.grm;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.xteam.cs.grm.ast.ArgumentAst;
import org.xteam.cs.grm.ast.AstImportAst;
import org.xteam.cs.grm.ast.ConstructorAst;
import org.xteam.cs.grm.ast.DeclarationAst;
import org.xteam.cs.grm.ast.GrammarFileAst;
import org.xteam.cs.grm.ast.IdentArgumentAst;
import org.xteam.cs.grm.ast.IdentAst;
import org.xteam.cs.grm.ast.ImportAst;
import org.xteam.cs.grm.ast.LexerImportAst;
import org.xteam.cs.grm.ast.PrecedenceDeclarationAst;
import org.xteam.cs.grm.ast.RuleAst;
import org.xteam.cs.grm.ast.StartDeclarationAst;
import org.xteam.cs.grm.ast.TerminalDeclarationAst;
import org.xteam.cs.grm.ast.TupleArgumentAst;
import org.xteam.cs.model.IParser;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.Span;

public class GrammarParser implements IParser {

	private GrammarLexer lexer;
	private IToken token;
	private IErrorReporter reporter;
	
	public GrammarParser() {
		lexer = new GrammarLexer();
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
	
	// grammar_file ::= header import* declaration* rule*
	public GrammarFileAst parse() throws IOException, ParseError {
		scanToken();
		if (token.type() != IGrammarTokens.GRAMMAR) {
			error(tokenSpan(), "expecting 'grammar'");
		}
		Span span = tokenSpan();
		scanToken();
		if (token.type() != IGrammarTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		IdentAst name = new IdentAst(tokenSpan(), (String)token.value());
		scanToken();
		if (token.type() != IGrammarTokens.SEMI) {
			error(tokenSpan(), "expecting ';'");
		}
		scanToken();
		AstList<ImportAst> imports = new AstList<ImportAst>();
		while (token.type() == IGrammarTokens.AST || token.type() == IGrammarTokens.LEXER) {
			imports.add(parseImport());
		}
		span = span.merge(Span.listSpan(imports));
		AstList<DeclarationAst> declarations = new AstList<DeclarationAst>();
		while (token.type() != IGrammarTokens.IDENT
				&& token.type() != IGrammarTokens.EOF) {
			declarations.add(parseDeclaration());
		}
		span = span.merge(Span.listSpan(declarations));
		AstList<RuleAst> rules = new AstList<RuleAst>();
		while (token.type() != IGrammarTokens.EOF) {
			rules.add(parseRule());
		}
		span = span.merge(Span.listSpan(rules));
		return new GrammarFileAst(span, name, imports, declarations, rules);
	}

	private ImportAst parseImport() throws IOException, ParseError {
		if (token.type() == IGrammarTokens.LEXER) {
			Span span = tokenSpan();
			scanToken();
			if (token.type() != IGrammarTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			IdentAst name = new IdentAst(tokenSpan(), (String) token.value());
			scanToken();
			if (token.type() != IGrammarTokens.SEMI) {
				error(tokenSpan(), "expecting ';'");
			}
			scanToken();
			return new LexerImportAst(span, name);
		} else if (token.type() == IGrammarTokens.AST) {
			Span span = tokenSpan();
			scanToken();
			if (token.type() != IGrammarTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			IdentAst name = new IdentAst(tokenSpan(), (String) token.value());
			scanToken();
			if (token.type() != IGrammarTokens.SEMI) {
				error(tokenSpan(), "expecting ';'");
			}
			scanToken();
			return new AstImportAst(span, name);
		} else {
			error(tokenSpan(), "expecting 'lexer' or 'ast'");
			return null;
		}
	}
	
	private DeclarationAst parseDeclaration() throws ParseError, IOException {
		if (token.type() == IGrammarTokens.TERMINAL) {
			Span span = tokenSpan();
			scanToken();
			// ident_coma_list:e1 ident_opt:e2 SEMI:e3
			AstList<IdentAst> terminals = parseIdentComaList(false);
			IdentAst type = null;
			if (token.type() == IGrammarTokens.COLO) {
				scanToken();
				if (token.type() != IGrammarTokens.IDENT) {
					error(tokenSpan(), "expecting <IDENT>");
				}
				type = new IdentAst(tokenSpan(), (String)token.value());
				scanToken();
			}
			if (token.type() != IGrammarTokens.SEMI) {
				error(tokenSpan(), "expecting ';'");
			}
			span = span.merge(tokenSpan());
			scanToken();
			return new TerminalDeclarationAst(span, terminals, type);
		}
		if (token.type() == IGrammarTokens.START) {
			Span span = tokenSpan();
			scanToken();
			if (token.type() != IGrammarTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			span = span.merge(tokenSpan());
			IdentAst nonTerminal = new IdentAst(tokenSpan(), (String)token.value());
			scanToken();
			if (token.type() != IGrammarTokens.SEMI) {
				error(tokenSpan(), "expecting ';'");
			}
			span = span.merge(tokenSpan());
			scanToken();
			return new StartDeclarationAst(span, nonTerminal);
		}
		if (token.type() == IGrammarTokens.NONASSOC
				|| token.type() == IGrammarTokens.LEFT
				|| token.type() == IGrammarTokens.RIGHT) {
			int kind = makeKind(token.type());
			Span span = tokenSpan();
			scanToken();
			AstList<IdentAst> idents = parseIdentComaList(false);
			if (token.type() != IGrammarTokens.SEMI) {
				error(tokenSpan(), "expecting ';'");
			}
			span = span.merge(tokenSpan());
			scanToken();
			return new PrecedenceDeclarationAst(span, kind, idents);
		}
		error(tokenSpan(), "expecting 'terminal', 'start', 'nonassoc', 'left' or 'right'");
		return null;
	}
	
	private int makeKind(int type) {
		if (type == IGrammarTokens.NONASSOC)
			return PrecedenceDeclarationAst.NONASSOC;
		if (type == IGrammarTokens.LEFT)
			return PrecedenceDeclarationAst.LEFT;
		return PrecedenceDeclarationAst.RIGHT;
	}

	private AstList<IdentAst> parseIdentComaList(boolean emptyOk) throws ParseError, IOException {
		AstList<IdentAst> idents = new AstList<IdentAst>();
		if (token.type() != IGrammarTokens.IDENT) {
			if (emptyOk)
				return idents;
			error(tokenSpan(), "expecting <IDENT>");
		}
		idents.add(new IdentAst(tokenSpan(), (String)token.value()));
		scanToken();
		while (token.type() == IGrammarTokens.COMA) {
			scanToken();
			if (token.type() != IGrammarTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			idents.add(new IdentAst(tokenSpan(), (String)token.value()));
			scanToken();
		}
		return idents;
	}

	// rule ::= ident EQUALS ident_list constructor_opt SEMI 
	private RuleAst parseRule() throws ParseError, IOException {
		if (token.type() != IGrammarTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		IdentAst lhs = new IdentAst(tokenSpan(), (String)token.value());
		Span span = tokenSpan();
		scanToken();
		if (token.type() != IGrammarTokens.EQUALS) {
			error(tokenSpan(), "expecting '::='");
		}
		scanToken();
		AstList<IdentAst> rhs = parseIdentList();
		IdentAst prec = null;
		if (token.type() == IGrammarTokens.PREC) {
			scanToken();
			if (token.type() != IGrammarTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			prec = new IdentAst(tokenSpan(), (String)token.value());
			scanToken();
		}
		ConstructorAst constructor = null;
		if (token.type() == IGrammarTokens.ARROW) {
			scanToken();
			constructor = parseConstructor();
		}
		if (token.type() != IGrammarTokens.SEMI) {
			error(tokenSpan(), "expecting ';'");
		}
		span = span.merge(tokenSpan());
		scanToken();
		return new RuleAst(span, lhs, rhs, prec, constructor);
	}
	
	private AstList<IdentAst> parseIdentList() throws ParseError, IOException {
		AstList<IdentAst> idents = new AstList<IdentAst>();
		while (token.type() == IGrammarTokens.IDENT) {
			idents.add(new IdentAst(tokenSpan(), (String)token.value()));
			scanToken();
		}
		return idents;
	}

	private ConstructorAst parseConstructor() throws ParseError, IOException {
		IdentAst name = null;
		if (token.type() == IGrammarTokens.IDENT) {
			name = new IdentAst(tokenSpan(), (String)token.value());
		} else if (token.type() == IGrammarTokens.LEXER) {
			name = new IdentAst(tokenSpan(), "lexer");
		} else {
			error(tokenSpan(), "expecting 'lexer' or <IDENT>");
		}
		Span span = tokenSpan();
		scanToken();
		if (token.type() != IGrammarTokens.LPAR) {
			error(tokenSpan(), "expecting '('");
		}
		scanToken();
		AstList<ArgumentAst> arguments = parseArgumentComaList();
		if (token.type() != IGrammarTokens.RPAR) {
			error(tokenSpan(), "expecting ')'");
		}
		scanToken();
		return new ConstructorAst(span, name, arguments);
	}

	private AstList<ArgumentAst> parseArgumentComaList() throws ParseError, IOException {
		AstList<ArgumentAst> idents = new AstList<ArgumentAst>();
		if (! isUnprotectedIdent(token)) {
			return idents;
		}
		idents.add(parseArgument());
		while (token.type() == IGrammarTokens.COMA) {
			scanToken();
			idents.add(parseArgument());
		}
		return idents;
	}

	private ArgumentAst parseArgument() throws ParseError, IOException {
		Span span = tokenSpan();
		if (! isUnprotectedIdent(token)) {
			error(span, "expecting <IDENT>");
		}
		String name = unprotectedIdentValue(token);
		scanToken();
		if (token.type() == IGrammarTokens.ARROW) {
			scanToken();
			if (token.type() != IGrammarTokens.IDENT
					&& token.type() != IGrammarTokens.STRING) {
				error(tokenSpan(), "expecting <IDENT> or <STRING>");
			}
			IdentAst one = new IdentAst(span, name);
			IdentAst other = new IdentAst(tokenSpan(), (String)token.value());
			scanToken();
			return new TupleArgumentAst(one.span().merge(other.span()), one, other);
		}
		return new IdentArgumentAst(span, name);
	}
	
	private String unprotectedIdentValue(IToken t) {
		if (t.type() == IGrammarTokens.IDENT)
			return (String) token.value();
		if (t.type() == IGrammarTokens.LEFT)
			return "left";
		if (t.type() == IGrammarTokens.RIGHT)
			return "right";
		return null;
	}

	private boolean isUnprotectedIdent(IToken t) {
		return t.type() == IGrammarTokens.IDENT
			|| t.type() == IGrammarTokens.LEFT
			|| t.type() == IGrammarTokens.RIGHT;
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
