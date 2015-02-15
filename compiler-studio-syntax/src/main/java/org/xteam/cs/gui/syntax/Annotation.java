package org.xteam.cs.gui.syntax;


public class Annotation {

	private Object tag;
	private int line;
	private IResource resource;
	private int start;
	private int end;
	private String msg;

	public Annotation(IResource resource, int line, int start, int end, String msg, Object tag) {
		this.line = line;
		this.start = start;
		this.end = end;
		this.resource = resource;
		this.msg = msg;
		this.tag = tag;
	}

	public Object getTag() {
		return tag;
	}

	public boolean contains(int offset) {
		return start <= offset && offset < end;
	}

	public String getText() {
		return msg;
	}

}
