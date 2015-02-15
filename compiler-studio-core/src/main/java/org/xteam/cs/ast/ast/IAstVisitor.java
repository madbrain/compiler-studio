

package org.xteam.cs.ast.ast;
 

public interface IAstVisitor {

	void visitAstFile(AstFileAst aAstFile);
	void visitNode(NodeAst aNode);
	void visitAbstractFlag(AbstractFlagAst aAbstractFlag);
	void visitIdent(IdentAst aIdent);
	void visitNodeItem(NodeItemAst nodeItemAst);
	void visitSimpleType(SimpleTypeAst simpleTypeAst);
	void visitRepeatableType(RepeatableTypeAst repeatableTypeAst);
	
}