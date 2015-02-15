#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.xteam.cs.gui.syntax.AnnotableEditorPane;
import org.xteam.cs.gui.syntax.IResource;
import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.gui.syntax.SyntaxStyle;
import org.xteam.cs.runtime.DefaultToken;
import org.xteam.cs.runtime.IErrorReporter;
import org.xteam.cs.runtime.ILexer;
import org.xteam.cs.runtime.IToken;
import org.xteam.cs.runtime.ITokenFactory;
import org.xteam.cs.runtime.LRParser;
import org.xteam.cs.runtime.LexerTable;
import org.xteam.cs.runtime.Span;
import org.xteam.cs.runtime.TableBasedLexer;
import ${package}.ast.AddExpr;
import ${package}.ast.AssignmentStatement;
import ${package}.ast.BinaryExpr;
import ${package}.ast.DefaultExprVisitor;
import ${package}.ast.DivExpr;
import ${package}.ast.Expr;
import ${package}.ast.ExprStatement;
import ${package}.ast.File;
import ${package}.ast.IntegerExpr;
import ${package}.ast.MulExpr;
import ${package}.ast.NegateExpr;
import ${package}.ast.SubExpr;
import ${package}.ast.VariableExpr;
import ${package}.lexer.ExprLexerFactory;
import ${package}.lexer.ExprSyntaxHelper;
import ${package}.lexer.IExprTokens;
import ${package}.parser.ExprParserFactory;
import ${package}.parser.ExprRuleReducer;

public class ExprInterpreter extends JFrame {
	
	private static final long serialVersionUID = 8967952526287231347L;
	
	private static String exampleContent = "v := 10${symbol_escape}n40 * (v + 2)";
	
	public static void main(String[] args) throws IOException {
		new ExprInterpreter().setVisible(true);
	}

	private StyleManager styleManager;
	private Resource resource;
	private AnnotableEditorPane editor;
	private JTextArea text;
	
