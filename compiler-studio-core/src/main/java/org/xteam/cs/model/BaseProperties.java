package org.xteam.cs.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;
import java.util.Properties;

public class BaseProperties {

	public void fillFrom(Properties properties) {
		Class<? extends BaseProperties> cls = getClass();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			try {
				Field field = cls.getField((String)entry.getKey());
				if (field.getType().equals(String.class)) {
					field.set(this, (String)entry.getValue());
				} else if (field.getType() == Boolean.class
						|| field.getType() == Boolean.TYPE) {
					field.set(this, Boolean.valueOf((String) entry.getValue()));
				} else if (field.getType() == Integer.class
						|| field.getType() == Integer.TYPE) {
					field.set(this, Integer.valueOf((String) entry.getValue()));
				} else if (EnumSet.class.isAssignableFrom(field.getType())) {
					Object item = findElement(field.getType(), (String)entry.getValue());
					field.set(this, item);
				}
			} catch (Exception e) {
			}
		}
	}

	private Object findElement(Class<?> cls, String name) {
		for (Field field : cls.getFields()) {
			if (field.getName().equals(name)
					&& Modifier.isPublic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())
					&& Modifier.isStatic(field.getModifiers())) {
				try {
					return field.get(null);
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	public Properties getPropertyMap() {
		Properties prop = new Properties();
		Class<? extends BaseProperties> cls = getClass();
		for (Field field : cls.getFields()) {
			try {
				prop.put(field.getName(), field.get(this).toString());
			} catch (Exception e) {
			}
		}
		return prop;
	}
	
}
