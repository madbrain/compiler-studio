package org.xteam.cs.grm.build;


/**
 * Action is used to ...
 * 
 * @author ludo
 */
public abstract class Action {
    
    public static final int ACCEPT  = 0;
    public static final int SHIFT   = 1;
    public static final int REDUCE  = 2;
    
    public static final int CONFLICT = 10;
    
    public abstract int type();
    
	public boolean isConflict() {
		return type() == CONFLICT;
	}

	public boolean isShift() {
		return type() == SHIFT;
	}

	public abstract void visit(IActionVisitor visitor);

	public int length() {
		return 1;
	}

}
