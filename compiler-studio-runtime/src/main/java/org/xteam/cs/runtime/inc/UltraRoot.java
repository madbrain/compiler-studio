package org.xteam.cs.runtime.inc;

public class UltraRoot extends ParentNode {
    
    private BOS bos;
	private EOS eos;

	public UltraRoot() {
        super();
        bos = new BOS();
		eos = new EOS();
		add(bos);
		add(new ErrorNode());
		add(eos);
    }
    
    public BOS bos() {
		return bos;
	}
	
	public EOS eos() {
		return eos;
	}

    protected void computeTextOffsetCache() {
        textOffsetCache = 0;
    }

	public void setNode(Node node) {
		replace(childAt(1), node);
	}

}
