package org.xteam.cs.grm.build;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.xteam.cs.grm.model.Symbol;

public class AutomatonBuilder {

	private GrammarAnalysisModel grammar;
	private Stack<LRState> stack;
	private LRAutomaton automaton;
	private ILRAnalyser analyzer;
    private LRState startState;

	public AutomatonBuilder(GrammarAnalysisModel grammar, ILRAnalyser analyzer) {
		this.grammar = grammar;
		this.analyzer = analyzer;
		this.stack = new Stack<LRState>();
		this.automaton = new LRAutomaton();
	}

	public LRAutomaton buildAutomaton() throws Exception {
		LRState current = LRState.buildFrom(grammar.grammar().getStart(), analyzer);
		automaton.addState(current);
		stack.push(current);
		startState = current;

		while (stack.size() != 0) {
			current = stack.pop();
			for (Symbol trans : current.getTransitions()) {
                Set<LR0Item> linkedItems = new HashSet<LR0Item>();
				LRState n = current.computeTransition(trans, analyzer, linkedItems);
				LRState m = automaton.addState(n);
				if (n.equals(m))
					stack.add(m);
                else
                    analyzer.fixState(m, linkedItems);
				current.addTransition(trans, new ShiftAction(m));
			}
		}
        analyzer.propagateAllLookaheads(automaton);
        analyzer.buildActions(automaton);
        return automaton;
	}
	
}
