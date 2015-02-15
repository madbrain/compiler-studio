package org.xteam.cs.gui.views;

import org.xteam.cs.gui.syntax.IResource;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;

public class ResourceAdapter implements IResource {

	private ProjectResource resource;

	public ResourceAdapter(ProjectResource resource) {
		this.resource = resource;
	}

	@Override
	public String getType() {
		return resource.getType();
	}

	@Override
	public ILexer getLexer() {
		return resource.getLexer();
	}

	@Override
	public String getContents() {
		return resource.getContents();
	}

	@Override
	public void markDirty() {
		resource.markDirty();
	}

	@Override
	public void analyse(String text, IErrorReporter reporter) {
		resource.analyse(text, reporter);
	}
	
}