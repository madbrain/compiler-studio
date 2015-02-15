package org.xteam.cs.grm.build;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class LRAutomaton {

	private List<LRState> states;
    private LRState startState;

	public LRAutomaton() {
		states = new ArrayList<LRState>();
        startState = null;
	}
	
	public int stateCount() {
		return states.size();
	}
	
	public List<LRState> getStates() {
		return states;
	}
	
	public LRState at(int i) {
		return states.get(i);
	}
    
    public LRState start() {
        return startState;
    }

	public LRState addState(LRState state) {
		for (int i = 0; i < states.size(); ++i) {
			LRState st = (LRState) states.get(i);
			if (st.isSame(state)) return st;
		}
		state.setNumber(states.size());
		states.add(state);
        if (startState == null)
            startState = state;
		return state;
	}

	public void output(PrintStream stream) {
		stream.println("digraph G {");
        stream.println("node [shape=\"box\"];");
		for (int i = 0; i < states.size(); ++i) {
			LRState state = (LRState) states.get(i);
			state.output(stream);
		}
		stream.println("}");
	}
    
}
