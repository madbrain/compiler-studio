package org.xteam.cs.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.WindowBar;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.gui.laf.InfoNodeLookAndFeel;
import net.infonode.util.Direction;

import org.xteam.cs.gui.syntax.StyleManager;
import org.xteam.cs.gui.syntax.SyntaxStyle;
import org.xteam.cs.gui.views.Editor;
import org.xteam.cs.gui.views.IResourceAdapter;
import org.xteam.cs.gui.views.JobsPanel;
import org.xteam.cs.gui.views.ProblemPanel;
import org.xteam.cs.gui.views.ProjectPanel;
import org.xteam.cs.gui.views.PropertiesDialog;
import org.xteam.cs.gui.views.SyntaxEditor;
import org.xteam.cs.jobs.IJob;
import org.xteam.cs.jobs.JobManager;
import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.IProgressMonitor;
import org.xteam.cs.model.IProjectListener;
import org.xteam.cs.model.IResourceListener;
import org.xteam.cs.model.Project;
import org.xteam.cs.model.ProjectEvent;
import org.xteam.cs.model.ProjectManager;
import org.xteam.cs.model.ProjectResource;
import org.xteam.cs.model.ResourceEvent;
import org.xteam.cs.syntax.ISyntaxToken;

public class CompilerStudio implements IWorkbench, IProjectListener {
	
	private static final int ICON_SIZE = 8;

	/**
	 * Custom view icon.
	 */
	private static final Icon VIEW_ICON = new Icon() {
		public int getIconHeight() {
			return ICON_SIZE;
		}

		public int getIconWidth() {
			return ICON_SIZE;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Color oldColor = g.getColor();

			g.setColor(new Color(70, 70, 70));
			g.fillRect(x, y, ICON_SIZE, ICON_SIZE);

			g.setColor(new Color(100, 230, 100));
			g.fillRect(x + 1, y + 1, ICON_SIZE - 2, ICON_SIZE - 2);

			g.setColor(oldColor);
		}
	};
	
	private JFrame frame;
	private View[] views = new View[10];
	private ViewMap viewMap = new ViewMap();
	private Map<ProjectResource, View> editors = new HashMap<ProjectResource, View>();
	private DockingWindowsTheme currentTheme = new ShapedGradientDockingTheme();
	private RootWindowProperties properties = new RootWindowProperties();
	private TabWindow editorTab;
	private RootWindow rootWindow;
	private JFileChooser fileChooser;
	
	private Project project;
	private ProjectManager projectManager;
	private StyleManager styleManager;
	private JobManager jobManager;

	public CompilerStudio() {
		
		projectManager = new ProjectManager();
		project = new Project(projectManager);
		project.addProjectListener(this);
		
		styleManager = createStyleManager();
		jobManager = new JobManager();
		
		frame = new JFrame("Compiler Studio");
		 createRootWindow();
		 setDefaultLayout();
		//frame.getContentPane().add(createToolBar(), BorderLayout.NORTH);
	    frame.getContentPane().add(rootWindow, BorderLayout.CENTER);
	    frame.setJMenuBar(createMenuBar());
	    frame.setSize(900, 700);
	    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    frame.addWindowListener(new WindowAdapter() {
	    	@Override
	    	public void windowClosing(WindowEvent e) {
	    		quit();
	    	}
	    });
	    frame.setVisible(true);
	}
	
	private StyleManager createStyleManager() {
		StyleManager styleManager = new StyleManager(ISyntaxToken.DEFAULT);
		styleManager.set(ISyntaxToken.DEFAULT, new SyntaxStyle(Color.BLACK));
		styleManager.set(ISyntaxToken.TYPES, new SyntaxStyle(new Color(0, 128, 0), Font.BOLD));
		styleManager.set(ISyntaxToken.KEYWORD, new SyntaxStyle(new Color(128, 0, 30), Font.BOLD));
		styleManager.set(ISyntaxToken.COMMENT, new SyntaxStyle(new Color(0, 128, 30), Font.ITALIC));
		styleManager.set(ISyntaxToken.STRING, new SyntaxStyle(new Color(0, 0, 196)));
		styleManager.set(ISyntaxToken.OPERATOR, new SyntaxStyle(new Color(196, 0, 128)));
		return styleManager;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menu = new JMenuBar();
		menu.add(createProjectMenu());
		return menu;
	}
	
