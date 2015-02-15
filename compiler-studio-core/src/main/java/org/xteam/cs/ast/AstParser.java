package org.xteam.cs.ast;

import java.io.IOException;
import java.io.Reader;

import org.xteam.cs.ast.ast.AbstractFlagAst;
import org.xteam.cs.ast.ast.AstFileAst;
import org.xteam.cs.ast.ast.IdentAst;
import org.xteam.cs.ast.ast.NodeAst;
import org.xteam.cs.ast.ast.NodeItemAst;
import org.xteam.cs.ast.ast.RepeatableTypeAst;
import org.xteam.cs.ast.ast.SimpleTypeAst;
import org.xteam.cs.ast.ast.TypeAst;
import org.xteam.cs.model.IParser;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.runtime.AstList;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.Span;

public class AstParser implements IParser {

	private AstLexer lexer;
	private IToken token;
	private IErrorReporter reporter;
	
	public AstParser() {
		lexer = new AstLexer();
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
	
	// ast_file ::= IDENT '{' node* '}'
	public AstFileAst parse() throws IOException, ParseError {
		scanToken();
		
		if (token.type() != AstTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		IdentAst name = new IdentAst(tokenSpan(), (String) token.value());
		scanToken();
		if (token.type() != AstTokens.LBRC) {
			error(tokenSpan(), "expecting '{'");
		}
		scanToken();
		Span span = name.span();
		AstList<NodeAst> nodes = new AstList<NodeAst>();
		while (token.type() != AstTokens.RBRC) {
			nodes.add(parseNode());
		}
		span  = span.merge(Span.listSpan(nodes));
		if (token.type() != AstTokens.RBRC) {
			error(tokenSpan(), "expecting '}'");
		}
		scanToken();
		if (token.type() != AstTokens.EOF) {
			error(tokenSpan(), "unexpected token");
		}
		return new AstFileAst(span, name, nodes);
	}
	
	// definition ::= SLASH? IDENT (':' IDENT)? '{' node_item*'}'
	private NodeAst parseNode() throws ParseError, IOException {
		Span ruleSpan = Span.NULL;
		AbstractFlagAst abstractFlag = null;
		if (token.type() == AstTokens.SLASH) {
			abstractFlag = new AbstractFlagAst(tokenSpan());
			scanToken();
		}
		if (token.type() != AstTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		ruleSpan = ruleSpan.merge(tokenSpan());
		IdentAst name = new IdentAst(tokenSpan(), (String)token.value());
		scanToken();
		IdentAst superName = null;
		if (token.type() == AstTokens.COLO) {
			scanToken();
			if (token.type() != AstTokens.IDENT) {
				error(tokenSpan(), "expecting <IDENT>");
			}
			superName = new IdentAst(tokenSpan(), (String)token.value());
			scanToken();
		}
		AstList<NodeItemAst> nodeItems = new AstList<NodeItemAst>();
		if (token.type() == AstTokens.SEMI) {
			ruleSpan = ruleSpan.merge(tokenSpan());
			scanToken();
		} else if (token.type() == AstTokens.LBRC) {
			scanToken();
			while (token.type() != AstTokens.RBRC) {
				nodeItems.add(parseNodeItem());
				if (token.type() != AstTokens.SEMI) {
					error(tokenSpan(), "expecting ';'");
				}
				scanToken();
			}
			ruleSpan = ruleSpan.merge(tokenSpan());
			scanToken();
		} else {
			error(tokenSpan(), "expecting ';' or '{'");
		}
		return new NodeAst(ruleSpan, abstractFlag, name, superName, nodeItems);
	}
	
	// node_item ::= IDENT ':' type
	private NodeItemAst parseNodeItem() throws IOException, ParseError {
		if (token.type() != AstTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		IdentAst name = new IdentAst(tokenSpan(), (String)token.value());
		Span span = name.span();
		scanToken();
		if (token.type() != AstTokens.COLO) {
			error(tokenSpan(), "expecting ':'");
		}
		scanToken();
		TypeAst type = parseType();
		return new NodeItemAst(span, name, type);
	}

	// type ::= IDENT '*' *
	private TypeAst parseType() throws ParseError, IOException {
		if (token.type() != AstTokens.IDENT) {
			error(tokenSpan(), "expecting <IDENT>");
		}
		TypeAst type = new SimpleTypeAst(tokenSpan(), (String)token.value());
		scanToken();
		while (token.type() == AstTokens.STAR) {
			type = new RepeatableTypeAst(type.span().merge(tokenSpan()), type);
			scanToken();
		}
		return type;
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
