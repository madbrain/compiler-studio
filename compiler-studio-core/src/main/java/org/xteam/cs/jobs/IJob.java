package org.xteam.cs.jobs;

import org.xteam.cs.model.IProgressMonitor;

public interface IJob {

	void run(IProgressMonitor monitor);

}
