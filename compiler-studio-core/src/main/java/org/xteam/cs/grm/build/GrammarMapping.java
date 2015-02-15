package org.xteam.cs.grm.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;

public class GrammarMapping {
	
	private Map<Symbol, Integer> symbolMapping = new HashMap<Symbol, Integer>();
	private Map<Integer, Symbol> inverseSymbolMapping = new HashMap<Integer, Symbol>();
	private Map<Rule, Integer> ruleMapping = new HashMap<Rule, Integer>();
	private Map<Integer, Rule> inverseRuleMapping = new HashMap<Integer, Rule>();
	private List<Terminal> grammarTokens = new ArrayList<Terminal>();

	public Map<Symbol, Integer> getSymbols() {
		return symbolMapping;
	}

	public Map<Rule, Integer> getRules() {
		return ruleMapping;
	}

	public void addRule(Rule rule, int index) {
		this.ruleMapping.put(rule, index);
		this.inverseRuleMapping .put(index, rule);
	}

	public Rule getRuleFor(int r) {
		return inverseRuleMapping.get(r);
	}

	public void addSymbol(Symbol sym, int value) {
		symbolMapping.put(sym, value);
		this.inverseSymbolMapping.put(value, sym);
	}

	public Symbol getSymbolFor(int t) {
		return inverseSymbolMapping.get(t);
	}

	public void addGrammarToken(Terminal sym, int index) {
		this.addSymbol(sym, index);
		grammarTokens.add(sym);
	}

	public List<Terminal> getGrammarTokens() {
		return grammarTokens;
	}

}
