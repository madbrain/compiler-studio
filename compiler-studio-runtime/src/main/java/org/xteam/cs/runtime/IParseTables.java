package org.xteam.cs.runtime;

public interface IParseTables {

	/**
	 * production[i][0] -> LHS symbol of action i
	 * production[i][1] -> RHS size of action i
	 * 
	 * @return the production table
	 */
	short[][] productionTable();

	short[][] actionTable();

	short[][] reduceTable();

	int startState();
	
	int acceptingProduction();

	//IAction getAction(int state, int symbol);
	
}
