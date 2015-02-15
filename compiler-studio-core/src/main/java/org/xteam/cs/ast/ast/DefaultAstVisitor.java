package org.xteam.cs.ast.ast;

public class DefaultAstVisitor implements IAstVisitor {
	
	@Override
	public void visitAstFile(AstFileAst aAstFile) {
		for (NodeAst node : aAstFile.getNodes()) {
			node.visit(this);
		}
	}

	@Override
	public void visitAbstractFlag(AbstractFlagAst aAbstractFlag) {
		
	}
	
	@Override
	public void visitNode(NodeAst aNode) {
		for (NodeItemAst item : aNode.getItems()) {
			item.visit(this);
		}
	}

	@Override
	public void visitNodeItem(NodeItemAst nodeItemAst) {
		nodeItemAst.getType().visit(this);
	}

	@Override
	public void visitRepeatableType(RepeatableTypeAst repeatableTypeAst) {
		repeatableTypeAst.getBase().visit(this);
	}

	@Override
	public void visitSimpleType(SimpleTypeAst simpleTypeAst) {
		
	}
	
	@Override
	public void visitIdent(IdentAst aIdent) {
		
	}

}
