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

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.World2.BorderSettings;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.exporting.WorldExportSettings;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.Populate;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.util.EnumListCellRenderer;
import org.pepsoft.worldpainter.util.FileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static org.pepsoft.minecraft.Constants.DIFFICULTY_HARD;
import static org.pepsoft.minecraft.Constants.DIFFICULTY_PEACEFUL;
import static org.pepsoft.minecraft.datapack.DataPack.isDataPackFile;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.worldpainter.Constants.DIM_NORMAL;
import static org.pepsoft.worldpainter.DefaultPlugin.JAVA_MCREGION;
import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_DETAIL;
import static org.pepsoft.worldpainter.ExceptionHandler.doWithoutExceptionReporting;
import static org.pepsoft.worldpainter.GameType.*;
import static org.pepsoft.worldpainter.Platform.Capability.*;
import static org.pepsoft.worldpainter.exporting.WorldExportSettings.EXPORT_EVERYTHING;
import static org.pepsoft.worldpainter.util.BackupUtils.cleanUpBackups;
import static org.pepsoft.worldpainter.util.FileUtils.selectFileForOpen;
import static org.pepsoft.worldpainter.util.MaterialUtils.gatherBlocksWithoutIds;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unused", "FieldCanBeLocal", "rawtypes", "Convert2Lambda", "Anonymous2MethodRef", "ConstantConditions"}) // Managed by NetBeans
public class ExportWorldDialog extends WPDialogWithPaintSelection {
    /** Creates new form ExportWorldDialog */
    public ExportWorldDialog(Window parent, World2 world, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Set<Layer> hiddenLayers, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin, WorldPainter view) {
        super(parent);
        this.world = world;
        final Dimension dim0 = world.getDimension(NORMAL_DETAIL);
        this.colourScheme = colourScheme;
        this.hiddenLayers = hiddenLayers;
        this.contourLines = contourLines;
        this.contourSeparation = contourSeparation;
        this.lightOrigin = lightOrigin;
        this.customBiomeManager = customBiomeManager;
        this.view = view;
        initComponents();

        final Configuration config = Configuration.getInstance();
        if (config.isEasyMode()) {
            checkBoxMapFeatures.setVisible(false);
            jLabel1.setVisible(false);
            labelPlatform.setVisible(false);
        }

        supportedPlatforms.addAll(PlatformManager.getInstance().getAllPlatforms());
        final Platform platform = world.getPlatform();
        if (supportedPlatforms.contains(platform)) {
            labelPlatformWarning.setVisible(false);
            if (config.getExportDirectory(platform) != null) {
                fieldDirectory.setText(config.getExportDirectory(platform).getAbsolutePath());
            } else {
                File exportDir = PlatformManager.getInstance().getDefaultExportDir(platform);
                if (exportDir != null) {
                    fieldDirectory.setText(exportDir.getAbsolutePath());
                } else {
                    fieldDirectory.setText(DesktopUtils.getDocumentsFolder().getAbsolutePath());
                }
            }
        } else {
            fieldDirectory.setText(null);
        }
        fieldName.setText(world.getName());

        createDimensionPropertiesEditors();
        checkBoxGoodies.setSelected(world.isCreateGoodiesChest());
        labelPlatform.setText("<html><u>" + platform.displayName + "</u></html>");
        labelPlatform.setToolTipText("\u70B9\u51FB\u4FEE\u6539\u5730\u56FE\u683C\u5F0F");
        comboBoxGameType.setModel(new DefaultComboBoxModel<>(platform.supportedGameTypes.toArray(new GameType[platform.supportedGameTypes.size()])));
        comboBoxGameType.setSelectedItem(world.getGameType());
        comboBoxGameType.setEnabled(comboBoxGameType.getItemCount() > 1);
        comboBoxGameType.setRenderer(new EnumListCellRenderer());
        checkBoxAllowCheats.setSelected(world.isAllowCheats());
        if (world.getDataPacks() != null) {
            for (File dataPackFile: world.getDataPacks()) {
                dataPacksListModel.addElement(dataPackFile);
            }
        }
        listDataPacks.setModel(dataPacksListModel);
        listDataPacks.setEnabled(platform.capabilities.contains(DATA_PACKS));
        checkBoxMapFeatures.setSelected(world.isMapFeatures());
        comboBoxDifficulty.setSelectedIndex(world.getDifficulty());

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
        fieldDirectory.getDocument().addDocumentListener(documentListener);
        fieldName.getDocument().addDocumentListener(documentListener);

        // Minecraft world border
        World2.BorderSettings borderSettings = world.getBorderSettings();
        spinnerMcBorderCentreX.setValue(borderSettings.getCentreX());
        spinnerMcBorderCentreY.setValue(borderSettings.getCentreY());
        spinnerMcBorderSize.setValue(borderSettings.getSize());
//        spinnerMcBorderBuffer.setValue(borderSettings.getSafeZone());
//        spinnerMcBorderDamage.setValue(borderSettings.getDamagePerBlock());
//        spinnerMcBorderWarningTime.setValue(borderSettings.getWarningTime());
//        spinnerMcBorderWarningDistance.setValue(borderSettings.getWarningBlocks());

        disableDisabledLayersWarning = true;
        dims:
        for (Dimension dim: world.getDimensions()) {
            for (CustomLayer customLayer: dim.getCustomLayers()) {
                if (! customLayer.isExport()) {
                    disableDisabledLayersWarning = false;
                    break dims;
                }
            }
        }

        rootPane.setDefaultButton(buttonExport);

        setControlStates();

        scaleToUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void createDimensionPropertiesEditors() {
        final SortedMap<Anchor, Dimension> dimensions = world.getDimensions().stream().collect(Collectors.toMap(
                Dimension::getAnchor,
                identity(),
                (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                TreeMap::new));
        for (Dimension dimension: dimensions.values()) {
            final DimensionPropertiesEditor editor = new DimensionPropertiesEditor();
            editor.init(colourScheme, customBiomeManager, dimension, DimensionPropertiesEditor.Mode.EXPORT);
            jTabbedPane1.addTab(dimension.getName(), editor);
            dimensionPropertiesEditors.put(dimension.getAnchor(), editor);
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
    @SuppressWarnings("HtmlRequiredLangAttribute") // Not real HTML
    private boolean checkCompatibility(Platform platform) {
        if (! platform.capabilities.contains(NAME_BASED)) {
            final Map<String, Set<String>> nameOnlyMaterials = gatherBlocksWithoutIds(world, platform);
            if (! nameOnlyMaterials.isEmpty()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<p>\u4E16\u754C\u4E0D\u80FD\u4EE5 ").append(platform.displayName).append(" \u683C\u5F0F\u5BFC\u51FA\uFF0C\u56E0\u4E3A\u5176\u5305\u542B\u4E0D\u517C\u5BB9\u7684\u65B9\u5757\u7C7B\u578B:");
                sb.append("<table><tr><th align='left'>\u65B9\u5757\u7C7B\u578B</th><th align='left'>\u6765\u6E90</th></tr>");
                nameOnlyMaterials.forEach((name, sources) ->
                        sb.append("<tr><td>").append(name).append("</td><td>").append(String.join(", ", sources)).append("</td></tr>"));
                sb.append("</table>");
                beepAndShowError(this, sb.toString(), "\u5730\u56FE\u683C\u5F0F\u4E0D\u652F\u6301");
                return false;
            }
        }
        final String incompatibilityReason = PlatformManager.getInstance().getPlatformProvider(platform).isCompatible(platform, world);
        if (incompatibilityReason != null) {
            beepAndShowError(this, String.format(/* language=HTML */ "<html>" +
                    "<p>\u4E16\u754C\u4E0D\u80FD\u4EE5\u683C\u5F0F %s \u5BFC\u51FA\uFF0C\u56E0\u4E3A\u5176\u4E0D\u517C\u5BB9:</p>" +
                    "<p>%s</p>" +
                    "</html>", platform.displayName, incompatibilityReason), "\u5730\u56FE\u683C\u5F0F\u4E0D\u517C\u5BB9");
            return false;
        }
        return true;
    }

    protected final void export(WorldExportSettings exportSettings) {
        exportSettings = (exportSettings != null)
                ? exportSettings
                : ((world.getExportSettings() != null) ? world.getExportSettings() : EXPORT_EVERYTHING);
        final boolean exportAllDimensions = exportSettings.getDimensionsToExport() == null, inhibitWarnings = (exportSettings != EXPORT_EVERYTHING);
        final Set<Point> selectedTiles = exportAllDimensions ? null : exportSettings.getTilesToExport();
        final int selectedDimension = exportAllDimensions ? DIM_NORMAL : exportSettings.getDimensionsToExport().iterator().next();

        // Check for errors
        if (! new File(fieldDirectory.getText().trim()).isDirectory()) {
            fieldDirectory.requestFocusInWindow();
            beepAndShowError(this, "\u9009\u62E9\u7684\u8F93\u51FA\u76EE\u5F55\u4E0D\u5B58\u5728\u6216\u4E0D\u4E3A\u76EE\u5F55.", "\u9519\u8BEF");
            return;
        }
        if (fieldName.getText().trim().isEmpty()) {
            fieldName.requestFocusInWindow();
            beepAndShowError(this, "\u4F60\u8FD8\u6CA1\u6709\u7ED9\u5730\u56FE\u547D\u540D.", "\u9519\u8BEF");
            return;
        }

        // Check for warnings
        final Platform platform = world.getPlatform();
        final StringBuilder sb = new StringBuilder("<ul>");
        boolean showWarning = false;
        for (DimensionPropertiesEditor editor: dimensionPropertiesEditors.values()) {
            final Generator generatorType = editor.getSelectedGeneratorType();
            final Dimension dimension = editor.getDimension();
            if ((editor.isPopulateSelected() || dimension.getAllLayers(true).contains(Populate.INSTANCE)) && (! platform.capabilities.contains(POPULATE))) {
                sb.append("<li>\u5730\u56FE\u683C\u5F0F "+ platform.displayName +" \u4E0D\u652F\u6301\u586B\u5145\u533A\u5757\u529F\u80FD\uFF1B\u6B64\u64CD\u4F5C\u5C06\u4E0D\u4F1A\u751F\u6548\u3002");
                showWarning = true;
            } else if (exportAllDimensions || (selectedDimension == dimension.getAnchor().dim)) {
                // The dimension is going to be exported
                if ((generatorType == Generator.FLAT) && (editor.isPopulateSelected() || dimension.getAllLayers(true).contains(Populate.INSTANCE))) {
                    sb.append("<li>\u8D85\u5E73\u5766\u4E16\u754C\u7C7B\u578B\u548C\u751F\u6210\u9009\u9879\u540C\u65F6\u88AB\u8BBE\u7F6E.<br>Minecraft <em>\u4E0D\u4F1A</em>\u4E3A\u8D85\u5E73\u5766\u5730\u56FE\u586B\u5145\u751F\u6210\u533A\u5757.");
                    showWarning = true;
                }
            }
            if ((generatorType != null) && (! platform.supportedGenerators.contains(generatorType))) {
                sb.append("<li>\u5730\u56FE\u683C\u5F0F " + platform.displayName + " \u4E0D\u652F\u6301" + generatorType.getDisplayName() + "\u4E16\u754C\u7C7B\u578B.<br>\u8BE5\u4E16\u754C\u5C06\u88AB\u91CD\u7F6E\u4E3A " + platform.supportedGenerators.get(0).getDisplayName() + ".");
                editor.setSelectedGeneratorType(platform.supportedGenerators.get(0));
                showWarning = true;
            }
        }
        if ((selectedTiles != null) && (selectedDimension == DIM_NORMAL)) {
            boolean spawnInSelection = false;
            Point spawnPoint = world.getSpawnPoint();
            for (Point tile: selectedTiles) {
                if ((spawnPoint.x >= (tile.x << 7)) && (spawnPoint.x < ((tile.x + 1) << 7)) && (spawnPoint.y >= (tile.y << 7)) && (spawnPoint.y < ((tile.y + 1) << 7))) {
                    spawnInSelection = true;
                    break;
                }
            }
            if (! spawnInSelection) {
                sb.append("<li>\u51FA\u751F\u70B9\u4E0D\u5728\u9009\u4E2D\u533A\u57DF\u4E2D.<br>\u5B83\u5C06\u88AB\u79FB\u52A8\u5230\u9009\u4E2D\u533A\u57DF\u7684\u4E2D\u5FC3.");
                showWarning = true;
            }
        }
        int disabledLayerCount = 0;
        for (Dimension dimension: world.getDimensions()) {
            for (CustomLayer customLayer: dimension.getCustomLayers()) {
                if ((customLayer.getExporterType() != null) && (! customLayer.isExport())) {
                    disabledLayerCount++;
                }
            }
        }
        if (disabledLayerCount > 0) {
            if (disabledLayerCount == 1) {
                sb.append("<li>\u5730\u56FE\u4E2D\u6709\u88AB\u9690\u85CF\u7684\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42!<br>\u8BE5\u8986\u76D6\u5C42\u5C06\u4E0D\u4F1A\u88AB\u5BFC\u51FA.");
            } else {
                sb.append("<li>\u5730\u56FE\u4E2D\u6709"+ disabledLayerCount +"\u4E2A\u88AB\u9690\u85CF\u7684\u81EA\u5B9A\u4E49\u8986\u76D6\u5C42!<br>\u8FD9\u4E9B\u8986\u76D6\u5C42\u5C06\u4E0D\u4F1A\u88AB\u5BFC\u51FA.");
            }
            showWarning = showWarning || (! disableDisabledLayersWarning);
        }
        for (int i = 0; i < dataPacksListModel.size(); i++) {
            final File dataPackFile = dataPacksListModel.getElementAt(i);
            if (! dataPackFile.exists()) {
                sb.append("<li>\u65E0\u6CD5\u627E\u5230\u6570\u636E\u5305\u6587\u4EF6 " + dataPackFile.getName() + ".<br>\u5B83\u5C06\u4E0D\u4F1A\u88AB\u5B89\u88C5\u5230\u4E16\u754C\u4E2D.");
                showWarning = true;
            } else if (! dataPackFile.isFile()) {
                sb.append("<li>\u6570\u636E\u5305\u6587\u4EF6 " + dataPackFile.getName() + " \u4E0D\u6B63\u5E38.<br>\u5B83\u5C06\u4E0D\u4F1A\u88AB\u5B89\u88C5\u5230\u4E16\u754C\u4E2D.");
                showWarning = true;
            } else if (! dataPackFile.canRead()) {
                sb.append("<li>\u65E0\u6CD5\u8BBF\u95EE\u6570\u636E\u5305\u6587\u4EF6 " + dataPackFile.getName() + ".<br>\u5B83\u5C06\u4E0D\u4F1A\u88AB\u5B89\u88C5\u5230\u4E16\u754C\u4E2D.");
                showWarning = true;
            }
        }
        sb.append("</ul>");
        if (showWarning) {
            final String warnings = sb.toString();
            if (inhibitWarnings && (warningsForWorld != null) && (warningsForWorld.get() == world) && warnings.equals(previouslyAcknowledgedWarnings)) {
                logger.warn("Skipping previously acknowledged warnings for this world: {}", previouslyAcknowledgedWarnings);
            } else {
                DesktopUtils.beep();
                String text = "<html>\u8BF7\u786E\u8BA4\u4F60\u8981\u5728\u51FA\u73B0\u4EE5\u4E0B\u9519\u8BEF\u7684\u60C5\u51B5\u4E0B\u7EE7\u7EED\u5BFC\u51FA\u4E16\u754C:<br>"
                        + warnings
                        + "\u4F60\u786E\u8BA4\u8981\u5BFC\u51FA\u8BE5\u4E16\u754C\u5417?";
                if (inhibitWarnings) {
                    text += "<br>" +
                            "<br>" +
                            "<strong>\u6CE8\u610F:</strong> \u4E0B\u6B21\u6D4B\u8BD5\u5BFC\u51FA\u65F6\uFF0C\u82E5\u9519\u8BEF\u76F8\u540C\uFF0C\u8BE5\u754C\u9762\u5C06\u4E0D\u4F1A\u663E\u793A<br>.</html>";
                }
                if (JOptionPane.showConfirmDialog(this, text, "\u67E5\u770B\u8B66\u544A", YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                    previouslyAcknowledgedWarnings = null;
                    warningsForWorld = null;
                    return;
                }
                previouslyAcknowledgedWarnings = warnings;
                warningsForWorld = new WeakReference<>(world);
            }
        } else {
            previouslyAcknowledgedWarnings = null;
            warningsForWorld = null;
        }

        final File baseDir = new File(fieldDirectory.getText().trim());
        final String name = fieldName.getText().trim();

        // Make sure the minimum free disk space is met
        try {
            if (! cleanUpBackups(baseDir, null)) {
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException("I/O error while cleaning backups", e);
        }

        if (! saveDimensionSettings()) {
            return;
        }
        if (! checkCompatibility(world.getPlatform())) {
            return;
        }

        world.setCreateGoodiesChest(checkBoxGoodies.isSelected());
        world.setGameType((GameType) comboBoxGameType.getSelectedItem());
        world.setAllowCheats(checkBoxAllowCheats.isSelected());
        world.setMapFeatures(checkBoxMapFeatures.isSelected());
        world.setDifficulty(comboBoxDifficulty.getSelectedIndex());
        world.setDataPacks(dataPacksListModel.isEmpty() ? null : Collections.list(dataPacksListModel.elements()));

        // Minecraft world border
        BorderSettings borderSettings = world.getBorderSettings();
        borderSettings.setCentreX((Integer) spinnerMcBorderCentreX.getValue());
        borderSettings.setCentreY((Integer) spinnerMcBorderCentreY.getValue());
        borderSettings.setSize((Integer) spinnerMcBorderSize.getValue());
//        borderSettings.setSafeZone((Integer) spinnerMcBorderBuffer.getValue());
//        borderSettings.setDamagePerBlock((Float) spinnerMcBorderDamage.getValue());
//        borderSettings.setWarningTime((Integer) spinnerMcBorderWarningTime.getValue());
//        borderSettings.setWarningBlocks((Integer) spinnerMcBorderWarningDistance.getValue());

        fieldDirectory.setEnabled(false);
        fieldName.setEnabled(false);
        buttonSelectDirectory.setEnabled(false);
        buttonExport.setEnabled(false);
        buttonTestExport.setEnabled(false);
        buttonCancel.setEnabled(false);
        for (DimensionPropertiesEditor editor: dimensionPropertiesEditors.values()) {
            editor.setEnabled(false);
        }
        checkBoxGoodies.setEnabled(false);
        comboBoxGameType.setEnabled(false);
        checkBoxAllowCheats.setEnabled(false);
        checkBoxMapFeatures.setEnabled(false);
        comboBoxDifficulty.setEnabled(false);
        listDataPacks.setEnabled(false);
        buttonAddDataPack.setEnabled(false);
        buttonRemoveDataPack.setEnabled(false);

        final Configuration config = Configuration.getInstance();
        config.setExportDirectory(world.getPlatform(), baseDir);

        final ExportProgressDialog dialog = new ExportProgressDialog(this, world, exportSettings, baseDir, name, previouslyAcknowledgedWarnings);
        view.setInhibitUpdates(true);
        try {
            dialog.setVisible(true);
        } finally {
            view.setInhibitUpdates(false);
        }
        if (! dialog.isAllowRetry()) {
            ok();
        } else {
            fieldName.setEnabled(true);
            buttonCancel.setEnabled(true);
            for (DimensionPropertiesEditor editor: dimensionPropertiesEditors.values()) {
                editor.setEnabled(true);
            }
            checkBoxGoodies.setEnabled(true);
            comboBoxGameType.setEnabled(true);
            checkBoxMapFeatures.setEnabled(true);
            listDataPacks.setEnabled(platform.capabilities.contains(DATA_PACKS));
            setControlStates();
        }
    }

    private boolean saveDimensionSettings() {
        for (DimensionPropertiesEditor editor: dimensionPropertiesEditors.values()) {
            if (! editor.saveSettings()) {
                jTabbedPane1.setSelectedComponent(editor);
                return false;
            }
        }
        return true;
    }

    private void testExport() {
        final TestExportDialog dialog = new TestExportDialog(this, world, colourScheme, customBiomeManager, hiddenLayers, contourLines, contourSeparation, lightOrigin);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            export(world.getExportSettings());
        }
    }

    private void setControlStates() {
        boolean notHardcore = comboBoxGameType.getSelectedItem() != HARDCORE;
        final boolean platformSupported = supportedPlatforms.contains(world.getPlatform());
        checkBoxAllowCheats.setEnabled((world.getPlatform() != JAVA_MCREGION) && notHardcore);
        comboBoxDifficulty.setEnabled(notHardcore);
        fieldDirectory.setEnabled(platformSupported);
        buttonSelectDirectory.setEnabled(platformSupported);
        buttonExport.setEnabled(platformSupported);
        buttonTestExport.setEnabled(platformSupported);
        final boolean dataPacksEnabled = listDataPacks.isEnabled();
        buttonAddDataPack.setEnabled(dataPacksEnabled);
        buttonRemoveDataPack.setEnabled(dataPacksEnabled && (listDataPacks.getSelectedIndex() != -1));
        if (! platformSupported) {
            buttonExport.setToolTipText(labelPlatformWarning.getToolTipText());
            buttonTestExport.setToolTipText(labelPlatformWarning.getToolTipText());
        } else {
            buttonExport.setToolTipText(null);
            buttonTestExport.setToolTipText(null);
        }
    }

    private void selectDir() {
        // Can't use FileUtils.selectFileForOpen() since it doesn't support
        // selecting a directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fieldDirectory.getText().trim()));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (doWithoutExceptionReporting(() -> fileChooser.showOpenDialog(this)) == JFileChooser.APPROVE_OPTION) {
            fieldDirectory.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void changePlatform() {
        if (! saveDimensionSettings()) {
            return;
        }
        if (App.getInstance().changeWorldHeight(this)) {
            while (jTabbedPane1.getTabCount() > 1) {
                jTabbedPane1.removeTabAt(jTabbedPane1.getTabCount() - 1);
            }
            createDimensionPropertiesEditors();
            platformChanged();
        }
    }

    private void addDataPack() {
        final File dataPackFile = selectFileForOpen(this, "\u9009\u62E9\u4E00\u4E2A\u6570\u636E\u5305", null, new FileFilter() {
            @Override
            public String getExtensions() {
                return "*.zip";
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".zip");
            }

            @Override
            public String getDescription() {
                return "\u6570\u636E\u5305 (*.zip)";
            }
        });
        if (dataPackFile != null) {
            if (! isDataPackFile(dataPackFile)) {
                beepAndShowError(this, "\u9009\u62E9\u7684\u6587\u4EF6 \"" + dataPackFile.getName() + "\" \u4E0D\u662F Minecraft \u6570\u636E\u5305.", "\u4E0D\u662F\u6570\u636E\u5305");
                return;
            } else if (dataPackFile.getName().equalsIgnoreCase("worldpainter.zip")) {
                beepAndShowError(this, "WorldPainter \u6570\u636E\u5305\u7531 WorldPainter \u81EA\u52A8\u7BA1\u7406\uFF0C\u4E0D\u80FD\u624B\u52A8\u5BFC\u5165.", "\u65E0\u6CD5\u6DFB\u52A0 WorldPainter \u6570\u636E\u5305");
                return;
            } else {
                for (Enumeration<File> e = dataPacksListModel.elements(); e.hasMoreElements(); ) {
                    final File existingDataPack = e.nextElement();
                    if (existingDataPack.getName().equalsIgnoreCase(dataPackFile.getName())) {
                        if (JOptionPane.showConfirmDialog(this, "\u5DF2\u7ECF\u6709\u540D\u4E3A \"" + dataPackFile.getName() + "\" \u7684\u6570\u636E\u5305\u4E86\n\u4F60\u8981\u66FF\u6362\u5B83\u5417?", "\u6570\u636E\u5305\u540D\u79F0\u5DF2\u5B58\u5728", YES_NO_OPTION) == YES_OPTION) {
                            dataPacksListModel.removeElement(existingDataPack);
                            break;
                        } else {
                            return;
                        }
                    }
                }
            }
            dataPacksListModel.addElement(dataPackFile);
        }
    }

    private void removeSelectedDataPacks() {
        final int[] selectedIndices = listDataPacks.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            dataPacksListModel.remove(selectedIndices[i]);
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

        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonCancel = new javax.swing.JButton();
        buttonExport = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        checkBoxGoodies = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        labelPlatform = new javax.swing.JLabel();
        labelPlatformWarning = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        comboBoxGameType = new javax.swing.JComboBox<>();
        checkBoxAllowCheats = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        comboBoxDifficulty = new javax.swing.JComboBox();
        checkBoxMapFeatures = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        fieldDirectory = new javax.swing.JTextField();
        fieldName = new javax.swing.JTextField();
        buttonSelectDirectory = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listDataPacks = new javax.swing.JList<>();
        buttonAddDataPack = new javax.swing.JButton();
        buttonRemoveDataPack = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        panelMinecraftWorldBorder = new javax.swing.JPanel();
        jLabel79 = new javax.swing.JLabel();
        spinnerMcBorderCentreX = new javax.swing.JSpinner();
        jLabel80 = new javax.swing.JLabel();
        spinnerMcBorderCentreY = new javax.swing.JSpinner();
        jLabel81 = new javax.swing.JLabel();
        spinnerMcBorderSize = new javax.swing.JSpinner();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        buttonTestExport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u5BFC\u51FA");

        buttonCancel.setText("\u53d6\u6d88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonExport.setText("\u5BFC\u51FA");
        buttonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExportActionPerformed(evt);
            }
        });

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.RIGHT);

        checkBoxGoodies.setSelected(true);
        checkBoxGoodies.setText(" ");
        checkBoxGoodies.setToolTipText("\u4F5C\u4E3A\u4E16\u754C\u521B\u5EFA\u8005\uFF0C\u51FA\u751F\u70B9\u9644\u8FD1\u4E3A\u4F60\u51C6\u5907\u4E00\u4E2A\u88C5\u6709\u5DE5\u5177\u548C\u8D44\u6E90\u7684\u7BB1\u5B50");

        jLabel1.setText("\u5730\u56FE\u683C\u5F0F:");

        labelPlatform.setForeground(new java.awt.Color(0, 0, 255));
        labelPlatform.setText("<html><u>[\u5B9E\u9A8C] Minecraft 1.17</u></html>");
        labelPlatform.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        labelPlatform.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelPlatformMouseClicked(evt);
            }
        });

        labelPlatformWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelPlatformWarning.setText("<html><b>\u672A\u77E5\u683C\u5F0F</b></html>");
        labelPlatformWarning.setToolTipText("<html>\u8BE5\u5730\u56FE\u672A\u77E5\u4E14\u65E0\u6CD5\u5BFC\u51FA\uFF0C\u53EF\u80FD\u662F\u56E0\u4E3A\u5176<br>\n\u7531\u4E00\u4E2A\u672A\u5B89\u88C5\u6216\u65E0\u6CD5\u52A0\u8F7D\u7684\u63D2\u4EF6\u652F\u6301.</html>");

        jLabel4.setText("\u6E38\u620F\u8BBE\u7F6E");

        jLabel5.setText("\u6E38\u620F\u6A21\u5F0F:");

        comboBoxGameType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxGameTypeActionPerformed(evt);
            }
        });

        checkBoxAllowCheats.setSelected(true);
        checkBoxAllowCheats.setText(" ");
        checkBoxAllowCheats.setToolTipText("\u662F\u5426\u5141\u8BB8\u547D\u4EE4");

        jLabel6.setText("\u56F0\u96BE\u5EA6:");

        comboBoxDifficulty.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\u548C\u5E73", "\u7B80\u5355", "\u666E\u901A", "\u56F0\u96BE" }));

        checkBoxMapFeatures.setSelected(true);
        checkBoxMapFeatures.setText(" ");

        jLabel7.setText("\u5168\u5C40\u8BBE\u5B9A");

        jLabel8.setLabelFor(checkBoxAllowCheats);
        jLabel8.setText("\u5141\u8BB8\u547D\u4EE4:");

        jLabel9.setLabelFor(checkBoxMapFeatures);
        jLabel9.setText("\u751F\u6210\u7ED3\u6784:");

        jLabel10.setLabelFor(checkBoxGoodies);
        jLabel10.setText("\u751F\u6210\u5956\u52B1\u7BB1:");

        jLabel2.setText("\u76EE\u5F55:");

        jLabel3.setText("\u540D\u79F0:");

        fieldDirectory.setText("jTextField1");

        fieldName.setText("jTextField2");

        buttonSelectDirectory.setText("...");
        buttonSelectDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDirectoryActionPerformed(evt);
            }
        });

        jLabel11.setText("\u6570\u636E\u5305:");

        listDataPacks.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listDataPacksValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listDataPacks);

        buttonAddDataPack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_add.png"))); // NOI18N
        buttonAddDataPack.setToolTipText("\u6DFB\u52A0\u6570\u636E\u5305");
        buttonAddDataPack.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonAddDataPack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddDataPackActionPerformed(evt);
            }
        });

        buttonRemoveDataPack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_delete.png"))); // NOI18N
        buttonRemoveDataPack.setToolTipText("\u79FB\u9664\u9009\u4E2D\u6570\u636E\u5305");
        buttonRemoveDataPack.setEnabled(false);
        buttonRemoveDataPack.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonRemoveDataPack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveDataPackActionPerformed(evt);
            }
        });

        jLabel12.setText("<html><i>\u4F7F\u7528\u98CE\u9669\u81EA\u8D1F. WorldPainter </i>\u4E0D\u4F1A<i>\u68C0\u67E5\u7248\u672C\u6709\u6548\u6027! <i></html>");

        panelMinecraftWorldBorder.setBorder(javax.swing.BorderFactory.createTitledBorder("\u4E16\u754C\u8FB9\u754C"));

        jLabel79.setText("\u4E2D\u5FC3:");

        spinnerMcBorderCentreX.setModel(new javax.swing.SpinnerNumberModel(0, -99999, 99999, 1));

        jLabel80.setText(", ");

        spinnerMcBorderCentreY.setModel(new javax.swing.SpinnerNumberModel(0, -99999, 99999, 1));

        jLabel81.setText("\u5927\u5C0F:");

        spinnerMcBorderSize.setModel(new javax.swing.SpinnerNumberModel(0, 0, 60000000, 1));

        jLabel85.setText("\u683C");

        jLabel86.setText("\u683C");

        javax.swing.GroupLayout panelMinecraftWorldBorderLayout = new javax.swing.GroupLayout(panelMinecraftWorldBorder);
        panelMinecraftWorldBorder.setLayout(panelMinecraftWorldBorderLayout);
        panelMinecraftWorldBorderLayout.setHorizontalGroup(
            panelMinecraftWorldBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinecraftWorldBorderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMinecraftWorldBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMinecraftWorldBorderLayout.createSequentialGroup()
                        .addComponent(jLabel79)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMcBorderCentreX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel80)
                        .addGap(0, 0, 0)
                        .addComponent(spinnerMcBorderCentreY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel85))
                    .addGroup(panelMinecraftWorldBorderLayout.createSequentialGroup()
                        .addComponent(jLabel81)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerMcBorderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel86)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelMinecraftWorldBorderLayout.setVerticalGroup(
            panelMinecraftWorldBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMinecraftWorldBorderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMinecraftWorldBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel79)
                    .addComponent(spinnerMcBorderCentreX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel80)
                    .addComponent(spinnerMcBorderCentreY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel85))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMinecraftWorldBorderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel81)
                    .addComponent(spinnerMcBorderSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel86))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldDirectory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDirectory))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonAddDataPack, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(buttonRemoveDataPack, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxGoodies))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelPlatformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel11))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxAllowCheats))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxGameType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkBoxMapFeatures)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(panelMinecraftWorldBorder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(fieldDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDirectory))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(labelPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelPlatformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxGoodies)
                    .addComponent(jLabel10))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(comboBoxGameType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(comboBoxDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxAllowCheats)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(checkBoxMapFeatures))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11))
                    .addComponent(panelMinecraftWorldBorder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(buttonAddDataPack)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveDataPack)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("General", jPanel1);

        buttonTestExport.setText("Test Export...");
        buttonTestExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTestExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonTestExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonExport)
                    .addComponent(buttonTestExport))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExportActionPerformed
        export(EXPORT_EVERYTHING);
    }//GEN-LAST:event_buttonExportActionPerformed

    private void buttonSelectDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectDirectoryActionPerformed
        selectDir();
    }//GEN-LAST:event_buttonSelectDirectoryActionPerformed

    private void platformChanged() {
        Platform newPlatform = world.getPlatform();
        labelPlatform.setText("<html><u>" + newPlatform.displayName + "</u></html>");
        GameType gameType = (GameType) comboBoxGameType.getSelectedItem();
        comboBoxGameType.setModel(new DefaultComboBoxModel<>(newPlatform.supportedGameTypes.toArray(new GameType[newPlatform.supportedGameTypes.size()])));
        if (newPlatform.supportedGameTypes.contains(gameType)) {
            comboBoxGameType.setSelectedItem(gameType);
        } else {
            comboBoxGameType.setSelectedItem(SURVIVAL);
        }
        comboBoxGameType.setEnabled(newPlatform.supportedGameTypes.size() > 1);
        if (newPlatform != JAVA_MCREGION) {
            checkBoxAllowCheats.setSelected(gameType == CREATIVE);
        } else {
            checkBoxAllowCheats.setSelected(false);
        }
        listDataPacks.setEnabled(newPlatform.capabilities.contains(DATA_PACKS));

        if (supportedPlatforms.contains(newPlatform)) {
            labelPlatformWarning.setVisible(false);
            File exportDir = Configuration.getInstance().getExportDirectory(newPlatform);
            if ((exportDir == null) || (!exportDir.isDirectory())) {
                exportDir = PlatformManager.getInstance().getDefaultExportDir(newPlatform);
            }
            if ((exportDir != null) && exportDir.isDirectory()) {
                fieldDirectory.setText(exportDir.getAbsolutePath());
            }
        } else {
            labelPlatformWarning.setVisible(true);
        }

        checkBoxGoodies.setSelected(world.isCreateGoodiesChest());

        pack();
        setControlStates();
    }

    private void comboBoxGameTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxGameTypeActionPerformed
        if ((world.getPlatform() != JAVA_MCREGION) && (comboBoxGameType.getSelectedItem() == CREATIVE)) {
            checkBoxAllowCheats.setSelected(true);
            comboBoxDifficulty.setSelectedIndex(DIFFICULTY_PEACEFUL);
        } else if (comboBoxGameType.getSelectedItem() == HARDCORE) {
            checkBoxAllowCheats.setSelected(false);
            comboBoxDifficulty.setSelectedIndex(DIFFICULTY_HARD);
        }
        setControlStates();
    }//GEN-LAST:event_comboBoxGameTypeActionPerformed

    private void labelPlatformMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelPlatformMouseClicked
        changePlatform();
    }//GEN-LAST:event_labelPlatformMouseClicked

    private void buttonTestExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTestExportActionPerformed
        testExport();
    }//GEN-LAST:event_buttonTestExportActionPerformed

    private void buttonAddDataPackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddDataPackActionPerformed
        addDataPack();
    }//GEN-LAST:event_buttonAddDataPackActionPerformed

    private void buttonRemoveDataPackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveDataPackActionPerformed
        removeSelectedDataPacks();
    }//GEN-LAST:event_buttonRemoveDataPackActionPerformed

    private void listDataPacksValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listDataPacksValueChanged
        setControlStates();
    }//GEN-LAST:event_listDataPacksValueChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddDataPack;
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonExport;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonRemoveDataPack;
    private javax.swing.JButton buttonSelectDirectory;
    private javax.swing.JButton buttonTestExport;
    private javax.swing.JCheckBox checkBoxAllowCheats;
    private javax.swing.JCheckBox checkBoxGoodies;
    private javax.swing.JCheckBox checkBoxMapFeatures;
    private javax.swing.JComboBox comboBoxDifficulty;
    private javax.swing.JComboBox<GameType> comboBoxGameType;
    private javax.swing.JTextField fieldDirectory;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelPlatform;
    private javax.swing.JLabel labelPlatformWarning;
    private javax.swing.JList<File> listDataPacks;
    private javax.swing.JPanel panelMinecraftWorldBorder;
    private javax.swing.JSpinner spinnerMcBorderCentreX;
    private javax.swing.JSpinner spinnerMcBorderCentreY;
    private javax.swing.JSpinner spinnerMcBorderSize;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final ColourScheme colourScheme;
    private final Set<Layer> hiddenLayers;
    private final boolean contourLines;
    private final int contourSeparation;
    private final TileRenderer.LightOrigin lightOrigin;
    private final CustomBiomeManager customBiomeManager;
    private final WorldPainter view;
    private final Map<Anchor, DimensionPropertiesEditor> dimensionPropertiesEditors = new HashMap<>();
    private final List<Platform> supportedPlatforms = new ArrayList<>();
    private final DefaultListModel<File> dataPacksListModel = new DefaultListModel<>();
    private boolean disableDisabledLayersWarning;

    private static String previouslyAcknowledgedWarnings;
    private static Reference<World2> warningsForWorld;

    private static final Logger logger = LoggerFactory.getLogger(ExportWorldDialog.class);
    private static final long serialVersionUID = 1L;
}