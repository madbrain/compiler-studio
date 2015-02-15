package org.xteam.cs.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.model.ProjectResource;

public class ResourceAdapterManager {
	
	private static ResourceAdapterManager singleton = new ResourceAdapterManager();

	public static ResourceAdapterManager getDefault() {
		return singleton;
	}

	private Map<Class<? extends ProjectResource>, IResourceAdapterFactory> factories = new HashMap<Class<? extends ProjectResource>, IResourceAdapterFactory>();
	private Map<ProjectResource, IResourceAdapter> adapters = new HashMap<ProjectResource, IResourceAdapter>();
	
	private ResourceAdapterManager() {
		loadFactories();
	}

	protected void loadFactories() {
		ServiceLoader<IResourceAdapterFactory> loader = ServiceLoader.load(IResourceAdapterFactory.class);
		Iterator<IResourceAdapterFactory> i = loader.iterator();
		while (i.hasNext()) {
			registerResourceAdapterFactory(i.next());
		}
	}

	private void registerResourceAdapterFactory(IResourceAdapterFactory factory) {
		factories.put(factory.getManagedClass(), factory);
	}

	public IResourceAdapter getAdapter(ProjectResource resource) {
		IResourceAdapter adapter = adapters.get(resource);
		if (adapter == null) {
			IResourceAdapterFactory factory = factories.get(resource.getClass());
			if (factory != null) {
				adapter = factory.createAdapter(resource);
				adapters.put(resource, adapter);
			}
		}
		return adapter;
	}
}
