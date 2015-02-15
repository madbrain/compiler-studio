package org.xteam.cs.ast.model;

import java.util.ArrayList;
import java.util.List;

public class AstNode {
	
	
	private String name;
	private boolean isAbstract;
	private AstNode superNode;
	private List<AstField> fields = new ArrayList<AstField>();
	
	public AstNode(String name, boolean isAbstract) {
		this.name = name;
		this.isAbstract = isAbstract;
	}

	public String getName() {
		return name;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public AstNode getSuper() {
		return superNode;
	}

	public void setSuper(AstNode superNode) {
		this.superNode = superNode;
	}

	public List<AstField> getFields() {
		return fields;
	}
	
	public List<AstField> getAllFields() {
		List<AstField> fields;
		if (superNode != null)
			fields = superNode.getAllFields();
		else
			fields = new ArrayList<AstField>();
		fields.addAll(this.fields);
		return fields;
	}

	public void add(AstField astField) {
		fields.add(astField);
	}

	public AstField getField(String fieldName) {
		for (AstField f : fields) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		if (superNode != null)
			return superNode.getField(fieldName);
		return null;
	}

}
