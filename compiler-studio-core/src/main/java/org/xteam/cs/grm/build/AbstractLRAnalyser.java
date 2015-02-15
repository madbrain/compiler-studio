package org.xteam.cs.grm.build;

import java.util.Set;

import org.xteam.cs.grm.model.Rule;

public abstract class AbstractLRAnalyser implements ILRAnalyser {

    protected GrammarAnalysisModel model;
    
    public AbstractLRAnalyser(GrammarAnalysisModel model) {
        this.model = model;
    }
    
    public void buildActions(LRAutomaton automaton) {
        for (LRState state : automaton.getStates()) {
            for (int j = 0; j < state.itemCount(); ++j) {
                LR0Item item = state.itemAt(j);
                Rule prod = item.production();
                if (item.atEnd()) {
                    if (prod.getLhs().equals(model.grammar().getStart())) {
                        // Accept
                    	state.addTransition(model.endTerminal(),
                                new AcceptAction(prod));
                    } else {
                        // Reduce
                        for (Word word : getLookahead(item)) {
                        	// XXX reduction is only on lookahead of 1
                            state.addTransition(word.at(0), new ReduceAction(prod));
                        }
                    }
                }
            }
        }
    }
    
    public abstract LR0Item createItem(Rule p);

    public abstract LR0Item createItem(Rule p, Set<Word> lookahead);

    public abstract Set<Word> getLookahead(LR0Item item);
    
    public void fixState(LRState m, Set<LR0Item> items) {
        // nothing to do
    }

    public void propagateAllLookaheads(LRAutomaton automaton) {
        // nothing to do
    }
}
