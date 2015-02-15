package org.xteam.cs.lex;

import java.io.IOException;
import java.io.Reader;

import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;

public class LexLexer implements ILexer, LexTokens {
	
	private static class Token extends DefaultToken {

		public Token(int type, int start, int length, Object value) {
			super(type, start, length, value);
		}
		
		@Override
		public String toString() {
			return typeName(type) + "[" + start + ":" + (start + length) + "]" + (value != null ? value.toString() : "");
		}
		
		private static String typeName(int t) {
			if (t == IDENT)
				return "IDENT";
			if (t == RBRC)
				return "RBRC";
			if (t == LBRC)
				return "LBRC";
			if (t == RPAR)
				return "RPAR";
			if (t == LPAR)
				return "LPAR";
			if (t == LT)
				return "LT";
			if (t == GT)
				return "GT";
			if (t == ANY)
				return "ANY";
			if (t == HAT)
				return "HAT";
			if (t == STAR)
				return "STAR";
			if (t == TILDE)
				return "TILDE";
			if (t == PLUS)
				return "PLUS";
			if (t == SEMI)
				return "SEMI";
			if (t == SLASH)
				return "SLASH";
			if (t == COMA)
				return "COMA";
			if (t == COMMENT)
				return "COMMENT";
			if (t == EQUALS)
				return "EQUALS";
			if (t == CHARCLASS)
				return "CHARCLASS";
			if (t == PIPE)
				return "PIPE";
			if (t == STRING)
				return "STRING";
			if (t == ARROW)
				return "ARROW";
			if (t == QUESTION)
				return "QUESTION";
			if (t == LEXEOF)
				return "LEXEOF";
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
	private boolean skipComment = false;
	private ITokenFactory factory;
	
	public LexLexer(ITokenFactory factory) {
		this.factory = factory;
	}
	
	public LexLexer() {
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
			} else if (c == '{') {
				return token(LBRC);
			} else if (c == '}') {
				return token(RBRC);
			} else if (c == '(') {
				return token(LPAR);
			} else if (c == ')') {
				return token(RPAR);
			} else if (c == '<') {
				int cc = getChar();
				if (cc == '<') {
					match("EOF>>");
					return token(LEXEOF);
				}
				ungetChar(cc);
				return token(LT);
			} else if (c == '>') {
				return token(GT);
			} else if (c == '[') {
				return charClass();
			} else if (c == '^') {
				return token(HAT);
			} else if (c == '.') {
				return token(ANY);
			} else if (c == '*') {
				return token(STAR);
			} else if (c == '~') {
				return token(TILDE);
			} else if (c == ';') {
				return token(SEMI);
			} else if (c == '=') {
				return token(EQUALS);
			} else if (c == '|') {
				return token(PIPE);
			} else if (c == '+') {
				return token(PLUS);
			} else if (c == ',') {
				return token(COMA);
			} else if (c == '?') {
				return token(QUESTION);
			} else if (c == '"') {
				return string();
			} else if (c == '/') {
				c = getChar();
				if (c == '/') {
					IToken comment = comment();
					if (comment != null)
						return comment;
				} else {
					ungetChar(c);
					return token(SLASH);
				}
			} else if (c == '-') {
				int cc = getChar();
				if (cc == '>') {
					return token(ARROW);
				}
				ungetChar(cc);
				unexpected(c);
			} else if (c == -1) {
				return token(EOF);
			} else {
				unexpected(c);
			}
		}
	}

	private void match(String pattern) throws IOException {
		for (int i = 0; i < pattern.length(); ++i) {
			int c;
			if (pattern.charAt(i) != (c = getChar()))
				unexpected(c);
		}
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

	private IToken comment() throws IOException {
		while (getChar() != '\n') {
		}
		return skipComment ? null : token(COMMENT);
	}
	
	private IToken charClass() throws IOException {
		StringBuffer buffer = new StringBuffer();
		int count = 0;
		int c;
		while (true) {
			c = getChar();
			if ((c == ']' && count == 0) || c == '\n' || c == -1)
				break;
			if (c == '[')
				++count;
			if (c == ']')
				--count;
			buffer.append((char)c);
		}
		if (c != ']')
			error("unterminated char class");
		return token(CHARCLASS, buffer.toString());
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

	private IToken nextIdentifier(int c) throws IOException {
		StringBuffer buffer = new StringBuffer();
		do {
			buffer.append((char)c);
			c = getChar();
		} while (Character.isJavaIdentifierPart(c));
		ungetChar(c);
		return token(IDENT, buffer.toString());
	}
	
	private IToken token(int type) {
		return token(type, null);
	}
	
	private IToken token(int type, Object value) {
		return factory.newToken(type, new Span(startPosition, position - startPosition), value);
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
		skipComment = doSkip;
	}

}
