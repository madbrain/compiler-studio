package org.xteam.cs.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.xteam.cs.model.ErrorMark;
import org.xteam.cs.model.ErrorMarkConsoleDiagnostic;
import org.xteam.cs.model.FileResource;
import org.xteam.cs.model.IProgressMonitor;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectManager;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.runtime.IErrorReporter;

public class Generator {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length == 0) {
			System.out.println("missing project file name");
			return;
		}
		ProjectManager manager = new ProjectManager();
		Project project = new Project(manager);
		project.open(new File(args[0]));
		manager.buildProject(project, new ProgressMonitor());
		boolean hasError = false;
		for (ProjectResource resource : project.getResources()) {
			List<ErrorMark> marks = resource.getMarks(ErrorMark.class);
			for (ErrorMark mark : marks) {
				if (mark.getLevel() == IErrorReporter.ERROR)
					hasError |= true;
			}
			ErrorMarkConsoleDiagnostic diag = new ErrorMarkConsoleDiagnostic(System.out);
			diag.printDiagnostic(new FileReader(((FileResource)resource).getFile()), marks);
		}
		if (! hasError) {
			project.generate(new ProgressMonitor());
		}
	}
	
	private static class ProgressMonitor implements IProgressMonitor {

		@Override
		public void beginTask(String msg, int amountOfWork) {
			System.out.println("=== " + msg);
		}

		@Override
		public void done() {
		}

		@Override
		public void subTask(String name) {
			System.out.println("*** " + name);
		}

		@Override
		public void worked(int amount) {
		}

		@Override
		public void internalWorked(double amount) {
		}
		
	}
}
