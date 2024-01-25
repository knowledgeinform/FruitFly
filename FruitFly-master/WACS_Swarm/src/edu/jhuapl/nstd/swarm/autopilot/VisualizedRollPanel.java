package edu.jhuapl.nstd.swarm.autopilot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import javax.swing.JPanel;

public class VisualizedRollPanel extends JPanel
{
    final private Object m_lock = new Object();
    private double m_roll_rad = 0;
    private double m_commandedRoll_rad = 0;
    private final static Stroke m_horizonStroke = new BasicStroke(1F);
    private final static Stroke m_rollStroke = new BasicStroke(5F);

    public void setRoll(final double roll_rad, final double commandedRoll_rad)
    {
        synchronized (m_lock)
        {
            m_roll_rad = roll_rad;
            m_commandedRoll_rad = commandedRoll_rad;
        }
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        double roll_rad;
        double commandedRoll_rad;
        synchronized (m_lock)
        {
            roll_rad = m_roll_rad;
            commandedRoll_rad = m_commandedRoll_rad;
        }

        final int lineLength = (int)(getWidth() * 0.85);
        final int lineHalfLength = lineLength / 2;
        final int panelCenterX = (int)(getWidth() / 2);
        final int panelCenterY = (int)(getHeight() / 2);

        int lineX1 = panelCenterX + (int)(Math.cos(roll_rad) * -lineHalfLength);
        int lineY1 = panelCenterY + (int)(Math.sin(-roll_rad) * lineHalfLength);
        int lineX2 = panelCenterX +(int)(Math.cos(roll_rad) * lineHalfLength);
        int lineY2 = panelCenterY + (int)(Math.sin(roll_rad) * lineHalfLength);

        int commandedLineX1 = panelCenterX + (int)(Math.cos(commandedRoll_rad) * -lineHalfLength);
        int commandedLineY1 = panelCenterY + (int)(Math.sin(-commandedRoll_rad) * lineHalfLength);
        int commandedLineX2 = panelCenterX +(int)(Math.cos(commandedRoll_rad) * lineHalfLength);
        int commandedLineY2 = panelCenterY + (int)(Math.sin(commandedRoll_rad) * lineHalfLength);

        Graphics2D graphics2D = (Graphics2D)g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics2D.setStroke(m_horizonStroke);
        graphics2D.setColor(Color.RED);
        graphics2D.drawLine(0, panelCenterY, getWidth(), panelCenterY);
        graphics2D.setStroke(m_rollStroke);
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.drawLine(commandedLineX1, commandedLineY1, commandedLineX2, commandedLineY2);
        graphics2D.setColor(Color.BLACK);
        graphics2D.drawLine(lineX1, lineY1, lineX2, lineY2);
        graphics2D.drawLine(lineX1, lineY1, lineX2, lineY2);

    }
}
