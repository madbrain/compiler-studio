package org.xteam.cs.gui.lex;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.runtime.TableBasedLexer;

public class TestLexerResource extends ProjectResource {

	private LexerBuild build;
	private List<IToken> tokens = new ArrayList<IToken>();

	public TestLexerResource(Project project, LexerBuild build) {
		super(project);
		this.build = build;
	}
	
	@Override
	public String getName() {
		return "Test " + build.getName() + " Lexer";
	}

	@Override
	public ILexer getLexer() {
		return null;
		
	}
	
	@Override
	public String getType() {
		return "text/testlex-" + build.getName();
	}
	
	@Override
	public void analyse(String text, IErrorReporter reporter) {
		ILexer lexer = new TableBasedLexer(build.getTables(), null, new ITokenFactory() {
			@Override
			public IToken newToken(int type, Span span, Object content) {
				return new DefaultToken(type, span.start(), span.length(), build.getMapping().getToken(type));
			}
		});
		lexer.setErrorReporter(reporter);
		lexer.skipComments(false);
		lexer.setInput(new StringReader(text));
		tokens.clear();
		int eof = build.getMapping().getTokenNumber("$EOF$");
		try {
			while (true) {
				IToken t = lexer.nextToken();
				if (t.type() == eof)
					break;
				tokens.add(t);
			}
		} catch (IOException e) {
		}
	}
	
	public List<IToken> getTokens() {
		return tokens;
	}

}
