package org.xteam.cs.jobs;

import org.xteam.cs.jobs.JobManager.Job;

public class JobChangedEvent {

	public static final int DONE    = 0;
	public static final int CHANGED = 1;
	
	private Job job;
	private int kind;

	public JobChangedEvent(int kind, Job job) {
		this.job = job;
		this.kind = kind;
	}

	public Job getJob() {
		return job;
	}

	public int getKind() {
		return kind;
	}

}
