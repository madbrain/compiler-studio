package org.xteam.cs.gui.grm;

import java.io.IOException;
import java.io.StringReader;

import org.xteam.cs.grm.build.ParserBuild;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IStatedLexer;
import org.xteam.cs.runtime.ISyntaxHelper;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.LRParser;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.runtime.TableBasedLexer;

public class TestAstParserResource extends ProjectResource {

	private ParserBuild build;
	private GenericAST root;

	public TestAstParserResource(Project project, ParserBuild build) {
		super(project);
		this.build = build;
	}

	@Override
	public ILexer getLexer() {
		return null;
	}

	@Override
	public String getType() {
		return "text/testast-" + build.getName();
	}
	
	@Override
	public String getName() {
		return "Test " + build.getName() + " AST Parser";
	}
	
	@Override
	public void analyse(String text, IErrorReporter reporter) {
		final LexerBuild lexerBuild = build.getLexerBuild();
		IStatedLexer lexer = new TableBasedLexer(lexerBuild.getTables(), null, new ITokenFactory() {
			@Override
			public IToken newToken(int type, Span span, Object content) {
				return new ASTTokenNode(type, lexerBuild.getMapping().getToken(type), span.start(), span.length(), content);
			}
		});
		lexer.skipComments(true);
		lexer.setErrorReporter(reporter);
		lexer.setInput(new StringReader(text));
		ISyntaxHelper helper = new ISyntaxHelper() {
			@Override
			public String getTokenString(int t) {
				return build.getLexerBuild().getMapping().getTokenName(t);
			}
			
			@Override
			public boolean isEof(int t) {
				return false;
			}
		};
		LRParser parser = new LRParser(build.getTables(), lexer,
				new ASTSyntaxReducer(build.getMapping(), lexer),
				helper, reporter);
		try {
			root = (GenericAST) parser.parse();
		} catch (IOException e) {
		}
	}
	
	public GenericAST getRoot() {
		return root;
	}

}
