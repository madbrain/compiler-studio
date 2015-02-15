package org.maven.cscodegen.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.xteam.cs.model.ErrorMark;
import org.xteam.cs.model.ErrorMarkConsoleDiagnostic;
import org.xteam.cs.model.FileResource;
import org.xteam.cs.model.IProgressMonitor;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectManager;
import org.xteam.cs.model.ProjectProperties;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.runtime.IErrorReporter;

/**
 * Compiler Studio Code Generation Tool
 * 
 * @goal generate
 * @phase generate-sources
 * @requiresProject
 */
public class CompilerStudioGenerateMojo extends AbstractMojo {

	/**
	 * Path where the CompilerStudio projects are located
	 * 
	 * @parameter property="cscodegen.projectRoot"
	 *            default-value="${basedir}/src/main/cs"
	 */
	private File projectRoot;
	
	/**
	 * Path where the generated sources should be placed
	 * 
	 * @parameter property="cscodegen.sourceRoot"
	 *            default-value="${project.build.directory}/generated-sources/cs"
	 * @required
	 */
	private File sourceRoot;
	
	/**
	 * Path where the generated resources should be placed
	 * 
	 * @parameter property="cscodegen.resourceRoot"
	 *            default-value="${project.build.directory}/generated-resources/cs"
	 * @required
	 */
	private File resourceRoot;
	
	/**
	 * Path where the generated resources should be placed
	 * 
	 * @parameter property="cscodegen.sourceRoot"
	 *            default-value="${project.build.directory}/logs/cs"
	 * @required
	 */
	private File logRoot;

	/**
	 * A list of project files to include. Can contain ant-style wildcards and
	 * double wildcards. Defaults to *.cpj
	 * 
	 * @parameter
	 */
	private String includes[];

	/**
	 * A list of project files to exclude. Can contain ant-style wildcards and
	 * double wildcards.
	 * 
	 * @parameter
	 */
	private String excludes[];

	/**
	 * @parameter property="project"
	 * @required
	 */
	private MavenProject project;
	
	/**
	 * Verbose mode
	 * 
	 * @parameter property="cscodegen.verbose"
	 *            default-value="false"
	 */
	private boolean verbose;
	
	/**
	 * @component
	 */
	private BuildContext buildContext;

	public void execute() throws MojoExecutionException {
		if (includes == null) {
			includes = new String[] { "*.cpj" };
		}
		 
		List<File> projectFiles = getProjectFiles(projectRoot, includes, excludes);
		
		 if (projectFiles.size() == 0) {
			 getLog().info("Nothing to generate");
			 return;
		 }
		
		ProjectManager manager = new ProjectManager();
		Project csProject = new Project(manager);
		
		for (File projectFile : projectFiles) {
			csProject.open(projectFile);
			setProjectProperties(csProject.getProperties());
			manager.buildProject(csProject, new ProgressMonitor());
			boolean hasError = false;
			for (ProjectResource resource : csProject.getResources()) {
				List<ErrorMark> marks = resource.getMarks(ErrorMark.class);
				for (ErrorMark mark : marks) {
					if (mark.getLevel() == IErrorReporter.ERROR)
						hasError |= true;
				}
				ErrorMarkConsoleDiagnostic diag = new ErrorMarkConsoleDiagnostic(System.out);
				try {
					diag.printDiagnostic(new FileReader(((FileResource)resource).getFile()), marks);
				} catch (IOException e) {
				}
			}
			if (! hasError) {
				csProject.generate(new ProgressMonitor());
			}
		}

		if (project != null && sourceRoot != null && sourceRoot.exists()) {
			project.addCompileSourceRoot(sourceRoot.getAbsolutePath());
			buildContext.refresh(sourceRoot);
		}
		
		if (project != null && resourceRoot != null && resourceRoot.exists()) {
			Resource resource = new Resource();
			resource.setDirectory(resourceRoot.getAbsolutePath());
			resource.setFiltering(false);
			project.addResource(resource);
			buildContext.refresh(resourceRoot);
		}
	}

	private void setProjectProperties(ProjectProperties properties) {
		properties.sourceFolder = sourceRoot.getAbsolutePath();
		properties.useResourceFolder = true;
		properties.resourceFolder = resourceRoot.getAbsolutePath();
		properties.logFolder = logRoot.getAbsolutePath();
	}
	
	private List<File> getProjectFiles(File dir, String includes[], String excludes[]) throws MojoExecutionException {
		Scanner scanner = buildContext.newScanner(dir);
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
		scanner.scan();
		String[] includedFiles = scanner.getIncludedFiles();
		List<File> fileToProcess = new ArrayList<File>();
		if (includedFiles != null) {
			for (String includedFile : includedFiles) {
				fileToProcess.add(new File(scanner.getBasedir(), includedFile));
			}
		}
		return fileToProcess;
	}

	private class ProgressMonitor implements IProgressMonitor {

		@Override
		public void beginTask(String msg, int amountOfWork) {
			getLog().info(msg);
		}

		@Override
		public void done() {
		}

		@Override
		public void subTask(String name) {
			if (verbose) {
				getLog().info("   " + name);
			}
		}

		@Override
		public void worked(int amount) {
		}

		@Override
		public void internalWorked(double amount) {
		}
		
	}
}
