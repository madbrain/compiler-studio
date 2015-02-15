/**
 * 
 */
package org.xteam.cs.gui.grm;

import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.grm.build.GrammarMapping;
import org.xteam.cs.grm.model.Action;
import org.xteam.cs.grm.model.LexerStateAction;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.runtime.IRuleReducer;
import org.xteam.cs.runtime.IStatedLexer;

public class ConcreteSyntaxReducer implements IRuleReducer {

	private GrammarMapping mapping;
	private IStatedLexer lexer;

	public ConcreteSyntaxReducer(GrammarMapping mapping, IStatedLexer lexer) {
		this.mapping = mapping;
		this.lexer = lexer;
	}

	@Override
	public Object reduce(int r, Object[] values) {
		List<ConcreteNode> nodes = new ArrayList<ConcreteNode>();
		for (int i = 0; i < values.length; ++i) {
			nodes.add((ConcreteNode) values[i]);
		}
		Rule rule = mapping.getRuleFor(r);
		Action action = rule.getAction();
		if (action instanceof LexerStateAction) {
			LexerStateAction lsa = (LexerStateAction) action;
			if (lsa.getCondition() < 0
					|| lsa.getCondition() == lexer.getState()) {
				lexer.setState(lsa.getState());
			}
		}
		return new RuleNode(rule, nodes);
	}
	
}