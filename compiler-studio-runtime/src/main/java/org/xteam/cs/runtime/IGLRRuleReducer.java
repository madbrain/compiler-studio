package org.xteam.cs.runtime;

public interface IGLRRuleReducer extends IRuleReducer {
	
	Object merge(int sym, Object o, Object n);
	
}
