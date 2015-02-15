package org.xteam.cs.grm;

import org.xteam.cs.model.EnumSet;
import org.xteam.cs.model.Property;

public class ConflictResolverMethod extends EnumSet {

	@Property(display="No Resolution")
	public static final ConflictResolverMethod NoResolution = new ConflictResolverMethod("NO_RESOL");
	
	@Property(display="SimpleResolution")
	public static final ConflictResolverMethod SimpleResolution = new ConflictResolverMethod("SLR");
	

	private String name;
	
	private ConflictResolverMethod(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
