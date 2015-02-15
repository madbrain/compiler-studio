package org.xteam.cs.gui.syntax;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;

import org.xteam.cs.runtime.IToken;

public class SyntaxView extends PlainView {

	private StyleManager manager;
	private SyntaxStyle defaultStyle;

	public SyntaxView(Element elem, StyleManager styleManager) {
		super(elem);
		this.manager = styleManager;
		this.defaultStyle = this.manager.getDefaultStyle();
	}

	@Override
	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
			int p1) throws BadLocationException {
		Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		Font saveFont = graphics.getFont();
		Color saveColor = graphics.getColor();
		SyntaxDocument doc = (SyntaxDocument) getDocument();
		Segment segment = getLineBuffer();
		try {
			Iterator<IToken> i = doc.getTokens(p0, p1);
			int start = p0;
			while (i.hasNext()) {
				IToken t = i.next();
				if (start < t.start()) {
					doc.getText(start, t.start() - start, segment);
					x = defaultStyle.drawText(segment, x, y, graphics, this, start);
				}
				int l = t.length();
				int s = t.start();
				if (s < p0) {
					l -= (p0 - s);
					s = p0;
				}
				if (s + l > p1) {
					l = p1 - s;
				}
				doc.getText(s, l, segment);
				x = manager.getStyle(t.type()).drawText(segment, x, y, graphics, this, t.start());
				start = t.start() + t.length();
			}
			if (start < p1) {
				doc.getText(start, p1 - start, segment);
				x = defaultStyle.drawText(segment, x, y, graphics, this, start);
			}
		} catch (BadLocationException e) {
		} finally {
			graphics.setFont(saveFont);
			graphics.setColor(saveColor);
		}
		return x;
	}

}