	public ExprInterpreter() {
		setTitle("Expression Interpreter GUI");
		setSize(500, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		styleManager = new StyleManager(DEFAULT);
		styleManager.set(DEFAULT, new SyntaxStyle(Color.BLACK));
		styleManager.set(IDENTIFIER, new SyntaxStyle(new Color(0, 128, 0), Font.BOLD));
		styleManager.set(INTEGER, new SyntaxStyle(new Color(128, 30, 0)));
		
		text = new JTextArea();
		text.setEditable(false);
		
		resource = new Resource();
		editor = new AnnotableEditorPane(resource, styleManager);
		editor.setText(exampleContent);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setDividerLocation(250);
		split.add(editor);
		split.add(text);
		
		getContentPane().add(split, BorderLayout.CENTER);
		
	}
	
	private class Resource implements IResource {
		
		private ILexer lexer;
		private File file;

		public Resource() {
			try {
				lexer = new TableBasedLexer(new LexerTable(
						ExprLexerFactory.class, "Expr.ltb"),
						null,
						new AstTokenMapper());
			} catch (IOException e) {
			}
		}

		@Override
		public String getType() {
			return "text/ast";
		}

		@Override
		public ILexer getLexer() {
			return lexer;
		}

		@Override
		public String getContents() {
			return "";
		}

		@Override
		public void markDirty() {
			
		}

		@Override
		public void analyse(String text, IErrorReporter reporter) {
			try {
				ILexer lexer = ExprLexerFactory.createLexer(null);
				lexer.setErrorReporter(reporter);
				lexer.setInput(new StringReader(text));
				lexer.skipComments(true);
				LRParser parser = ExprParserFactory.createParser(lexer,
					new ExprRuleReducer(null), new ExprSyntaxHelper(), reporter);
				file = (File) parser.parse();
				if (! reporter.hasErrors()) {
					final ExprSemantic semantic = new ExprSemantic(reporter);
					semantic.analyse(file);
					
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setValues(semantic.getValues());
						}
					});
					
				}
			} catch (IOException e) {
			}
		}
		
	}
	
	private void setValues(List<Integer> values) {
		StringBuffer buffer = new StringBuffer();
		for (Integer value : values) {
			buffer.append(value).append("${symbol_escape}n");
		}
		text.setText(buffer.toString());
	}
	
	public static final int DEFAULT    = 0;
	public static final int IDENTIFIER = 1;
	public static final int INTEGER    = 2;
	
	private class AstTokenMapper implements ITokenFactory {

		@Override
		public IToken newToken(int type, Span span, Object content) {
			if (type == IExprTokens.IDENTIFIER) {
				return new DefaultToken(IDENTIFIER, span.start(), span.length());
			}
			if (type == IExprTokens.INTEGER) {
				return new DefaultToken(INTEGER, span.start(), span.length());
			}
			if (type == IExprTokens._EOF_) {
				return null;
			}
			return new DefaultToken(DEFAULT, span.start(), span.length());
		}

	}
	
	private static class ExprSemantic extends DefaultExprVisitor {

		public interface IBinary<T> {

			T evaluate(T left, T right);

		}

		private IErrorReporter reporter;
		private Map<String, Integer> context = new HashMap<String, Integer>();
		private Stack<Integer> stack = new Stack<Integer>();
		private List<Integer> results = new ArrayList<Integer>();

		public ExprSemantic(IErrorReporter reporter) {
			this.reporter = reporter;
		}

		public List<Integer> getValues() {
			return results;
		}

		public void analyse(File file) {
			context.clear();
			results .clear();
			file.visit(this);
		}
		
		@Override
		public void visitAssignmentStatement(AssignmentStatement statement) {
			stack.clear();
			Integer value = evaluate(statement.getExpr());
			if (value != null) {
				context.put(statement.getVariable().getName(), value);
			}
		}
		
		@Override
		public void visitExprStatement(ExprStatement statement) {
			stack.clear();
			Integer value = evaluate(statement.getExpr());
			if (value != null) {
				results.add(value);
			}
		}

		private Integer evaluate(Expr expr) {
			expr.visit(this);
			return stack.pop();
		}
		
		@Override
		public void visitAddExpr(AddExpr expr) {
			doBinary(expr, new IBinary<Integer>() {
				@Override
				public Integer evaluate(Integer left, Integer right) {
					return left + right;
				}
			});
		}
		
		@Override
		public void visitSubExpr(SubExpr expr) {
			doBinary(expr, new IBinary<Integer>() {
				@Override
				public Integer evaluate(Integer left, Integer right) {
					return left - right;
				}
			});
		}
		
		@Override
		public void visitMulExpr(MulExpr expr) {
			doBinary(expr, new IBinary<Integer>() {
				@Override
				public Integer evaluate(Integer left, Integer right) {
					return left * right;
				}
			});
		}
		
		@Override
		public void visitDivExpr(final DivExpr expr) {
			doBinary(expr, new IBinary<Integer>() {
				@Override
				public Integer evaluate(Integer left, Integer right) {
					if (right == 0) {
						reporter.reportError(IErrorReporter.ERROR, expr.span(), "division by zero");
						return null;
					}
					return left / right;
				}
			});
		}
		
		@Override
		public void visitNegateExpr(NegateExpr expr) {
			Integer e = evaluate(expr.getExpr());
			if (e != null) {
				stack.push(-e);
			} else {
				stack.push(null);
			}
		}
		
		@Override
		public void visitIntegerExpr(IntegerExpr expr) {
			stack.push(Integer.parseInt(expr.getValue()));
		}
		
		@Override
		public void visitVariableExpr(VariableExpr expr) {
			Integer value = context.get(expr.getName());
			if (value == null) {
				reporter.reportError(IErrorReporter.ERROR, expr.span(), "unknown variable '"
						+ expr.getName() + "'");
			}
			stack.push(value);
		}

		private void doBinary(BinaryExpr expr, IBinary<Integer> binary) {
			Integer left = evaluate(expr.getLeft());
			Integer right = evaluate(expr.getRight());
			if (left != null && right != null) {
				stack.push(binary.evaluate(left, right));
			} else {
				stack.push(null);
			}
		}

	}
	
}
