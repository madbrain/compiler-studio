/**
 * 
 */
package org.xteam.cs.gui.grm;

import java.util.List;

import org.xteam.cs.grm.model.Rule;

public class RuleNode extends ConcreteNode {

	private Rule rule;
	private List<ConcreteNode> children;

	public RuleNode(Rule rule, List<ConcreteNode> children) {
		this.rule = rule;
		this.children = children;
	}
	
	@Override
	public String toString() {
		return rule.toString();
	}

	public List<ConcreteNode> getChildren() {
		return children;
	}
	
}