// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import net.runelite.client.ui.overlay.RenderableEntity;

public class TextComponent implements RenderableEntity
{
    private String text;
    private Point position;
    private Color color;
    
    public TextComponent() {
        this.position = new Point();
        this.color = Color.WHITE;
    }
    
    public Dimension render(final Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.drawString(this.text, this.position.x + 1, this.position.y + 1);
        graphics.setColor(this.color);
        graphics.drawString(this.text, this.position.x, this.position.y);
        final FontMetrics fontMetrics = graphics.getFontMetrics();
        return new Dimension(fontMetrics.stringWidth(this.text), fontMetrics.getHeight());
    }
    
    void setText(final String text) {
        this.text = text;
    }
    
    void setPosition(final Point position) {
        this.position = position;
    }
    
    void setColor(final Color color) {
        this.color = color;
    }
}
