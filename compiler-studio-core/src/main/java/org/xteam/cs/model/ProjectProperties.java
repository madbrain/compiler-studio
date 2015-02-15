package org.xteam.cs.model;

public class ProjectProperties extends BaseProperties {

	@Group(id="generator", display="Generator Options")
	@Property(display="Source Folder")
	public String sourceFolder = "src-gen";
	
	@Group(id="generator")
	@Property(display="Use Resource Folder")
	public boolean useResourceFolder = false;
	
	@Group(id="generator")
	@Property(display="Resource Folder", condition="useResourceFolder")
	public String resourceFolder = "rsc-gen";
	
	@Group(id="generator")
	@Property(display="Package")
	public String mainPackage = "compiler";
	
	@Group(id="generator")
	@Property(display="Log Folder")
	public String logFolder = "log";
	
}
