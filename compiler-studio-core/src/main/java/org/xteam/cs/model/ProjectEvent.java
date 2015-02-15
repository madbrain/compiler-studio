package org.xteam.cs.model;

public class ProjectEvent {
	
	public static final int CHANGE_STATE = 0;

	public static final int CHANGE_STRUCTURE = 1;

	public static final int ADD_FILE = 2;

	public static final int END_BUILD = 3;

	public static final int CHANGE_PROPERTIES = 4;

	private Project project;
	private int kind;
	private ProjectResource resource;

	public ProjectEvent(Project project, int kind) {
		this.project = project;
		this.kind = kind;
	}

	public ProjectEvent(Project project, int kind, ProjectResource resource) {
		this.project = project;
		this.kind = kind;
		this.resource = resource;
	}

	public int getKind() {
		return kind;
	}

	public ProjectResource getResource() {
		return resource;
	}

	public Project getProject() {
		return project;
	}

}
