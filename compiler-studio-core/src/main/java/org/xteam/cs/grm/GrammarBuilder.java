package org.xteam.cs.grm;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xteam.cs.ast.AstFile;
import org.xteam.cs.ast.AstProperties;
import org.xteam.cs.ast.model.AstField;
import org.xteam.cs.ast.model.AstModel;
import org.xteam.cs.ast.model.AstNode;
import org.xteam.cs.generator.AbstractBuilder;
import org.xteam.cs.grm.ast.GrammarFileAst;
import org.xteam.cs.grm.build.ParserBuild;
import org.xteam.cs.grm.build.ParserBuilder;
import org.xteam.cs.grm.build.ParserTableFileEmiter;
import org.xteam.cs.grm.model.Action;
import org.xteam.cs.grm.model.AstListAction;
import org.xteam.cs.grm.model.AstNodeAction;
import org.xteam.cs.grm.model.Binding;
import org.xteam.cs.grm.model.Grammar;
import org.xteam.cs.grm.model.LexerStateAction;
import org.xteam.cs.grm.model.Rule;
import org.xteam.cs.grm.model.Symbol;
import org.xteam.cs.grm.model.Terminal;
import org.xteam.cs.lex.LexerFile;
import org.xteam.cs.lex.LexerProperties;
import org.xteam.cs.lex.build.LexerBuild;
import org.xteam.cs.model.BuilderErrorReporter;
import org.xteam.cs.model.ErrorMark;
import org.xteam.cs.model.ParseError;
import org.xteam.cs.model.ProjectProperties;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.model.SubProgressMonitor;
import org.xteam.cs.types.ListType;
import org.xteam.cs.types.NodeType;
import org.xteam.cs.types.PrimitiveType;
import org.xteam.cs.types.Type;


public class GrammarBuilder extends AbstractBuilder {

	@Override
	public void build() {
		for (GrammarFile file : project.getResources(GrammarFile.class)) {
			build(file);
		}
	}

	private void build(GrammarFile file) {
		file.clearMark(ErrorMark.class);
		GrammarParser parser = new GrammarParser();
		BuilderErrorReporter reporter = new BuilderErrorReporter(file);
		parser.setErrorReporter(reporter);
		parser.setInput(new StringReader(file.getContents()));
		try {
			file.setParserModel(null);
			monitor.beginTask("Compiling " + file, 3);
			monitor.subTask("Parsing");
			GrammarFileAst ast = parser.parse();
			monitor.worked(1);
			monitor.subTask("Analysing");
			GrammarSemantic semantic = new GrammarSemantic(reporter);
			Grammar grammar = semantic.analyse(ast, new EvaluationContext(file.getProject()));
			monitor.worked(1);
			if (reporter.hasErrors())
				return;
			ParserBuilder builder = new ParserBuilder(reporter);
			ParserBuild build = builder.run(grammar,
					project,
					(GrammarProperties)file.getProperties(), semantic.getLexerBuild(),
					semantic.getAstModel(),
					new SubProgressMonitor(monitor));
			monitor.done();
			if (reporter.hasErrors())
				return;
			file.setParserModel(build);
		} catch (ParseError e) {
		} catch (IOException e) {
		}		
	}

	@Override
	public void generate() {
		for (GrammarFile file : project.getResources(GrammarFile.class)) {
			generate(file);
		}
	}

