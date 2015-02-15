package org.xteam.cs.grm;

import org.xteam.cs.model.EnumSet;
import org.xteam.cs.model.Property;

public class LogFormat extends EnumSet {
	
	@Property(display="Text")
	public static final LogFormat TEXT = new LogFormat("TEXT");
	
	@Property(display="Dot")
	public static final LogFormat DOT = new LogFormat("DOT");
	
	@Property(display="Html")
	public static final LogFormat HTML = new LogFormat("HTML");
	
	private String name;

	private LogFormat(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
