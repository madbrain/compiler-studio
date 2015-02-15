/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.xteam.cs.grm.model;

import java.util.ArrayList;
import java.util.List;


public class NonTerminal extends Symbol {

	private List<Rule> rules = new ArrayList<Rule>();
	
	public NonTerminal(String name) {
		super(name);
	}

	public List<Rule> getRules() {
		return rules;
	}

}
