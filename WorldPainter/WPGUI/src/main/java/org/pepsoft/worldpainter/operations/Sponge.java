/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.HeightMapTileFactory;
import org.pepsoft.worldpainter.TileFactory;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.layers.FloodWithLava;

import javax.swing.*;

/**
 *
 * @author pepijn
 */
public class Sponge extends RadiusOperation {
    public Sponge(WorldPainterView view) {
        super("Sponge", "\u6D77\u7EF5\u5DE5\u5177-\u5438\u6536\u6216\u6E05\u9664\u5CA9\u6D46\u6216\u6C34", view, 100, "operation.sponge");
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
        final int waterHeight, minHeight = dimension.getMinHeight();
        final TileFactory tileFactory = dimension.getTileFactory();
        if (tileFactory instanceof HeightMapTileFactory) {
            waterHeight = ((HeightMapTileFactory) tileFactory).getWaterHeight();
        } else {
            // If we can't determine the water height disable the inverse
            // functionality, which resets to the default water height
            waterHeight = -1;
        }
        dimension.setEventsInhibited(true);
        try {
            final int radius = getEffectiveRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (getStrength(centreX, centreY, centreX + dx, centreY + dy) != 0f) {
                        if (inverse) {
                            if (waterHeight != -1) {
                                dimension.setWaterLevelAt(centreX + dx, centreY + dy, waterHeight);
                                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, centreX + dx, centreY + dy, false);
                            }
                        } else {
                            dimension.setWaterLevelAt(centreX + dx, centreY + dy, minHeight);
                        }
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    private static final JPanel OPTIONS_PANEL = new StandardOptionsPanel("\u6D77\u7EF5\u5DE5\u5177", "<ul><li>\u5DE6\u952E\u79FB\u9664\u5CA9\u6D46\u6216\u6C34<li>\u53F3\u952E\u4F7F\u5176\u91CD\u7F6E\u4E3A\u9ED8\u8BA4\u6DB2\u4F53\u79CD\u7C7B\u548C\u9AD8\u5EA6</ul>");
}