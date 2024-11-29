/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainter;

import javax.swing.*;

/**
 *
 * @author pepijn
 */
public class Flatten extends RadiusOperation {
    public Flatten(WorldPainter view) {
        super("Flatten", "\u5E73\u6574\u5DE5\u5177-\u4F7F\u533A\u57DF\u53D8\u5E73\u6574", view, 100, "operation.flatten");
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    protected void tick(final int centreX, final int centreY, final boolean inverse, final boolean first, final float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        if (first) {
            targetHeight = dimension.getHeightAt(centreX, centreY);
            if (targetHeight == -Float.MAX_VALUE) {
                DesktopUtils.beep();
            }
        }
        if (targetHeight == -Float.MAX_VALUE) {
            return;
        }
        dimension.setEventsInhibited(true);
        try {
            int radius = getEffectiveRadius();
            boolean applyTheme = options.isApplyTheme();
            for (int x = centreX - radius; x <= centreX + radius; x++) {
                for (int y = centreY - radius; y <= centreY + radius; y++) {
                    float currentHeight = dimension.getHeightAt(x, y);
                    float strength = dynamicLevel * getStrength(centreX, centreY, x, y);
                    if (strength > 0.0f) {
                        float newHeight = strength * targetHeight  + (1f - strength) * currentHeight;
                        dimension.setHeightAt(x, y, newHeight);
                        if (applyTheme) {
                            dimension.applyTheme(x, y);
                        }
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    private final TerrainShapingOptions<Flatten> options = new TerrainShapingOptions<>();
    private final TerrainShapingOptionsPanel optionsPanel = new TerrainShapingOptionsPanel("\u5E73\u6574\u5DE5\u5177", "<p>\u70B9\u51FB\u5E73\u6574\u5730\u5F62", options);
    private float targetHeight = -Float.MAX_VALUE;
}