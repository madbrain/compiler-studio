package org.xteam.cs.gui.views;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.xteam.cs.model.ProjectResource;

public abstract class Editor extends JPanel {
	
	private static final long serialVersionUID = -5738158072184608088L;
	
	private ProjectResource resource;
	
	public Editor(ProjectResource resource) {
		this.resource = resource;
		setLayout(new BorderLayout());
	}
	
	public void close() {
	}

	public ProjectResource getResource() {
		return resource;
	}

}
