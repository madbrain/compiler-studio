package com.example.mini.cmodel;

public class MiniType {

	private static final int STRING_ID  = 0;
	private static final int INTEGER_ID = 1;
	private static final int VOID_ID    = 3;
	
	public static final MiniType STRING = new MiniType(STRING_ID);
	public static final MiniType INTEGER = new MiniType(INTEGER_ID);
	public static final MiniType VOID = new MiniType(VOID_ID);

	private int id;
	
	private MiniType(int id) {
		this.id = id;
	}

}
