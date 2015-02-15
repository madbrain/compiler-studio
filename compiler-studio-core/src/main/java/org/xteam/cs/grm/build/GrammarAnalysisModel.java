package org.xteam.cs.grm.build;

import java.util.ArrayList;
import java.util.List;

import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.NonTerminal;
import org.xteam.cs.grm.model.PropagateAction;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;

public class GrammarAnalysisModel {

	private Grammar grammar;
	private FirstSet firstSet;
	private FollowSet followSet;
	private Terminal endTerminal;
	private NonTerminal startSymbol;
	private int lookahead;
	private Nullables nullables;
	private Rule acceptingRule;
	private LRAutomaton automaton;

	public GrammarAnalysisModel(Grammar grammar, int lookahead) {
		this.grammar = grammar;
		this.lookahead = lookahead;
	}

	public FirstSet getFirstSet() {
		if (firstSet == null) {
			firstSet = new FirstSet(grammar, lookahead);
		}
		return firstSet;
	}
	
	public FollowSet getFollowSet() {
		if (followSet == null) {
			followSet = new FollowSet(this);
		}
		return followSet;
	}
	
	public Nullables getNullables() {
		if (nullables == null) {
			nullables = new Nullables(grammar);
		}
		return nullables;
	}
	
	public Rule getAcceptingRule() {
		return acceptingRule;
	}

	public Grammar grammar() {
		return grammar;
	}

	public void augment() {
		endTerminal = new Terminal("$EOF$", null);
		grammar.getSymbols().add(endTerminal);
		startSymbol = new NonTerminal("$start$");
		grammar.getSymbols().add(startSymbol);
		List<Symbol> rhs = new ArrayList<Symbol>();
		rhs.add(grammar.getStart());
		rhs.add(endTerminal);
		acceptingRule = new Rule(startSymbol, rhs, new PropagateAction(0));
		grammar.getRules().add(acceptingRule);
		grammar.setStart(startSymbol);
	}
	
	public Terminal endTerminal() {
		return endTerminal;
	}

}
