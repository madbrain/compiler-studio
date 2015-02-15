package org.xteam.cs.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Project {
	
	private File file;
	private boolean isDirty;
	private List<ProjectResource> resources = new ArrayList<ProjectResource>();
	private ProjectProperties properties = new ProjectProperties();
	
	private List<IProjectListener> listeners = new ArrayList<IProjectListener>();
	private ProjectManager manager;
	
	public Project(ProjectManager projectManager) {
		this.manager = projectManager;
	}

	public File getFile() {
		return file;
	}

	public boolean isDirty() {
		return isDirty;
	}
	
	public List<ProjectResource> getResources() {
		return resources;
	}
	
	public <T extends ProjectResource> List<T> getResources(Class<T> cls) {
		List<T> res = new ArrayList<T>();
		for (ProjectResource r : resources) {
			if (r.getClass().equals(cls))
				res.add((T)r);
		}
		return res;
	}
	
	public void saveAs(File file) {
		if (manager.saveProject(this, file)) {
			this.file = file;
			this.isDirty = false;
			fireEvent(new ProjectEvent(this, ProjectEvent.CHANGE_STATE));
		}
	}

	public void save() {
		if (manager.saveProject(this, file)) {
			this.isDirty = false;
			fireEvent(new ProjectEvent(this, ProjectEvent.CHANGE_STATE));
		}
	}

	public void reset() {
		clear();
		fireEvent(new ProjectEvent(this, ProjectEvent.CHANGE_STRUCTURE));
	}
	
	public void propertyChanged() {
		isDirty = true;
		fireEvent(new ProjectEvent(this, ProjectEvent.CHANGE_PROPERTIES));
	}

	public void clear() {
		resources.clear();
		isDirty = false;
		properties = new ProjectProperties();
	}

	public void open(File file) {
		if (manager.loadProject(this, file)) {
			this.file = file;
			this.isDirty = false;
			fireEvent(new ProjectEvent(this, ProjectEvent.CHANGE_STRUCTURE));
		}
	}

	public void addFile(File elementFile) {
		for (ProjectResource f : resources) {
			if (f instanceof FileResource && ((FileResource)f).getFile().equals(elementFile))
				return;
		}
		ProjectResource newFile = manager.createResource(this, elementFile);
		if (newFile != null) {
			resources.add(newFile);
			isDirty = true;
			fireEvent(new ProjectEvent(this, ProjectEvent.ADD_FILE, newFile));
		}
	}

	public void addProjectListener(IProjectListener l) {
		this.listeners.add(l);
	}
	
	public void removeProjectListener(IProjectListener l) {
		this.listeners.remove(l);
	}
	
	private void fireEvent(ProjectEvent event) {
		for (IProjectListener l : listeners) {
			l.projectChanged(event);
		}
	}

	public ProjectResource getResource(String filename) {
		for (ProjectResource res : resources) {
			if (res.getPath().equals(filename)) {
				return res;
			}
		}
		return null;
	}
	
	public void setProperties(Properties properties) {
		this.properties.fillFrom(properties);
	}

	public ProjectProperties getProperties() {
		return properties;
	}
	
	public void generate(IProgressMonitor monitor) {
		manager.generateProject(this, monitor);
	}
	
	public void finishBuilding() {
		fireEvent(new ProjectEvent(this, ProjectEvent.END_BUILD));
	}
	
	public <T> List<T> getMarks(Class<T> cls) {
		List<T> marks = new ArrayList<T>();
		for (ProjectResource res : resources) {
			marks.addAll(res.getMarks(cls));
		}
		return marks;
	}
	
	public File makeAbsolute(File f) {
		if (f.isAbsolute())
			return f;
		return new File(getFile().getParentFile(), f.getPath());
	}
	
	@Override
	public String toString() {
		return file == null ? "<unamed>" : file.getName();
	}

}
