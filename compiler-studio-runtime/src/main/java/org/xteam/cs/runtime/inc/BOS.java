package org.xteam.cs.runtime.inc;

public class BOS extends Token {

    public BOS() {
        super(0xff, "");
    }
    
    /**
     * never dettach BOS, but clear it instead.
     */
    public void dettach() {
    	clear();
    }
    
    /*public boolean isLayout(ITokenCategorizer categorizer) {
		return false;
	}*/

}
