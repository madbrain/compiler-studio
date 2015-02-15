package org.xteam.cs.gui.lex;

import org.xteam.cs.gui.IResourceAdapterFactory;
import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.lex.LexerFile;
import org.xteam.cs.model.ProjectResource;

public class LexerFileAdapterFactory implements IResourceAdapterFactory {

	@Override
	public Class<? extends ProjectResource> getManagedClass() {
		return LexerFile.class;
	}

	@Override
	public IResourceAdapter createAdapter(ProjectResource resource) {
		return new LexerFileAdapter((LexerFile)resource);
	}

}
