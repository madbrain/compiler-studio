package org.xteam.cs.grm.build;


/**
 * ShiftAction is used to ...
 * 
 * @author ludo
 */
public class ShiftAction extends Action {

    private LRState state;
    
    /**
     * @param state
     */
    public ShiftAction(LRState state) {
        this.state = state;
    }
    
    public int type() {
        return SHIFT;
    }
    
    public LRState to() {
        return state;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof ShiftAction))
        	return false;
        return state.isSame(((ShiftAction) o).state);
    }
    
    public String toString() {
        return "s"+state.number();
    }

	public void visit(IActionVisitor visitor) {
		visitor.visitShift(this);
	}

}
