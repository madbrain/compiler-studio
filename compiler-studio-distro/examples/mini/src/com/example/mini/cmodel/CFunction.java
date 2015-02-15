package com.example.mini.cmodel;

import java.util.ArrayList;
import java.util.List;



public class CFunction {

	protected String name;
	protected boolean used;
	protected boolean isResolved;
	protected List<CVariable> arguments = new ArrayList<CVariable>();
	protected List<CStatement> statements = new ArrayList<CStatement>();
	protected MiniType returnType;
	protected List<CVariable> declarations = new ArrayList<CVariable>();

	public CFunction(String name) {
		this.name = name;
		this.returnType = MiniType.VOID;
	}

	public void markUsed() {
		this.used = true;
	}

	public void addArgument(String argName, MiniType type) {
		this.arguments.add(new CVariable(argName, type));
	}

	public List<CVariable> getArguments() {
		return arguments;
	}
	
	public String getName() {
		return name;
	}

	public boolean isResolved() {
		return isResolved;
	}

	public void markResolved() {
		isResolved = true;
	}

	public void addStatement(CStatement statement) {
		this.statements.add(statement);
	}
	
	public List<CStatement> getStatements() {
		return statements;
	}

	public void setType(MiniType type) {
		this.returnType = type;
	}

	public MiniType getReturnType() {
		return returnType;
	}

	public boolean isUsed() {
		return used;
	}

	public void addDeclarations(List<CVariable> declarations) {
		for (CVariable decl : declarations) {
			if (! arguments.contains(decl)) {
				this.declarations.add(decl);
			}
		}
	}
	
	public List<CVariable> getDeclarations() {
		return declarations;
	}

}
