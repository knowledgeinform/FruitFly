package edu.jhuapl.nstd.swarm.display;

import edu.jhuapl.jlib.math.Accessor;
import edu.jhuapl.jlib.math.Angle;
import edu.jhuapl.jlib.math.Angle.AngleUnit;
import edu.jhuapl.jlib.math.Length;
import edu.jhuapl.jlib.math.NavyAngle;
import edu.jhuapl.jlib.math.Speed;
import edu.jhuapl.jlib.math.position.AbsolutePosition;
import edu.jhuapl.jlib.math.position.LatLonAltPosition;
import edu.jhuapl.nstd.swarm.belief.AgentBearingBelief;
import edu.jhuapl.nstd.swarm.belief.AgentPositionBelief;
import edu.jhuapl.nstd.swarm.belief.BearingTimeName;
import edu.jhuapl.nstd.swarm.belief.METBelief;
import edu.jhuapl.nstd.swarm.belief.METTimeName;
import edu.jhuapl.nstd.swarm.belief.PositionTimeName;
import edu.jhuapl.nstd.swarm.display.event.AgentTrackerEvent;
import edu.jhuapl.nstd.swarm.display.event.AgentTrackerListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JPanel;

public class BearingDial extends JPanel
{
  private String _selectedAgent;
  private Rectangle _currentSize;
  private NavyAngle _currentBearing;
  private NavyAngle _desiredBearing;
  private AbsolutePosition _currentPosition;
  private Speed _windSpeed;
  private Point2D.Double[] _result;
  private Point2D.Double[] _needle;
  private Point2D.Double[] _needleTri;
  private Point2D.Double[] _north;
  private Point2D.Double[] _circlePts;
  private boolean _needsRepaint = false;
  private AffineTransform _rotate;
  private AffineTransform _scale;
  private AffineTransform _translateOrigin;
  private AffineTransform _flip;
  private AffineTransform _translateYValues;
  private AffineTransform _combinedTransform;
  private static final double REGION_SIZE = 100.0D;

  public BearingDial()
  {
    this._flip = new AffineTransform(1.0F, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F);
    this._combinedTransform = new AffineTransform();

    this._result = new Point2D.Double[4];

    this._needle = new Point2D.Double[4];
    this._needle[0] = new Point2D.Double(0.0D, -15.0D);
    this._needle[1] = new Point2D.Double(10.0D, 0.0D);
    this._needle[2] = new Point2D.Double(0.0D, 100.0D);
    this._needle[3] = new Point2D.Double(-10.0D, 0.0D);

    this._needleTri = new Point2D.Double[3];
    this._needleTri[0] = new Point2D.Double(3.0D, 45.0D);
    this._needleTri[1] = new Point2D.Double(0.0D, 80.0D);
    this._needleTri[2] = new Point2D.Double(-3.0D, 45.0D);

    this._north = new Point2D.Double[3];
    this._north[0] = new Point2D.Double(0.0D, 90.0D);
    this._north[1] = new Point2D.Double(5.0D, 100.0D);
    this._north[2] = new Point2D.Double(-5.0D, 100.0D);

    this._circlePts = new Point2D.Double[2];
    this._circlePts[0] = new Point2D.Double(-100.0D, 100.0D);
    this._circlePts[1] = new Point2D.Double(100.0D, -100.0D);
  }


