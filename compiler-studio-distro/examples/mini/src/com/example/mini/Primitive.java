package com.example.mini;

import com.example.mini.cmodel.CFunction;
import com.example.mini.cmodel.MiniType;

public class Primitive extends CFunction {

	public Primitive(String name, MiniType returnType, MiniType...argTypes) {
		super(name);
		int index = 0;
		this.returnType = returnType;
		for (MiniType type : argTypes) {
			addArgument("a" + index++, type);
		}
		markResolved();
	}

}
