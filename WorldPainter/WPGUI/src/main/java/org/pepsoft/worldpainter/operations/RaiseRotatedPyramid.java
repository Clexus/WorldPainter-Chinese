/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Terrain;
import org.pepsoft.worldpainter.WorldPainter;

import javax.swing.*;

/**
 *
 * @author pepijn
 */
public class RaiseRotatedPyramid extends MouseOrTabletOperation {
    public RaiseRotatedPyramid(WorldPainter worldPainter) {
        super("Raise Rotated Pyramid", "拔升旋转金字塔工具-在地面上升起一座旋转45度的金字塔", worldPainter, 100, "operation.raiseRotatedPyramid", "pyramid");
    }

    @Override
    public JPanel getOptionsPanel() {
        return OPTIONS_PANEL;
    }

    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        float height = dimension.getHeightAt(centreX, centreY);
        dimension.setEventsInhibited(true);
        try {
            if (height < (dimension.getMaxHeight() - 1.5f)) {
                dimension.setHeightAt(centreX, centreY, height + 1);
            }
            dimension.setTerrainAt(centreX, centreY, Terrain.SANDSTONE);
            int maxR = dimension.getMaxHeight() - dimension.getMinHeight();
            for (int r = 1; r < maxR; r++) {
                if (! raiseRing(dimension, centreX, centreY, r, height--)) {
                    break;
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    private boolean raiseRing(Dimension dimension, int x, int y, int r, float desiredHeight) {
        boolean raised = false;
        for (int i = 0; i < r; i++) {
            float actualHeight = dimension.getHeightAt(x - r + i, y - i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x - r + i, y - i, desiredHeight);
                dimension.setTerrainAt(x - r + i, y - i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x + i, y - r + i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + i, y - r + i, desiredHeight);
                dimension.setTerrainAt(x + i, y - r + i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x + r - i, y + i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x + r - i, y + i, desiredHeight);
                dimension.setTerrainAt(x + r - i, y + i, Terrain.SANDSTONE);
            }
            actualHeight = dimension.getHeightAt(x - i, y + r - i);
            if (actualHeight < desiredHeight) {
                raised = true;
                dimension.setHeightAt(x - i, y + r - i, desiredHeight);
                dimension.setTerrainAt(x - i, y + r - i, Terrain.SANDSTONE);
            }
        }
        return raised;
    }

    private static final StandardOptionsPanel OPTIONS_PANEL = new StandardOptionsPanel("拔升旋转金字塔工具", "<p>点击在地表升起一座45度旋转过的四边金字塔");
}