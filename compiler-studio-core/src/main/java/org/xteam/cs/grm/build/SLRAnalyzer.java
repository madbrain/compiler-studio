package org.xteam.cs.grm.build;

import java.util.Set;

/**
 * @author ludo
 *
 */
public class SLRAnalyzer extends LR0Analyzer {

	public SLRAnalyzer(GrammarAnalysisModel model) {
        super(model);
	}
	
    public Set<Word> getLookahead(LR0Item item) {
        return model.getFollowSet().at(item.production().getLhs());
    }
    
}
