package org.xteam.cs.grm;

import org.xteam.cs.model.EnumSet;
import org.xteam.cs.model.Property;

public class AnalysisMethod extends EnumSet {
	
	@Property(display="LR0")
	public static final AnalysisMethod LR0 = new AnalysisMethod("LR0");
	
	@Property(display="SLR")
	public static final AnalysisMethod SLR = new AnalysisMethod("SLR");
	
	@Property(display="LALR")
	public static final AnalysisMethod LALR = new AnalysisMethod("LALR");

	@Property(display="LR")
	public static final AnalysisMethod LR = new AnalysisMethod("LR");

	private String name;
	
	private AnalysisMethod(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}