	private void generate(GrammarFile file) {
		ParserBuild build = file.getParserModel();
		
		if (build == null)
			return;
		
		ProjectProperties projectProperties = (ProjectProperties)project.getProperties();
		GrammarProperties grammarProperties = (GrammarProperties) file.getProperties();
		
		LexerProperties lexerProperties = null;
		if (build.getLexerBuild() != null) {
			lexerProperties = (LexerProperties) findLexerBuildFile(build.getLexerBuild()).getProperties();
		}
		AstProperties astProperties = null;
		if (build.getAstModel() != null) {
			astProperties = (AstProperties) findAstModelFile(build.getAstModel()).getProperties();
		}
		
		
		File currentFile = makeFilename(projectProperties, grammarProperties.grammarPackage,
				".", false);
		currentFile.mkdirs();
		
		currentFile = makeFilename(projectProperties, grammarProperties.grammarPackage,
				build.getName() + ".gtb", true);
		currentFile.getParentFile().mkdirs();
		
		int amountOfWork = 2;
		boolean generateReducer = false;
		boolean generateTokens = false;
		if (build.isValidForAST()) {
			amountOfWork++;
			generateReducer = true;
		}
		if (build.getMapping().getGrammarTokens().size() > 0) {
			amountOfWork++;
			generateTokens = true;
		}
		
		monitor.beginTask("Generating for " + file, amountOfWork);
		
		monitor.subTask("Generating " + currentFile.getName());
		ParserTableFileEmiter emiter = new ParserTableFileEmiter();
		emiter.emit(build.getTables(), currentFile);
		monitor.worked(1);
		
		try {
			Properties prop = new Properties();
			prop.setProperty("resource.loader", "defaultLoader");
			prop.setProperty("defaultLoader.resource.loader.class",
							"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			Velocity.init(prop);
			
			VelocityContext context = new VelocityContext();
			context.put("grmGen", new GrmGenModel(build, projectProperties,
					grammarProperties,
					lexerProperties,
					astProperties));
			
			currentFile = makeFilename(projectProperties, grammarProperties.grammarPackage,
					build.getName() + "ParserFactory.java", false);
			generate(context, currentFile, "/resources/templates/grm/grm-factory.vm");
			
			if (generateReducer) {
				currentFile = makeFilename(projectProperties,
						grammarProperties.grammarPackage, build.getName() + "RuleReducer.java", false);
				generate(context, currentFile, "/resources/templates/grm/grm-rule-reducer.vm");
			}
			
			if (generateTokens) {
				currentFile = makeFilename(projectProperties,
						grammarProperties.grammarPackage, build.getName() + "SyntaxHelper.java", false);
				generate(context, currentFile, "/resources/templates/grm/grm-syntax-helper.vm");
				
				currentFile = makeFilename(projectProperties,
						grammarProperties.grammarPackage, "I" + build.getName() + "GrammarTokens.java", false);
				generate(context, currentFile, "/resources/templates/grm/grm-tokens.vm");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ProjectResource findAstModelFile(AstModel astModel) {
		for (AstFile file : project.getResources(AstFile.class)) {
			if (file.getAstModel() == astModel) {
				return file;
			}
		}
		return null;
	}

	private LexerFile findLexerBuildFile(LexerBuild lexerBuild) {
		for (LexerFile file : project.getResources(LexerFile.class)) {
			if (file.getBuild() == lexerBuild) {
				return file;
			}
		}
		return null;
	}

	public static class GrmGenModel {

		private ProjectProperties projectProperties;
		private GrammarProperties grammarProperties;
		private ParserBuild model;
		private LexerProperties lexerProperties;
		private AstProperties astProperties;

		public GrmGenModel(ParserBuild build,
				ProjectProperties projectProperties,
				GrammarProperties grammarProperties, LexerProperties lexerProperties, AstProperties astProperties) {
			this.model = build;
			this.projectProperties = projectProperties;
			this.grammarProperties = grammarProperties;
			this.lexerProperties = lexerProperties;
			this.astProperties = astProperties;
		}
		
		public String getPackage() {
			return projectProperties.mainPackage + "." + grammarProperties.grammarPackage;
		}
		
		public String getAstPackage() {
			return projectProperties.mainPackage + "." + astProperties.astPackage;
		}
		
		public ParserBuild getModel() {
			return model;
		}
		
		public List<Terminal> getTokens() {
			return model.getMapping().getGrammarTokens();
		}
		
		public int getTokenValue(Terminal terminal) {
			return model.getMapping().getSymbols().get(terminal);
		}
		
		public List<Rule> getRules() {
			return new ArrayList<Rule>(model.getMapping().getRules().keySet());
		}
		
		public int getRuleValue(Rule rule) {
			return model.getMapping().getRules().get(rule);
		}
		
		public boolean isListAction(Action action) {
			return action instanceof AstListAction;
		}
		
		public boolean isNodeAction(Action action) {
			return action instanceof AstNodeAction;
		}
		
		public boolean isLexerAction(Action action) {
			return action instanceof LexerStateAction;
		}
		
		public String buildSpan(Rule rule) {
			StringBuffer buffer = new StringBuffer();
			List<Symbol> rhs = rule.getRhs();
			if (rhs.isEmpty())
				return "Span.NULL";
			int spanStart = rhs.size()-1;
			int spanEnd = 0;
			for (int i = 0; i < rhs.size(); ++i) {
				if (rhs.get(i).isConstant()) {
					spanStart = i;
					break;
				}
			}
			for (int i = rhs.size()-1; i >= 0; --i) {
				if (rhs.get(i).isConstant()) {
					spanEnd = i;
					break;
				}
			}
			if (spanStart != 0 || spanEnd != rhs.size() - 1) {
				buffer.append("span(new AstNode[] {");
				for (int i = 0; i < spanStart; ++i) {
					if (i != 0)
						buffer.append(", ");
					buffer.append("((AstNode)values[").append(i).append("])");
				}
				buffer.append("}, ");
				if (spanStart <= spanEnd) {
					buffer.append(getElement(spanStart, rhs)).append(".start(), ");
					buffer.append(getElement(spanEnd, rhs)).append(".end(), ");
					buffer.append("new AstNode[] {");
					for (int i = spanEnd + 1; i < rhs.size(); ++i) {
						if (i != spanEnd + 1)
							buffer.append(", ");
						buffer.append("((AstNode)values[").append(i).append("])");
					}
					buffer.append("})");
				} else {
					buffer.append("-1, -1, new AstNode[] {})");
				}
			} else {
				buffer.append("span(").append(getElement(0, rhs)).append(".start(), ")
					.append(getElement(spanEnd, rhs)).append(".end())");
			}
			return buffer.toString();
		}
		
		private String getElement(int index, List<Symbol> rhs) {
			if (rhs.get(index).isTerminal()) {
				return "((IToken)values[" + index + "])";
			}
			return "((AstNode)values[" + index + "]).span()";
		}
		
		public String buildArgs(Rule rule) {
			AstNodeAction action = (AstNodeAction) rule.getAction();
			
			StringBuffer buffer = new StringBuffer();
			AstNode node = action.getNode();
			
			for (AstField field : node.getAllFields()) {
				int index = -1;
				String value = null;
				for (Binding b : action.getBindings()) {
					if (b.getField() == field) {
						index = b.getIndex();
						value = b.getValue();
						break;
					}
				}
				if (value != null) {
					buffer.append(", \"" + value.replace("\"", "\\\"") + "\"");
				} else if (index < 0)
					buffer.append(", null");
				else {
					buffer.append(", (").append(typeString(field.getType()))
						.append(")").append(getElementValue(index, rule.getRhs()));
				}
			}
			return buffer.toString();
		}
		
		private String getElementValue(int index, List<Symbol> rhs) {
			if (rhs.get(index).isTerminal()) {
				return "((IToken)values[" + index + "]).value()";
			}
			return "values[" + index + "]";
		}
		
		public String buildListAction(Rule rule) {
			AstListAction action = (AstListAction) rule.getAction();
			String type = typeString(((ListType) rule.getLhs().getType()).getElementType());
			if (action.getListIndex() >= 0) {
				return "AstList."
					+ (action.getListIndex() < action.getElementIndex() ? "add" : "prepend")
					+ "((AstList<"+type+">)values["+action.getListIndex()+"], ("+type+")values["+action.getElementIndex()+"])";
			}
			if (action.getElementIndex() < 0) {
				return "new AstList<"+type+">()";
			}
			return "new AstList<"+type+">(("+type+")values["+action.getElementIndex()+"])";
		}
		
		public String buildPropagateAction(int index) {
			if (index < 0)
				return "null";
			return "values["+index+"]";
		}
		
		public String typeString(Type type) {
			if (type.isPrimitive()) {
				PrimitiveType pt = (PrimitiveType) type;
				if (pt.getType() == PrimitiveType.STRING)
					return "String";
				if (pt.getType() == PrimitiveType.INT)
					return "Integer";
				return type.toString();
			}
			if (type.isRepeatable()) {
				return astProperties.astListClass + "<" + typeString(((ListType)type).getElementType()) + ">";
			}
			return ((NodeType)type).getAstNode().getName();
		}
		
	}
	
}
