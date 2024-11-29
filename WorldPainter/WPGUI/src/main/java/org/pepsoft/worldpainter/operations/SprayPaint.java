package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.painting.DimensionPainter;
import org.pepsoft.worldpainter.painting.Paint;

import javax.swing.*;

/**
 * Created by pepijn on 14-5-15.
 */
public class SprayPaint extends AbstractPaintOperation {
    public SprayPaint(WorldPainterView view) {
        super("Spray Paint", "\u55B7\u67AA-\u55B7\u5C04\u4EFB\u610F\u65B9\u5757\u3001\u8986\u76D6\u5C42\u6216\u751F\u7269\u7FA4\u7CFB\u5230\u4E16\u754C\u4E0A", view, 100, "operation.sprayPaint");
    }

    @Override
    public JPanel getOptionsPanel() {
        return OPTIONS_PANEL;
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        painter.setUndo(inverse);
        dimension.setEventsInhibited(true);
        try {
            painter.drawPoint(dimension, centreX, centreY, dynamicLevel);
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    @Override
    protected void paintChanged(Paint newPaint) {
        newPaint.setDither(true);
        painter.setPaint(newPaint);
    }

    private static final JPanel OPTIONS_PANEL = new StandardOptionsPanel("\u55B7\u67AA", "<ul>\n" +
            "    <li>\u5DE6\u952E\u5C06\u5F53\u524D\u9009\u4E2D\u7684\u753B\u7B14\u55B7\u6D12\u5230\u9009\u4E2D\u4F4D\u7F6E\n" +
            "    <li>\u53F3\u952E\u8986\u76D6\u5C42\u4EE5\u79FB\u9664\u8BE5\u8986\u76D6\u5C42\n" +
            "    <li>\u53F3\u952E\u65B9\u5757\u4F7F\u5176\u91CD\u7F6E\u4E3A\u9ED8\u8BA4\u4E3B\u9898\n" +
            "    <li>\u53F3\u952E\u751F\u7269\u7FA4\u7CFB\u4F7F\u5176\u53D8\u4E3A\u81EA\u52A8\u7684\u751F\u7269\u7FA4\u7CFB\n" +
            "</ul>");
    private final DimensionPainter painter = new DimensionPainter();
}