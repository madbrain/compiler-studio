package org.xteam.cs.grm;

import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.lex.build.LexerBuild;

public interface IEvaluationContext {

	LexerBuild getLexerBuild(String name);

	AstModel getAstModel(String name);

}
