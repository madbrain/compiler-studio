package org.xteam.cs.ast;

import java.util.Stack;

import org.xteam.cs.ast.ast.AstFileAst;
import org.xteam.cs.ast.ast.DefaultAstVisitor;
import org.xteam.cs.ast.ast.NodeAst;
import org.xteam.cs.ast.ast.NodeItemAst;
import org.xteam.cs.ast.ast.RepeatableTypeAst;
import org.xteam.cs.ast.ast.SimpleTypeAst;
import org.xteam.cs.ast.ast.TypeAst;
import org.xteam.cs.ast.model.AstField;
import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.ast.model.AstNode;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.semantic.Error;
import org.xteam.cs.runtime.semantic.Ok;
import org.xteam.cs.runtime.semantic.Result;
import org.xteam.cs.types.ListType;
import org.xteam.cs.types.NodeType;
import org.xteam.cs.types.PrimitiveType;
import org.xteam.cs.types.Type;

public class AstSemantic {

	private IErrorReporter reporter;

	public AstSemantic(IErrorReporter reporter) {
		this.reporter = reporter;
	}

	public AstModel analyse(AstFileAst ast) {
		AstModel model = new AstModel(ast.getName().getValue());
		for (NodeAst nodeAst : ast.getNodes()) {
			String name = nodeAst.getName().getValue();
			boolean isAbstract = nodeAst.getAbstractFlag() != null;
			if (model.getNode(name) != null) {
				reporter.reportError(IErrorReporter.ERROR,
						nodeAst.getName().span(),
						"node '" + name + "' already defined");
			} else {
				AstNode node = new AstNode(name, isAbstract);
				model.add(node);
			}
		}
		for (NodeAst nodeAst : ast.getNodes()) {
			String name = nodeAst.getName().getValue();
			AstNode node = model.getNode(name);
			if (node != null) {
				if (nodeAst.getSuperNode() != null) {
					String superName = nodeAst.getSuperNode().getValue();
					AstNode superNode = model.getNode(superName);
					if (superNode == null) {
						reporter.reportError(IErrorReporter.ERROR,
								nodeAst.getSuperNode().span(),
								"super node '" + name + "' is not defined");
					} else {
						node.setSuper(superNode);
					}
				}
				for (NodeItemAst item : nodeAst.getItems()) {
					String fieldName = item.getName().getValue();
					if (node.getField(fieldName) != null) {
						reporter.reportError(IErrorReporter.ERROR,
								item.getName().span(),
								"field '" + fieldName + " ' in node ' "
									+ name + "' already defined");
					} else {
						Type type = makeType(model, item.getType());
						if (type != null) {
							node.add(new AstField(fieldName, type));
						}
					}
				}
			}
		}
		return model;
	}

	private Type makeType(AstModel model, TypeAst type) {
		TypeMaker typeMaker = new TypeMaker(model);
		type.visit(typeMaker);
		return typeMaker.getType();
	}

	private class TypeMaker extends DefaultAstVisitor {
		
		
		Stack<Result<Type>> types = new Stack<Result<Type>>();
		AstModel model;

		public TypeMaker(AstModel model) {
			
			this.model = model;
		}
		
		public Type getType() {
			 return types.peek().isError() ? null : types.peek().value();
		}
		
		@Override
		public void visitRepeatableType(RepeatableTypeAst repeatableTypeAst) {
			repeatableTypeAst.getBase().visit(this);
			if (types.peek().isError())
				return;
			types.push(new Ok<Type>(new ListType(repeatableTypeAst.span(), types.pop().value())));
		}

		@Override
		public void visitSimpleType(SimpleTypeAst simpleTypeAst) {
			String name = simpleTypeAst.getName();
			if (PrimitiveType.isPrimitive(name))
				types.push(new Ok<Type>(new PrimitiveType(simpleTypeAst.span(), PrimitiveType.get(name))));
			else {
				AstNode ref = model.getNode(name);
				if (ref == null) {
					reporter.reportError(IErrorReporter.ERROR,
							simpleTypeAst.span(),
							"node '" + name + "' is not defined");
					types.push(new Error<Type>());
				} else {
					types.push(new Ok<Type>(new NodeType(simpleTypeAst.span(), ref)));
				}
			}
		}
		
	}
}
