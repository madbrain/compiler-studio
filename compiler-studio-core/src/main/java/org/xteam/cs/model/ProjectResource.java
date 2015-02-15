package org.xteam.cs.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;

public abstract class ProjectResource {

	
	private List<IResourceListener> listeners = new ArrayList<IResourceListener>();
	protected Project project;
	private List<IMark> marks = new ArrayList<IMark>();
	private BaseProperties properties;

	public ProjectResource(Project project) {
		this.project = project;
		this.properties = createProperties();
	}

	protected BaseProperties createProperties() {
		return new BaseProperties();
	}

	public String getName() {
		return "<unnamed>";
	}
	
	public String getPath() {
		return "";
	}
	
	public Project getProject() {
		return project;
	}
	
	public BaseProperties getProperties() {
		return properties;
	}
	
	public void setProperties(Properties properties) {
		this.properties.fillFrom(properties);
	}
	
	public void addResourceListener(IResourceListener l) {
		this.listeners.add(l);
	}
	
	public void removeResourceListener(IResourceListener l) {
		this.listeners.remove(l);
	}
	
	protected void fireEvent(ResourceEvent event) {
		for (IResourceListener l : listeners) {
			l.resourceChanged(event);
		}
	}

	public abstract String getType();
	
	public abstract ILexer getLexer();
	
	public void analyse(String text, IErrorReporter reporter) {
		// do nothing at this level
	}
	
	public void markDirty(boolean isDirty) {
		// do nothing
	}
	
	public void markDirty() {
		markDirty(true);
	}
	
	public boolean isDirty() {
		return false;
	}
	
	public void clearMark(Class<? extends IMark> cls) {
		Iterator<IMark> i = marks.iterator();
		while (i.hasNext()) {
			if (i.next().getClass().equals(cls)) {
				i.remove();
			}
		}
	}
	
	public <T> List<T> getMarks(Class<T> cls) {
		List<T> res = new ArrayList<T>();
		for (IMark mark : marks) {
			if (mark.getClass().equals(cls)) {
				res.add((T)mark);
			}
		}
		return res;
	}
	
	public void addMark(IMark mark) {
		marks.add(mark);
	}
	
	public String getContents() {
		return "";
	}
	
	public void save(String contents) {
	}

}
