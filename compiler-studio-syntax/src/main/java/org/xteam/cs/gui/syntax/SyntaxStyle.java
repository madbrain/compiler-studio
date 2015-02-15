package org.xteam.cs.gui.syntax;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import javax.swing.text.Utilities;

public class SyntaxStyle {

	private Color color;
	private int fontStyle;
	
	public SyntaxStyle(Color color) {
		this(color, 0);
	}
	
	public SyntaxStyle(Color color, int fontStyle) {
		this.color = color;
		this.fontStyle = fontStyle;
	}

	public Color getColor() {
		return color;
	}
	
	public int getFontStyle() {
		return fontStyle;
	}

	public int drawText(Segment segment, int x, int y, Graphics graphics,
			TabExpander tabExpander, int start) {
		graphics.setFont(graphics.getFont().deriveFont(getFontStyle()));
		graphics.setColor(getColor());
        int ret = Utilities.drawTabbedText(segment, x, y, graphics, tabExpander, start);
		return ret;
	}

}
