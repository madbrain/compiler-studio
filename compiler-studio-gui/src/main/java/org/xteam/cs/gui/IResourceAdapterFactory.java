package org.xteam.cs.gui;

import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.model.ProjectResource;

public interface IResourceAdapterFactory {

	Class<? extends ProjectResource> getManagedClass();

	IResourceAdapter createAdapter(ProjectResource resource);

}
