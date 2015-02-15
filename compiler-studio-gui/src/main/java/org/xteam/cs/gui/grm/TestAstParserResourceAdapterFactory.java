package org.xteam.cs.gui.grm;

import org.xteam.cs.gui.IResourceAdapterFactory;
import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.model.ProjectResource;

public class TestAstParserResourceAdapterFactory implements IResourceAdapterFactory {

	@Override
	public Class<? extends ProjectResource> getManagedClass() {
		return TestAstParserResource.class;
	}

	@Override
	public IResourceAdapter createAdapter(ProjectResource resource) {
		return new TestAstParserResourceAdapter((TestAstParserResource)resource);
	}

}
