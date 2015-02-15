package org.xteam.cs.gui.grm;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.xteam.cs.gui.syntax.AnnotableEditorPane;
import org.xteam.cs.gui.syntax.IResource;
import org.xteam.cs.gui.syntax.Reconciler;
import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.gui.views.Editor;
import org.xteam.cs.gui.views.ResourceAdapter;

public class TestASTParserEditor extends Editor {
	
	private static final long serialVersionUID = -8228319810735580944L;
	
	private AnnotableEditorPane editor;
	private JTree tree;
	private ParseTreeModel treeModel;

	public TestASTParserEditor(TestAstParserResource resource, StyleManager styleManager) {
		super(resource);
		treeModel = new ParseTreeModel(resource);
		this.tree = new JTree(treeModel);
		this.editor = new EditorPane(new ResourceAdapter(resource), styleManager);
		final JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(editor),
				new JScrollPane(tree));
		add(split, BorderLayout.CENTER);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				split.setDividerLocation(0.5);
			}
		});
	}
	
	private class EditorPane extends AnnotableEditorPane {

		public EditorPane(IResource resource, StyleManager styleManager) {
			super(resource, styleManager);
		}
		
		@Override
		protected Reconciler createReconciler() {
			return new ParserReconciler(getDocument(), resource, getHighlighter());
		}
		
	}
	
	private class ParserReconciler extends Reconciler {

		public ParserReconciler(Document document, IResource resource,
				Highlighter highlighter) {
			super(document, resource, highlighter);
		}
		
		@Override
		public void doReconcile() {
			super.doReconcile();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					treeModel.refresh();
				}
			});
			
		}
		
	}
	
	private static class ParseTreeModel implements TreeModel {

		private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
		private TestAstParserResource resource;

		public ParseTreeModel(TestAstParserResource resource) {
			this.resource = resource;
		}

		void refresh() {
			if (resource.getRoot() != null) {
				TreeModelEvent e = new TreeModelEvent(this, new TreePath(
						resource.getRoot()));
				for (TreeModelListener l : listeners) {
					l.treeStructureChanged(e);
				}
			}
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listeners.add(l);
		}
		
		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listeners .remove(l);
		}
		
		@Override
		public Object getRoot() {
			return resource.getRoot();
		}
		
		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof GenericAST) {
				GenericAST rn = (GenericAST) parent;
				return rn.size();
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof GenericAST) {
				GenericAST rn = (GenericAST) parent;
				return rn.getChildren().indexOf(child);
			}
			return 0;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof GenericAST) {
				GenericAST rn = (GenericAST) parent;
				return rn.getChildren().get(index);
			}
			return null;
		}

		@Override
		public boolean isLeaf(Object node) {
			return node instanceof ASTTokenNode;
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			
		}
		
	}
	
	@Override
	public void close() {
		this.editor.close();
	}
}
