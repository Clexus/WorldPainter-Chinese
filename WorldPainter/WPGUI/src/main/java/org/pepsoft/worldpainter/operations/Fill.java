package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.brushes.Brush;
import org.pepsoft.worldpainter.painting.DimensionPainter;
import org.pepsoft.worldpainter.painting.Paint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import static org.pepsoft.util.swing.MessageUtils.showWarning;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE_BITS;

/**
 * Created by pepijn on 14-5-15.
 */
public class Fill extends AbstractBrushOperation implements PaintOperation {
    public Fill(WorldPainterView view) {
        super("Fill", "\u586B\u5145-\u4F7F\u7528\u4EFB\u4F55\u7C7B\u578B\u7684\u8986\u76D6\u5C42\u6216\u65B9\u5757\u586B\u5145\u4E16\u754C\u4E2D\u7684\u4E00\u4E2A\u533A\u57DF", view, "operation.fill");
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        // We have seen in the wild that this sometimes gets called recursively (perhaps someone clicks to fill more
        // than once and then it takes more than two seconds so it is continued in the background and event queue
        // processing is resumed?), which causes errors, so just ignore it if we are already filling.
        if (alreadyFilling) {
            logger.debug("Fill operation already in progress; ignoring repeated invocation");
            return;
        }
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        if (! dimension.isTilePresent(centreX >> TILE_SIZE_BITS, centreY >> TILE_SIZE_BITS)) {
            // Just silently fail if the user clicks outside the present area
            return;
        }
        alreadyFilling = true;
        try {
            painter.setUndo(inverse);
            synchronized (dimension) {
                dimension.setEventsInhibited(true);
            }
            try {
                if (! painter.fill(dimension, centreX, centreY, SwingUtilities.getWindowAncestor(getView()))) {
                    showWarning(getView(), "\u9700\u8981\u586B\u5145\u7684\u533A\u57DF\u8FC7\u5927\uFF0C\u53EF\u80FD\u65E0\u6CD5\u88AB\u5B8C\u5168\u586B\u5145.", "\u533A\u57DF\u8FC7\u5927");
                }
            } catch (IndexOutOfBoundsException e) {
                // This most likely indicates that the area being flooded was too large
                synchronized (dimension) {
                    if (dimension.undoChanges()) {
                        dimension.clearRedo();
                        dimension.armSavePoint();
                    }
                }
                JOptionPane.showMessageDialog(getView(), "\u9700\u8981\u586B\u5145\u7684\u533A\u57DF\u8FC7\u5927\u6216\u8FC7\u4E8E\u590D\u6742\uFF0C\u8BF7\u5C1D\u8BD5\u4E00\u4E2A\u66F4\u5C0F\u7684\u533A\u57DF.", "\u533A\u57DF\u8FC7\u5927", JOptionPane.ERROR_MESSAGE);
            } finally {
                synchronized (dimension) {
                    dimension.setEventsInhibited(false);
                }
            }
        } finally {
            alreadyFilling = false;
        }
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

    private final DimensionPainter painter = new DimensionPainter();
    private boolean alreadyFilling;

    private static final JPanel OPTIONS_PANEL = new StandardOptionsPanel("\u586B\u5145", "<ul>" +
            "<li>\u5DE6\u952E\u70B9\u51FB\u4E00\u4E2A\u4F4D\u7F6E\uFF0C\u5C06\u8BE5\u533A\u57DF\u586B\u5145\u4E3A\u5F53\u524D\u9009\u4E2D\u7684\u753B\u7B14\u7C7B\u578B\uFF0C\u5176\u4E2D\u5F53\u524D\u9009\u4E2D\u753B\u7B14\u7C7B\u578B\u7684\u503C\u4E0E\u6307\u793A\u4F4D\u7F6E\u76F8\u540C\u3002\n" +
            "<li>\u9009\u4E2D\u8986\u76D6\u5C42\u65F6\uFF0C\u53F3\u952E\u5C06\u70B9\u51FB\u4F4D\u7F6E\u7684\u8986\u76D6\u5C42(\u82E5\u4E0E\u9009\u4E2D\u8986\u76D6\u5C42\u76F8\u540C)\u4ECE\u533A\u57DF\u4E2D\u79FB\u9664\u3002\n" +
            "<li>\u9009\u4E2D\u4E00\u4E2A\u65B9\u5757\u65F6\uFF0C\u53F3\u952E\u5C06\u70B9\u51FB\u4F4D\u7F6E\u7684\u65B9\u5757(\u82E5\u4E0E\u9009\u4E2D\u65B9\u5757\u76F8\u540C)\u91CD\u7F6E\u4E3A\u5F53\u524D\u4E3B\u9898\u7684\u65B9\u5757\u3002\n" +
            "<li>\u9009\u4E2D\u4E00\u4E2A\u751F\u7269\u7FA4\u7CFB\u65F6\uFF0C\u53F3\u952E\u5C06\u70B9\u51FB\u4F4D\u7F6E\u7684\u751F\u7269\u7FA4\u7CFB(\u82E5\u4E0E\u9009\u4E2D\u7FA4\u7CFB\u76F8\u540C)\u91CD\u7F6E\u4E3A\u81EA\u52A8\u751F\u7269\u7FA4\u7CFB\u3002" +
            "</ul>");
    private static final Logger logger = LoggerFactory.getLogger(Fill.class);
}