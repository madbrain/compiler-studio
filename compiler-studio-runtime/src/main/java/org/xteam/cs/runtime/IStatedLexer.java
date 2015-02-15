package org.xteam.cs.runtime;


public interface IStatedLexer extends ILexer {
    
    int initialState();
    
    int getState();

    void setState(int state);
    
	//void setInput(ILexerInput input);
    
}
