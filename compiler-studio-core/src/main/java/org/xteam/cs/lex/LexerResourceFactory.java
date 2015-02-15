package org.xteam.cs.lex;

import java.io.File;

import org.xteam.cs.model.IResourceFactory;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectResource;

public class LexerResourceFactory implements IResourceFactory {

	@Override
	public ProjectResource create(Project project, File file) {
		return new LexerFile(project, file);
	}

	@Override
	public String getExtension() {
		return "lex";
	}

}