	private JMenu createProjectMenu() {
		JMenu projectMenu = new JMenu("Project");
		projectMenu.add("New").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectNew();
			}
		});
		projectMenu.add("Open").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectOpen();
			}
		});
		projectMenu.add("Save").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectSave();
			}
		});
		projectMenu.add("Save As").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectSaveAs();
			}
		});

		projectMenu.addSeparator();
		
		projectMenu.add("Add File").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectAddFile();
			}
		});

		projectMenu.addSeparator();
		
		projectMenu.add("Quit").addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		return projectMenu;
	}
	
	private void projectNew() {
		if (project.isDirty()) {
			if (JOptionPane.showConfirmDialog(frame,
					"Current project has unsaved changes.\nDiscard those changes?") == JOptionPane.CANCEL_OPTION)
				return;
		}
		project.reset();
	}
	
	private void projectOpen() {
		if (project.isDirty()) {
			if (JOptionPane.showConfirmDialog(frame,
					"Current project has unsaved changes.\nDiscard those changes?") == JOptionPane.CANCEL_OPTION)
				return;
		}
		JFileChooser chooser = getFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(projectFilter);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			project.open(chooser.getSelectedFile());
		}
	}
	
	private void projectSave() {
		if (project.getFile() == null) {
			projectSaveAs();
		} else {
			project.save();
		}
	}
	
	private void projectSaveAs() {
		JFileChooser chooser = getFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(projectFilter);
		if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
			project.saveAs(chooser.getSelectedFile());
		}
	}
	
	private static final FileFilter projectFilter = new FileFilter() {
		
		@Override
		public String getDescription() {
			return "Compiler Project (*.cpj)";
		}
		
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".cpj");
		}
	};
	
	private static final FileFilter elementFilter = new FileFilter() {
		
		@Override
		public String getDescription() {
			return "Element File (*.grm, *.ast, *.lex)";
		}
		
		@Override
		public boolean accept(File f) {
			return f.isDirectory() ||
				f.getName().endsWith(".grm") || f.getName().endsWith(".ast") || f.getName().endsWith(".lex");
		}
	};
	
	private void projectAddFile() {
		JFileChooser chooser = getFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(elementFilter);
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			project.addFile(chooser.getSelectedFile());
		}
	}
	
	private void quit() {
		if (project.isDirty()) {
			if (JOptionPane.showConfirmDialog(frame,
			"Current project has unsaved changes.\nDiscard those changes?") == JOptionPane.CANCEL_OPTION)
				return;
		}
		System.exit(0);
	}
	
	private JFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new File("."));
		}
		return fileChooser;
	}

	private static final int PROJECT_VIEW = 0;
	private static final int PROBLEM_VIEW = 1;
	private static final int JOBS_VIEW    = 2;
	
	private void createRootWindow() {
		views[PROJECT_VIEW] = new View("Project", VIEW_ICON, new ProjectPanel(project, this));
		views[PROBLEM_VIEW] = new View("Problems", VIEW_ICON, new ProblemPanel(project, this));
		views[JOBS_VIEW] = new View("Jobs", VIEW_ICON, new JobsPanel(jobManager, this));
		viewMap.addView(PROJECT_VIEW, views[PROJECT_VIEW]);

		MixedViewHandler handler = new MixedViewHandler(viewMap,
				new ViewSerializer() {
					public void writeView(View view, ObjectOutputStream out)
							throws IOException {
						out.writeUTF(((Editor) view.getComponent()).getResource().getPath());
					}

					public View readView(ObjectInputStream in)
							throws IOException {
						String filename = in.readUTF();
						return getEditorView(project.getResource(filename));
						
					}
				});
		rootWindow = DockingUtil.createRootWindow(viewMap, handler, true);
		rootWindow.addListener(new DockingWindowAdapter() {
			@Override
			public void windowAdded(DockingWindow addedToWindow,
					DockingWindow addedWindow) {
				updateViews(addedWindow, true);
			}

			@Override
			public void windowRemoved(DockingWindow removedFromWindow,
					DockingWindow removedWindow) {
				updateViews(removedWindow, false);
			}

			@Override
			public void windowClosing(DockingWindow closingWindow) throws OperationAbortedException {
				for (ProjectResource res : editors.keySet()) {
					if (editors.get(res) == closingWindow) {
						if (res.isDirty()) {
							if (JOptionPane.showConfirmDialog(frame,
								res.getName() + " has unsaved changes.\nDiscard those changes?") == JOptionPane.CANCEL_OPTION)
								throw new OperationAbortedException();
						}
						((Editor)editors.get(res).getComponent()).close();
						break;
					}
				}
			}
		});
		properties.addSuperObject(currentTheme.getRootWindowProperties());
		rootWindow.getRootWindowProperties().addSuperObject(properties);
	}
	
	private void updateViews(DockingWindow window, boolean added) {
		if (window instanceof View) {
			Component comp = ((View)window).getComponent();
			if (comp instanceof Editor) {
				if (added) {
					editors.put(((Editor) comp).getResource(), (View)window);
				} else {
					editors.remove(((Editor) comp).getResource());
				}
			}
		} else {
			for (int i = 0; i < window.getChildWindowCount(); i++)
				updateViews(window.getChildWindow(i), added);
		}
	}
	
	private View getEditorView(ProjectResource resource) {
		if (! editors.containsKey(resource)) {
			Editor editor = null;
			IResourceAdapter adapter = ResourceAdapterManager.getDefault().getAdapter(resource);
			if (adapter != null) {
				editor = adapter.createEditor(styleManager);
			}
			if (editor == null)
				editor = new SyntaxEditor(resource, styleManager);
			editors.put(resource, new View(resource.getName(), null, editor));
		}
		return editors.get(resource);
	}
	
	@Override
	public void openEditor(ProjectResource resource) {
		View view = getEditorView(resource);
		if (! editorTab.isShowing()) {
			setDefaultLayout(); // XXX a bit brutal
		}
		editorTab.addTab(view);
	}
	
	@Override
	public void openPropertyEditor(String title, BaseProperties properties) {
		PropertiesDialog dialog = new PropertiesDialog(title, properties);
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
		if (dialog.getPropertyChanged()) {
			project.propertyChanged();
		}
	}
	
	@Override
	public void projectChanged(ProjectEvent event) {
		if (event.getKind() == ProjectEvent.ADD_FILE) {
			event.getResource().addResourceListener(resourceListener);
			buildProject();
		} else if (event.getKind() == ProjectEvent.CHANGE_STRUCTURE) {
			for (ProjectResource resource : project.getResources()) {
				resource.removeResourceListener(resourceListener);
				resource.addResourceListener(resourceListener);
			}
			buildProject();
		}
	}
	
	private IResourceListener resourceListener = new IResourceListener() {
		
		@Override
		public void resourceChanged(ResourceEvent event) {
			ProjectResource resource = event.getResource();
			updateViewName(resource);
			buildProject(resource);
		}
		
		private void buildProject(ProjectResource resource) {
			if (! resource.isDirty()) {
				CompilerStudio.this.buildProject();
			}
		}

		private void updateViewName(ProjectResource resource) {
			View view = getEditorView(resource);
			String name = resource.getName();
			if (resource.isDirty())
				name += "*";
			view.getViewProperties().setTitle(name);
		}
	};
	
	private void buildProject() {
		jobManager.run(new IJob() {
			@Override
			public void run(IProgressMonitor monitor) {
				projectManager.buildProject(project, monitor);
			}
		});
	}
	
	@Override
	public void runJob(IJob job) {
		jobManager.run(job);
	}
	
	private void setDefaultLayout() {
		View[] editorViews = editors.values().toArray(new View[editors.size()]);
		editorTab = new TabWindow(editorViews);
		editorTab.getWindowProperties().setCloseEnabled(false);
		
		View[] utilityViews = new View[] { views[PROBLEM_VIEW], views[JOBS_VIEW] };

		rootWindow.setWindow(new SplitWindow(true, 0.3f,
				views[PROJECT_VIEW],
				new SplitWindow(false, 0.8f,
						editorTab,
						new TabWindow(utilityViews))));

		WindowBar windowBar = rootWindow.getWindowBar(Direction.DOWN);

		while (windowBar.getChildWindowCount() > 0)
			windowBar.getChildWindow(0).close();

		//windowBar.addTab(views[3]);
	}

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(new InfoNodeLookAndFeel());

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CompilerStudio();
			}
		});
	}
	
}
