package org.xteam.cs.grm;

import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.Group;
import org.xteam.cs.model.Property;

public class GrammarProperties extends BaseProperties {

	@Group(id="analysis", display="Grammar Analysis")
	@Property(display="Analysis Method")
	public AnalysisMethod analysisMethod = AnalysisMethod.LALR;
	
	@Group(id="analysis")
	@Property(display="Lookahead")
	public int lookahead = 1;
	
	@Group(id="analysis")
	@Property(display="Conflict Resolver Method")
	public ConflictResolverMethod conflictResolverMethod = ConflictResolverMethod.SimpleResolution;

	@Group(id="generator", display="Generator Parameters")
	@Property(display="Subpackage")
	public String grammarPackage = "parser";
	
	@Group(id="generator")
	@Property(display="Log Format")
	public LogFormat logFormat = LogFormat.TEXT;
}
