package org.xteam.cs.model;

public interface IProgressMonitor {
	
	void beginTask(String name, int amountOfWork);
	
	void subTask(String name);

	void worked(int amount);

	void done();
	
	void internalWorked(double amount);

}
