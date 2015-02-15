package org.xteam.cs.grm;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;

public class GrammarLexer implements ILexer, IGrammarTokens {

	private static class Token extends DefaultToken {

		public Token(int type, int start, int length, Object value) {
			super(type, start, length, value);
		}
		
		@Override
		public String toString() {
			return typeName(type) + "[" + start + ":" + (start + length) + "]" + (value != null ? value.toString() : "");
		}
		
		private static String typeName(int t) {
			if (t == GRAMMAR)
				return "GRAMMAR";
			if (t == TERMINAL)
				return "TERMINAL";
			if (t == NONASSOC)
				return "NONASSOC";
			if (t == LEFT)
				return "left";
			if (t == RIGHT)
				return "right";
			if (t == PREC)
				return "PREC";
			if (t == START)
				return "START";
			if (t == LEXER)
				return "LEXER";
			if (t == AST)
				return "AST";
			if (t == ASGN)
				return "ASGN";
			if (t == SEMI)
				return "SEMI";
			if (t == COMA)
				return "COMA";
			if (t == EQUALS)
				return "EQUALS";
			if (t == ARROW)
				return "ARROW";
			if (t == LPAR)
				return "LPAR";
			if (t == RPAR)
				return "RPAR";
			if (t == COLO)
				return "COLO";
			if (t == STRING)
				return "STRING";
			if (t == IDENT)
				return "IDENT";
			if (t == COMMENT)
				return "COMMENT";
			if (t == EOF)
				return "EOF";
			throw new RuntimeException();
		}
		
	}

	private Reader reader;
	private int lastChar;
	private int position;
	private int startPosition;
	private IErrorReporter reporter;
	private boolean skipComments = false;
	private ITokenFactory factory;
	
	private static final Map<String, Integer> keywords = new HashMap<String, Integer>();
	static {
		keywords.put("grammar", GRAMMAR);
		keywords.put("terminal", TERMINAL);
		keywords.put("start", START);
		keywords.put("lexer", LEXER);
		keywords.put("ast", AST);
		keywords.put("nonassoc", NONASSOC);
		keywords.put("left", LEFT);
		keywords.put("right", RIGHT);
	}
	
	public GrammarLexer(ITokenFactory factory) {
		this.factory = factory;
	}
	
	public GrammarLexer() {
		this.factory = new ITokenFactory() {
			
			@Override
			public IToken newToken(int type, Span span, Object content) {
				return new Token(type, span.start(), span.length(), content);
			}
		};
	}

	@Override
	public IToken nextToken() throws IOException {
		startPosition = position;
		while (true) {
			int c = getChar();
			if (c == ' ' || c == '\t' || c == '\n') {
				startPosition = position;
			} else if (Character.isJavaIdentifierStart(c)) {
				return nextIdentifier(c);
			} else if (c == ':') {
				int cc = getChar();
				if (cc == ':') {
					cc = getChar();
					if (cc != '=') {
						unexpected(cc);
					}
					return token (EQUALS);
				}
				ungetChar(cc);
				return token(COLO);
			} else if (c == ';') {
				return token(SEMI);
			} else if (c == ',') {
				return token(COMA);
			} else if (c == '(') {
				return token(LPAR);
			} else if (c == ')') {
				return token(RPAR);
			} else if (c == '=') {
				return token(ASGN);
			} else if (c == '%') {
				return token(PREC);
			} else if (c == '-') {
				if ((c = getChar()) != '>')
					unexpected(c);
				return token(ARROW);
			} else if (c == '"') {
				return string();
			} else if (c == '/') {
				c = getChar();
				if (c == '/') {
					IToken comment = comment();
					if (comment != null)
						return comment;
				} else {
					unexpected(c);
				}
			} else if (c == -1) {
				return token(EOF);
			} else {
				unexpected(c);
			}
		}
	}

	private IToken comment() throws IOException {
		while (getChar() != '\n') {
		}
		return skipComments ? null : token(COMMENT);
	}

	private IToken nextIdentifier(int c) throws IOException {
		StringBuffer buffer = new StringBuffer();
		do {
			buffer.append((char)c);
			c = getChar();
		} while (Character.isJavaIdentifierPart(c));
		ungetChar(c);
		Integer keyword = keywords.get(buffer.toString());
		if (keyword != null) {
			return token(keyword.intValue());
		}
		return token(IDENT, buffer.toString());
	}
	
	private IToken string() throws IOException {
		StringBuffer buffer = new StringBuffer();
		int c;
		while (true) {
			c = getChar();
			if (c == '"' || c == '\n' || c == -1)
				break;
			buffer.append((char)c);
			if (c == '\\') {
				c = getChar();
				buffer.append((char)c);
			}
		}
		if (c != '"')
			error("unterminated string");
		return token(STRING, buffer.toString());
	}
	
	private void unexpected(int c) throws IOException {
		error("unexpected char '" + (char)c + "' [" + c + "]");
	}
	
	private void error(String msg) throws IOException {
		if (reporter != null)
			reporter.reportError(IErrorReporter.ERROR, new Span(startPosition, position-startPosition), msg);
		else
			throw new IOException(msg);
	}
	
	private IToken token(int type) {
		return token(type, null);
	}
	
	private IToken token(int type, Object content) {
		return factory.newToken(type, new Span(startPosition, position - startPosition), content);
	}

	private int getChar() throws IOException {
		++position;
		if (lastChar >= 0) {
			int t = lastChar;
			lastChar = -1;
			return t;
		}
		return reader.read();
	}
	
	private void ungetChar(int c) {
		--position;
		lastChar = c;
	}

	@Override
	public void setInput(Reader reader) {
		this.reader = reader;
		this.lastChar = -1;
		this.position = 0;
	}

	@Override
	public void setErrorReporter(IErrorReporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void skipComments(boolean doSkip) {
		skipComments = doSkip;
	}
}
