package org.xteam.cs.ast;

import java.io.File;

import org.xteam.cs.model.IResourceFactory;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectResource;

public class AstResourceFactory implements IResourceFactory {

	@Override
	public ProjectResource create(Project project, File file) {
		return new AstFile(project, file);
	}

	@Override
	public String getExtension() {
		return "ast";
	}

}
