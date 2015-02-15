package org.xteam.cs.lex;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.xteam.cs.lex.ast.LexerFileAst;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.FileResource;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.model.Project;
import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.syntax.ISyntaxToken;

public class LexerFile extends FileResource {

	private LexerBuild build;

	public LexerFile(Project project, File file) {
		super(project, file);
	}
	
	@Override
	protected BaseProperties createProperties() {
		return new LexerProperties();
	}

	@Override
	public String getType() {
		return "text/lex";
	}

	@Override
	public ILexer getLexer() {
		return new LexLexer(new TokenMapper());
	}

	public void setBuildLexer(LexerBuild build) {
		this.build = build;
	}
	
	public LexerBuild getBuild() {
		return build;
	}
	
	@Override
	public void analyse(String text, IErrorReporter reporter) {
		try {
			LexParser parser = new LexParser();
			parser.setErrorReporter(reporter);
			parser.setInput(new StringReader(text));
			LexerFileAst ast = parser.parse();
			LexSemantic semantic = new LexSemantic(reporter);
			semantic.analyse(ast);
		} catch (ParseError e) {
		} catch (IOException e) {
		}
	}
	
	private static class TokenMapper implements ITokenFactory {

		@Override
		public IToken newToken(int type, Span span, Object content) {
			if (type == LexTokens.IDENT) {
				String name = (String) content;
				if (name.equals("next") || name.equals("return")
						|| name.equals("keep") || name.equals("value")
						|| name.equals("lexer") || name.equals("comment"))
					return new DefaultToken(ISyntaxToken.KEYWORD, span.start(), span.length());
				return new DefaultToken(ISyntaxToken.DEFAULT, span.start(), span.length());
			}
			if (type == LexTokens.COMMENT) {
				return new DefaultToken(ISyntaxToken.COMMENT, span.start(), span.length());
			}
			if (type == LexTokens.STRING) {
				return new DefaultToken(ISyntaxToken.STRING, span.start(), span.length());
			}
			if (type == LexTokens.LEXEOF) {
				return new DefaultToken(ISyntaxToken.KEYWORD, span.start(), span.length());
			}
			if (type == LexTokens.CHARCLASS) {
				return new DefaultToken(ISyntaxToken.OPERATOR, span.start(), span.length());
			}
			if (type == LexTokens.EOF) {
				return null;
			}
			return new DefaultToken(ISyntaxToken.DEFAULT, span.start(), span.length());
		}
		
	}

}
