package org.xteam.cs.gui.syntax;

import javax.swing.text.Position;

public interface Annotatable {
	 public void addAnnotation(Position startPos, int length, Annotation annotation);
	 public void removeAnnotation(Annotation annotation);

}
