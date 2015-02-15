package org.xteam.cs.grm.build;

import java.util.Set;

import org.xteam.cs.grm.model.Rule;

public interface ILRAnalyser {

    public abstract LR0Item createItem(Rule p);

    public abstract LR0Item createItem(Rule prod, Set<Word> lookahead);

    public abstract Set<Word> getLookahead(LR0Item item);

    public abstract void fixState(LRState m, Set<LR0Item> items);

    public abstract void propagateAllLookaheads(LRAutomaton automaton);

	public abstract void buildActions(LRAutomaton automaton);

}