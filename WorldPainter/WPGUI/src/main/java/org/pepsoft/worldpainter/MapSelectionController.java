package org.pepsoft.worldpainter;

import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.operations.Operation;
import org.pepsoft.worldpainter.tools.Eyedropper;
import org.pepsoft.worldpainter.tools.Eyedropper.PaintType;
import org.pepsoft.worldpainter.tools.Eyedropper.SelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.beans.PropertyVetoException;
import java.util.Set;

import static java.awt.Color.BLACK;
import static org.pepsoft.worldpainter.tools.Eyedropper.PaintType.LAYER;

class MapSelectionController {
    MapSelectionController(App app, WorldPainter view) {
        this.app = app;
        this.view = view;
        eyedropper = new Eyedropper(view, app.getColourScheme(), app.getCustomBiomeManager());
    }

    /**
     * Temporarily selects the eyedropper tool to let the user select a paint from the map.
     *
     * @param paintTypes             The type(s) of paint to select, or {@code null} to select among all paint types.
     * @param paintSelectionListener The listener which will be invoked with the selected terrain or layer, if the user
     *                               does not press Esc or select a different tool first.
     */
    void selectPaintOnMap(Set<PaintType> paintTypes, SelectionListener paintSelectionListener) {
        if (this.paintSelectionListener != null) {
            throw new IllegalStateException("Paint selection already in progress");
        }
        this.paintSelectionListener = paintSelectionListener;
        final String paintTypeDescription;
        if (paintTypes == null) {
            paintTypeDescription = "\u753B\u7B14";
        } else {
            final StringBuilder sb = new StringBuilder();
            for (PaintType paintType: paintTypes) {
                switch (paintType) {
                    case LAYER:
                        if (sb.length() > 0) {
                            sb.append(" \u6216 ");
                        }
                        sb.append("\u8986\u76D6\u5C42");
                        break;
                    case TERRAIN:
                        if (sb.length() > 0) {
                            sb.append(" \u6216 ");
                        }
                        sb.append("\u65B9\u5757\u79CD\u7C7B");
                        break;
                    case BIOME:
                        if (sb.length() > 0) {
                            sb.append(" \u6216 ");
                        }
                        sb.append("\u751F\u7269\u7FA4\u7CFB");
                        break;
                    case ANNOTATION:
                        // If LAYER is also selected the user is not being asked to select a specific colour, so don't
                        // add that as it would be misleading
                        if (! paintTypes.contains(LAYER)) {
                            if (sb.length() > 0) {
                                sb.append(" \u6216 ");
                            }
                            sb.append("\u6807\u6CE8\u989C\u8272");
                        }
                        break;
                }
            }
            paintTypeDescription = sb.toString();
        }
        final JLabel label = new JLabel("<html><font size='+1'>\u70B9\u51FB\u5730\u56FE\u9009\u62E9\u4E00\u4E2A" + paintTypeDescription + ".<br>\u6309 Esc \u53D6\u6D88.</font></html>");
        label.setBorder(new CompoundBorder(new LineBorder(BLACK), new EmptyBorder(5, 5, 5, 5)));
        app.pushGlassPaneComponent(label);
        final Operation activeOperation = app.getActiveOperation();
        if (activeOperation != null) {
            try {
                activeOperation.setActive(false);
            } catch (PropertyVetoException e) {
                logger.error("Property veto exception while deactivating operation " + activeOperation, e);
            }
        }
        paintSelectionDrawBrushWasActive = view.isDrawBrush();
        if (paintSelectionDrawBrushWasActive) {
            view.setDrawBrush(false);
        }
        eyedropper.setCallback(new SelectionListener() {
            @Override
            public void terrainSelected(Terrain terrain) {
                cancelPaintSelection(false, false);
                paintSelectionListener.terrainSelected(terrain);
            }

            @Override
            public void layerSelected(Layer layer, int value) {
                cancelPaintSelection(false, false);
                paintSelectionListener.layerSelected(layer, value);
            }

            @Override public void selectionCancelled(boolean byUser) {}
        });
        eyedropper.setPaintTypes(paintTypes);
        try {
            eyedropper.setActive(true);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("Property veto exception while activating eyedropper", e);
        }
    }

    void cancelPaintSelection(boolean notifyListener, boolean cancelledByUser) {
        if (paintSelectionListener != null) {
            SelectionListener paintSelectionListener = this.paintSelectionListener;
            this.paintSelectionListener = null;
            app.popGlassPaneComponent();
            eyedropper.setCallback(null);
            eyedropper.setPaintTypes(null);
            try {
                eyedropper.setActive(false);
            } catch (PropertyVetoException e) {
                logger.error("Property veto exception while deactivating eyedropper", e);
            }
            if (paintSelectionDrawBrushWasActive) {
                view.setDrawBrush(true);
            }
            final Operation activeOperation = app.getActiveOperation();
            if (activeOperation != null) {
                try {
                    activeOperation.setActive(true);
                } catch (PropertyVetoException e) {
                    throw new RuntimeException("Property veto exception while activating " + activeOperation, e);
                }
            }
            if (notifyListener) {
                paintSelectionListener.selectionCancelled(cancelledByUser);
            }
        }
    }

    boolean isSelectionActive() {
        return paintSelectionListener != null;
    }

    private final App app;
    private final WorldPainter view;
    private final Eyedropper eyedropper;
    private SelectionListener paintSelectionListener;
    private boolean paintSelectionDrawBrushWasActive;

    private static final Logger logger = LoggerFactory.getLogger(MapSelectionController.class);
}