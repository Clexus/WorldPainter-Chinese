/*
 * ImportHeightMapDialog.java
 *
 * Created on 22-jan-2012, 19:47:55
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.IconUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.util.swing.TileProvider;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.heightMaps.*;
import org.pepsoft.worldpainter.heightMaps.gui.ImportPresetListCellRenderer;
import org.pepsoft.worldpainter.importing.HeightMapImporter;
import org.pepsoft.worldpainter.layers.Void;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.themes.SimpleTheme;
import org.pepsoft.worldpainter.themes.TerrainListCellRenderer;
import org.pepsoft.worldpainter.themes.Theme;
import org.pepsoft.worldpainter.themes.impl.simple.SimpleThemeEditor;
import org.pepsoft.worldpainter.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static com.google.common.primitives.Ints.asList;
import static javax.swing.JOptionPane.*;
import static org.pepsoft.minecraft.Constants.DEFAULT_MAX_HEIGHT_MCREGION;
import static org.pepsoft.minecraft.Constants.DEFAULT_WATER_LEVEL;
import static org.pepsoft.util.AwtUtils.doLaterOnEventThread;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.util.swing.MessageUtils.showInfo;
import static org.pepsoft.util.swing.ProgressDialog.NOT_CANCELABLE;
import static org.pepsoft.util.swing.SpinnerUtils.setMaximum;
import static org.pepsoft.util.swing.SpinnerUtils.setMinimum;
import static org.pepsoft.worldpainter.App.*;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.DefaultPlugin.JAVA_MCREGION;
import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_DETAIL;
import static org.pepsoft.worldpainter.Dimension.Anchor.NORMAL_MASTER;
import static org.pepsoft.worldpainter.Dimension.Role.MASTER;
import static org.pepsoft.worldpainter.HeightTransform.IDENTITY;
import static org.pepsoft.worldpainter.Terrain.GRASS;
import static org.pepsoft.worldpainter.Terrain.PICK_LIST;
import static org.pepsoft.worldpainter.WPTileProvider.Effect.FADE_TO_FIFTY_PERCENT;
import static org.pepsoft.worldpainter.WPTileProvider.HIDE_ALL_LAYERS;
import static org.pepsoft.worldpainter.util.LayoutUtils.setDefaultSizeAndLocation;
import static org.pepsoft.worldpainter.util.MinecraftUtil.blocksToWalkingTime;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"ConstantConditions", // Guaranteed by code
        "FieldCanBeLocal", "unused", "Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
public class ImportHeightMapDialog extends WorldPainterDialog implements DocumentListener, SimpleThemeEditor.ChangeListener {
    public ImportHeightMapDialog(Window parent, ColourScheme colourScheme, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin, File preselectedFile) {
        this(parent, null, colourScheme, null, contourLines, contourSeparation, lightOrigin, preselectedFile);
    }

    public ImportHeightMapDialog(Window parent, Dimension currentDimension, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin, File preselectedFile) {
        super(parent);
        this.currentDimension = currentDimension;
        this.colourScheme = colourScheme;
        this.customBiomeManager = customBiomeManager;
        this.contourLines = contourLines;
        this.contourSeparation = contourSeparation;
        this.lightOrigin = lightOrigin;

        initComponents();
        tiledImageViewer2.setZoom(0);
        tiledImageViewer2.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int oldZoom = tiledImageViewer2.getZoom(), zoom;
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(oldZoom - e.getWheelRotation(), 6);
                } else {
                    zoom = Math.max(oldZoom - e.getWheelRotation(), -4);
                }
                if (zoom != oldZoom) {
                    tiledImageViewer2.setZoom(zoom, e.getX(), e.getY());
                }
            }
        });
        tiledImageViewerContainer1.setView(tiledImageViewer2);
        
        themeEditor.setColourScheme(colourScheme);
        spinnerOffsetX.setEditor(new NumberEditor(spinnerOffsetX, "0"));
        spinnerOffsetY.setEditor(new NumberEditor(spinnerOffsetY, "0"));
        checkBoxCreateTiles.setSelected(true);
        labelWarning.setVisible(false);
        comboBoxPlatform.setModel(new DefaultComboBoxModel<>(PlatformManager.getInstance().getAllPlatforms().toArray(new Platform[0])));
        labelWarningCutOffBelow.setVisible(false);
        labelWarningCutOffAbove.setVisible(false);
        comboBoxSingleTerrain.setRenderer(new TerrainListCellRenderer(colourScheme));
        comboBoxSingleTerrain.setSelectedItem(GRASS);
        comboBoxPreset.setRenderer(new ImportPresetListCellRenderer());

        fieldFilename.getDocument().addDocumentListener(this);
        
        rootPane.setDefaultButton(buttonOk);
        
        if (currentDimension != null) {
            platform = currentDimension.getWorld().getPlatform();
            jTabbedPane1.setEnabledAt(1, false);
            comboBoxPlatform.setSelectedItem(platform);
            comboBoxPlatform.setEnabled(false);
            buttonResetDefaults.setEnabled(false);
            spinnerWorldMiddle.setValue(((HeightMapTileFactory) currentDimension.getTileFactory()).getTheme().clone().getWaterHeight());
            buttonLoadDefaults.setEnabled(true);
            buttonSaveAsDefaults.setEnabled(true);
            checkBoxOnlyRaise.setSelected(true);
            comboBoxSingleTerrain.setModel(new DefaultComboBoxModel<>(Terrain.getConfiguredValues()));
            checkBoxMasterDimension.setSelected(currentDimension.getAnchor().role == MASTER);
            checkBoxMasterDimension.setEnabled(false);
        } else {
            platform = Configuration.getInstance().getDefaultPlatform();
            themeEditor.setTheme(SimpleTheme.createDefault(GRASS, platform.minZ, platform.standardMaxHeight, DEFAULT_WATER_LEVEL, true, true));
            themeEditor.setChangeListener(this);
            comboBoxPlatform.setSelectedItem(platform);
            labelNoUndo.setText(" ");
            checkBoxCreateTiles.setEnabled(false);
            checkBoxOnlyRaise.setEnabled(false);
            comboBoxSingleTerrain.setModel(new DefaultComboBoxModel<>(PICK_LIST));
            radioButtonLeaveTerrain.setEnabled(false);
            loadDefaults();
        }
        final boolean masterDimensionSelected = checkBoxMasterDimension.isSelected();
        jLabel20.setVisible(masterDimensionSelected);
        labelExportedSize.setVisible(masterDimensionSelected);
        jLabel22.setVisible(masterDimensionSelected);
        labelExportedOffset.setVisible(masterDimensionSelected);

        scaleToUI();
        pack();
        setDefaultSizeAndLocation(this, 60);

        programmaticChange = false;
        platformChanged();
        updateImageLevelLabels();
        setControlStates();
        updatePreview(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if ((preselectedFile != null) && preselectedFile.isFile()) {
                    fieldFilename.setText(preselectedFile.getAbsolutePath());
                } else {
                    selectFile();
                }
            }
        });
    }

    public World2 getImportedWorld() {
        if (currentDimension != null) {
            throw new IllegalStateException();
        }
        final HeightMapImporter importer = createImporter(true);
        if (importer == null) {
            return null;
        }
        World2 world = ProgressDialog.executeTask(this, new ProgressTask<World2>() {
            @Override
            public String getName() {
                return "\u5BFC\u5165\u9AD8\u5EA6\u56FE";
            }

            @Override
            public World2 execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                return importer.importToNewWorld((checkBoxMasterDimension.isSelected()) ? NORMAL_MASTER : NORMAL_DETAIL, progressReceiver);
            }
        }, NOT_CANCELABLE);
        Configuration.getInstance().setHeightMapsDirectory(selectedFile.getParentFile());
        return world;
    }

    // DocumentListener

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

    // SimpleThemeEditor.ChangeListener

    @Override
    public void settingsModified(SimpleThemeEditor editor) {
        if (currentDimension != null) {
            buttonResetDefaults.setEnabled(true);
        }
        buttonSaveAsDefaults.setEnabled(true);
        buttonLoadDefaults.setEnabled(true);
        updatePreview(false);
    }

    private HeightMapImporter createImporter(boolean interactive) {
        // TODO keep this? It shouldn't be necessary!
        if ((selectedFile == null) || (! selectedFile.exists())) {
            if (interactive) {
                beepAndShowError(this, "\u8BF7\u9009\u62E9\u4E00\u4E2A\u8981\u5BFC\u5165\u7684\u56FE\u50CF\u6587\u4EF6", "\u672A\u9009\u62E9\u6587\u4EF6");
            }
            return null;
        } else if (image == null) {
            if (interactive) {
                beepAndShowError(this, "\u8BF7\u9009\u62E9\u4E00\u4E2A\u6709\u6548\u7684\u56FE\u7247\u6587\u4EF6.", "\u672A\u9009\u62E9\u6709\u6548\u7684\u56FE\u7247\u6587\u4EF6");
            }
            return null;
        }
        final float scale = (float) spinnerScale.getValue();
        HeightMap heightMap = BitmapHeightMap.build()
                .withName(selectedFile.getName())
                .withImage(image)
                .withFile(selectedFile)
                .now();
        if (scale != 100.f) {
            heightMap = heightMap.smoothed();
        }
        final int offsetX = (int) spinnerOffsetX.getValue();
        final int offsetY = (int) spinnerOffsetY.getValue();
        if ((scale != 100.0f) || (offsetX != 0) || (offsetY != 0)) {
            heightMap = new TransformingHeightMap(heightMap.getName() + " \u5DF2\u8F6C\u6362", heightMap, scale / 100, scale / 100, offsetX, offsetY, 0.0f);
        }
        if (checkBoxInvert.isSelected()) {
            heightMap = new DifferenceHeightMap(new ConstantHeightMap((float) (Math.pow(2, bitDepth) - 1)), heightMap);
        }

        String name = selectedFile.getName();
        final int p = name.lastIndexOf('.');
        if (p != -1) {
            name = name.substring(0, p);
        }

        final int waterLevel = (int) spinnerWorldMiddle.getValue();
        final int minHeight = (int) comboBoxMinHeight.getSelectedItem(), maxHeight = (int) comboBoxMaxHeight.getSelectedItem();

        final HeightMapImporter importer = new HeightMapImporter();
        final Platform platform = (Platform) comboBoxPlatform.getSelectedItem();
        importer.setPlatform(platform);
        importer.setHeightMap(heightMap);
        importer.setImageFile(selectedFile);
        importer.setName(name);
        if (currentDimension != null) {
            importer.setTileFactory(currentDimension.getTileFactory());
            if (radioButtonSingleTerrain.isSelected()) {
                final SimpleTheme theme = SimpleTheme.createSingleTerrain((Terrain) comboBoxSingleTerrain.getSelectedItem(), minHeight, maxHeight, waterLevel);
                theme.setSeed(seed);
                importer.setTheme(theme);
            } else if (radioButtonLeaveTerrain.isSelected()) {
                importer.setTheme(null);
            }
        } else {
            themeEditor.save();
            final SimpleTheme theme;
            if (radioButtonSingleTerrain.isSelected()) {
                theme = SimpleTheme.createSingleTerrain((Terrain) comboBoxSingleTerrain.getSelectedItem(), minHeight, maxHeight, waterLevel);
            } else {
                theme = themeEditor.getTheme();
                theme.setMinMaxHeight(minHeight, maxHeight, IDENTITY);
            }
            theme.setSeed(seed);
            importer.setTileFactory(new HeightMapTileFactory(seed, new SumHeightMap(new ConstantHeightMap(waterLevel - 4), new NoiseHeightMap((float) 20, 1.0, 1, 0)), minHeight, maxHeight, false, theme));
        }
        importer.setMinHeight(minHeight);
        importer.setMaxHeight(maxHeight);
        importer.setImageLowLevel((Double) spinnerImageLow.getValue());
        importer.setImageHighLevel((Double) spinnerImageHigh.getValue());
        importer.setWorldLowLevel((Integer) spinnerWorldLow.getValue());
        importer.setWorldWaterLevel(waterLevel);
        importer.setWorldHighLevel((Integer) spinnerWorldHigh.getValue());
        importer.setVoidBelow(checkBoxVoid.isSelected());
        if (checkBoxVoid.isSelected()) {
            importer.setVoidBelowLevel((Double) spinnerVoidBelow.getValue());
        }
        return importer;
    }

    private void setControlStates() {
        File file = new File(fieldFilename.getText());
        if (file.isFile() && (! file.equals(selectedFile))) {
            selectedFile = file;
            loadImage();
        }
        boolean fileSelected = (selectedFile != null) && selectedFile.isFile();
        comboBoxPreset.setEnabled(fileSelected);
        buttonOk.setEnabled(fileSelected);
        spinnerImageLow.setEnabled(fileSelected);
        spinnerImageHigh.setEnabled(fileSelected);
    }

    private void loadImage() {
        try {
            image = null; // Set image to null first to make more memory available for loading the new image
            image = ImageIO.read(selectedFile);
            if (image == null) {
                labelImageDimensions.setForeground(Color.RED);
                labelImageDimensions.setIcon(ICON_WARNING);
                labelImageDimensions.setText("\u6587\u4EF6\u683C\u5F0F\u4E0D\u662F\u6709\u6548\u7684\u56FE\u7247\u683C\u5F0F\u6216\u6587\u4EF6\u5DF2\u635F\u574F!");
                selectedFile = null;
            } else if ((image.getType() == BufferedImage.TYPE_BYTE_BINARY) || (image.getType() == BufferedImage.TYPE_BYTE_INDEXED)) {
                labelImageDimensions.setForeground(Color.RED);
                labelImageDimensions.setIcon(ICON_WARNING);
                labelImageDimensions.setText("\u4E0D\u652F\u6301\u7D22\u5F15\u56FE! \u8BF7\u8F6C\u6362\u4E3A\u975E\u7D22\u5F15\u56FE.");
                selectedFile = null;
            } else if (image.isAlphaPremultiplied()) {
                labelImageDimensions.setForeground(Color.RED);
                labelImageDimensions.setIcon(ICON_WARNING);
                labelImageDimensions.setText("\u4E0D\u652F\u6301\u9884\u4E58alpha\u901A\u9053! \u8BF7\u8F6C\u6362\u4E3A\u975E\u9884\u4E58\u7684.");
                selectedFile = null;
            } else {
                final BitmapHeightMap heightMap = BitmapHeightMap.build().withImage(image).now();
                final int width = heightMap.getWidth(), height = heightMap.getHeight();
                programmaticChange = true;
                try {
                    labelImageDimensions.setForeground(null);
                    labelImageDimensions.setIcon(null);
                    bitDepth = heightMap.getBitDepth();
                    imageMinHeight = heightMap.getMinHeight();
                    imageMaxHeight = heightMap.getMaxHeight();
                    final boolean invert = checkBoxInvert.isSelected();
                    final double[] range = heightMap.getRange();
                    imageLowValue = invert ? (imageMaxHeight - range[1]) : range[0];
                    imageHighValue = invert ? (imageMaxHeight - range[0]) : range[1];
                    final double stepSize;
                    if (heightMap.isFloatingPoint()) {
                        final double delta = imageHighValue - imageLowValue;
                        if (delta <= 1.0) {
                            stepSize = 0.001;
                        } else if (delta <= 10.0) {
                            stepSize = 0.01;
                        } else if (delta <= 100.0) {
                            stepSize = 0.1;
                        } else {
                            stepSize = 1.0;
                        }
                    } else {
                        stepSize = 1.0;
                    }
                    setMinimum(spinnerImageLow, imageMinHeight);
                    setMaximum(spinnerImageLow, imageMaxHeight);
                    ((SpinnerNumberModel) spinnerImageLow.getModel()).setStepSize(stepSize);
                    setMinimum(spinnerImageHigh, imageMinHeight);
                    setMaximum(spinnerImageHigh, imageMaxHeight);
                    ((SpinnerNumberModel) spinnerImageHigh.getModel()).setStepSize(stepSize);
                    setMinimum(spinnerVoidBelow, imageMinHeight);
                    setMaximum(spinnerVoidBelow, imageMaxHeight);
                    ((SpinnerNumberModel) spinnerVoidBelow.getModel()).setStepSize(stepSize);
                    spinnerVoidBelow.setValue(imageLowValue);
                } finally {
                    programmaticChange = false;
                }
                if (heightMap.isFloatingPoint()) {
                    labelImageLowestLevel.setText(FLOAT_NUMBER_FORMAT.format(imageLowValue));
                    labelImageHighestLevel.setText(FLOAT_NUMBER_FORMAT.format(imageHighValue));
                } else {
                    labelImageLowestLevel.setText(INT_NUMBER_FORMAT.format(imageLowValue));
                    labelImageHighestLevel.setText(INT_NUMBER_FORMAT.format(imageHighValue));
                }

                // Set levels to reasonable defaults
                selectDefaultVerticalScaling(false);

                if (heightMap.isFloatingPoint()) {
                    labelImageDimensions.setText(String.format("\u56FE\u7247\u5927\u5C0F: %,d x %,d, %d \u4F4D, \u6700\u4F4E\u503C: %,f, \u6700\u9AD8\u503C: %,f", width, height, bitDepth, imageLowValue, imageHighValue));
                } else {
                    labelImageDimensions.setText(String.format("\u56FE\u7247\u5927\u5C0F: %,d x %,d, %d \u4F4D, \u6700\u4F4E\u503C: %,d, \u6700\u9AD8\u503C: %,d", width, height, bitDepth, Math.round(imageLowValue), Math.round(imageHighValue)));
                }
                updateWorldDimensions();
                updatePreview(true);
            }
        } catch (IOException e) {
            logger.error("I/O error loading image " + selectedFile, e);
            labelImageDimensions.setForeground(Color.RED);
            labelImageDimensions.setText(String.format("\u52A0\u8F7D\u56FE\u7247\u65F6\u51FA\u73B0I/O\u9519\u8BEF (\u9519\u8BEF\u6D88\u606F: %s)!", e.getMessage()));
            selectedFile = null;
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException loading image " + selectedFile, e);
            labelImageDimensions.setForeground(Color.RED);
            if (e.getMessage().equals("Invalid scanline stride")) {
                labelImageDimensions.setText("\u56FE\u50CF\u6570\u636E\u592A\u5927; \u8BF7\u5C1D\u8BD5\u524A\u51CF\u7EF4\u5EA6\u6216\u4F4D\u6DF1\u5EA6");
            } else {
                labelImageDimensions.setText("\u56FE\u7247\u6570\u636E\u51FA\u9519: " + e.getMessage());
            }
            selectedFile = null;
        }
    }

    private void selectDefaultVerticalScaling(boolean preserveCurrent) {
        if (image == null) {
            return;
        }
        final ImportPreset currentPreset = preserveCurrent ? (ImportPreset) comboBoxPreset.getSelectedItem() : null;
        final Vector<ImportPreset> presets = new Vector<>(ImportPreset.PRESETS.length + 1);
        presets.add(null);
        for (ImportPreset preset: ImportPreset.PRESETS) {
            if (preset.isValid(imageMinHeight, imageMaxHeight, imageLowValue, imageHighValue, platform, (Integer) comboBoxMinHeight.getSelectedItem(), (Integer) comboBoxMaxHeight.getSelectedItem())) {
                presets.add(preset);
            }
        }
        final DefaultComboBoxModel<ImportPreset> presetsModel = new DefaultComboBoxModel<>(presets);
        comboBoxPreset.setModel(presetsModel);
        comboBoxPreset.setEnabled(presets.size() > 1);
        if ((currentPreset != null) && presets.contains(currentPreset)) {
            comboBoxPreset.setSelectedItem(currentPreset);
        } else {
            comboBoxPreset.setSelectedItem(presets.lastElement());
        }
        applyPreset((ImportPreset) comboBoxPreset.getSelectedItem());
    }

    private void updateWorldDimensions() {
        final float dimensionScale = checkBoxMasterDimension.isSelected() ? 16.0f : 1.0f;
        labelExportedOffset.setText(INT_NUMBER_FORMAT.format(Math.round((int) spinnerOffsetX.getValue() * dimensionScale)) + ", " + INT_NUMBER_FORMAT.format(Math.round((int) spinnerOffsetY.getValue() * dimensionScale)));
        if (image == null) {
            return;
        }
        final float importScale = (float) spinnerScale.getValue();
        final int scaledWidth = Math.round(image.getWidth() * (importScale / 100));
        final int scaledHeight = Math.round(image.getHeight() * (importScale / 100));
        labelWorldDimensions.setText("\u7F29\u653E\u540E\u5927\u5C0F: " + INT_NUMBER_FORMAT.format(scaledWidth) + " x " + INT_NUMBER_FORMAT.format(scaledHeight) + " \u683C");
        final int exportedWidth = Math.round(scaledWidth * dimensionScale);
        final int exportedHeight = Math.round(scaledHeight * dimensionScale);
        labelExportedSize.setText(INT_NUMBER_FORMAT.format(exportedWidth) + " x " + INT_NUMBER_FORMAT.format(exportedHeight) + " \u683C");
        String westEastTime = blocksToWalkingTime(exportedWidth);
        String northSouthTime = blocksToWalkingTime(exportedHeight);
        if (westEastTime.equals(northSouthTime)) {
            labelWalkingTime.setText(westEastTime);
        } else {
            labelWalkingTime.setText("\u7531\u897F\u81F3\u4E1C: " + westEastTime + ", \u7531\u5317\u81F3\u5357: " + northSouthTime);
        }
    }

    private void updateImageLevelLabels() {
        if (programmaticChange) {
            return;
        }
        final int minHeight = (Integer) comboBoxMinHeight.getSelectedItem();
        final int maxHeight = (Integer) comboBoxMaxHeight.getSelectedItem() - 1;
        final double imageLowLevel = (Double) spinnerImageLow.getValue();
        final double imageHighLevel = (Double) spinnerImageHigh.getValue();
        final int worldLowLevel = (Integer) spinnerWorldLow.getValue();
        final int worldMiddleLevel = (Integer) spinnerWorldMiddle.getValue();
        final int worldHighLevel = (Integer) spinnerWorldHigh.getValue();
        final double levelScale = (worldHighLevel - worldLowLevel) / (imageHighLevel - imageLowLevel);
        final long imageMiddleLevel = (long) ((worldMiddleLevel - worldLowLevel) / levelScale + imageLowLevel);
        final int worldLowestLevel = (int) ((imageLowValue - imageLowLevel) * levelScale + worldLowLevel);
        final int worldHighestLevel = (int) ((imageHighValue - imageLowLevel) * levelScale + worldLowLevel);
        if (imageMiddleLevel < imageLowValue) {
            labelImageWaterLevel.setText("-");
        } else if (imageMiddleLevel > imageHighValue) {
            labelImageWaterLevel.setText("> " + FLOAT_NUMBER_FORMAT.format(imageHighValue));
        } else {
            labelImageWaterLevel.setText(INT_NUMBER_FORMAT.format(imageMiddleLevel));
        }
        if (worldLowestLevel < minHeight) {
            labelWorldLowestLevel.setText("<html><b>&lt; " + INT_NUMBER_FORMAT.format(minHeight) + "</b></html>");
            labelWarningCutOffBelow.setVisible(true);
        } else if (worldLowestLevel > maxHeight) {
            labelWorldLowestLevel.setText("<html><b>&gt; " + INT_NUMBER_FORMAT.format(maxHeight) + "</b></html>");
            labelWarningCutOffBelow.setVisible(false);
        } else {
            labelWorldLowestLevel.setText(INT_NUMBER_FORMAT.format(worldLowestLevel));
            labelWarningCutOffBelow.setVisible(false);
        }
        if (worldHighestLevel < minHeight) {
            labelWorldHighestLevel.setText("<html><b>&lt; " + INT_NUMBER_FORMAT.format(minHeight) + "</b></html>");
            labelWarningCutOffAbove.setVisible(false);
        } else if (worldHighestLevel > maxHeight) {
            labelWorldHighestLevel.setText("<html><b>&gt; " + INT_NUMBER_FORMAT.format(maxHeight) + "</b></html>");
            labelWarningCutOffAbove.setVisible(true);
        } else {
            labelWorldHighestLevel.setText(INT_NUMBER_FORMAT.format(worldHighestLevel));
            labelWarningCutOffAbove.setVisible(false);
        }
    }

    private void loadDefaults() {
        loadDefaultTheme();
        updateImageLevelLabels();
        updatePreview(false);
    }

    private void loadDefaultTheme() {
        final int minHeight = comboBoxMinHeight.getSelectedItem() != null ? (Integer) comboBoxMinHeight.getSelectedItem() : platform.minZ;
        final int maxHeight = comboBoxMaxHeight.getSelectedItem() != null ? (Integer) comboBoxMaxHeight.getSelectedItem() : platform.standardMaxHeight;
        Theme defaultTheme = Configuration.getInstance().getHeightMapDefaultTheme();
        if (defaultTheme == null) {
            defaultTheme = SimpleTheme.createDefault(GRASS, minHeight, maxHeight, DEFAULT_WATER_LEVEL, true, true);
            if (currentDimension == null) {
                buttonResetDefaults.setEnabled(false);
            }
        } else {
            buttonResetDefaults.setEnabled(true);
        }
        defaultTheme.setMinMaxHeight(minHeight, maxHeight, IDENTITY);
        spinnerWorldMiddle.setValue(defaultTheme.getWaterHeight());
        themeEditor.setTheme((SimpleTheme) defaultTheme);
        buttonLoadDefaults.setEnabled(false);
        buttonSaveAsDefaults.setEnabled(false);
    }

    private void saveAsDefaults() {
        if (themeEditor.save()) {
            Theme defaults = themeEditor.getTheme();
            Configuration.getInstance().setHeightMapDefaultTheme(defaults);
            buttonResetDefaults.setEnabled(true);
            buttonLoadDefaults.setEnabled(false);
            buttonSaveAsDefaults.setEnabled(false);
        }
    }

    private void resetDefaults() {
        if (currentDimension != null) {
            Theme theme = ((HeightMapTileFactory) currentDimension.getTileFactory()).getTheme();
            buttonResetDefaults.setEnabled(false);
            spinnerWorldMiddle.setValue(theme.getWaterHeight());
            updateImageLevelLabels();
            updatePreview(false);
            themeEditor.setTheme((SimpleTheme) theme);
            buttonLoadDefaults.setEnabled(true);
            buttonSaveAsDefaults.setEnabled(true);
        } else {
            Configuration.getInstance().setHeightMapDefaultTheme(null);
            loadDefaults();
            buttonSaveAsDefaults.setEnabled(false);
            showInfo(this, "\u4E3B\u9898\u5DF2\u91CD\u7F6E\u4E3A\u51FA\u5382\u8BBE\u7F6E.", "\u9ED8\u8BA4\u4E3B\u9898\u5DF2\u91CD\u7F6E");
        }
    }

    private void importToDimension() {
        if (currentDimension == null) {
            throw new IllegalStateException();
        }
        final HeightMapImporter importer = createImporter(true);
        if (importer == null) {
            return;
        }
        importer.setOnlyRaise(checkBoxOnlyRaise.isSelected());
        ProgressDialog.executeTask(this, new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "\u5BFC\u5165\u9AD8\u5EA6\u56FE";
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                importer.importToDimension(currentDimension, checkBoxCreateTiles.isSelected(), progressReceiver);
                return null;
            }
        }, NOT_CANCELABLE);
        Configuration.getInstance().setHeightMapsDirectory(selectedFile.getParentFile());
        currentDimension.clearUndo();
        currentDimension.armSavePoint();
    }

    @Override
    protected void ok() {
        if (currentDimension != null) {
            importToDimension();
        } else if (checkBoxMasterDimension.isSelected()) {
            final Configuration config = Configuration.getInstance();
            if (! config.isMessageDisplayedCountAtLeast(MESSAGE_KEY_MASTER_WARNING, 3)) {
                DesktopUtils.beep();
                if (JOptionPane.showConfirmDialog(this, /* language=HTML */ "<html>" +
                        "<h1>\u5173\u4E8E\u6269\u5C55\u533A\u7EF4\u5EA6</h1>" +
                        "<p>\u6269\u5C55\u533A\u7EF4\u5EA6\u5C06\u4F1A\u4EE5 256 \u500D\u5927\u5C0F(\u4EE5\u9762\u79EF\u7B97)\u5BFC\u51FA\uFF0C\u6709\u52A9\u4E8E\u521B\u5EFA\u5927\u578B\u5730\u56FE.</p>" +
                        "<ul>" +
                        "    <li>\u4F60<b>\u65E0\u6CD5</b>\u64A4\u9500\u8BE5\u64CD\u4F5C; \u5982\u679C\u4F60\u4E0D\u60F3\u8981\u8FD9\u4E2A\u6269\u5C55\u533A\u7EF4\u5EA6<br>\u4F60\u9700\u8981\u5728\u4E4B\u540E\u4ECE\u5934\u5F00\u59CB." +
                        "    <li>\u52A0\u8F7D, \u7F16\u8F91\u6216\u4FDD\u5B58\u901F\u5EA6\u53EF\u80FD\u5F88\u5FEB, \u4F46\u662F\u5BFC\u51FA\u4E0D\u4F1A!<br>\u5BFC\u51FA\u65F6\u95F4\u76F8\u5BF9\u4E8E\u540C\u7B49\u50CF\u7D20\u5927\u5C0F\u7684\u5730\u56FE\u5C06\u4F1A\u53D8\u4E3A<b>256\u500D</b>." +
                        "    <li>\u4F60\u53EF\u4EE5\u4EE51:1\u7684\u6BD4\u4F8B\u7CBE\u8C03\u6269\u5C55\u533A\u7EF4\u5EA6\u7684\u5730\u5F62<br>\u53EA\u9700\u5207\u6362\u5230\u4E3B\u4E16\u754C\u89C6\u56FE (\u4F7F\u7528\u5FEB\u6377\u952E" + COMMAND_KEY_NAME + "+M \u6216\u4ECE\u89C6\u56FE\u83DC\u5355\u8FDB\u5165) \u540E\u6DFB\u52A0\u5206\u533A (\u4F7F\u7528\u5FEB\u6377\u952E" + COMMAND_KEY_NAME + "+T \u6216\u4F7F\u7528\u7F16\u8F91\u83DC\u5355) \u5373\u53EF." +
                        "</ul>" +
                        "<p>\u4F60\u786E\u8BA4\u8981\u6DFB\u52A0\u6269\u5C55\u533A\u7EF4\u5EA6\u5417?</p>" +
                        "</html>", "\u662F\u5426\u521B\u5EFA\u6269\u5C55\u533A\u7EF4\u5EA6?", YES_NO_OPTION, WARNING_MESSAGE) != YES_OPTION) {
                    return;
                }
                config.setMessageDisplayed(MESSAGE_KEY_MASTER_WARNING);
            }
        }
        super.ok();
    }

    private void platformChanged() {
        if (programmaticChange) {
            return;
        }
        platform = (Platform) comboBoxPlatform.getSelectedItem();
        final int maxHeight;
        programmaticChange = true;
        try {
            if (currentDimension != null) {
                final int minHeight = currentDimension.getMinHeight();
                maxHeight = currentDimension.getMaxHeight();
                comboBoxMinHeight.setModel(new DefaultComboBoxModel<>(new Integer[] { minHeight }));
                comboBoxMinHeight.setSelectedItem(minHeight);
                comboBoxMinHeight.setEnabled(false);
                comboBoxMaxHeight.setModel(new DefaultComboBoxModel<>(new Integer[] { maxHeight }));
                comboBoxMaxHeight.setSelectedItem(maxHeight);
                comboBoxMaxHeight.setEnabled(false);
            } else {
                final List<Integer> minHeights = new ArrayList<>(asList(platform.minHeights));
                minHeights.removeIf(height -> height < MIN_HEIGHT);
                if (minHeights.isEmpty()) {
                    minHeights.add(MIN_HEIGHT);
                }
                comboBoxMinHeight.setModel(new DefaultComboBoxModel<>(minHeights.toArray(new Integer[minHeights.size()])));
                comboBoxMinHeight.setSelectedItem(Math.max(platform.minZ, MIN_HEIGHT));
                comboBoxMinHeight.setEnabled(minHeights.size() > 1);
                maxHeight = platform.standardMaxHeight;
                final List<Integer> maxHeights = new ArrayList<>(asList(platform.maxHeights));
                maxHeights.removeIf(height -> height > MAX_HEIGHT);
                if (maxHeights.isEmpty()) {
                    maxHeights.add(MAX_HEIGHT);
                }
                comboBoxMaxHeight.setModel(new DefaultComboBoxModel<>(maxHeights.toArray(new Integer[maxHeights.size()])));
                comboBoxMaxHeight.setSelectedItem(Math.min(platform.standardMaxHeight, MAX_HEIGHT));
                comboBoxMaxHeight.setEnabled(maxHeights.size() > 1);
            }
            minHeightChanged();
            maxHeightChanged();
            spinnerWorldHigh.setValue((int) Math.min(maxHeight - 1, (long) Math.pow(2, bitDepth) - 1)); // TODO overflow
            loadDefaultTheme();
        } finally {
            programmaticChange = false;
        }
    }

    private void minHeightChanged() {
        int minHeight = (int) comboBoxMinHeight.getSelectedItem();
        programmaticChange = true;
        try {
            setMinimum(spinnerWorldLow, minHeight);
            setMinimum(spinnerWorldMiddle, minHeight);
            setMinimum(spinnerWorldHigh, minHeight);
            labelMinHeight.setText(INT_NUMBER_FORMAT.format(minHeight));
        } finally {
            programmaticChange = false;
        }

        // Set levels to reasonable defaults
        selectDefaultVerticalScaling(true);
        updatePreview(false);
    }

    private void maxHeightChanged() {
        int maxHeight = (int) comboBoxMaxHeight.getSelectedItem();
        programmaticChange = true;
        try {
            setMaximum(spinnerWorldLow, maxHeight - 1);
            setMaximum(spinnerWorldMiddle, maxHeight - 1);
            setMaximum(spinnerWorldHigh, maxHeight - 1);
            labelMaxHeight.setText(INT_NUMBER_FORMAT.format(maxHeight - 1));
        } finally {
            programmaticChange = false;
        }

        // Set levels to reasonable defaults
        selectDefaultVerticalScaling(true);
        updatePreview(false);

        labelWarning.setVisible((comboBoxPlatform.getSelectedItem() == JAVA_MCREGION) && (maxHeight != DEFAULT_MAX_HEIGHT_MCREGION));
    }

    private void updatePreview(boolean recentre) {
        doLaterOnEventThread(UPDATE_HEIGHT_MAP_PREVIEW, 250, () -> {
            if (image != null) {
                final HeightMapImporter importer = createImporter(false);
                // No idea how this could ever be null, but it has been observed in the wild:
                if (importer != null) {
                    final TileProvider previewProvider = importer.getPreviewProvider(checkBoxOnlyRaise.isSelected() ? currentDimension : null, colourScheme, contourLines, contourSeparation, lightOrigin);
                    if (previewProvider != null) {
                        tiledImageViewer2.setTileProvider(LAYER_HEIGHT_MAP, previewProvider);
                        if (recentre) {
                            tiledImageViewer2.moveTo(image.getWidth() / 2, image.getHeight() / 2);
                        }
                    }
                }
            }
            if (currentDimension != null) {
                tiledImageViewer2.setTileProvider(LAYER_CURRENT_DIMENSION, new WPTileProvider(currentDimension, colourScheme, customBiomeManager, HIDE_ALL_LAYERS, contourLines, contourSeparation, lightOrigin, false, FADE_TO_FIFTY_PERCENT, true, null));
            }
        });
    }

    private void applyPreset(ImportPreset preset) {
        if (preset != null) {
            programmaticChange = true;
            try {
                final ImportPreset.Mapping mapping = preset.getMapping(imageMaxHeight, imageLowValue, imageHighValue, platform, (Integer) comboBoxMaxHeight.getSelectedItem());
                spinnerImageLow.setValue(mapping.imageLow);
                spinnerWorldLow.setValue(mapping.worldLow);
                spinnerImageHigh.setValue(mapping.imageHigh);
                spinnerWorldHigh.setValue(mapping.worldHigh);
            } finally {
                programmaticChange = false;
            }
        }
        updateImageLevelLabels();
    }

    private void resetPreset() {
        if (programmaticChange) {
            return;
        }
        programmaticChange = true;
        try {
            comboBoxPreset.setSelectedItem(null);
        } finally {
            programmaticChange = false;
        }
    }

    private void selectFile() {
        File myHeightMapDir = heightMapDir;
        if (myHeightMapDir == null) {
            myHeightMapDir = Configuration.getInstance().getHeightMapsDirectory();
        }
        if (myHeightMapDir == null) {
            myHeightMapDir = Configuration.getInstance().getMasksDirectory();
        }
        final File file = ImageUtils.selectImageForOpen(this, "\u9AD8\u5EA6\u56FE\u6587\u4EF6", myHeightMapDir);
        if (file != null) {
            heightMapDir = file.getParentFile();
            fieldFilename.setText(file.getAbsolutePath());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tiledImageViewer2 = new org.pepsoft.util.swing.TiledImageViewer();
        themeButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        fieldFilename = new javax.swing.JTextField();
        buttonSelectFile = new javax.swing.JButton();
        labelImageDimensions = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOk = new javax.swing.JButton();
        checkBoxInvert = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        spinnerScale = new javax.swing.JSpinner();
        labelWorldDimensions = new javax.swing.JLabel();
        comboBoxMaxHeight = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        spinnerImageLow = new javax.swing.JSpinner();
        spinnerWorldLow = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        labelImageWaterLevel = new javax.swing.JLabel();
        spinnerWorldMiddle = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        spinnerImageHigh = new javax.swing.JSpinner();
        spinnerWorldHigh = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();
        labelImageLowestLevel = new javax.swing.JLabel();
        labelWorldLowestLevel = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        labelImageHighestLevel = new javax.swing.JLabel();
        labelWorldHighestLevel = new javax.swing.JLabel();
        labelWarningCutOffAbove = new javax.swing.JLabel();
        labelWarningCutOffBelow = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        labelMinHeight = new javax.swing.JLabel();
        labelMaxHeight = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        checkBoxVoid = new javax.swing.JCheckBox();
        spinnerVoidBelow = new javax.swing.JSpinner();
        labelWarning = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        spinnerOffsetX = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        spinnerOffsetY = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        labelWalkingTime = new javax.swing.JLabel();
        radioButtonApplyTheme = new javax.swing.JRadioButton();
        radioButtonSingleTerrain = new javax.swing.JRadioButton();
        comboBoxSingleTerrain = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        comboBoxPreset = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        labelExportedSize = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        labelExportedOffset = new javax.swing.JLabel();
        comboBoxMinHeight = new javax.swing.JComboBox<>();
        jLabel21 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        radioButtonLeaveTerrain = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        themeEditor = new org.pepsoft.worldpainter.themes.impl.simple.SimpleThemeEditor();
        buttonLoadDefaults = new javax.swing.JButton();
        buttonSaveAsDefaults = new javax.swing.JButton();
        buttonResetDefaults = new javax.swing.JButton();
        checkBoxCreateTiles = new javax.swing.JCheckBox();
        labelNoUndo = new javax.swing.JLabel();
        checkBoxOnlyRaise = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        comboBoxPlatform = new javax.swing.JComboBox<>();
        tiledImageViewerContainer1 = new org.pepsoft.util.swing.TiledImageViewerContainer();
        checkBoxMasterDimension = new javax.swing.JCheckBox();
        buttonMasterInfo = new javax.swing.JButton();

        tiledImageViewer2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u5BFC\u5165\u9AD8\u5EA6\u56FE");

        jLabel1.setText("\u9009\u62E9\u8981\u5BFC\u5165\u4E3A\u9AD8\u5EA6\u56FE\u7684\u6587\u4EF6:");

        buttonSelectFile.setText("...");
        buttonSelectFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectFileActionPerformed(evt);
            }
        });

        labelImageDimensions.setText("\u56FE\u50CF\u5927\u5C0F: ? x ?, \u4F4D\u6DF1\u5EA6: ?, \u6700\u4F4E\u503C: ?, \u6700\u9AD8\u503C: ?");

        buttonCancel.setText("\u53D6\u6D88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOk.setText("\u786E\u8BA4");
        buttonOk.setEnabled(false);
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOkActionPerformed(evt);
            }
        });

        checkBoxInvert.setText("\u53CD\u8F6C (\u767D\u8272\u4E3A\u9AD8, \u9ED1\u8272\u4E3A\u4F4E)");
        checkBoxInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxInvertActionPerformed(evt);
            }
        });

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jLabel3.setText("\u7F29\u653E:");

        spinnerScale.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(100.0f), Float.valueOf(0.01f), Float.valueOf(999.99f), Float.valueOf(0.1f)));
        spinnerScale.setEditor(new javax.swing.JSpinner.NumberEditor(spinnerScale, "0.00"));
        spinnerScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerScaleStateChanged(evt);
            }
        });

        labelWorldDimensions.setText("\u7F29\u653E\u540E\u5927\u5C0F: ? x ? \u683C");

        comboBoxMaxHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxMaxHeightActionPerformed(evt);
            }
        });

        jLabel4.setText("%");

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel5.setText("\u7531\u56FE\u50CF\u83B7\u53D6\u7684\u6570\u636E:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel6.setText("\u8F6C\u6362\u5230 Minecraft \u540E:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel7.setText("\u6700\u4F4E\u503C\u6620\u5C04:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel7, gridBagConstraints);

        spinnerImageLow.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 4.294967295E9d, 1.0d));
        spinnerImageLow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerImageLowStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerImageLow, gridBagConstraints);

        spinnerWorldLow.setModel(new javax.swing.SpinnerNumberModel(0, 0, 65535, 1));
        spinnerWorldLow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWorldLowStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerWorldLow, gridBagConstraints);

        jLabel8.setText("\u6C34\u5E73\u9762\u9AD8\u5EA6:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel8, gridBagConstraints);

        labelImageWaterLevel.setText("62");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelImageWaterLevel, gridBagConstraints);

        spinnerWorldMiddle.setModel(new javax.swing.SpinnerNumberModel(62, 0, 65535, 1));
        spinnerWorldMiddle.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWorldMiddleStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerWorldMiddle, gridBagConstraints);

        jLabel9.setText("\u6700\u9AD8\u503C\u6620\u5C04:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel9, gridBagConstraints);

        spinnerImageHigh.setModel(new javax.swing.SpinnerNumberModel(255.0d, 0.0d, 4.294967295E9d, 1.0d));
        spinnerImageHigh.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerImageHighStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerImageHigh, gridBagConstraints);

        spinnerWorldHigh.setModel(new javax.swing.SpinnerNumberModel(255, 0, 65535, 1));
        spinnerWorldHigh.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerWorldHighStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(spinnerWorldHigh, gridBagConstraints);

        jLabel15.setText("\u6700\u4F4E\u503C:");
        jLabel15.setToolTipText("\u9AD8\u5EA6\u56FE\u4E2D\u5B9E\u9645\u51FA\u73B0\u7684\u6700\u4F4E\u503C.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel15, gridBagConstraints);

        labelImageLowestLevel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelImageLowestLevel, gridBagConstraints);

        labelWorldLowestLevel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelWorldLowestLevel, gridBagConstraints);

        jLabel18.setText("\u6700\u9AD8\u503C:");
        jLabel18.setToolTipText("\u9AD8\u5EA6\u56FE\u4E2D\u5B9E\u9645\u51FA\u73B0\u7684\u6700\u9AD8\u503C.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel18, gridBagConstraints);

        labelImageHighestLevel.setText("255");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelImageHighestLevel, gridBagConstraints);

        labelWorldHighestLevel.setText("255");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelWorldHighestLevel, gridBagConstraints);

        labelWarningCutOffAbove.setFont(labelWarningCutOffAbove.getFont().deriveFont(labelWarningCutOffAbove.getFont().getStyle() | java.awt.Font.BOLD));
        labelWarningCutOffAbove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelWarningCutOffAbove.setText("\u66F4\u9AD8\u7684\u90E8\u5206\u5DF2\u88AB\u88C1\u5207!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(labelWarningCutOffAbove, gridBagConstraints);

        labelWarningCutOffBelow.setFont(labelWarningCutOffBelow.getFont().deriveFont(labelWarningCutOffBelow.getFont().getStyle() | java.awt.Font.BOLD));
        labelWarningCutOffBelow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelWarningCutOffBelow.setText("\u66F4\u4F4E\u7684\u90E8\u5206\u5DF2\u88AB\u88C1\u5207!!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(labelWarningCutOffBelow, gridBagConstraints);

        jLabel16.setText("\u5EFA\u7B51\u9650\u5236:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel16, gridBagConstraints);

        jLabel17.setText("\u5EFA\u7B51\u9650\u5236:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel17, gridBagConstraints);

        labelMinHeight.setText("-64");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelMinHeight, gridBagConstraints);

        labelMaxHeight.setText("319");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 19);
        jPanel1.add(labelMaxHeight, gridBagConstraints);

        jLabel2.setText("\u5EFA\u7B51\u9650\u5236: \u6700\u4F4E:");

        jLabel10.setText("\u683C");

        checkBoxVoid.setText("\u4E8E\u5730\u56FE\u9AD8\u5EA6\u4F4E\u4E8E\u8BE5\u503C\u90E8\u5206\u521B\u5EFA\u865A\u7A7A:");
        checkBoxVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxVoidActionPerformed(evt);
            }
        });

        spinnerVoidBelow.setModel(new javax.swing.SpinnerNumberModel(1.0d, 1.0d, 4.294967295E9d, 1.0d));
        spinnerVoidBelow.setEnabled(false);
        spinnerVoidBelow.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerVoidBelowStateChanged(evt);
            }
        });

        labelWarning.setFont(labelWarning.getFont().deriveFont(labelWarning.getFont().getStyle() | java.awt.Font.BOLD));
        labelWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelWarning.setText("\u53EA\u5728\u6709mod\u65F6\u6709\u6548!");

        jLabel11.setText("\u504F\u79FB:");
        jLabel11.setToolTipText("\u9AD8\u5EA6\u56FE\u8D77\u70B9\u4F1A\u4F4D\u4E8E\u5730\u56FE\u7684\u8FD9\u4E9B\u5750\u6807");

        spinnerOffsetX.setModel(new javax.swing.SpinnerNumberModel(0, -999999, 999999, 1));
        spinnerOffsetX.setToolTipText("\u9AD8\u5EA6\u56FE\u8D77\u70B9\u4F1A\u4F4D\u4E8E\u5730\u56FE\u7684\u8FD9\u4E9B\u5750\u6807");
        spinnerOffsetX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerOffsetXStateChanged(evt);
            }
        });

        jLabel12.setText(",");
        jLabel12.setToolTipText("\u9AD8\u5EA6\u56FE\u8D77\u70B9\u4F1A\u4F4D\u4E8E\u5730\u56FE\u7684\u8FD9\u4E9B\u5750\u6807");

        spinnerOffsetY.setModel(new javax.swing.SpinnerNumberModel(0, -999999, 999999, 1));
        spinnerOffsetY.setToolTipText("\u9AD8\u5EA6\u56FE\u8D77\u70B9\u4F1A\u4F4D\u4E8E\u5730\u56FE\u7684\u8FD9\u4E9B\u5750\u6807");
        spinnerOffsetY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerOffsetYStateChanged(evt);
            }
        });

        jLabel14.setText("\u8FB9\u7F18\u81F3\u8FB9\u7F18\u884C\u8D70\u65F6\u95F4:");

        labelWalkingTime.setText("...");

        themeButtonGroup.add(radioButtonApplyTheme);
        radioButtonApplyTheme.setSelected(true);
        radioButtonApplyTheme.setText("\u4E0E\u4E3B\u9898\u76F8\u540C");
        radioButtonApplyTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonApplyThemeActionPerformed(evt);
            }
        });

        themeButtonGroup.add(radioButtonSingleTerrain);
        radioButtonSingleTerrain.setText("\u5355\u4E2A\u65B9\u5757:");
        radioButtonSingleTerrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonSingleTerrainActionPerformed(evt);
            }
        });

        comboBoxSingleTerrain.setEnabled(false);
        comboBoxSingleTerrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxSingleTerrainActionPerformed(evt);
            }
        });

        jLabel19.setText("\u9884\u8BBE:");

        comboBoxPreset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxPresetActionPerformed(evt);
            }
        });

        jLabel20.setText("\u5230\u5904\u5927\u5C0F:");

        labelExportedSize.setText("? x ? \u683C");

        jLabel22.setText("\u5BFC\u51FA\u540E\u504F\u79FB:");

        labelExportedOffset.setText("0, 0");

        comboBoxMinHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxMinHeightActionPerformed(evt);
            }
        });

        jLabel21.setText(", \u6700\u9AD8:");

        jLabel23.setText("\u65B9\u5757\u7C7B\u578B:");

        themeButtonGroup.add(radioButtonLeaveTerrain);
        radioButtonLeaveTerrain.setText("\u7559\u7A7A");
        radioButtonLeaveTerrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonLeaveTerrainActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonApplyTheme)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonSingleTerrain)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxSingleTerrain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonLeaveTerrain))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerOffsetX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerOffsetY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labelWorldDimensions)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxMinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxMaxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(labelWarning))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(checkBoxVoid)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spinnerVoidBelow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelWalkingTime))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxPreset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelExportedSize)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelExportedOffset)))
                .addContainerGap(71, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinnerScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel11)
                    .addComponent(spinnerOffsetX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(spinnerOffsetY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelWorldDimensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(labelExportedSize)
                    .addComponent(jLabel22)
                    .addComponent(labelExportedOffset))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(labelWalkingTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(comboBoxMaxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(labelWarning)
                    .addComponent(comboBoxMinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(comboBoxPreset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxVoid)
                    .addComponent(spinnerVoidBelow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonApplyTheme)
                    .addComponent(radioButtonSingleTerrain)
                    .addComponent(comboBoxSingleTerrain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(radioButtonLeaveTerrain))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("\u7F29\u653E", jPanel2);

        buttonLoadDefaults.setText("\u52A0\u8F7D\u9ED8\u8BA4\u503C");
        buttonLoadDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadDefaultsActionPerformed(evt);
            }
        });

        buttonSaveAsDefaults.setText("\u4FDD\u5B58\u4E3A\u9ED8\u8BA4\u503C");
        buttonSaveAsDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveAsDefaultsActionPerformed(evt);
            }
        });

        buttonResetDefaults.setText("\u91CD\u7F6E");
        buttonResetDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetDefaultsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(themeEditor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonResetDefaults)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSaveAsDefaults)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonLoadDefaults)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(themeEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLoadDefaults)
                    .addComponent(buttonSaveAsDefaults)
                    .addComponent(buttonResetDefaults))
                .addContainerGap())
        );

        jTabbedPane1.addTab("\u4E3B\u9898", jPanel3);

        checkBoxCreateTiles.setText("\u521B\u5EFA\u65B0\u5206\u533A");

        labelNoUndo.setText("<html><b>\u6CE8\u610F:</b> \u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!</html>");

        checkBoxOnlyRaise.setText("\u4EC5\u5E94\u7528\u4E8E\u9AD8\u4E8E\u5F53\u524D\u5730\u5F62\u90E8\u5206");
        checkBoxOnlyRaise.setToolTipText("\u9009\u4E2D\u540E\uFF0C\u9AD8\u5EA6\u56FE\u5C06\u4EC5\u5E94\u7528\u4E8E\u9AD8\u4E8E\u73B0\u6709\u5730\u5F62\u7684\u4F4D\u7F6E");
        checkBoxOnlyRaise.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxOnlyRaiseActionPerformed(evt);
            }
        });

        jLabel13.setText("\u5730\u56FE\u683C\u5F0F:");

        comboBoxPlatform.setRenderer(new PlatformListCellRenderer());
        comboBoxPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxPlatformActionPerformed(evt);
            }
        });

        tiledImageViewerContainer1.setMinimumSize(new java.awt.Dimension(384, 22));

        checkBoxMasterDimension.setText("\u4EE5\u6269\u5C55\u533A\u7EF4\u5EA6\u521B\u5EFA (1:16 \u6BD4\u4F8B)");
        checkBoxMasterDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxMasterDimensionActionPerformed(evt);
            }
        });

        buttonMasterInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/information.png"))); // NOI18N
        buttonMasterInfo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonMasterInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMasterInfoActionPerformed(evt);
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
                        .addComponent(buttonOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(fieldFilename)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectFile))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addComponent(labelImageDimensions)
                            .addComponent(checkBoxInvert)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxCreateTiles)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxOnlyRaise)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelNoUndo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(tiledImageViewerContainer1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkBoxMasterDimension)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonMasterInfo)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fieldFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonSelectFile))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelImageDimensions)
                        .addGap(4, 4, 4)
                        .addComponent(checkBoxInvert)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(comboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxMasterDimension)
                            .addComponent(buttonMasterInfo))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxCreateTiles)
                            .addComponent(labelNoUndo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBoxOnlyRaise))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTabbedPane1))
                    .addComponent(tiledImageViewerContainer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOk))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void spinnerImageLowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerImageLowStateChanged
        final double lowLevel  = (Double) spinnerImageLow.getValue();
        final double highLevel = (Double) spinnerImageHigh.getValue();
        if (lowLevel > highLevel) {
            spinnerImageHigh.setValue(lowLevel);
        }
        resetPreset();
        updateImageLevelLabels();
        updatePreview(false);
        setControlStates();
    }//GEN-LAST:event_spinnerImageLowStateChanged

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void spinnerImageHighStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerImageHighStateChanged
        final double lowLevel  = (Double) spinnerImageLow.getValue();
        final double highLevel = (Double) spinnerImageHigh.getValue();
        if (highLevel < lowLevel) {
            spinnerImageLow.setValue(highLevel);
        }
        resetPreset();
        updateImageLevelLabels();
        updatePreview(false);
        setControlStates();
    }//GEN-LAST:event_spinnerImageHighStateChanged

    private void spinnerWorldLowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWorldLowStateChanged
        int lowLevel    = (Integer) spinnerWorldLow.getValue();
        int highLevel   = (Integer) spinnerWorldHigh.getValue();
        if (lowLevel > highLevel) {
            spinnerWorldHigh.setValue(lowLevel);
        }
        resetPreset();
        updateImageLevelLabels();
        updatePreview(false);
        setControlStates();
    }//GEN-LAST:event_spinnerWorldLowStateChanged

    private void spinnerWorldMiddleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWorldMiddleStateChanged
        updateImageLevelLabels();
        updatePreview(false);
    }//GEN-LAST:event_spinnerWorldMiddleStateChanged

    private void spinnerWorldHighStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerWorldHighStateChanged
        int lowLevel    = (Integer) spinnerWorldLow.getValue();
        int highLevel   = (Integer) spinnerWorldHigh.getValue();
        if (highLevel < lowLevel) {
            spinnerWorldLow.setValue(highLevel);
        }
        resetPreset();
        updateImageLevelLabels();
        updatePreview(false);
        setControlStates();
    }//GEN-LAST:event_spinnerWorldHighStateChanged

    private void buttonSelectFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectFileActionPerformed
        selectFile();
    }//GEN-LAST:event_buttonSelectFileActionPerformed

    private void spinnerScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerScaleStateChanged
        if (image != null) {
            updateWorldDimensions();
            updatePreview(false);
        }
    }//GEN-LAST:event_spinnerScaleStateChanged

    private void buttonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOkActionPerformed
        if (jTabbedPane1.getSelectedIndex() == 0) {
            themeEditor.setWaterHeight((int) spinnerWorldMiddle.getValue());
            if (themeEditor.save()) {
                ok();
            } else {
                jTabbedPane1.setSelectedIndex(1);
            }
        } else if (themeEditor.save()) {
            spinnerWorldMiddle.setValue(themeEditor.getTheme().getWaterHeight());
            ok();
        }
    }//GEN-LAST:event_buttonOkActionPerformed

    private void comboBoxMaxHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxMaxHeightActionPerformed
        maxHeightChanged();
    }//GEN-LAST:event_comboBoxMaxHeightActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        if (!programmaticChange) {
            switch (jTabbedPane1.getSelectedIndex()) {
                case 0:
                    themeEditor.save();
                    spinnerWorldMiddle.setValue(themeEditor.getTheme().getWaterHeight());
                    break;
                case 1:
                    themeEditor.setWaterHeight((int) spinnerWorldMiddle.getValue());
                    break;
            }
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void checkBoxVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxVoidActionPerformed
        spinnerVoidBelow.setEnabled(checkBoxVoid.isSelected());
        updatePreview(false);
    }//GEN-LAST:event_checkBoxVoidActionPerformed

    private void spinnerOffsetXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerOffsetXStateChanged
        updateWorldDimensions();
        if (image != null) {
            updatePreview(false);
        }
    }//GEN-LAST:event_spinnerOffsetXStateChanged

    private void spinnerOffsetYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerOffsetYStateChanged
        updateWorldDimensions();
        if (image != null) {
            updatePreview(false);
        }
    }//GEN-LAST:event_spinnerOffsetYStateChanged

    private void buttonLoadDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadDefaultsActionPerformed
        loadDefaults();
    }//GEN-LAST:event_buttonLoadDefaultsActionPerformed

    private void buttonSaveAsDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveAsDefaultsActionPerformed
        saveAsDefaults();
    }//GEN-LAST:event_buttonSaveAsDefaultsActionPerformed

    private void buttonResetDefaultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetDefaultsActionPerformed
        resetDefaults();
    }//GEN-LAST:event_buttonResetDefaultsActionPerformed

    private void comboBoxPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxPlatformActionPerformed
        platformChanged();
    }//GEN-LAST:event_comboBoxPlatformActionPerformed

    private void checkBoxInvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxInvertActionPerformed
        if (selectedFile != null) {
            loadImage();
        }
    }//GEN-LAST:event_checkBoxInvertActionPerformed

    private void radioButtonApplyThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonApplyThemeActionPerformed
        comboBoxSingleTerrain.setEnabled(false);
        jTabbedPane1.setEnabledAt(1, currentDimension == null);
        updatePreview(false);
    }//GEN-LAST:event_radioButtonApplyThemeActionPerformed

    private void radioButtonSingleTerrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonSingleTerrainActionPerformed
        comboBoxSingleTerrain.setEnabled(true);
        jTabbedPane1.setEnabledAt(1, false);
        updatePreview(false);
    }//GEN-LAST:event_radioButtonSingleTerrainActionPerformed

    private void comboBoxSingleTerrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxSingleTerrainActionPerformed
        updatePreview(false);
    }//GEN-LAST:event_comboBoxSingleTerrainActionPerformed

    private void comboBoxPresetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxPresetActionPerformed
        applyPreset((ImportPreset) comboBoxPreset.getSelectedItem());
    }//GEN-LAST:event_comboBoxPresetActionPerformed

    private void spinnerVoidBelowStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerVoidBelowStateChanged
        updatePreview(false);
    }//GEN-LAST:event_spinnerVoidBelowStateChanged

    private void checkBoxOnlyRaiseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxOnlyRaiseActionPerformed
        updatePreview(false);
    }//GEN-LAST:event_checkBoxOnlyRaiseActionPerformed

    private void checkBoxMasterDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxMasterDimensionActionPerformed
        final boolean masterDimensionSelected = checkBoxMasterDimension.isSelected();
        jLabel20.setVisible(masterDimensionSelected);
        labelExportedSize.setVisible(masterDimensionSelected);
        jLabel22.setVisible(masterDimensionSelected);
        labelExportedOffset.setVisible(masterDimensionSelected);
        updateWorldDimensions();
    }//GEN-LAST:event_checkBoxMasterDimensionActionPerformed

    private void buttonMasterInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMasterInfoActionPerformed
        NewWorldDialog.showMasterDimensionInfo(this);
    }//GEN-LAST:event_buttonMasterInfoActionPerformed

    private void comboBoxMinHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxMinHeightActionPerformed
        minHeightChanged();
    }//GEN-LAST:event_comboBoxMinHeightActionPerformed

    private void radioButtonLeaveTerrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonLeaveTerrainActionPerformed
        comboBoxSingleTerrain.setEnabled(false);
        jTabbedPane1.setEnabledAt(1, false);
        updatePreview(false);
    }//GEN-LAST:event_radioButtonLeaveTerrainActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonLoadDefaults;
    private javax.swing.JButton buttonMasterInfo;
    private javax.swing.JButton buttonOk;
    private javax.swing.JButton buttonResetDefaults;
    private javax.swing.JButton buttonSaveAsDefaults;
    private javax.swing.JButton buttonSelectFile;
    private javax.swing.JCheckBox checkBoxCreateTiles;
    private javax.swing.JCheckBox checkBoxInvert;
    private javax.swing.JCheckBox checkBoxMasterDimension;
    private javax.swing.JCheckBox checkBoxOnlyRaise;
    private javax.swing.JCheckBox checkBoxVoid;
    private javax.swing.JComboBox<Integer> comboBoxMaxHeight;
    private javax.swing.JComboBox<Integer> comboBoxMinHeight;
    private javax.swing.JComboBox<Platform> comboBoxPlatform;
    private javax.swing.JComboBox<ImportPreset> comboBoxPreset;
    private javax.swing.JComboBox<Terrain> comboBoxSingleTerrain;
    private javax.swing.JTextField fieldFilename;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelExportedOffset;
    private javax.swing.JLabel labelExportedSize;
    private javax.swing.JLabel labelImageDimensions;
    private javax.swing.JLabel labelImageHighestLevel;
    private javax.swing.JLabel labelImageLowestLevel;
    private javax.swing.JLabel labelImageWaterLevel;
    private javax.swing.JLabel labelMaxHeight;
    private javax.swing.JLabel labelMinHeight;
    private javax.swing.JLabel labelNoUndo;
    private javax.swing.JLabel labelWalkingTime;
    private javax.swing.JLabel labelWarning;
    private javax.swing.JLabel labelWarningCutOffAbove;
    private javax.swing.JLabel labelWarningCutOffBelow;
    private javax.swing.JLabel labelWorldDimensions;
    private javax.swing.JLabel labelWorldHighestLevel;
    private javax.swing.JLabel labelWorldLowestLevel;
    private javax.swing.JRadioButton radioButtonApplyTheme;
    private javax.swing.JRadioButton radioButtonLeaveTerrain;
    private javax.swing.JRadioButton radioButtonSingleTerrain;
    private javax.swing.JSpinner spinnerImageHigh;
    private javax.swing.JSpinner spinnerImageLow;
    private javax.swing.JSpinner spinnerOffsetX;
    private javax.swing.JSpinner spinnerOffsetY;
    private javax.swing.JSpinner spinnerScale;
    private javax.swing.JSpinner spinnerVoidBelow;
    private javax.swing.JSpinner spinnerWorldHigh;
    private javax.swing.JSpinner spinnerWorldLow;
    private javax.swing.JSpinner spinnerWorldMiddle;
    private javax.swing.ButtonGroup themeButtonGroup;
    private org.pepsoft.worldpainter.themes.impl.simple.SimpleThemeEditor themeEditor;
    private org.pepsoft.util.swing.TiledImageViewer tiledImageViewer2;
    private org.pepsoft.util.swing.TiledImageViewerContainer tiledImageViewerContainer1;
    // End of variables declaration//GEN-END:variables

    private final long seed = new Random().nextLong();
    private final Dimension currentDimension;
    private final ColourScheme colourScheme;
    private final boolean contourLines;
    private final int contourSeparation;
    private final TileRenderer.LightOrigin lightOrigin;
    private final CustomBiomeManager customBiomeManager;
    private File selectedFile, heightMapDir;
    private volatile BufferedImage image;
    private int bitDepth = 8;
    private double imageLowValue, imageHighValue = 255.0, imageMinHeight, imageMaxHeight = 255.0;
    private boolean programmaticChange = true;
    private Platform platform;

    private static final String UPDATE_HEIGHT_MAP_PREVIEW = ImportHeightMapDialog.class.getName() + ".updateHeightMap";
    private static final Icon ICON_WARNING = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/error.png");
    private static final int LAYER_HEIGHT_MAP = 0;
    private static final int LAYER_CURRENT_DIMENSION = -1;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ImportHeightMapDialog.class);
    private static final long serialVersionUID = 1L;
}