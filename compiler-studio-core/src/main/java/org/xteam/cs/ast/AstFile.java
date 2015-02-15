package org.xteam.cs.ast;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.xteam.cs.ast.ast.AstFileAst;
import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.FileResource;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.model.Project;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;

public class AstFile extends FileResource {

	private AstModel model;

	public AstFile(Project project, File file) {
		super(project, file);
	}
	
	@Override
	protected BaseProperties createProperties() {
		return new AstProperties();
	}
	
	@Override
	public String getType() {
		return "text/ast";
	}

	@Override
	public ILexer getLexer() {
		return new AstLexer(new AstTokenMapper());
	}

	@Override
	public void analyse(String text, IErrorReporter reporter) {
		try {
			AstParser parser = new AstParser();
			parser.setErrorReporter(reporter);
			parser.setInput(new StringReader(text));
			AstFileAst ast = parser.parse();
			AstSemantic semantic = new AstSemantic(reporter);
			semantic.analyse(ast);
		} catch (ParseError e) {
		} catch (IOException e) {
		}
	}

	public void setAstModel(AstModel model) {
		this.model = model;
	}

	public AstModel getAstModel() {
		return model;
	}

}
