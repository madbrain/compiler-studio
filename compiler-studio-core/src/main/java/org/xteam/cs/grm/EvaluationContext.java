/**
 * 
 */
package org.xteam.cs.grm;

import org.xteam.cs.ast.AstFile;
import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.lex.LexerFile;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.model.Project;

public class EvaluationContext implements IEvaluationContext {

	private Project project;
	
	public EvaluationContext(Project project) {
		this.project = project;
	}

	@Override
	public LexerBuild getLexerBuild(String name) {
		for (LexerFile lf : project.getResources(LexerFile.class)) {
			if (lf.getBuild() != null && lf.getBuild().getName().equals(name)) {
				return lf.getBuild();
			}
		}
		return null;
	}

	@Override
	public AstModel getAstModel(String name) {
		for (AstFile lf : project.getResources(AstFile.class)) {
			if (lf.getAstModel() != null && lf.getAstModel().getName().equals(name)) {
				return lf.getAstModel();
			}
		}
		return null;
	}
	
}