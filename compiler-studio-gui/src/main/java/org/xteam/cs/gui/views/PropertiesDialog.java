package org.xteam.cs.gui.views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.xteam.cs.model.BaseProperties;
import org.xteam.cs.model.EnumSet;
import org.xteam.cs.model.Group;
import org.xteam.cs.model.Property;

public class PropertiesDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = -2024872891022240543L;
	
	private BaseProperties properties;

	private JPanel propertiesPane;
	
	private Map<String, GroupPanel> groups = new HashMap<String, GroupPanel>();
	private Map<String, CheckItem> cbs = new HashMap<String, CheckItem>();
	private Map<String, JComponent> fields = new HashMap<String, JComponent>();
	private boolean propertyChanged = false;
	
	public PropertiesDialog(String title, BaseProperties properties) {
		this.properties = properties;
		setTitle(title);
		setSize(500, 400);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        //
        final JButton setButton = new JButton("Ok");
        setButton.setActionCommand("Ok");
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);
        
        propertiesPane = new JPanel();
        propertiesPane.setLayout(new BoxLayout(propertiesPane, BoxLayout.PAGE_AXIS));
        propertiesPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        createDialogFields();
        
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(setButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        Container contentPane = getContentPane();
        contentPane.add(propertiesPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);

	}

	private void createDialogFields() {
		Class<? extends BaseProperties> cls = properties.getClass();
		for (Field field : cls.getFields()) {
			try {
				GroupPanel currentGroup = null;
				Group g = field.getAnnotation(Group.class);
				if (g != null) {
					currentGroup = groups.get(g.id());
					if (currentGroup == null) {
						groups.put(g.id(), currentGroup = new GroupPanel());
						propertiesPane.add(currentGroup);
						addCondition(g.condition(), currentGroup);
					}
					if (g.display().length() > 0)
						currentGroup.setTitle(g.display());
				}
				String name = field.getName();
				Property p = field.getAnnotation(Property.class);
				if (p != null) {
					name = p.display();
				}
				JComponent comp = createComponent(field.getType(),
						field.get(properties), name, field.getName());
				if (p != null) {
					addCondition(p.condition(), comp);
				}
				if (currentGroup != null)
					currentGroup.add(name, comp);
				else
					propertiesPane.add(makeLine(name, comp));
				fields.put(field.getName(), comp);
			} catch (Exception e) {
			}
		}
		propertiesPane.add(Box.createVerticalStrut(Short.MAX_VALUE));
	}
	
	private void addCondition(String condition, JComponent comp) {
		if (condition.length() > 0) {
			CheckItem dep = cbs.get(condition);
			if (dep != null)
				dep.dependencies.add(comp);
		}
	}
	
	private static Box makeLine(String name, JComponent comp) {
		Box line = Box.createHorizontalBox();
		line.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		if (! (comp instanceof JCheckBox)) {
			line.add(new JLabel(name + ":"));
			line.add(Box.createRigidArea(new Dimension(10, 0)));
		}
		line.add(comp);
		line.add(Box.createHorizontalGlue());
		return line;
	}
	
	private JComponent createComponent(Class<?> cls, Object object, String name, String id) {
		if (cls == String.class) {
			return new JTextField((String) object);
		}
		if (cls == Boolean.TYPE || cls == Boolean.class) {
			JCheckBox cb = new JCheckBox(name, (Boolean) object);
			cbs.put(id, new CheckItem(cb));
			cb.addActionListener(checkboxListener);
			return cb;
		}
		if (cls == Integer.TYPE || cls == Integer.class) {
			return new JTextField(String.valueOf((Integer)object));
		}
		if (EnumSet.class.isAssignableFrom(cls)) {
			return createCumbo(cls, object);
		}
		return null;
	}
	
	private ActionListener checkboxListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for (CheckItem item : cbs.values()) {
				if (item.cb == e.getSource()) {
					item.changeState();
					break;
				}
			}
		}
	};

	private JComponent createCumbo(Class<?> cls, Object object) {
		List<ComboItem> items = new ArrayList<ComboItem>();
		ComboItem selected = null;
		for (Field field : cls.getFields()) {
			if (Modifier.isPublic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())
					&& Modifier.isStatic(field.getModifiers())) {
				String display = field.getName();
				Property p = field.getAnnotation(Property.class);
				if (p != null) {
					display = p.display();
				}
				try {
					Object value = field.get(null);
					ComboItem item = new ComboItem(display, value);
					items.add(item);
					if (value == object)
						selected = item;
				} catch (Exception e) {
				}
			}
		}
		JComboBox cb = new JComboBox(items.toArray(new ComboItem[items.size()]));
		cb.setSelectedItem(selected);
		return cb;
	}
	
	private static class CheckItem {

		public List<Component> dependencies = new ArrayList<Component>();
		private JCheckBox cb;

		public CheckItem(JCheckBox cb) {
			this.cb = cb;
		}

		public void changeState() {
			boolean b = cb.isSelected();
			for (Component c : dependencies) {
				c.setEnabled(b);
			}
		}
		
	}
	
	private static class ComboItem {

		private String display;
		private Object value;

		public ComboItem(String display, Object value) {
			this.display = display;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return display;
		}
		
	}

	private static class GroupPanel extends JPanel {
		
		private static final long serialVersionUID = -4940232927148037641L;
		
		private List<JComponent> elements = new ArrayList<JComponent>();

		public void setTitle(String title) {
			setBorder(BorderFactory.createTitledBorder(title));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}
		
		@Override
		public void setEnabled(boolean value) {
			for (JComponent child : elements) {
				child.setEnabled(value);
			}
		}
		
		 public void add(String name, JComponent comp) {
			 elements .add(comp);
			 JComponent line = makeLine(name, comp);
			 add(line);
		 }

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Ok")) {
			setPropertyValues();
			propertyChanged  = true;
		}
		setVisible(false);
	}

	private void setPropertyValues() {
		Class<? extends BaseProperties> cls = properties.getClass();
		for (Field field : cls.getFields()) {
			try {
				JComponent comp = fields.get(field.getName());
				if (field.getType().equals(String.class)) {
					field.set(properties, ((JTextField) comp).getText());
				} else if (field.getType() == Boolean.class
						|| field.getType() == Boolean.TYPE) {
					field.set(properties, ((JCheckBox) comp).isSelected());
				} else if (field.getType() == Integer.class
						|| field.getType() == Integer.TYPE) {
					field.set(properties, Integer.decode(((JTextField) comp).getText()));
				} else if (EnumSet.class.isAssignableFrom(field.getType())) {
					ComboItem item = (ComboItem)(((JComboBox) comp).getSelectedItem());
					field.set(properties, item.value);
				}
			} catch (Exception e) {
			}
		}
	}

	public boolean getPropertyChanged() {
		return propertyChanged;
	}

}
