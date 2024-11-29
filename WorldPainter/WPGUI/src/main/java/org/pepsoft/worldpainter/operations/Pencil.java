/*
 * WorldPainter, a graphical and interactive map generator for Minecraft.
 * Copyright \u00A9 2011-2015  pepsoft.org, The Netherlands
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.painting.DimensionPainter;
import org.pepsoft.worldpainter.painting.Paint;

import javax.swing.*;
import java.awt.*;

/**
 * Draw dots and straight or freehand lines with any terrain or layer.
 *
 * Created by pepijn on 15-05-15.
 */
public class Pencil extends AbstractPaintOperation {
    public Pencil(WorldPainterView view) {
        super("Pencil", "\u94C5\u7B14-\u4F7F\u7528\u4EFB\u4F55\u65B9\u5757\u6216\u8986\u76D6\u5C42\u7ED8\u5236\u70B9\u3001\u76F4\u7EBF\u6216\u81EA\u7531\u624B\u7ED8\u7EBF\u6761\u3002", view, 100, "operation.pencil");
    }

    @Override
    protected void tick(int centreX, int centreY, boolean undo, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        dimension.setEventsInhibited(true);
        try {
            painter.setUndo(undo);
            if (first) {
                // Either a single click, or the start of a drag
                lockedX = centreX;
                lockedY = centreY;
                lockedAxis = null;
                if (isShiftDown()) {
                    // Shift was pressed: draw a line from the last location
                    if ((previousX != Integer.MIN_VALUE) && (previousY != Integer.MIN_VALUE)) {
                        if (isCtrlDown()) {
                            // Ctrl was also pressed: snap the line to 45 degree
                            // angles
                            int[] snappedCoords = snapCoords(previousX, previousY, centreX, centreY, getAxis(previousX, previousY, centreX, centreY));
                            centreX = snappedCoords[0];
                            centreY = snappedCoords[1];
                        }
                        painter.drawLine(dimension, previousX, previousY, centreX, centreY);
                    }
                    inhibitDrag = true;
                } else {
                    // Shift was not pressed: just draw a single dot
                    painter.drawPoint(dimension, centreX, centreY);
                    inhibitDrag = false;
                }
                previousX = centreX;
                previousY = centreY;
            } else if (! inhibitDrag) {
                // Continuation of a drag
                if (isCtrlDown()) {
                    // Ctrl was pressed: snap the line to 45 degree angles
                    // relative to the point where the drag was started
                    if (lockedAxis == null) {
                        lockedAxis = getAxis(lockedX, lockedY, centreX, centreY);
                    }
                    int[] snappedCoords = snapCoords(lockedX, lockedY, centreX, centreY, lockedAxis);
                    centreX = snappedCoords[0];
                    centreY = snappedCoords[1];
                }
                if ((centreX != previousX) || (centreY != previousY)) {
                    if ((Math.abs(centreX - previousX) <= 1) && (Math.abs(centreY - previousY) <= 1)) {
                        painter.drawPoint(dimension, centreX, centreY);
                    } else {
                        painter.drawLine(dimension, previousX, previousY, centreX, centreY);
                    }
                    previousX = centreX;
                    previousY = centreY;
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    @Override
    protected void paintChanged(Paint newPaint) {
        newPaint.setDither(false);
        painter.setPaint(getPaint());
    }

    private Axis getAxis(int x1, int y1, int x2, int y2) {
        if ((x1 == x2) && (y1 == y2)) {
            return null;
        }
        double angle = Math.atan((double) (y2 - y1) / (x2 - x1));
        if (x2 < x1) {
            angle += Math.PI;
        } else if (angle < 0) {
            angle += Math.PI * 2;
        }
        switch ((int) Math.round(angle * 4 / Math.PI)) {
            case 0:
            case 4:
            case 8:
                return Axis.W_E;
            case 1:
            case 5:
                return Axis.NW_SE;
            case 2:
            case 6:
                return Axis.N_S;
            case 3:
            case 7:
                return Axis.NE_SW;
            default:
                throw new InternalError();
        }
    }

    /**
     * Snap a point to the closest point on a line defined by another point and an axis.
     *
     * @param x1 The X coordinate of the point defining the line.
     * @param y1 THe Y coordinate of the point defining the line.
     * @param x2 The X coordinate of the point to snap to the line.
     * @param y2 The Y coordinate of the point to snap to the line.
     * @param axis The axis through {@code x1,y2} defining the line to snap to.
     * @return An array containing the coordinates of the closest point on the specified line to {@code x2,y2}.
     *     Index 0 contains the x coordinate and index 1 the y coordinate.
     */
    private int[] snapCoords(int x1, int y1, int x2, int y2, Axis axis) {
        if (axis == null) {
            return new int[] {x2, y2};
        }
        switch (axis) {
            case W_E:
                return new int[] {x2, y1};
            case NW_SE:
                Point closestPoint = closestPoint(new Point(x1, y1), new Point(x1 + 1000, y1 + 1000), new Point(x2, y2));
                return new int[] {closestPoint.x, closestPoint.y};
            case N_S:
                return new int[] {x1, y2};
            case NE_SW:
                closestPoint = closestPoint(new Point(x1, y1), new Point(x1 + 1000, y1 - 1000), new Point(x2, y2));
                return new int[] {closestPoint.x, closestPoint.y};
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the closest point on the infinite line through p1 and p2 to
     * p3.
     *
     * @param p1 First point of the line
     * @param p2 Second point of the line
     * @param p3 Point to which we want to find the closest point on
     *     the line defined by p1,p2
     * @return The closest point on the line through p1 and p2 to p3
     */
    private Point closestPoint(Point p1, Point p2, Point p3) {
        final double xDelta = p2.getX() - p1.getX();
        final double yDelta = p2.getY() - p1.getY();

        if ((xDelta == 0) && (yDelta == 0)) {
            throw new IllegalArgumentException("p1 and p2 cannot be the same point");
        }

        final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        return new Point((int) Math.round(p1.getX() + u * xDelta), (int) Math.round(p1.getY() + u * yDelta));
    }

    @Override
    public JPanel getOptionsPanel() {
        return OPTIONS_PANEL;
    }

    private final DimensionPainter painter = new DimensionPainter();
    private int previousX = Integer.MIN_VALUE, previousY = Integer.MIN_VALUE, lockedX = Integer.MIN_VALUE, lockedY = Integer.MIN_VALUE;
    private Axis lockedAxis;
    private boolean inhibitDrag;

    enum Axis {W_E, NW_SE, N_S, NE_SW}

    private static final JPanel OPTIONS_PANEL = new StandardOptionsPanel("\u94C5\u7B14", "<p>\u4F7F\u7528\u5DE6\u952E\u6267\u884C\u4EE5\u4E0B\u64CD\u4F5C\u4EE5\u5E94\u7528\u5F53\u524D\u9009\u4E2D\u7684\u753B\u7B14\u7C7B\u578B:\n" +
            "<ul><li>\u62D6\u52A8\u4EE5\u7ED8\u5236\u81EA\u7531\u5F62\u72B6\u7684\u7EBF\u6761\n" +
            "<li>\u70B9\u51FB\u4EE5\u7ED8\u5236\u70B9\n" +
            "<li>Shift+\u70B9\u51FB\u4EE5\u4ECE\u4E0A\u4E00\u4E2A\u70B9\u6216\u6700\u540E\u4E00\u6761\u7EBF\u7684\u7ED3\u5C3E\u7ED8\u5236\u76F4\u7EBF\n" +
            "<li>\u6309\u4F4FCtrl\u952E\u9650\u5236\u7ED8\u5236\u89D2\u5EA6\u4E3A45\u5EA6\u89D2</ul>\n" +
            "<p>\u4F7F\u7528\u53F3\u952E\u6267\u884C\u4EE5\u4E0B\u64CD\u4F5C:\n" +
            "<ul><li>\u9009\u62E9\u8986\u76D6\u5C42\u65F6\uFF1A\u79FB\u9664\u8BE5\u8986\u76D6\u5C42 \n" +
            "<li>\u9009\u62E9\u65B9\u5757\u65F6\uFF1A\u91CD\u7F6E\u4E3A\u5F53\u524D\u4E3B\u9898\n" +
            "<li>\u9009\u62E9\u751F\u7269\u7FA4\u7CFB\u65F6\uFF1A\u91CD\u7F6E\u4E3A\u81EA\u52A8\u751F\u7269\u7FA4\u7CFB" +
            "</ul>");
}