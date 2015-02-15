package org.xteam.cs.grm.build;

import java.util.Collections;
import java.util.Set;

import org.xteam.cs.grm.model.Rule;

public class LR0Analyzer extends AbstractLRAnalyser {

	public LR0Analyzer(GrammarAnalysisModel model) {
		super(model);
	}

	public LR0Item createItem(Rule p) {
		return new LR0Item(p);
	}

	public LR0Item createItem(Rule p, Set lookahead) {
		return createItem(p);
	}

	public Set getLookahead(LR0Item item) {
        return Collections.EMPTY_SET;
	}
	
}
