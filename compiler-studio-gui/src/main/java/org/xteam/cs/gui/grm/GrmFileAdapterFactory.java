package org.xteam.cs.gui.grm;

import org.xteam.cs.grm.GrammarFile;
import org.xteam.cs.gui.IResourceAdapterFactory;
import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.model.ProjectResource;

public class GrmFileAdapterFactory implements IResourceAdapterFactory {

	@Override
	public Class<? extends ProjectResource> getManagedClass() {
		return GrammarFile.class;
	}

	@Override
	public IResourceAdapter createAdapter(ProjectResource resource) {
		return new GrmFileAdapter((GrammarFile)resource);
	}

}
