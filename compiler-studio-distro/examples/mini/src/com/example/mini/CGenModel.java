package com.example.mini;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.example.mini.cmodel.CFunction;
import com.example.mini.cmodel.CStatement;
import com.example.mini.cmodel.MiniType;

public class CGenModel {

	private Map<String, CFunction> functions;
	private HashMap<String, CFunction> primitives;

	public CGenModel(Map<String, CFunction> cfuntions, HashMap<String, CFunction> primitives) {
		this.functions = cfuntions;
		this.primitives = primitives;
	}
	
	public Collection<CFunction> getFunctions() {
		return functions.values();
	}
	
	public String makeType(MiniType t) {
		if (t == MiniType.INTEGER) {
			return "int";
		}
		if (t == MiniType.STRING) {
			return "char*";
		}
		if (t == MiniType.VOID) {
			return "void";
		}
		return "badType";
	}
	
	public boolean isUsed(String primitive) {
		return primitives.containsKey(primitive);
	}
	
	public String makeStatement(CStatement statement) {
		return new StatementPrinter().print(statement);
	}

}
