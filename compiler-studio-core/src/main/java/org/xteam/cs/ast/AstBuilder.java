package org.xteam.cs.ast;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xteam.cs.ast.ast.AstFileAst;
import org.xteam.cs.ast.model.AstField;
import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.ast.model.AstNode;
import org.xteam.cs.generator.AbstractBuilder;
import org.xteam.cs.model.BuilderErrorReporter;
import org.xteam.cs.model.ErrorMark;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.model.ProjectProperties;
import org.xteam.cs.types.ListType;
import org.xteam.cs.types.NodeType;
import org.xteam.cs.types.PrimitiveType;
import org.xteam.cs.types.Type;

public class AstBuilder extends AbstractBuilder {

	@Override
	public void build() {
		for (AstFile file : project.getResources(AstFile.class)) {
			build(file);
		}
	}

	private void build(AstFile file) {
		file.clearMark(ErrorMark.class);
		AstParser parser = new AstParser();
		BuilderErrorReporter reporter = new BuilderErrorReporter(file);
		parser.setErrorReporter(reporter);
		parser.setInput(new StringReader(file.getContents()));
		try {
			file.setAstModel(null);
			monitor.beginTask("Compiling " + file, 2);
			monitor.subTask("Parsing");
			AstFileAst ast = parser.parse();
			monitor.worked(1);
			monitor.subTask("Analysing");
			AstSemantic semantic = new AstSemantic(reporter);
			AstModel model = semantic.analyse(ast);
			monitor.worked(1);
			if (reporter.hasErrors())
				return;
			file.setAstModel(model);
		} catch (ParseError e) {
		} catch (IOException e) {
		}		
	}

	@Override
	public void generate() {
		for (AstFile file : project.getResources(AstFile.class)) {
			generate(file);
		}
	}

	private void generate(AstFile file) {
		
		AstModel model = file.getAstModel();
		ProjectProperties projectProperties = (ProjectProperties)project.getProperties();
		AstProperties astProperties = (AstProperties) file.getProperties();
		
		if (model == null)
			return;
		
		try {
			Properties prop = new Properties();
			prop.setProperty("resource.loader", "defaultLoader");
			prop.setProperty("defaultLoader.resource.loader.class",
							"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			Velocity.init(prop);
			
			monitor.beginTask("Generation for " + file, model.getNodes().size() + 2);
			
			File currentFile = makeFilename(projectProperties, astProperties.astPackage, ".", false);
			currentFile.mkdirs();

			VelocityContext context = new VelocityContext();
			context.put("astGen", new AstGenModel(model,
					projectProperties, astProperties));
			for (AstNode node : model.getNodes()) {
				currentFile = makeFilename(projectProperties, astProperties.astPackage,
						node.getName() + ".java", false);
				context.put("node", node);
				generate(context, currentFile, "/resources/templates/ast/ast-node.vm");
			}
			currentFile = makeFilename(projectProperties, astProperties.astPackage,
					"I" + model.getName() + "Visitor.java", false);
			generate(context, currentFile, "/resources/templates/ast/ast-visitor.vm");

			currentFile = makeFilename(projectProperties, astProperties.astPackage,
					"Default" + model.getName() + "Visitor.java", false);
			generate(context, currentFile, "/resources/templates/ast/ast-default-visitor.vm");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class AstGenModel {
		
		private AstProperties properties;
		private ProjectProperties projectProperties;
		private AstModel model;
		
		public AstGenModel(AstModel model,
				ProjectProperties projectProperties,
				AstProperties properties) {
			this.model = model;
			this.projectProperties = projectProperties;
			this.properties = properties;
		}

		public String getPackage() {
			return projectProperties.mainPackage + "." + properties.astPackage;
		}
		
		public AstModel getModel() {
			return model;
		}
		
		public String getAstNode() {
			return properties.astNodeClass;
		}
		
		public List<AstField> collectSuperFields(AstNode node) {
			if (node.getSuper() != null)
				return node.getSuper().getAllFields();
			return new ArrayList<AstField>();
		}
		
		public String fieldType(Type type) {
			if (type.isPrimitive()) {
				PrimitiveType pt = (PrimitiveType) type;
				if (pt.getType() == PrimitiveType.INT)
					return "int";
				if (pt.getType() == PrimitiveType.STRING)
					return "String";
				return type.toString();
			}
			if (type.isRepeatable()) {
				return properties.astListClass + "<" + fieldType(((ListType)type).getElementType()) + ">";
			}
			return ((NodeType)type).getAstNode().getName();
		}
		
		public String firstUpper(String str) {
			return Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
		
	}

}
