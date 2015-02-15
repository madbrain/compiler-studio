package org.xteam.cs.model;

public class ResourceEvent {
	
	public static final int CHANGE_STATE = 0;

	private int kind;
	private ProjectResource resource;

	public ResourceEvent(int kind, ProjectResource resource) {
		this.kind = kind;
		this.resource = resource;
	}

	public ProjectResource getResource() {
		return resource;
	}

}
