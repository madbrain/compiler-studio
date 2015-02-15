package org.xteam.cs.gui.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.xteam.cs.gui.IWorkbench;
import org.xteam.cs.jobs.IJobListener;
import org.xteam.cs.jobs.JobChangedEvent;
import org.xteam.cs.jobs.JobManager;
import org.xteam.cs.jobs.JobManager.Job;

public class JobsPanel extends JPanel {

	private static final long serialVersionUID = -5618791942607593755L;
	
	private JList list;
	
	public JobsPanel(JobManager jobManager, IWorkbench workbench) {
		
		setLayout(new BorderLayout());
		
		list = new JList(new JobListModel(jobManager));
		list.setCellRenderer(new JobCell());
		add(new JScrollPane(list), BorderLayout.CENTER);
	}
	
	private class JobListModel implements ListModel, IJobListener {

		private List<ListDataListener> listeners = new ArrayList<ListDataListener>();
		
		private JobManager jobManager;

		public JobListModel(JobManager jobManager) {
			this.jobManager = jobManager;
			this.jobManager.addJobListener(this);
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}
		
		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}

		@Override
		public Object getElementAt(int index) {
			return jobManager.getJobs().get(index);
		}

		@Override
		public int getSize() {
			return jobManager.getJobs().size();
		}

		@Override
		public void changed(final JobChangedEvent event) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					int index = jobManager.getJobs().indexOf(event.getJob());
					int kind = event.getKind() == JobChangedEvent.DONE ?
							ListDataEvent.INTERVAL_REMOVED : ListDataEvent.CONTENTS_CHANGED;
					ListDataEvent dv = new ListDataEvent(this, kind, index, index);
					for (ListDataListener l : listeners) {
						l.contentsChanged(dv);
					}
				}
			});
		}
		
	}
	
	private static class JobCell extends JPanel implements ListCellRenderer {

		private static final long serialVersionUID = -1416673117438571387L;
		
		JProgressBar progressBar;
		JLabel taskLabel;
		JLabel subTaskLabel;
		
		public JobCell() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			taskLabel = new JLabel();
			taskLabel.setFont(taskLabel.getFont().deriveFont(Font.BOLD, 12.0f));
			add(taskLabel);
			add(Box.createRigidArea(new Dimension(0, 5)));
			subTaskLabel = new JLabel();
			add(subTaskLabel);
			add(Box.createRigidArea(new Dimension(0, 5)));
			progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
			add(progressBar);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JobManager.Job job = (Job) value;
			progressBar.setMinimum(0);
			progressBar.setMaximum(job.getTotal());
			progressBar.setValue(job.getWorked());
			taskLabel.setText(job.getTaskName());
			subTaskLabel.setText(job.getSubTaskName());
			return this;
		}
		
	}

}
