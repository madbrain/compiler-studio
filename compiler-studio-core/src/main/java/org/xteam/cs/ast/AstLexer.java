package org.xteam.cs.ast;

import java.io.IOException;
import java.io.Reader;

import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;

public class AstLexer implements ILexer, AstTokens {
	
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
			if (t == COLO)
				return "COLO";
			if (t == STAR)
				return "STAR";
			if (t == SEMI)
				return "SEMI";
			if (t == SLASH)
				return "SLASH";
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
	
	public AstLexer(ITokenFactory factory) {
		this.factory = factory;
	}
	
	public AstLexer() {
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
			} else if (c == ':') {
				return token(COLO);
			} else if (c == '*') {
				return token(STAR);
			} else if (c == ';') {
				return token(SEMI);
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
			} else if (c == -1) {
				return token(EOF);
			} else {
				unexpected(c);
			}
		}
	}
	
	private void unexpected(int c) throws IOException {
		if (reporter != null)
			reporter.reportError(IErrorReporter.ERROR,
					new Span(startPosition, 1), "unexpected char '" + (char)c + "' [" + c + "]");
		else
			throw new IOException("unexpected char '" + (char)c + "' [" + c + "]");
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
		skipComments = doSkip;
	}

}
