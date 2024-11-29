package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.brushes.Brush;
import org.pepsoft.worldpainter.layers.Annotations;
import org.pepsoft.worldpainter.layers.exporters.AnnotationsExporter;
import org.pepsoft.worldpainter.painting.DimensionPainter;
import org.pepsoft.worldpainter.painting.Paint;
import org.pepsoft.worldpainter.painting.PaintFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pepijn on 14-5-15.
 */
public class Text extends AbstractBrushOperation implements PaintOperation {
    public Text(WorldPainterView view) {
        super("Text", "\u6587\u672C\u5DE5\u5177-\u4F7F\u7528\u4EFB\u610F\u8986\u76D6\u5C42\u6216\u65B9\u5757\u4EE5\u4EFB\u610F\u5B57\u4F53\u3001\u5B57\u53F7\u548C\u89D2\u5EA6\u751F\u6210\u6587\u5B57", view, "operation.text");
    }

    @Override
    public Paint getPaint() {
        return painter.getPaint();
    }

    @Override
    public void setPaint(Paint paint) {
        if (getBrush() != null) {
            paint.setBrush(getBrush());
        }
        painter.setPaint(paint);
    }

    @Override
    public JPanel getOptionsPanel() {
        return OPTIONS_PANEL;
    }

    @Override
    protected void brushChanged(Brush newBrush) {
        if (painter.getPaint() != null) {
            painter.getPaint().setBrush(newBrush);
        }
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        if (painter.getPaint() instanceof PaintFactory.NullPaint) {
            // No paint set yet; do nothing
            return;
        }
        AnnotationsExporter.AnnotationsSettings settings = (AnnotationsExporter.AnnotationsSettings) getDimension().getLayerSettings(Annotations.INSTANCE);
        if (settings == null) {
            settings = new AnnotationsExporter.AnnotationsSettings();
        }
        TextDialog dialog = new TextDialog(SwingUtilities.getWindowAncestor(getView()), settings.getDefaultFont(), settings.getDefaultSize(), savedText);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            Font font = dialog.getSelectedFont();
            settings.setDefaultFont(font.getFamily());
            settings.setDefaultSize(font.getSize());
            dimension.setLayerSettings(Annotations.INSTANCE, settings);
            savedText = dialog.getText();
            if (! savedText.trim().isEmpty()) {
                painter.setFont(font);
                painter.setTextAngle(dialog.getSelectedAngle());
                dimension.setEventsInhibited(true);
                try {
                    painter.drawText(dimension, centreX, centreY, savedText);
                } finally {
                    dimension.setEventsInhibited(false);
                }
            }
        }
    }

    private final DimensionPainter painter = new DimensionPainter();
    private String savedText;

    private static final JPanel OPTIONS_PANEL = new StandardOptionsPanel("\u6587\u672C\u5DE5\u5177", "<p>\u70B9\u51FB\u4EE5\u5728\u6307\u5B9A\u4F4D\u7F6E\u5F62\u6210\u6587\u672C\uFF0C\u6587\u672C\u7684\u5DE6\u4E0A\u89D2\u4E0E\u5F53\u524D\u9009\u4E2D\u7684\u753B\u7B14\u7C7B\u578B\u5BF9\u9F50\u3002");
}