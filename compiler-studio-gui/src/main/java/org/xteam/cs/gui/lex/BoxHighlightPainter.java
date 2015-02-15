package org.xteam.cs.gui.lex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

public class BoxHighlightPainter extends DefaultHighlightPainter {

	public BoxHighlightPainter() {
		super(new Color(64, 128, 0));
	}

	public void paint(Graphics g, int offs0, int offs1, Shape bounds,
			JTextComponent c) {
		Rectangle alloc = bounds.getBounds();
		try {
			// --- determine locations ---
			TextUI mapper = c.getUI();
			Rectangle p0 = mapper.modelToView(c, offs0);
			Rectangle p1 = mapper.modelToView(c, offs1);

			// --- render ---
			Color color = getColor();

			if (color == null) {
				g.setColor(c.getSelectionColor());
			} else {
				g.setColor(color);
			}
			if (p0.y == p1.y) {
				// same line, render a rectangle
				Rectangle r = p0.union(p1);
				g.drawRect(r.x, r.y, r.width, r.height);
			} else {
				// different lines
				int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
				g.drawLine(p0.x, p0.y, p0.x + p0ToMarginWidth, p0.y);
				if ((p0.y + p0.height) != p1.y) {
					g.drawRect(alloc.x, p0.y, alloc.width, p0.height);
					//g.drawLine(alloc.x, p0.y + p0.height, alloc.x + alloc.width, p0.y + p0.height);
				}
				g.drawRect(alloc.x, p1.y, alloc.x - p1.x, p1.height);
				//g.drawLine(alloc.x, p1.y, p1.x, p1.y);
			}
		} catch (BadLocationException e) {
			// can't render
		}
	}

	public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
			JTextComponent c, View view) {
		Color color = getColor();

		if (color == null) {
			g.setColor(c.getSelectionColor());
		} else {
			g.setColor(color);
		}
		if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
			// Contained in view, can just use bounds.
			Rectangle alloc;
			if (bounds instanceof Rectangle) {
				alloc = (Rectangle) bounds;
			} else {
				alloc = bounds.getBounds();
			}
			g.drawRect(alloc.x, alloc.y, alloc.width, alloc.height);
			//g.drawLine(alloc.x, alloc.y + alloc.height, alloc.x + alloc.width, alloc.y + alloc.height);
			return alloc;
		} else {
			// Should only render part of View.
			try {
				// --- determine locations ---
				Shape shape = view.modelToView(offs0, Position.Bias.Forward,
						offs1, Position.Bias.Backward, bounds);
				Rectangle r = (shape instanceof Rectangle) ? (Rectangle) shape
						: shape.getBounds();
				//g.drawLine(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
				g.drawRect(r.x, r.y, r.width, r.height);
				return r;
			} catch (BadLocationException e) {
				// can't render
			}
		}
		// Only if exception
		return null;
	}
	
}
