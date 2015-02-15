package org.xteam.cs.grm.build;

import java.io.IOException;

import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.runtime.IParseTables;
import org.xteam.cs.runtime.ParseTables;

public class TableBuilder {
	
	public IParseTables run(GrammarAnalysisModel model, LRAutomaton automaton,
			GrammarMapping mapping) throws IOException {
		Grammar grammar = model.grammar();
		
		// action table
		short[][] actionTable = new short[automaton.getStates().size()][];
		
		for (LRState state : automaton.getStates()) {
			ActionEmiter emiter = new ActionEmiter(mapping);
			for (Symbol sym : state.getTransitionSymbols()) {
				if (sym.isTerminal()) {
					int code = mapping.getSymbols().get(sym);
					Action action = state.getTransition(sym);
					emiter.emitAction(code, action);
				}
			}
			actionTable[state.number()] = emiter.getTable();
		}
		
		// reduce table
		short[][] reduceTable = new short[automaton.getStates().size()][];
		for (LRState state : automaton.getStates()) {
			ActionEmiter emiter = new ActionEmiter(mapping);
			for (Symbol sym : state.getTransitionSymbols()) {
				if (!sym.isTerminal()) {
					int code = mapping.getSymbols().get(sym);
					Action action = state.getTransition(sym);
					emiter.emitGoto(code, action);
				}
			}
			reduceTable[state.number()] = emiter.getTable();
		}
		
		// production table
		short[][] productionTable = new short[grammar.getRules().size()][2];
		int index = 0;
		for (Rule rule : grammar.getRules()) {
			productionTable[index][0] = (short)mapping.getSymbols().get(rule.getLhs()).intValue();
			productionTable[index][1] = (short)rule.getRhs().size();
			index++;
		}
		
		return new ParseTables(actionTable, reduceTable, productionTable,
				mapping.getRules().get(model.getAcceptingRule()));
	}

}
