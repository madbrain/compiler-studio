package org.xteam.cs.jobs;

import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.model.IProgressMonitor;

public class JobManager {
	
	public class Job implements Runnable, IProgressMonitor {

		private IJob ijob;
		private String taskName;
		private String subTaskName;
		private boolean isDone = false;
		private int total;
		private double worked;

		private Job(IJob ijob) {
			this.ijob = ijob;
		}
		
		@Override
		public void beginTask(String name, int amountOfWork) {
			this.taskName = name;
			this.subTaskName = "";
			this.total = amountOfWork;
			this.worked = 0;
			changed();
			//System.out.println("==> " + name + "(" + amountOfWork + ")");
		}
		
		@Override
		public void done() {
			isDone  = true;
			fireChanged(new JobChangedEvent(JobChangedEvent.DONE, this));
			//System.out.println("==> done");
		}

		@Override
		public void subTask(String name) {
			subTaskName = name;
			changed();
			//System.out.println("--> " + name);
		}

		@Override
		public void worked(int amount) {
			this.worked += amount;
			changed();
			//System.out.println("+(" + amount + ")");
		}
		
		@Override
		public void internalWorked(double amount) {
			worked += amount;
			changed();
			//System.out.println("+(" + amount + ")");
		}
		
		private void changed() {
			fireChanged(new JobChangedEvent(JobChangedEvent.CHANGED, this));
		}

		@Override
		public void run() {
			ijob.run(this);
			finish(this);
		}

		public int getTotal() {
			return total*100;
		}

		public int getWorked() {
			return (int)(worked * 100);
		}

		public String getTaskName() {
			return taskName;
		}

		public String getSubTaskName() {
			return subTaskName;
		}
		
	}
	
	private List<Job> jobs = new ArrayList<Job>();
	private List<IJobListener> listeners = new ArrayList<IJobListener>();

	public synchronized void run(IJob job) {
		Job j = new Job(job);
		jobs.add(j);
		new Thread(j, "Job").start();
	}
	
	private synchronized void finish(Job j) {
		jobs.remove(j);
	}
	
	public List<Job> getJobs() {
		return jobs;
	}
	
	public void addJobListener(IJobListener l) {
		listeners .add(l);
	}
	
	public void removeJobListener(IJobListener l) {
		listeners.remove(l);
	}
	
	private void fireChanged(JobChangedEvent event) {
		for (IJobListener listener : listeners) {
			listener.changed(event);
		}
	}

}
