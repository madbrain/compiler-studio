package org.xteam.cs.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xteam.cs.model.IBuilder;
import org.xteam.cs.model.IProgressMonitor;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectProperties;

public abstract class AbstractBuilder implements IBuilder {

	protected Project project;
	protected IProgressMonitor monitor;
	
	@Override
	public void initialize(Project project, IProgressMonitor monitor) {
		this.project = project;
		this.monitor = monitor;
	}
	
	protected void generate(VelocityContext context, File currentFile, String templateName) throws Exception {
		monitor.subTask("Generating " + currentFile.getName());
		Template tokensTemplate = Velocity.getTemplate(templateName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile));
		tokensTemplate.merge(context, writer);
		writer.close();
		monitor.worked(1);
	}

	protected File makeFilename(ProjectProperties projectProperties,
			String resourcePackage, String filename, boolean isResource) {
		String pack = projectProperties.mainPackage + "." + resourcePackage;
		String base = isResource && projectProperties.useResourceFolder ?
				projectProperties.resourceFolder : projectProperties.sourceFolder;
		File f = new File(base
				+ File.separator + pack.replace('.', File.separatorChar)
				+ File.separator + filename);
		return project.makeAbsolute(f);
	}
	
}
