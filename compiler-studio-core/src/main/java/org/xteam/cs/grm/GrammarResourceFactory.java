package org.xteam.cs.grm;

import java.io.File;

import org.xteam.cs.model.IResourceFactory;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectResource;

public class GrammarResourceFactory implements IResourceFactory {

	@Override
	public ProjectResource create(Project project, File file) {
		return new GrammarFile(project, file);
	}

	@Override
	public String getExtension() {
		return "grm";
	}

}
