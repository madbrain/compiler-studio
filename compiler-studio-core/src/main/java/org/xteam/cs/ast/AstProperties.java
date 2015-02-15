package org.xteam.cs.ast;

import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.Group;
import org.xteam.cs.model.Property;

public class AstProperties extends BaseProperties {

	@Group(id="generator", display="Generator Options")
	@Property(display="AST Subpackage")
	public String astPackage = "ast";
	
	@Group(id="generator")
	@Property(display="AST Node Class")
	public String astNodeClass = "org.xteam.cs.runtime.AstNode";
	
	@Group(id="generator")
	@Property(display="AST List Class")
	public String astListClass = "org.xteam.cs.runtime.AstList";
	
}
