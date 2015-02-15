package org.xteam.cs.gui;

import org.xteam.cs.jobs.IJob;
import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.ProjectResource;

public interface IWorkbench {

	void openEditor(ProjectResource resource);

	void openPropertyEditor(String title, BaseProperties properties);

	void runJob(IJob iJob);

}
