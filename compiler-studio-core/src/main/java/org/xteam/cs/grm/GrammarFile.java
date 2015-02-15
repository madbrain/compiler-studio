package org.xteam.cs.grm;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.xteam.cs.grm.ast.GrammarFileAst;
import org.xteam.cs.grm.build.ParserBuild;
import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.FileResource;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.model.Project;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;

public class GrammarFile extends FileResource {

	private ParserBuild build;

	public GrammarFile(Project project, File file) {
		super(project, file);
	}
	
	@Override
	protected BaseProperties createProperties() {
		return new GrammarProperties();
	}

	@Override
	public String getType() {
		return "text/grm";
	}

	@Override
	public ILexer getLexer() {
		return new GrammarLexer(new GrmTokenMapper());
	}

	@Override
	public void analyse(String text, IErrorReporter reporter) {
		try {
			GrammarParser parser = new GrammarParser();
			parser.setErrorReporter(reporter);
			parser.setInput(new StringReader(text));
			GrammarFileAst ast = parser.parse();
			GrammarSemantic semantic = new GrammarSemantic(reporter);
			semantic.analyse(ast, new EvaluationContext(getProject()));
		} catch (ParseError e) {
		} catch (IOException e) {
		}
	}
	
	public ParserBuild getParserModel() {
		return build;
	}

	public void setParserModel(ParserBuild build) {
		this.build = build;
	}

}
