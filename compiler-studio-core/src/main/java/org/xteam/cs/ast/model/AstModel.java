package org.xteam.cs.ast.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstModel {
	
	private String name;
	private Map<String, AstNode> nodes = new HashMap<String, AstNode>();
	
	public AstModel(String name) {
		this.name = name;
	}

	public AstNode getNode(String name) {
		return nodes.get(name);
	}

	public void add(AstNode node) {
		nodes.put(node.getName(), node);
	}

	public String getName() {
		return name;
	}
	
	public List<AstNode> getNodes() {
		return new ArrayList<AstNode>(nodes.values());
	}
	
}
