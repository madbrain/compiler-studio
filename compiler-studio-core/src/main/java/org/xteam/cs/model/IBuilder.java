package org.xteam.cs.model;

public interface IBuilder {
	
	void initialize(Project project, IProgressMonitor monitor);

	void build();

	void generate();

}
