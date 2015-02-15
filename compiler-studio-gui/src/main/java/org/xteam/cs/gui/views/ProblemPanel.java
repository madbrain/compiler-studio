package org.xteam.cs.gui.views;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.xteam.cs.gui.CompilerStudio;
import org.xteam.cs.model.ErrorMark;
import org.xteam.cs.model.IProjectListener;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectEvent;

public class ProblemPanel extends JPanel {

	private static final long serialVersionUID = -5090279819785466378L;

	public ProblemPanel(Project project, CompilerStudio compilerStudio) {
		setLayout(new BorderLayout());
		JTable table = new JTable(new ProjectProblemAdapter(project));
		add(new JScrollPane(table), BorderLayout.CENTER);
	}
	
	private static class ProjectProblemAdapter implements TableModel, IProjectListener {

		private List<TableModelListener> listeners = new ArrayList<TableModelListener>(); 
		private Project project;
		private List<ErrorMark> marks;

		public ProjectProblemAdapter(Project project) {
			this.project = project;
			this.project.addProjectListener(this);
			marks = project.getMarks(ErrorMark.class);
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
			listeners.add(l);
		}
		
		@Override
		public void removeTableModelListener(TableModelListener l) {
			listeners.remove(l);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return String.class;
			if (columnIndex == 1)
				return String.class;
			return null;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Resource";
			}
			if (columnIndex == 1) {
				return "Message";
			}
			return null;
		}

		@Override
		public int getRowCount() {
			return marks.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return marks.get(rowIndex).getResource().getName();
			if (columnIndex == 1)
				return marks.get(rowIndex).getMessage();
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			
		}

		@Override
		public void projectChanged(ProjectEvent event) {
			TableModelEvent te = new TableModelEvent(this);
			for (TableModelListener l : listeners) {
				if (event.getKind() == ProjectEvent.END_BUILD) {
					marks = event.getProject().getMarks(ErrorMark.class);
					l.tableChanged(te);
				}
			}
		}
		
	}

}
