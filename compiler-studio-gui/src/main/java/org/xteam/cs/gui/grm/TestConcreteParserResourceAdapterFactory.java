package org.xteam.cs.gui.grm;

import org.xteam.cs.gui.IResourceAdapterFactory;
import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.model.ProjectResource;

public class TestConcreteParserResourceAdapterFactory implements IResourceAdapterFactory {

	@Override
	public Class<? extends ProjectResource> getManagedClass() {
		return TestConcreteParserResource.class;
	}

	@Override
	public IResourceAdapter createAdapter(ProjectResource resource) {
		return new TestConcreteParserResourceAdapter((TestConcreteParserResource)resource);
	}

}
