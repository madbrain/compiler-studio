package org.xteam.cs.runtime.inc;

public abstract class Node {
    
    public static final int TEXT_CHANGE   = 1;
    public static final int CHILD_CHANGE  = 2;
    public static final int LOCAL_CHANGE  = 4;
    public static final int NESTED_CHANGE = 8;
    public static final int MARK          = 16;
	public static final int RELEXED       = 32;
    
    protected ParentNode parent;
    private int flags;
    protected int textOffsetCache;
	protected int type;

    public Node() {
        this.flags = 0;
        this.textOffsetCache = -1;
        this.parent = null;
    }
    
    public int type() {
		return type;
	}
    
    public void setType(int type) {
    	this.type = type;
    }
    
    //public abstract boolean isLayout(ITokenCategorizer categorizer);
    
    public boolean isConnector() {
    	return false;
    }
    
    public void setParent(ParentNode parent) {
        this.parent = parent;
    }
    
    public ParentNode getParent() {
        return parent;
    }
    
    public boolean isToken() {
        return false;
    }
    
    /**
     * Return the first token of this subtree.
     * 
     * @return
     */
    public abstract Token firstToken();
    
    /**
     * Return the last token of this subtree.
     * 
     * @return
     */
    public abstract Token lastToken();
    
    /**
     * Return the first token after this subtree
     * 
     * @return
     */
    public Token nextToken() {
        return parent.nextOf(this).firstToken();
    }
    
    public Node nextSubtree() {
        return parent.nextOf(this);
    }
    
    protected void invalidateTextExtentCache() {

    }

    public void unsetAllFlag(int flag) {
        unsetFlag(flag);
    }
    
    public void setFlag(int flag) {
        flags |= flag;
    }
    
    public void unsetFlag(int flag) {
        flags &= ~flag;
    }

    public boolean isSet(int flag) {
        return (flags & flag) != 0;
    }

    public int getOffset() {
        if (textOffsetCache < 0)
            computeTextOffsetCache();
        return textOffsetCache;
    }
    
    protected void invalidateTextOffsetCache() {
        if (textOffsetCache >= 0)
            textOffsetCache = -1;
    }

    protected void computeTextOffsetCache() {
    	if (parent != null)
    		parent.computeChildTextOffset();
    }
    
    protected void setTextOffsetCache(int offset) {
    		textOffsetCache = offset;
	}
    
    public String getText() {
		return getText(0, length());
	}

    public abstract String getText(int offset, int length);

	public abstract void getText(StringBuffer buffer);

	public abstract int length();

    public abstract void insert(String text, int i, int length);
    
    public void replaceBy(Node node) {
        parent.replace(this, node);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        buffer.append(textOffsetCache);
        buffer.append(",");
        buffer.append(length());
        buffer.append(")");
        buffer.append("{ ");
        if (isSet(TEXT_CHANGE)) buffer.append("TEXT ");
        if (isSet(CHILD_CHANGE)) buffer.append("CHILD ");
        if (isSet(LOCAL_CHANGE)) buffer.append("LOCAL ");
        if (isSet(NESTED_CHANGE)) buffer.append("NESTED ");
        if (isSet(MARK)) buffer.append("MARK ");
        if (isSet(RELEXED)) buffer.append("RELEXED ");
        buffer.append("}");
        return buffer.toString();
    }

}
