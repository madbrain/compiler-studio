package com.example.mini;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mini.cmodel.CVariable;
import com.example.mini.cmodel.MiniType;

public class Scope {

	private Map<String, CVariable> vars = new HashMap<String, CVariable>();
	private Map<String, CVariable> declarations = new HashMap<String, CVariable>();
	
	public Scope(List<CVariable> arguments) {
		for (CVariable var : arguments) {
			vars.put(var.getName(), var);
		}
	}

	public CVariable get(String name) {
		return vars.get(name);
	}

	public CVariable put(String name, MiniType type) {
		String newName = name;
		if (vars.containsKey(name) && vars.get(name).getType() != type) {
			int index = 0;
			do {
				newName = name + index++;
			} while (declarations.containsKey(newName));
			CVariable var = vars.get(name);
			declarations.put(var.getName(), var);
		}
		CVariable var = new CVariable(newName, type);
		vars.put(name, var);
		return var;
	}

	public List<CVariable> getDeclarations() {
		List<CVariable> decls = new ArrayList<CVariable>(vars.values());
		decls.addAll(declarations.values());
		return decls;
	}

}
