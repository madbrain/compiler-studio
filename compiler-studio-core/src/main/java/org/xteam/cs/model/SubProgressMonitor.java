package org.xteam.cs.model;

public class SubProgressMonitor implements IProgressMonitor {

	private IProgressMonitor subMonitor;
	private int amount;

	public SubProgressMonitor(IProgressMonitor monitor) {
		this.subMonitor = monitor;
	}

	@Override
	public void beginTask(String name, int amountOfWork) {
		this.amount = amountOfWork;
	}

	@Override
	public void done() {
		worked(1);
	}

	@Override
	public void subTask(String name) {
		subMonitor.subTask(name);
	}

	@Override
	public void worked(int amount) {
		subMonitor.internalWorked(amount / (double)this.amount);
	}

	@Override
	public void internalWorked(double amount) {
		subMonitor.internalWorked(amount / (double)this.amount);
	}

}