  public void update (double newBearing, double newSpeed)
  {
      try
      {
        if (!isVisible()) {
          return;
        }

        this._currentBearing = new NavyAngle (newBearing, Angle.DEGREES);
        this._desiredBearing = new NavyAngle (newBearing, Angle.DEGREES);
        this._windSpeed = new Speed (newSpeed, Speed.METERS_PER_SECOND);
        this._needsRepaint = true;

        if (this._needsRepaint)
          repaint();
      }
      catch (Exception e)
      {
          System.err.println ("Exception in update thread - caught and ignored");
            e.printStackTrace();
      }
  }

  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D)g;

    Rectangle rect = getBounds(null);
    if ((this._currentSize == null) || (!this._currentSize.equals(rect))) {
      this._currentSize = rect;

      int smallestSize = this._currentSize.height;
      if (this._currentSize.width < this._currentSize.height) {
        smallestSize = this._currentSize.width;
      }

      Point2D.Double center = new Point2D.Double(this._currentSize.x + this._currentSize.width / 2.0D, this._currentSize.y + this._currentSize.height / 2.0D);

      double scale = (smallestSize - 5.0D) / 200.0D;

      this._scale = AffineTransform.getScaleInstance(scale, scale);
      this._translateOrigin = AffineTransform.getTranslateInstance(center.x, center.y);

      this._translateYValues = AffineTransform.getTranslateInstance(0.0D, this._currentSize.height);

      this._combinedTransform.setToIdentity();
      this._combinedTransform.preConcatenate(this._scale);
      this._combinedTransform.preConcatenate(this._translateOrigin);
      this._combinedTransform.preConcatenate(this._flip);
      this._combinedTransform.preConcatenate(this._translateYValues);
    }

    g2.setColor(new Color(225, 225, 225));
    g2.fill(this._currentSize);

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    drawDial(g2);

    this._needsRepaint = false;
  }

  private void drawDial(Graphics2D g2)
  {
    this._combinedTransform.transform(this._circlePts, 0, this._result, 0, this._circlePts.length);
    Ellipse2D.Double circle = new Ellipse2D.Double(this._result[0].x, this._result[0].y, this._result[1].x - this._result[0].x, this._result[1].y - this._result[0].y);

    g2.setColor(Color.WHITE);
    g2.fill(circle);

    Polygon poly = createPolygon(this._north, this._combinedTransform);
    g2.setColor(Color.BLUE);
    g2.fillPolygon(poly);

    Stroke defaultStroke = g2.getStroke();
    g2.setStroke(new BasicStroke(5.0F));
    g2.setColor(Color.BLACK);
    g2.draw(circle);
    g2.setStroke(defaultStroke);

    if (this._currentBearing == null)
      return;

    Angle desiredAngle;
    if (this._currentBearing.equals(this._desiredBearing)) {
      desiredAngle = NavyAngle.NORTH.counterClockwiseAngleTo(this._desiredBearing);
    }
    else {
      desiredAngle = this._currentBearing.counterClockwiseAngleTo(this._desiredBearing);
    }

    double desired = Accessor.getDoubleValue(desiredAngle, Angle.RADIANS);

    AffineTransform rotate = AffineTransform.getRotateInstance(desired);

    rotate.preConcatenate(this._combinedTransform);

    poly = createPolygon(this._needle, rotate);
    g2.setColor(Color.BLACK);
    g2.fillPolygon(poly);
    poly = createPolygon(this._needleTri, rotate);
    g2.setColor(new Color(180, 0, 0));
    g2.fillPolygon(poly);

    int printAngle = (int)(360.0D - desired * 57.295779513082323D);
    if (printAngle == 360) printAngle = 0;

    int smallestSize = this._currentSize.height;
    if (this._currentSize.width < this._currentSize.height) {
      smallestSize = this._currentSize.width;
    }

    double scale = smallestSize / 150.0D;

    int yoffset = (int)((this._currentSize.height - smallestSize) / 2.0D / scale);
    int xoffset = (int)((this._currentSize.width - smallestSize) / 2.0D / scale);

    g2.setColor(Color.BLACK);
    g2.scale(scale, scale);
    g2.drawString(Integer.toString(printAngle) + "°", xoffset + 2, yoffset + 10);

    if (this._currentPosition != null) {
      g2.drawString(Integer.toString((int)Accessor.getDoubleValue(this._currentPosition.asLatLonAltPosition().getAltitude(), Length.METERS)) + "m", xoffset + 2, yoffset + 148);
    }

    if (this._windSpeed != null)
      g2.drawString(Integer.toString((int)this._windSpeed.getDoubleValue(Speed.METERS_PER_SECOND)) + "m/s", xoffset + 120, yoffset + 148);
  }

  private Polygon createPolygon(Point2D.Double[] pts, AffineTransform transform)
  {
    transform.transform(pts, 0, this._result, 0, pts.length);

    Polygon poly = new Polygon();

    for (int i = 0; i < pts.length; ++i) {
      poly.addPoint((int)this._result[i].x, (int)this._result[i].y);
    }

    return poly;
  }
}