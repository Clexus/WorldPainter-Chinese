/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportWorldDialog.java
 *
 * Created on Mar 29, 2011, 5:09:50 PM
 */

package org.pepsoft.worldpainter;

import org.pepsoft.util.AttributeKey;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.exporting.WorldExportSettings;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.merging.JavaWorldMerger;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.plugins.PlatformProvider;
import org.pepsoft.worldpainter.util.MapUtils;
import org.pepsoft.worldpainter.util.MaterialUtils;
import org.pepsoft.worldpainter.util.MinecraftUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.worldpainter.App.MERGE_WARNING_KEY;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.Dimension.Anchor.*;
import static org.pepsoft.worldpainter.Platform.Capability.*;
import static org.pepsoft.worldpainter.exporting.WorldExportSettings.EXPORT_EVERYTHING;
import static org.pepsoft.worldpainter.util.BackupUtils.cleanUpBackups;

/**
 *
 * @author pepijn
 */
// TODO: add support for multiple dimensions
@SuppressWarnings({"unused", "FieldCanBeLocal", "Convert2Lambda", "Anonymous2MethodRef", "ConstantConditions"}) // Managed by NetBeans
public class MergeWorldDialog extends WorldPainterDialog {
    /** Creates new form ExportWorldDialog */
    public MergeWorldDialog(Window parent, World2 world, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Set<Layer> hiddenLayers, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin, WorldPainter view) {
        super(parent);
        this.world = world;
        this.colourScheme = colourScheme;
        this.hiddenLayers = hiddenLayers;
        this.contourLines = contourLines;
        this.contourSeparation = contourSeparation;
        this.lightOrigin = lightOrigin;
        this.customBiomeManager = customBiomeManager;
        this.view = view;
        final WorldExportSettings exportSettings = (world.getExportSettings() != null) ? world.getExportSettings() : EXPORT_EVERYTHING;
        selectedTiles = exportSettings.getTilesToExport();
        selectedDimension = (selectedTiles != null) ? exportSettings.getDimensionsToExport().iterator().next() : DIM_NORMAL;
        savedSteps = exportSettings.getStepsToSkip();
        
        initComponents();

        Configuration config = Configuration.getInstance();
        if (world.getMergedWith() != null) {
            fieldSelectedMapDir.setText(world.getMergedWith().getParentFile().getAbsolutePath());
        } else if (world.getImportedFrom() != null) {
            fieldSelectedMapDir.setText(world.getImportedFrom().getParentFile().getAbsolutePath());
        } else if ((config != null) && (config.getSavesDirectory() != null)) {
            fieldSelectedMapDir.setText(config.getSavesDirectory().getAbsolutePath());
        } else {
            File minecraftDir = MinecraftUtil.findMinecraftDir();
            if (minecraftDir != null) {
                fieldSelectedMapDir.setText(new File(minecraftDir, "saves").getAbsolutePath());
            } else {
                fieldSelectedMapDir.setText(DesktopUtils.getDocumentsFolder().getAbsolutePath());
            }
        }
        ((SpinnerNumberModel) spinnerSurfaceThickness.getModel()).setMaximum(world.getMaxHeight());
        if (selectedTiles != null) {
            radioButtonExportSelection.setText("\u5408\u5E76 " + selectedTiles.size() + " \u4E2A\u9009\u4E2D\u7684\u5206\u533A");
            radioButtonExportSelection.setSelected(true);
            checkBoxSurface.setSelected(selectedDimension == DIM_NORMAL);
            checkBoxNether.setSelected(selectedDimension == DIM_NETHER);
            checkBoxEnd.setSelected(selectedDimension == DIM_END);
        } else if (exportSettings.getDimensionsToExport() != null) {
            checkBoxSurface.setSelected(exportSettings.getDimensionsToExport().contains(DIM_NORMAL));
            checkBoxNether.setSelected(exportSettings.getDimensionsToExport().contains(DIM_NETHER));
            checkBoxEnd.setSelected(exportSettings.getDimensionsToExport().contains(DIM_END));
        } else {
            checkBoxSurface.setSelected(world.isDimensionPresent(NORMAL_DETAIL));
            checkBoxNether.setSelected(world.isDimensionPresent(NETHER_DETAIL));
            checkBoxEnd.setSelected(world.isDimensionPresent(END_DETAIL));
        }
        world.getAttribute(ATTRIBUTE_MERGE_SETTINGS).ifPresent(mergeSettings -> {
            if (mergeSettings.replaceChunks) {
                radioButtonReplaceChunks.setSelected(true);
            } else {
                radioButtonAll.setSelected(true);
            }
            checkBoxAboveMergeBlocks.setSelected(mergeSettings.mergeBlocksAboveGround);
            checkBoxBelowMergeBlocks.setSelected(mergeSettings.mergeBlocksUnderground);
            checkBoxAboveMergeBiomes.setSelected(mergeSettings.mergeBiomesAboveGround);
            checkBoxBelowMergeBiomes.setSelected(mergeSettings.mergeBiomesUnderground);
            checkBoxRemoveTrees.setSelected(mergeSettings.clearTrees);
            checkBoxRemoveVegetation.setSelected(mergeSettings.clearVegetation);
            checkBoxRemoveManMadeAboveGround.setSelected(mergeSettings.clearManMadeAboveGround);
            checkBoxRemoveResources.setSelected(mergeSettings.clearResources);
            checkBoxFillCaves.setSelected(mergeSettings.fillCaves);
            checkBoxRemoveManMadeBelowGround.setSelected(mergeSettings.clearManMadeBelowGround);
            spinnerSurfaceThickness.setValue(mergeSettings.surfaceMergeDepth);
        });

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setControlStates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setControlStates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setControlStates();
            }
        };
        fieldSelectedMapDir.getDocument().addDocumentListener(documentListener);

        setLocationRelativeTo(parent);

        rootPane.setDefaultButton(buttonMerge);

        setControlStates();
        scaleToUI();
        pack();
    }

    private void merge() {
        // TODOMC13 elegantly prevent merging with incompatible platform, just like Export screen
        // Check for errors
        if (mapDir == null) {
            fieldSelectedMapDir.requestFocusInWindow();
            beepAndShowError(this, "\u6CA1\u6709\u9009\u4E2D\u73B0\u6709\u7684\u5730\u56FE.", "\u9519\u8BEF");
            return;
        } else if (platform == null) {
            fieldSelectedMapDir.requestFocusInWindow();
            beepAndShowError(this, "\u9009\u4E2D\u7684\u5730\u56FE\u683C\u5F0F\u4E0D\u53D7\u652F\u6301.", "\u9519\u8BEF");
            return;
        }
        if (! checkCompatibility(platform)) {
            return;
        }
        if ((! radioButtonExportEverything.isSelected()) && ((selectedTiles == null) || selectedTiles.isEmpty())) {
            radioButtonExportEverything.requestFocusInWindow();
            beepAndShowError(this, "\u6CA1\u6709\u9009\u62E9\u8981\u5408\u5E76\u7684\u5206\u533A.", "\u9519\u8BEF");
            return;
        }
        if ((! checkBoxSurface.isSelected()) && (! checkBoxNether.isSelected()) && (! checkBoxEnd.isSelected())) {
            checkBoxSurface.requestFocusInWindow();
            beepAndShowError(this, "\u6CA1\u6709\u9009\u62E9\u8981\u5408\u5E76\u7684\u7EF4\u5EA6.", "\u9519\u8BEF");
            return;
        }

        final WorldExportSettings exportSettings;
        if (radioButtonExportEverything.isSelected()) {
            Set<Integer> dimensionsToExport = new HashSet<>();
            if (checkBoxSurface.isSelected()) {
                dimensionsToExport.add(DIM_NORMAL);
            }
            if (checkBoxNether.isSelected()) {
                dimensionsToExport.add(DIM_NETHER);
            }
            if (checkBoxEnd.isSelected()) {
                dimensionsToExport.add(DIM_END);
            }
            boolean allDimensionsSelected = true;
            for (Dimension dimension: world.getDimensions()) {
                if (! dimensionsToExport.contains(dimension.getAnchor().dim)) {
                    allDimensionsSelected = false;
                    break;
                }
            }
            exportSettings = allDimensionsSelected
                    ? ((savedSteps != null) ? new WorldExportSettings(null, null, savedSteps) : EXPORT_EVERYTHING)
                    : new WorldExportSettings(dimensionsToExport, null, savedSteps);
        } else {
            exportSettings = new WorldExportSettings(singleton(selectedDimension), selectedTiles, savedSteps);
        }
        final boolean replaceChunks = radioButtonReplaceChunks.isSelected();
        final JavaWorldMerger merger = new JavaWorldMerger(world, exportSettings, mapDir, platform);
        try {
            if (replaceChunks) {
                merger.setReplaceChunks(true);
            } else {
                merger.setMergeBlocksAboveGround(checkBoxAboveMergeBlocks.isSelected());
                merger.setMergeBlocksUnderground(checkBoxBelowMergeBlocks.isSelected());
                merger.setMergeBiomesAboveGround(platform.supportsBiomes() && checkBoxAboveMergeBiomes.isSelected());
                merger.setMergeBiomesUnderground((platform.capabilities.contains(BIOMES_3D) || platform.capabilities.contains(NAMED_BIOMES)) && checkBoxBelowMergeBiomes.isSelected());
                merger.setClearManMadeAboveGround(checkBoxRemoveManMadeAboveGround.isSelected());
                merger.setClearManMadeBelowGround(checkBoxRemoveManMadeBelowGround.isSelected());
                merger.setClearResources(checkBoxRemoveResources.isSelected());
                merger.setClearTrees(checkBoxRemoveTrees.isSelected());
                merger.setClearVegetation(checkBoxRemoveVegetation.isSelected());
                merger.setFillCaves(checkBoxFillCaves.isSelected());
                merger.setSurfaceMergeDepth((Integer) spinnerSurfaceThickness.getValue());
            }
            merger.performSanityChecks();
        } catch (IllegalArgumentException e) {
            logger.error(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            beepAndShowError(this, e.getLocalizedMessage(), "\u9519\u8BEF");
            return;
        } catch (IOException e) {
            throw new RuntimeException("I/O error reading level.dat file", e);
        }

        // Check for warnings
        StringBuilder sb = new StringBuilder("<html>\u8BF7\u786E\u8BA4\u5728\u6709\u4EE5\u4E0B\u8B66\u544A\u7684\u60C5\u51B5\u4E0B\u4ECD\u8981\u5408\u5E76\u4E16\u754C:<br><ul>");
        boolean showWarning = false;
        if ((radioButtonExportSelection.isSelected()) && (! disableTileSelectionWarning)) {
            String dim;
            switch (selectedDimension) {
                case DIM_NORMAL:
                    dim = "\u4E3B\u4E16\u754C";
                    break;
                case DIM_NETHER:
                    dim = "\u4E0B\u754C";
                    break;
                case DIM_END:
                    dim = "\u672B\u5730";
                    break;
                default:
                    throw new InternalError();
            }
            sb.append("<li>\u4ECD\u6709\u6D3B\u8DC3\u7684\u5206\u533A\u9009\u533A! "+dim+" \u4E2D\u53EA\u6709 " + selectedTiles.size() + " \u4E2A\u5206\u533A\u4F1A\u88AB\u5408\u5E76");
            showWarning = true;
        }
        sb.append("</ul>\u4F60\u786E\u8BA4\u8981\u7EE7\u7EED\u5408\u5E76\u5417?</html>");
        if (showWarning && (JOptionPane.showConfirmDialog(this, sb.toString(), "\u67E5\u770B\u8B66\u544A", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
            return;
        }

        // Make sure the minimum free disk space is met
        try {
            if (! cleanUpBackups(mapDir.getParentFile(), null)) {
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException("I/O error while cleaning backups", e);
        }

        fieldSelectedMapDir.setEnabled(false);
        buttonSelectDirectory.setEnabled(false);
        buttonMerge.setEnabled(false);
        radioButtonAll.setEnabled(false);
        radioButtonReplaceChunks.setEnabled(false);
        radioButtonExportEverything.setEnabled(false);
        radioButtonExportSelection.setEnabled(false);
        checkBoxFillCaves.setEnabled(false);
        checkBoxRemoveManMadeAboveGround.setEnabled(false);
        checkBoxRemoveManMadeBelowGround.setEnabled(false);
        checkBoxRemoveResources.setEnabled(false);
        checkBoxRemoveTrees.setEnabled(false);
        checkBoxRemoveVegetation.setEnabled(false);
        spinnerSurfaceThickness.setEnabled(false);
        labelSelectTiles.setForeground(null);
        labelSelectTiles.setCursor(null);
        checkBoxSurface.setEnabled(false);
        checkBoxNether.setEnabled(false);
        checkBoxEnd.setEnabled(false);
        checkBoxAboveMergeBlocks.setEnabled(false);
        checkBoxBelowMergeBlocks.setEnabled(false);
        checkBoxAboveMergeBiomes.setEnabled(false);
        checkBoxBelowMergeBiomes.setEnabled(false);

        Configuration config = Configuration.getInstance();
        config.setSavesDirectory(mapDir.getParentFile());
        config.setMessageDisplayed(MERGE_WARNING_KEY);
        world.setMergedWith(new File(mapDir, "level.dat"));

        try {
            backupDir = merger.selectBackupDir(mapDir);
        } catch (IOException e) {
            throw new RuntimeException("I/O error while creating backup directory", e);
        }

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        MergeProgressDialog dialog = new MergeProgressDialog(this, merger, backupDir);
        view.setInhibitUpdates(true);
        try {
            dialog.setVisible(true);
        } finally {
            view.setInhibitUpdates(false);
        }

        synchronized (merger) {
            if (! merger.isAborted()) {
                if (! radioButtonExportEverything.isSelected()) {
                    world.setExportSettings(exportSettings);
                }
                world.setAttribute(ATTRIBUTE_MERGE_SETTINGS, new MergeSettings(
                        radioButtonReplaceChunks.isSelected(),
                        checkBoxAboveMergeBlocks.isSelected(),
                        checkBoxBelowMergeBlocks.isSelected(),
                        checkBoxAboveMergeBiomes.isSelected(),
                        checkBoxBelowMergeBiomes.isSelected(),
                        checkBoxRemoveTrees.isSelected(),
                        checkBoxRemoveVegetation.isSelected(),
                        checkBoxRemoveManMadeAboveGround.isSelected(),
                        checkBoxRemoveResources.isSelected(),
                        checkBoxFillCaves.isSelected(),
                        checkBoxRemoveManMadeBelowGround.isSelected(),
                        (Integer) spinnerSurfaceThickness.getValue()));
            }

            if (merger.getWarnings() != null) {
                DesktopUtils.beep();
                ImportWarningsDialog warningsDialog = new ImportWarningsDialog(MergeWorldDialog.this, "\u5408\u5E76\u8B66\u544A", "<html>\u5408\u5E76\u8FC7\u7A0B\u4EA7\u751F\u4E86\u4E95\u76D6! \u73B0\u6709\u5730\u56FE\u53EF\u80FD\u5B58\u5728\u635F\u574F. \u6709\u90E8\u5206\u533A\u5757\u672A\u88AB\u6B63\u786E\u5408\u5E76.<br>\u8BF7\u4E8E\u4E0B\u65B9\u67E5\u770B\u8FD9\u4E9B\u8B66\u544A:</html>");
                warningsDialog.setWarnings(merger.getWarnings());
                warningsDialog.setVisible(true);
            }
        }

        ok();
    }

    private void setControlStates() {
        final boolean mergeAll = radioButtonAll.isSelected();
        final boolean mergeEverything = radioButtonExportEverything.isSelected();
        final boolean surfacePresent = world.isDimensionPresent(NORMAL_DETAIL);
        final boolean netherPresent = world.isDimensionPresent(NETHER_DETAIL);
        final boolean endPresent = world.isDimensionPresent(END_DETAIL);
        final boolean oneDimensionPresent = world.getDimensions().size() == 1;
        boolean biomesSupported = false, threeDeeBiomesSupported = false;
        final File mapDir = new File(fieldSelectedMapDir.getText().trim());
        if (mapDir.isDirectory()) {
            this.mapDir = mapDir;
            final PlatformProvider.MapInfo mapInfo = PlatformManager.getInstance().identifyMap(mapDir);
            platform = (mapInfo != null) ? mapInfo.platform : null;
            if (platform != null) {
                biomesSupported = platform.supportsBiomes();
                threeDeeBiomesSupported = platform.capabilities.contains(BIOMES_3D) || platform.capabilities.contains(NAMED_BIOMES);
                labelPlatform.setText(platform.displayName);
                labelPlatform.setIcon(mapInfo.icon);
            } else {
                labelPlatform.setText("\u672A\u68C0\u6D4B\u5230\u652F\u6301\u683C\u5F0F");
                labelPlatform.setIcon(null);
            }
        } else {
            this.mapDir = null;
            labelPlatform.setText(null);
        }
        checkBoxAboveMergeBlocks.setEnabled(mergeAll);
        checkBoxBelowMergeBlocks.setEnabled(mergeAll);
        checkBoxAboveMergeBiomes.setEnabled(mergeAll && biomesSupported);
        checkBoxBelowMergeBiomes.setEnabled(mergeAll && threeDeeBiomesSupported);
        checkBoxFillCaves.setEnabled(mergeAll);
        checkBoxRemoveManMadeAboveGround.setEnabled(mergeAll);
        checkBoxRemoveManMadeBelowGround.setEnabled(mergeAll);
        checkBoxRemoveResources.setEnabled(mergeAll);
        checkBoxRemoveTrees.setEnabled(mergeAll);
        checkBoxRemoveVegetation.setEnabled(mergeAll);
        spinnerSurfaceThickness.setEnabled(mergeAll);
        checkBoxSurface.setEnabled(mergeEverything && surfacePresent && (! oneDimensionPresent));
        checkBoxNether.setEnabled(mergeEverything && netherPresent && (! oneDimensionPresent));
        checkBoxEnd.setEnabled(mergeEverything && endPresent && (! oneDimensionPresent));
        if (radioButtonExportSelection.isSelected()) {
            labelSelectTiles.setForeground(Color.BLUE);
            labelSelectTiles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            labelSelectTiles.setForeground(null);
            labelSelectTiles.setCursor(null);
        }
    }

    private void selectMap() {
        File file = new File(fieldSelectedMapDir.getText().trim());
        PlatformProvider.MapInfo selectedMap = MapUtils.selectMap(this, file.isDirectory() ? ((platform != null) ? file.getParentFile() : file) : null);
        if (selectedMap != null) {
            fieldSelectedMapDir.setText(selectedMap.dir.getAbsolutePath());
        }
    }

    private void selectTiles() {
        if (radioButtonExportSelection.isSelected()) {
            ExportTileSelectionDialog dialog = new ExportTileSelectionDialog(this, world, selectedDimension, selectedTiles, colourScheme, customBiomeManager, hiddenLayers, contourLines, contourSeparation, lightOrigin);
            dialog.setVisible(true);
            selectedDimension = dialog.getSelectedDimension();
            checkBoxSurface.setSelected(selectedDimension == DIM_NORMAL);
            checkBoxNether.setSelected(selectedDimension == DIM_NETHER);
            checkBoxEnd.setSelected(selectedDimension == DIM_END);
            selectedTiles = dialog.getSelectedTiles();
            radioButtonExportSelection.setText("\u5408\u5E76 " + selectedTiles.size() + " \u4E2A\u9009\u4E2D\u7684\u5206\u533A");
            pack();
            setControlStates();
            disableTileSelectionWarning = true;
        }
    }

    /**
     * Check whether a platform is compatible with the loaded world. If not,
     * reports the reason to the user with a popup and returns {@code false},
     * otherwise returns {@code true}.
     *
     * @param platform The platform to check for compatibility.
     * @return {@code true} is the platform is compatible with the loaded world.
     */
    private boolean checkCompatibility(Platform platform) {
        if (! platform.capabilities.contains(NAME_BASED)) {
            Map<String, Set<String>> nameOnlyMaterials = MaterialUtils.gatherBlocksWithoutIds(world, platform);
            if (! nameOnlyMaterials.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<p>\u9009\u4E2D\u5730\u56FE\u7684\u683C\u5F0F ").append(platform.displayName).append(" \u4E0D\u517C\u5BB9\uFF0C\u56E0\u4E3A\u4E16\u754C\u4E2D\u5B58\u5728\u4EE5\u4E0B\u4E0D\u517C\u5BB9\u7684\u65B9\u5757\u7C7B\u578B:");
                sb.append("<table><tr><th align='left'>\u65B9\u5757\u7C7B\u578B</th><th align='left'>\u6765\u6E90</th></tr>");
                nameOnlyMaterials.forEach((name, sources) ->
                        sb.append("<tr><td>").append(name).append("</td><td>").append(String.join(",", sources)).append("</td></tr>"));
                sb.append("</table>");
                beepAndShowError(this, sb.toString(), "\u5730\u56FE\u683C\u5F0F\u4E0D\u517C\u5BB9");
                fieldSelectedMapDir.requestFocusInWindow();
                return false;
            }
        }
        final String incompatibilityReason = platform.isCompatible(world);
        if (incompatibilityReason != null) {
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, String.format(/* language=HTML */ "<html>" +
                    "<p>\u9009\u4E2D\u5730\u56FE\u7684\u683C\u5F0F %s \u56E0\u4E3A\u4E0B\u65B9\u539F\u56E0\u4E0E\u8BE5\u4E16\u754C\u4E0D\u517C\u5BB9:" +
                    "<ul><li>%s" +
                    "</ul>" +
                    "</html>", platform.displayName, incompatibilityReason), "\u5730\u56FE\u683C\u5F0F\u4E0D\u517C\u5BB9", JOptionPane.ERROR_MESSAGE);
            fieldSelectedMapDir.requestFocusInWindow();
            return false;
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        fieldSelectedMapDir = new javax.swing.JTextField();
        buttonSelectDirectory = new javax.swing.JButton();
        buttonMerge = new javax.swing.JButton();
        radioButtonAll = new javax.swing.JRadioButton();
        radioButtonReplaceChunks = new javax.swing.JRadioButton();
        radioButtonExportEverything = new javax.swing.JRadioButton();
        radioButtonExportSelection = new javax.swing.JRadioButton();
        labelSelectTiles = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        checkBoxRemoveTrees = new javax.swing.JCheckBox();
        checkBoxRemoveVegetation = new javax.swing.JCheckBox();
        checkBoxRemoveManMadeAboveGround = new javax.swing.JCheckBox();
        checkBoxRemoveResources = new javax.swing.JCheckBox();
        checkBoxFillCaves = new javax.swing.JCheckBox();
        checkBoxRemoveManMadeBelowGround = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        spinnerSurfaceThickness = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        checkBoxSurface = new javax.swing.JCheckBox();
        checkBoxNether = new javax.swing.JCheckBox();
        checkBoxEnd = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        labelPlatform = new javax.swing.JLabel();
        checkBoxAboveMergeBlocks = new javax.swing.JCheckBox();
        checkBoxAboveMergeBiomes = new javax.swing.JCheckBox();
        checkBoxBelowMergeBlocks = new javax.swing.JCheckBox();
        checkBoxBelowMergeBiomes = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u5408\u5E76\u4E2D");

        jLabel2.setText("\u9009\u62E9\u4E00\u4E2A\u8981\u548C\u5F53\u524D\u4E16\u754C\u5408\u5E76\u7684\u5DF2\u6709\u4E16\u754C:");

        buttonSelectDirectory.setText("...");
        buttonSelectDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDirectoryActionPerformed(evt);
            }
        });

        buttonMerge.setText("\u5408\u5E76");
        buttonMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMergeActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonAll);
        radioButtonAll.setSelected(true);
        radioButtonAll.setText("\u5408\u5E76\u65E7\u7684\u4E0E\u65B0\u7684\u533A\u5757");
        radioButtonAll.setToolTipText("<html><i>\u5C06\u4F1A\u5408\u5E76\u6240\u6709\u5185\u5BB9 (\u65B9\u5757\u79CD\u7C7B\u548C\u9AD8\u5EA6\u53D8\u5316,<br>\n\u65B0\u7684\u8986\u76D6\u5C42\u7B49\u7B49). \u82B1\u8D39\u65F6\u95F4\u8F83\u957F.</i></html>");
        radioButtonAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonAllActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioButtonReplaceChunks);
        radioButtonReplaceChunks.setText("\u5B8C\u5168\u5C06\u65E7\u533A\u5757\u66FF\u6362\u4E3A\u65B0\u533A\u5757");
        radioButtonReplaceChunks.setToolTipText("<html><i>\u5C06\u4F1A</i>\u66FF\u6362<i>\u6240\u6709\u975E\u53EA\u8BFB\u533A\u5757,<br>\u6467\u6BC1\u5DF2\u6709\u5730\u56FE\u7684\u6240\u6709\u5185\u5BB9! </i></html>");
        radioButtonReplaceChunks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonReplaceChunksActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonExportEverything);
        radioButtonExportEverything.setSelected(true);
        radioButtonExportEverything.setText("\u5408\u5E76\u6240\u6709\u5206\u533A");
        radioButtonExportEverything.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonExportEverythingActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonExportSelection);
        radioButtonExportSelection.setText("\u5408\u5E76\u9009\u4E2D\u5206\u533A");
        radioButtonExportSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonExportSelectionActionPerformed(evt);
            }
        });

        labelSelectTiles.setText("<html><u>\u9009\u62E9\u5206\u533A</u></html>");
        labelSelectTiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelSelectTilesMouseClicked(evt);
            }
        });

        jLabel1.setText("\u9009\u62E9\u8981\u5730\u56FE\u4E0A\u5408\u5E76\u7684\u90E8\u5206:");

        jLabel4.setText("<html>\u9009\u62E9\u8981\u6267\u884C\u4EC0\u4E48\u6837\u7684\u5408\u5E76 (\u4EC5\u9650<b>\u9009\u4E2D\u5206\u533A</b>\u4E2D\u7684<b>\u975E\u53EA\u8BFB</b>\u533A\u5757):</html>");

        jLabel5.setText("\u5DF2\u6709\u5730\u56FE\u7684\u9009\u9879:");

        jLabel6.setText("<html>\u5730\u9762<b>\u4E4B\u4E0A</b>:</html>");

        checkBoxRemoveTrees.setText("\u79FB\u9664\u6240\u6709\u6811\u548C\u5DE8\u578B\u8611\u83C7");
        checkBoxRemoveTrees.setToolTipText("\u79FB\u9664\u6240\u6709\u6811\u548C\u5DE8\u578B\u8611\u83C7\uFF0C\u5305\u542B\u53EF\u53EF\u679C\u3001\u85E4\u8513\u548C\u6811\u82D7.");

        checkBoxRemoveVegetation.setText("\u79FB\u9664\u6240\u6709\u4F4E\u77EE\u690D\u7269\u548C\u519C\u4F5C\u7269");
        checkBoxRemoveVegetation.setToolTipText("\u79FB\u9664\u6240\u6709\u9AD8\u8349\u3001\u82B1\u3001\u8611\u83C7\u3001\u5730\u72F1\u75A3\u3001\u5357\u74DC\u548C\u897F\u74DC\u3001\u80E1\u841D\u535C\u3001\u9A6C\u94C3\u85AF\u3001\u5C0F\u9EA6\u7B49\u7B49");

        checkBoxRemoveManMadeAboveGround.setText("\u79FB\u9664\u6240\u6709\u4EBA\u9020\u7ED3\u6784");
        checkBoxRemoveManMadeAboveGround.setToolTipText("\u79FB\u9664\u6240\u6709\u5730\u8868\u4EE5\u4E0A\u4E0D\u53EF\u80FD\u81EA\u7136\u51FA\u73B0\u7684\u65B9\u5757.");

        checkBoxRemoveResources.setText("\u79FB\u9664\u6240\u6709\u8D44\u6E90/\u539F\u77FF");
        checkBoxRemoveResources.setToolTipText("\u4F7F\u7528\u77F3\u5934\u66FF\u6362\u6240\u6709\u8D44\u6E90\u7C7B\u65B9\u5757 (\u77F3\u82F1\u77FF\u5219\u662F\u4E0B\u754C\u5CA9).");

        checkBoxFillCaves.setText("\u586B\u5145\u6240\u6709\u6D1E\u7A74\u548C\u7A7A\u6D1E");
        checkBoxFillCaves.setToolTipText("<html>\u5C06\u7A7A\u6C14\u3001\u6C34\u3001\u5CA9\u6D46\u548C\u5176\u4ED6\u6240\u6709\u65E0\u5B9E\u4F53\u65B9\u5757\u66FF\u6362\u4E3A\u77F3\u5934.<br>\n\u8981\u66FF\u6362\u6240\u6709\u7684\u4EBA\u9020\u65B9\u5757, \u8BF7\u540C\u65F6\u4F7F\u7528\"\u79FB\u9664\u6240\u6709\u4EBA\u9020\u7ED3\u6784\".</html>");

        checkBoxRemoveManMadeBelowGround.setText("\u79FB\u9664\u6240\u6709\u4EBA\u9020\u7ED3\u6784");
        checkBoxRemoveManMadeBelowGround.setToolTipText("\u4F7F\u7528\u77F3\u5934\u6216\u7A7A\u6C14\u79FB\u9664\u6240\u6709\u5730\u4E0B\u7684\u975E\u81EA\u7136\u65B9\u5757.");

        jLabel7.setText("<html>\u5730\u9762<b>\u4E4B\u4E0B</b>:</html>");

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        jLabel3.setText(" ");
        jLabel3.setToolTipText("<html>\u5C06\u4F1A\u79FB\u9664<em>\u6240\u6709\u7684</em>\u6728\u5934\u548C\u6811\u53F6, \u5305\u62EC\u4EBA\u5DE5\u653E\u7F6E\u7684!<br>\nWorldPainter \u65E0\u6CD5\u5206\u8FA8\u81EA\u7136\u751F\u6210\u548C\u4EBA\u5DE5\u653E\u7F6E\u4E4B\u95F4\u7684\u533A\u522B.<br>\n\u8BF7\u786E\u4FDD\u4F60\u5DF2\u7528\u53EA\u8BFB\u8986\u76D6\u5C42\u4FDD\u62A4\u4E86\u4F60\u7684\u5EFA\u7B51.</html>");

        jLabel8.setText("\u8981\u66FF\u6362\u7684\u8868\u5C42\u9AD8\u5EA6:");

        spinnerSurfaceThickness.setModel(new javax.swing.SpinnerNumberModel(3, 1, 256, 1));

        jLabel9.setText("\u683C");

        checkBoxSurface.setText("\u4E3B\u4E16\u754C");
        checkBoxSurface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxSurfaceActionPerformed(evt);
            }
        });

        checkBoxNether.setText("\u4E0B\u754C");
        checkBoxNether.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxNetherActionPerformed(evt);
            }
        });

        checkBoxEnd.setText("\u672B\u5730");
        checkBoxEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxEndActionPerformed(evt);
            }
        });

        jLabel10.setText("\u5730\u56FE\u683C\u5F0F:");

        checkBoxAboveMergeBlocks.setSelected(true);
        checkBoxAboveMergeBlocks.setText("\u5408\u5E76\u65B9\u5757");

        checkBoxAboveMergeBiomes.setText("\u66FF\u6362\u751F\u7269\u7FA4\u7CFB");

        checkBoxBelowMergeBlocks.setText("\u5408\u5E76\u65B9\u5757");

        checkBoxBelowMergeBiomes.setText("\u66FF\u6362\u751F\u7269\u7FA4\u7CFB");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(fieldSelectedMapDir)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectDirectory))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(radioButtonExportEverything)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(radioButtonExportSelection)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelSelectTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel1)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(checkBoxSurface)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(checkBoxNether)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(checkBoxEnd))
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(radioButtonAll)
                                            .addComponent(radioButtonReplaceChunks)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerSurfaceThickness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(buttonMerge)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxRemoveTrees)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3))
                            .addComponent(checkBoxRemoveVegetation)
                            .addComponent(checkBoxRemoveManMadeAboveGround)
                            .addComponent(jLabel5)
                            .addComponent(checkBoxAboveMergeBlocks)
                            .addComponent(checkBoxAboveMergeBiomes))
                        .addGap(0, 0, 0)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxRemoveManMadeBelowGround)
                            .addComponent(checkBoxFillCaves)
                            .addComponent(checkBoxRemoveResources)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBoxBelowMergeBlocks)
                            .addComponent(checkBoxBelowMergeBiomes))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelPlatform)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldSelectedMapDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDirectory))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(labelPlatform))
                .addGap(18, 18, 18)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonReplaceChunks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxAboveMergeBlocks)
                    .addComponent(checkBoxBelowMergeBlocks))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxAboveMergeBiomes)
                    .addComponent(checkBoxBelowMergeBiomes))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxRemoveTrees)
                    .addComponent(checkBoxRemoveResources)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxRemoveVegetation)
                    .addComponent(checkBoxFillCaves))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxRemoveManMadeAboveGround)
                    .addComponent(checkBoxRemoveManMadeBelowGround))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonExportEverything)
                    .addComponent(radioButtonExportSelection)
                    .addComponent(labelSelectTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxSurface)
                    .addComponent(checkBoxNether)
                    .addComponent(checkBoxEnd))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(spinnerSurfaceThickness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(buttonMerge))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMergeActionPerformed
        merge();
    }//GEN-LAST:event_buttonMergeActionPerformed

    private void buttonSelectDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectDirectoryActionPerformed
        selectMap();
    }//GEN-LAST:event_buttonSelectDirectoryActionPerformed

    private void radioButtonExportEverythingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonExportEverythingActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonExportEverythingActionPerformed

    private void radioButtonExportSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonExportSelectionActionPerformed
        if (radioButtonExportSelection.isSelected()) {
            selectTiles();
        } else {
            setControlStates();
        }
    }//GEN-LAST:event_radioButtonExportSelectionActionPerformed

    private void labelSelectTilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelSelectTilesMouseClicked
        selectTiles();
    }//GEN-LAST:event_labelSelectTilesMouseClicked

    private void radioButtonAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonAllActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonAllActionPerformed

    private void radioButtonReplaceChunksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonReplaceChunksActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonReplaceChunksActionPerformed

    private void checkBoxSurfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxSurfaceActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxSurfaceActionPerformed

    private void checkBoxNetherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxNetherActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxNetherActionPerformed

    private void checkBoxEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxEndActionPerformed
        setControlStates();
    }//GEN-LAST:event_checkBoxEndActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonMerge;
    private javax.swing.JButton buttonSelectDirectory;
    private javax.swing.JCheckBox checkBoxAboveMergeBiomes;
    private javax.swing.JCheckBox checkBoxAboveMergeBlocks;
    private javax.swing.JCheckBox checkBoxBelowMergeBiomes;
    private javax.swing.JCheckBox checkBoxBelowMergeBlocks;
    private javax.swing.JCheckBox checkBoxEnd;
    private javax.swing.JCheckBox checkBoxFillCaves;
    private javax.swing.JCheckBox checkBoxNether;
    private javax.swing.JCheckBox checkBoxRemoveManMadeAboveGround;
    private javax.swing.JCheckBox checkBoxRemoveManMadeBelowGround;
    private javax.swing.JCheckBox checkBoxRemoveResources;
    private javax.swing.JCheckBox checkBoxRemoveTrees;
    private javax.swing.JCheckBox checkBoxRemoveVegetation;
    private javax.swing.JCheckBox checkBoxSurface;
    private javax.swing.JTextField fieldSelectedMapDir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel labelPlatform;
    private javax.swing.JLabel labelSelectTiles;
    private javax.swing.JRadioButton radioButtonAll;
    private javax.swing.JRadioButton radioButtonExportEverything;
    private javax.swing.JRadioButton radioButtonExportSelection;
    private javax.swing.JRadioButton radioButtonReplaceChunks;
    private javax.swing.JSpinner spinnerSurfaceThickness;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final ColourScheme colourScheme;
    private final Set<Layer> hiddenLayers;
    private final boolean contourLines;
    private final int contourSeparation;
    private final TileRenderer.LightOrigin lightOrigin;
    private final CustomBiomeManager customBiomeManager;
    private final WorldPainter view;
    private final Set<WorldExportSettings.Step> savedSteps;
    private File mapDir;
    private Platform platform;
    private volatile File backupDir;
    private int selectedDimension;
    private Set<Point> selectedTiles;
    private boolean disableTileSelectionWarning;

    private static final AttributeKey<MergeSettings> ATTRIBUTE_MERGE_SETTINGS = new AttributeKey<>("MergeWorldDialog.mergeSettings");
    private static final Logger logger = LoggerFactory.getLogger(MergeWorldDialog.class);
    private static final long serialVersionUID = 1L;

    static class MergeSettings implements Serializable {
        public MergeSettings(boolean replaceChunks, boolean mergeBlocksAboveGround, boolean mergeBlocksUnderground, boolean mergeBiomesAboveGround, boolean mergeBiomesUnderground, boolean clearTrees, boolean clearVegetation, boolean clearManMadeAboveGround, boolean clearResources, boolean fillCaves, boolean clearManMadeBelowGround, int surfaceMergeDepth) {
            this.replaceChunks = replaceChunks;
            this.mergeBlocksAboveGround = mergeBlocksAboveGround;
            this.mergeBlocksUnderground = mergeBlocksUnderground;
            this.mergeBiomesAboveGround = mergeBiomesAboveGround;
            this.mergeBiomesUnderground = mergeBiomesUnderground;
            this.clearTrees = clearTrees;
            this.clearVegetation = clearVegetation;
            this.clearManMadeAboveGround = clearManMadeAboveGround;
            this.clearResources = clearResources;
            this.fillCaves = fillCaves;
            this.clearManMadeBelowGround = clearManMadeBelowGround;
            this.surfaceMergeDepth = surfaceMergeDepth;
        }

        final boolean replaceChunks,
                mergeBlocksAboveGround, mergeBlocksUnderground, mergeBiomesAboveGround, mergeBiomesUnderground,
                clearTrees, clearVegetation, clearManMadeAboveGround,
                clearResources, fillCaves, clearManMadeBelowGround;
        final int surfaceMergeDepth;

        private static final long serialVersionUID = 1L;
    }
}