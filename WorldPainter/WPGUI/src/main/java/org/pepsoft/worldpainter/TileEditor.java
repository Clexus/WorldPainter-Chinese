/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TileEditor.java
 *
 * Created on Mar 21, 2012, 12:14:42 PM
 */
package org.pepsoft.worldpainter;

import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.history.HistoryEntry;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.NotPresent;
import org.pepsoft.worldpainter.layers.NotPresentBlock;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Dimension.Role.DETAIL;
import static org.pepsoft.worldpainter.Dimension.Role.MASTER;

/**
 *
 * @author pepijn
 */
public class TileEditor extends WorldPainterModalFrame implements TileSelector.Listener {
    /** Creates new form TileEditor */
    public TileEditor(java.awt.Frame parent, Dimension dimension, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Set<Layer> hiddenLayers, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin) {
        super(parent);
        this.dimension = dimension;
        final Dimension.Anchor anchor = dimension.getAnchor();
        if (anchor.role == DETAIL) {
            backgroundDimension = dimension.getWorld().getDimension(new Dimension.Anchor(anchor.dim, MASTER, anchor.invert, 0));
        } else {
            backgroundDimension = null;
        }
        initComponents();
        
        // Fix the incredibly ugly default font of the JTextPane
        Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument) jTextPane1.getDocument()).getStyleSheet().addRule(bodyRule);
        
        tileSelector1.setColourScheme(colourScheme);
        tileSelector1.setHiddenLayers(hiddenLayers);
        tileSelector1.setContourLines(contourLines);
        tileSelector1.setContourSeparation(contourSeparation);
        tileSelector1.setLightOrigin(lightOrigin);
        tileSelector1.setCustomBiomeManager(customBiomeManager);
        tileSelector1.setDimension(dimension);
        tileSelector1.addListener(this);

        getRootPane().setDefaultButton(buttonClose);

        scaleToUI();
        pack();
        scaleWindowToUI();
        setLocationRelativeTo(parent);
    }

    public boolean isTilesChanged() {
        return tilesChanged;
    }

    public void moveTo(Point coords) {
        tileSelector1.moveTo(coords);
    }

    // TileSelector.Listener
    
    @Override
    public void selectionChanged(TileSelector tileSelector, Set<Point> newSelection) {
        setControlStates();
    }
    
    private void setControlStates() {
        Set<Point> selectedTiles = tileSelector1.getSelectedTiles();
        boolean allowAddTiles, allowRemoveTiles;
        if (selectedTiles.isEmpty()) {
            allowAddTiles = allowRemoveTiles = false;
        } else {
            allowAddTiles = false;
            allowRemoveTiles = false;
            for (Point selectedTile: selectedTiles) {
                Tile existingTile = dimension.getTile(selectedTile);
                if (existingTile != null) {
                    allowRemoveTiles = true;
                    if (existingTile.hasLayer(NotPresent.INSTANCE) || existingTile.hasLayer(NotPresentBlock.INSTANCE)) {
                        allowAddTiles = true;
                    }
                } else {
                    allowAddTiles = true;
                }
            }
        }
        buttonAddTiles.setEnabled(allowAddTiles);
        buttonRemoveTiles.setEnabled(allowRemoveTiles);
    }
    
    private void addTiles() {
        Set<Point> selectedTiles = tileSelector1.getSelectedTiles();
        Set<Point> tilesToAdd = new HashSet<>(), tilesToExpand = new HashSet<>();
        int newLowestTileX = dimension.getLowestX(), newHighestTileX = dimension.getHighestX();
        int newLowestTileY = dimension.getLowestY(), newHighestTileY = dimension.getHighestY();
        for (Point selectedTile: selectedTiles) {
            Tile existingTile = dimension.getTile(selectedTile);
            if (existingTile == null) {
                tilesToAdd.add(selectedTile);
                if (selectedTile.x < newLowestTileX) {
                    newLowestTileX = selectedTile.x;
                }
                if (selectedTile.x > newHighestTileX) {
                    newHighestTileX = selectedTile.x;
                }
                if (selectedTile.y < newLowestTileY) {
                    newLowestTileY = selectedTile.y;
                }
                if (selectedTile.y > newHighestTileY) {
                    newHighestTileY = selectedTile.y;
                }
            } else if (existingTile.hasLayer(NotPresent.INSTANCE) || existingTile.hasLayer(NotPresentBlock.INSTANCE)) {
                tilesToExpand.add(selectedTile);
            }
        }
        if (tilesToAdd.isEmpty() && tilesToExpand.isEmpty()) {
            return;
        }
        
        // Try to guestimate whether there is enough memory to add the selected
        // number of tiles, and if not, ask the user whether they want to
        // continue at their own risk
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long memoryInUse = totalMemory - freeMemory;
        long availableMemory = maxMemory - memoryInUse;
        // Allow room for export
        availableMemory -= 250000000L;
        // Convert to KB
        availableMemory /= 1024;
        // Guestimate data and image size
        long totalEstimatedDataSize = tilesToAdd.size() * NewWorldDialog.ESTIMATED_TILE_DATA_SIZE;
        long totalEstimatedImageSize = (newHighestTileX - newLowestTileX + 1L) * TILE_SIZE * (newHighestTileY - newLowestTileY + 1) * TILE_SIZE * 4 / 1024;
        long currentImageSize = (dimension.getHighestX() - dimension.getLowestX() + 1L) * TILE_SIZE * (dimension.getHighestY() - dimension.getLowestY() + 1) * TILE_SIZE * 4 / 1024;
        long totalEstimatedSize = totalEstimatedDataSize + totalEstimatedImageSize - currentImageSize;
        if (totalEstimatedSize > availableMemory) {
            if (JOptionPane.showConfirmDialog(this, "There may not be enough memory to add " + tilesToAdd.size() + " tiles!\nIt may fail, or cause errors later on.\nPlease consider adding fewer tiles, or installing more memory.\nDo you want to continue?", "Too Many Tiles", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
        } else {
            String action = (tilesToAdd.isEmpty())
                ? ("\u6269\u5C55 " + tilesToExpand.size() + " \u4E2A\u5206\u533A")
                    : (tilesToExpand.isEmpty()
                        ? ("\u6DFB\u52A0 " + tilesToAdd.size() + " \u4E2A\u65B0\u5206\u533A")
                        : ("\u6DFB\u52A0 " + tilesToAdd.size() + " \u4E2A\u65B0\u5206\u533A\u5E76\u6269\u5C55 " + tilesToExpand.size() + " \u4E2A\u5206\u533A"));
            if (JOptionPane.showConfirmDialog(this, "\u4F60\u786E\u8BA4\u8981" + action + "\u5417?\n\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!", "\u786E\u8BA4\u6DFB\u52A0/\u6269\u5C55\u5206\u533A", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        dimension.setEventsInhibited(true);
        dimension.clearUndo();
        try {
            final ScaledDimension scaledBackground = (! tilesToAdd.isEmpty()) && (backgroundDimension != null) ? new ScaledDimension(backgroundDimension, backgroundDimension.getScale()) : null;
            for (Point newTileCoords: tilesToAdd) {
                Tile newTile = null;
                if (backgroundDimension != null) {
                    newTile = scaledBackground.getTile(newTileCoords);
                    // TODO: handle copied custom layers
                    // TODO: handle NotPresent areas
                }
                if (newTile == null) {
                    newTile = dimension.getTileFactory().createTile(newTileCoords.x, newTileCoords.y);
                }
                dimension.addTile(newTile);
            }
            for (Point expandTileCoords: tilesToExpand) {
                final Tile tile = dimension.getTileForEditing(expandTileCoords);
                tile.clearLayerData(NotPresent.INSTANCE);
                tile.clearLayerData(NotPresentBlock.INSTANCE);
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
        dimension.armSavePoint();
        tilesChanged = true;

        World2 world = dimension.getWorld();
        if (world != null) {
            world.addHistoryEntry(HistoryEntry.WORLD_TILES_ADDED, dimension.getName(), tilesToAdd.size());
        }

        tileSelector1.clearSelection();
        tileSelector1.refresh();
        setControlStates();
    }

    private void removeTiles() {
        Set<Point> selectedTiles = tileSelector1.getSelectedTiles();
        Set<Point> tilesToRemove = selectedTiles.stream().filter(selectedTile -> dimension.getTile(selectedTile) != null).collect(Collectors.toSet());
        if (tilesToRemove.isEmpty()) {
            return;
        }
        if (tilesToRemove.size() == dimension.getTileCount()) {
            JOptionPane.showMessageDialog(this, "<html>\u4F60\u4E0D\u80FD\u79FB\u9664\u7EF4\u5EA6\u4E2D<em>\u6240\u6709\u7684</em>\u5206\u533A!</html>", "\u9009\u4E2D\u4E86\u6240\u6709\u5206\u533A", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "\u4F60\u786E\u8BA4\u8981\u79FB\u9664" + tilesToRemove.size() + "\u4E2A\u5206\u533A\u5417?\n\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!", "\u786E\u8BA4\u79FB\u9664\u5206\u533A", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dimension.setEventsInhibited(true);
            dimension.clearUndo();
            try {
                tilesToRemove.forEach(dimension::removeTile);
            } finally {
                dimension.setEventsInhibited(false);
            }
            dimension.armSavePoint();
            tilesChanged = true;
            World2 world = dimension.getWorld();
            if (world != null) {
                world.addHistoryEntry(HistoryEntry.WORLD_TILES_REMOVED, dimension.getName(), tilesToRemove.size());
            }
            tileSelector1.clearSelection();
            tileSelector1.refresh();
            setControlStates();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonClose = new javax.swing.JButton();
        buttonAddTiles = new javax.swing.JButton();
        buttonRemoveTiles = new javax.swing.JButton();
        tileSelector1 = new org.pepsoft.worldpainter.TileSelector();
        jTextPane1 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("WorldPainter - \u6DFB\u52A0, \u6269\u5C55\u6216\u79FB\u9664\u5206\u533A");

        buttonClose.setText("\u5173\u95ED");
        buttonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCloseActionPerformed(evt);
            }
        });

        buttonAddTiles.setText("\u6DFB\u52A0\u6216\u6269\u5C55\u5206\u533A");
        buttonAddTiles.setEnabled(false);
        buttonAddTiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddTilesActionPerformed(evt);
            }
        });

        buttonRemoveTiles.setText("\u79FB\u9664\u5206\u533A");
        buttonRemoveTiles.setEnabled(false);
        buttonRemoveTiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveTilesActionPerformed(evt);
            }
        });

        tileSelector1.setAllowNonExistentTileSelection(true);

        jTextPane1.setEditable(false);
        jTextPane1.setContentType("text/html"); // NOI18N
        jTextPane1.setText("WorldPainter \u7684\u8FD0\u884C\u57FA\u7840\u4E3A 128 x 128 \u683C\u7684\u5206\u533A.\n\u4F60\u53EF\u4EE5\u5728\u6B64\u754C\u9762\u6DFB\u52A0, \u6269\u5C55\u6216\u79FB\u9664\u5206\u533A.<br>\n<br>\n\u4F7F\u7528\u5DE6\u952E\u9009\u62E9\u5206\u533A,\n\u4F7F\u7528\u4E2D\u952E\u6216\u53F3\u952E\u79FB\u52A8\u5730\u56FE,\n\u7136\u540E\u9009\u62E9\u4EE5\u4E0B\u64CD\u4F5C:<br>\n<br>\n\u62D6\u62FD\u6765\u9009\u4E2D\u5206\u533A; \u6309\u4F4F Ctrl \u5E76\u62D6\u62FD\u6765\u53D6\u6D88\u9009\u62E9\u5206\u533A.<br>\n<br>\n<b>\u6CE8\u610F:</b> \u8BE5\u64CD\u4F5C\u5C06\u79FB\u9664\u6240\u6709\u7684\u64A4\u9500\u8BB0\u5F55!");
        jTextPane1.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonClose)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonAddTiles)
                            .addComponent(buttonRemoveTiles))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tileSelector1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tileSelector1, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonClose))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(buttonAddTiles)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveTiles)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCloseActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCloseActionPerformed

    private void buttonAddTilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddTilesActionPerformed
        addTiles();
    }//GEN-LAST:event_buttonAddTilesActionPerformed

    private void buttonRemoveTilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveTilesActionPerformed
        removeTiles();
    }//GEN-LAST:event_buttonRemoveTilesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddTiles;
    private javax.swing.JButton buttonClose;
    private javax.swing.JButton buttonRemoveTiles;
    private javax.swing.JTextPane jTextPane1;
    private org.pepsoft.worldpainter.TileSelector tileSelector1;
    // End of variables declaration//GEN-END:variables

    private final Dimension dimension, backgroundDimension;
    private boolean tilesChanged = false;
    
    private static final long serialVersionUID = 1L;
}