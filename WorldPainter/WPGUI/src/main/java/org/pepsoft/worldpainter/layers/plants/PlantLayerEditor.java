/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers.plants;

import org.pepsoft.minecraft.Material;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.exporting.ExportSettings;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.platforms.JavaExportSettings;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.Math.max;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.pepsoft.util.swing.MessageUtils.showInfo;
import static org.pepsoft.worldpainter.Platform.Capability.NAME_BASED;
import static org.pepsoft.worldpainter.layers.plants.Category.*;
import static org.pepsoft.worldpainter.layers.plants.Plants.ALL_PLANTS;
import static org.pepsoft.worldpainter.util.I18nHelper.m;

/**
 *
 * @author Pepijn Schmitz
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class PlantLayerEditor extends AbstractLayerEditor<PlantLayer> {
    /**
     * Creates new form PlantLayerEditor
     */
    public PlantLayerEditor() {
        initComponents();

        fieldName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                settingsChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                settingsChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                settingsChanged();
            }
        });
    }

    // LayerEditor

    @Override
    public List<Component> getAdditionalButtons() {
        return singletonList(buttonClear);
    }

    @Override
    public PlantLayer createLayer() {
        return new PlantLayer("\u690D\u88AB", "\u4E00\u7CFB\u5217\u690D\u7269\u7684\u7EC4\u5408", Color.GREEN);
    }

    @Override
    public void setLayer(PlantLayer layer) {
        super.setLayer(layer);
        reset();
    }

    @Override
    public void setContext(LayerEditorContext context) {
        super.setContext(context);
        initPlantControls();
    }

    @Override
    public void commit() {
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        saveSettings(layer);
    }

    @Override
    public void reset() {
        fieldName.setText(layer.getName());
        paintPicker1.setPaint(layer.getPaint());
        paintPicker1.setOpacity(layer.getOpacity());
        checkBoxGenerateTilledDirt.setSelected(layer.isGenerateFarmland());
        checkBoxOnlyValidBlocks.setSelected(layer.isOnlyOnValidBlocks());
        final Platform platform = context.getDimension().getWorld().getPlatform();
        for (int i = 0; i < ALL_PLANTS.length; i++) {
            PlantLayer.PlantSettings settings = layer.getSettings(i);
            if (! isCompatibleWithPlatform(ALL_PLANTS[i], platform)) {
                // Force incompatible plants to zero, since the user can't edit them to do that
                spinners[i].setValue(0);
                if ((settings != null) && (settings.occurrence > 0)) {
                    // The plant was previously selected, so make that clear by making the label struck through
                    // instead of disabled
                    plantLabels[i].setEnabled(true);
                    plantLabels[i].setText("<html><s>" + ALL_PLANTS[i].getName() + "</s></html>");
                } else {
                    plantLabels[i].setEnabled(false);
                    plantLabels[i].setText(ALL_PLANTS[i].getName());
                }
                plantLabels[i].setToolTipText("\u8BE5\u690D\u7269\u4E0E\u5F53\u524D\u7684\u5730\u56FE\u683C\u5F0F\u4E0D\u517C\u5BB9");
            } else if (settings != null) {
                spinners[i].setValue((int) settings.occurrence);
                plantLabels[i].setEnabled(true);
                plantLabels[i].setText(ALL_PLANTS[i].getName());
                plantLabels[i].setToolTipText(null);
                if (growthFromSpinners[i] != null) {
                    growthFromSpinners[i].setValue(settings.growthFrom);
                    growthToSpinners[i].setValue(settings.growthTo);
                }
            } else {
                spinners[i].setValue(0);
                if (growthFromSpinners[i] != null) {
                    growthFromSpinners[i].setValue(max(ALL_PLANTS[i].getDefaultGrowth() / 2, 1));
                    growthToSpinners[i].setValue(ALL_PLANTS[i].getDefaultGrowth());
                }
                plantLabels[i].setEnabled(true);
                plantLabels[i].setText(ALL_PLANTS[i].getName());
                plantLabels[i].setToolTipText(null);
            }
        }
        updatePercentages();
        setControlStates();
    }

    @Override
    public ExporterSettings getSettings() {
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        final PlantLayer previewLayer = saveSettings(null);
        return new ExporterSettings() {
            @Override
            public boolean isApplyEverywhere() {
                return false;
            }

            @Override
            public PlantLayer getLayer() {
                return previewLayer;
            }

            @Override
            public ExporterSettings clone() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }

    @Override
    public boolean isCommitAvailable() {
        return (! fieldName.getText().trim().isEmpty()) && (totalOccurrence > 0L) && (totalOccurrence <= Integer.MAX_VALUE);
    }

    private void initPlantControls() {
        JPanel panel = new JPanel(new GridBagLayout());
        panelPlantControls.add(panel);
        addCategory(panel, PLANTS_AND_FLOWERS);
        addFiller(panel);
        panel = new JPanel(new GridBagLayout());
        panelPlantControls.add(panel);
        addCategory(panel, SAPLINGS);
        addCategory(panel, CROPS);
        addCategory(panel, "\u5404\u79CD\u690D\u7269", MUSHROOMS, CACTUS, SUGAR_CANE, FLOATING_PLANTS, END);
        addFiller(panel);
        panel = new JPanel(new GridBagLayout());
        panelPlantControls.add(panel);
        addCategory(panel, "\u6C34\u7CFB\u690D\u7269", WATER_PLANTS, DRIPLEAF);
        addCategory(panel, NETHER);
        addCategory(panel, "\u60AC\u6302\u690D\u7269", "\u7528\u4E8E\u9876\u5C42\u4E16\u754C\u6216\u6D1E\u7A74/\u901A\u9053\u4E16\u754C\u7684\u9876\u5C42", HANGING_DRY_PLANTS, HANGING_WATER_PLANTS);
        addFiller(panel);
        panelPlantControls.setPreferredSize(panelPlantControls.getMinimumSize());
    }

    private void addCategory(JPanel panel, Category category) {
        addCategory(panel, m(category), category);
    }

    private void addCategory(JPanel panel, String title, Category... categories) {
        addCategory(panel, title, null, categories);
    }

    private void addCategory(JPanel panel, String title, String subTitle, Category... categories) {
        final boolean newColumn = panel.getComponentCount() == 0;
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = newColumn ? 3 : GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.insets = new Insets(4, 0, 4, 0);
        panel.add(new JLabel("<html><b>" + title + "</b></html>"), constraints);
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        if (newColumn) {
            panel.add(new JLabel("\u751F\u957F\u7A0B\u5EA6:"), constraints);
        }
        if (subTitle != null) {
            panel.add(new JLabel(subTitle), constraints);
        }
        final Platform platform = context.getDimension().getWorld().getPlatform();
        for (Category category: categories) {
            for (int i = 0; i < ALL_PLANTS.length; i++) {
                final Plant plant = ALL_PLANTS[i];
                if (plant.getCategories()[0] == category) {
                    addPlantRow(panel, plant, i, isCompatibleWithPlatform(plant, platform));
                }
            }
        }
    }

    private boolean isCompatibleWithPlatform(Plant plant, Platform platform) {
        if (! platform.capabilities.contains(NAME_BASED)) {
            for (Material material: plant.getAllMaterials()) {
                if (material.blockType == -1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addPlantRow(final JPanel panel, final Plant plant, final int index, final boolean enabled) {
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.insets = new Insets(1, 0, 1, 4);
        synchronized (icons) {
            final BufferedImage icon = icons.get(plant.getIconName());
            if (icon != null) {
                plantLabels[index] = new JLabel(plant.getName(), new ImageIcon(icon), JLabel.TRAILING);
            } else {
                plantLabels[index] = new JLabel(plant.getName());
            }
        }
        panel.add(plantLabels[index], constraints);

        SpinnerModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
        spinners[index] = new JSpinner(spinnerModel);
        ((JSpinner.NumberEditor) spinners[index].getEditor()).getTextField().setColumns(3);
        spinners[index].addChangeListener(percentageListener);
        panel.add(spinners[index], constraints);

        percentageLabels[index] = new JLabel("100%");
        panel.add(percentageLabels[index], constraints);

        if (! enabled) {
            spinners[index].setEnabled(false);
            spinners[index].setToolTipText("\u8BE5\u690D\u7269\u4E0E\u5F53\u524D\u7684\u5730\u56FE\u683C\u5F0F\u4E0D\u517C\u5BB9");
        }

        if (plant.getMaxGrowth() > 1) {
            spinnerModel = new SpinnerNumberModel(max(plant.getDefaultGrowth() / 2, 1), 1, plant.getMaxGrowth(), 1);
            growthFromSpinners[index] = new JSpinner(spinnerModel);
            growthFromSpinners[index].addChangeListener(e -> {
                int newValue = (Integer) growthFromSpinners[index].getValue();
                if ((Integer) growthToSpinners[index].getValue() < newValue) {
                    growthToSpinners[index].setValue(newValue);
                }
                settingsChanged();
            });
            panel.add(growthFromSpinners[index], constraints);

            final JLabel dashLabel = new JLabel("-");
            panel.add(dashLabel);

            constraints.gridwidth = GridBagConstraints.REMAINDER;
            spinnerModel = new SpinnerNumberModel(plant.getDefaultGrowth(), 1, plant.getMaxGrowth(), 1);
            growthToSpinners[index] = new JSpinner(spinnerModel);
            growthToSpinners[index].addChangeListener(e -> {
                final int newValue = (Integer) growthToSpinners[index].getValue();
                if ((Integer) growthFromSpinners[index].getValue() > newValue) {
                    growthFromSpinners[index].setValue(newValue);
                }
                settingsChanged();
            });
            panel.add(growthToSpinners[index], constraints);

            if (! enabled) {
                growthFromSpinners[index].setEnabled(false);
                dashLabel.setEnabled(false);
                growthToSpinners[index].setEnabled(false);
            }
        } else {
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(new JLabel(), constraints);
        }
    }

    private void addFiller(final JPanel panel) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 1.0;
        panel.add(Box.createGlue(), constraints);
    }

    public static void loadIconsInBackground() {
        new Thread("Plant Icon Loader") {
            @Override
            public void run() {
                synchronized (icons) {
                    File resourcesJar = BiomeSchemeManager.getLatestMinecraftJar();
                    if (resourcesJar == null) {
                        logger.warn("Could not find Minecraft jar for loading plant icons");
                        return;
                    } else {
                        logger.info("Loading plant icons from {}", resourcesJar);
                    }
                    try (JarFile jarFile = new JarFile(resourcesJar)) {
                        for (Plant plant: ALL_PLANTS) {
                            icons.put(plant.getIconName(), findIcon(jarFile, plant.getIconName()));
                        }
                    } catch (IOException e) {
                        logger.error("I/O error while trying to load plant icons; not loading icons", e);
                    }
                }
            }
        }.start();
    }

    private static BufferedImage findIcon(JarFile jarFile, String name) {
        try {
            JarEntry entry = jarFile.getJarEntry("assets/minecraft/textures/" + name);
            if (entry != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Loading plant icon " + name + " from " + jarFile.getName());
                }
                try (InputStream in = jarFile.getInputStream(entry)) {
                    BufferedImage icon = ImageIO.read(in);
                    if (icon.getHeight() > icon.getWidth()) {
                        // Assume this is an animation strip; take the top square of it
                        icon = icon.getSubimage(0, 0, icon.getWidth(), icon.getWidth());
                    }
                    return icon;
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not find plant icon " + name + " in Minecraft jar " + jarFile.getName());
                }
                return null;
            }
        } catch (IOException e) {
            logger.error("I/O error while trying to load plant icon " + name + "; continuing without icon", e);
            return null;
        }
    }

    private void updatePercentages() {
        totalOccurrence = 0;
        for (JSpinner spinner: spinners) {
            totalOccurrence += (Integer) spinner.getValue();
        }
        if (normalFont == null) {
            normalFont = plantLabels[0].getFont().deriveFont(Font.PLAIN);
            boldFont = normalFont.deriveFont(Font.BOLD);
        }
        cropsSelected = false;
        for (int i = 0; i < spinners.length; i++) {
            int value = (Integer) spinners[i].getValue();
            if ((value == 0) && percentageLabels[i].isEnabled()) {
                percentageLabels[i].setEnabled(false);
                plantLabels[i].setFont(normalFont);
                percentageLabels[i].setText("\u2007\u2007\u2007%");
                if ((growthFromSpinners[i] != null) && growthFromSpinners[i].isEnabled()) {
                    growthFromSpinners[i].setEnabled(false);
                    growthToSpinners[i].setEnabled(false);
                }
            } else if (value > 0) {
                if (! percentageLabels[i].isEnabled()) {
                    percentageLabels[i].setEnabled(true);
                    plantLabels[i].setFont(boldFont);
                }
                if (asList(ALL_PLANTS[i].getCategories()).contains(CROPS)) {
                    cropsSelected = true;
                }
                int percentage = (int) (value * 100 / totalOccurrence);
                if (percentage < 10) {
                    percentageLabels[i].setText("\u2007\u2007" + percentage + "%");
                } else if (percentage < 100) {
                    percentageLabels[i].setText("\u2007" + percentage + "%");
                } else {
                    percentageLabels[i].setText(percentage + "%");
                }
                if ((growthFromSpinners[i] != null) && (! growthFromSpinners[i].isEnabled())) {
                    growthFromSpinners[i].setEnabled(true);
                    growthToSpinners[i].setEnabled(true);
                }
            }
        }
        settingsChanged();
    }

    private void settingsChanged() {
        setControlStates();
        context.settingsChanged();
    }

    private void setControlStates() {
        checkBoxGenerateTilledDirt.setEnabled(cropsSelected);
    }

    private PlantLayer saveSettings(PlantLayer layer) {
        if (layer == null) {
            layer = createLayer();
        }
        layer.setName(fieldName.getText().trim());
        layer.setPaint(paintPicker1.getPaint());
        layer.setOpacity(paintPicker1.getOpacity());
        layer.setGenerateFarmland(checkBoxGenerateTilledDirt.isSelected());
        layer.setOnlyOnValidBlocks(checkBoxOnlyValidBlocks.isSelected());
        for (int i = 0; i < ALL_PLANTS.length; i++) {
            PlantLayer.PlantSettings settings = new PlantLayer.PlantSettings();
            settings.occurrence = (short) ((int) ((Integer) spinners[i].getValue()));
            if (growthFromSpinners[i] != null) {
                settings.growthFrom = (Integer) growthFromSpinners[i].getValue();
                settings.growthTo = (Integer) growthToSpinners[i].getValue();
            } else {
                settings.growthFrom = 1;
                settings.growthTo = 1;
            }
            layer.setSettings(i, settings);
        }
        return layer;
    }

    private void clear() {
        for (int i = 0; i < ALL_PLANTS.length; i++) {
            spinners[i].setValue(0);
            if (growthFromSpinners[i] != null) {
                growthFromSpinners[i].setValue(max(ALL_PLANTS[i].getDefaultGrowth() / 2, 1));
                growthToSpinners[i].setValue(ALL_PLANTS[i].getDefaultGrowth());
            }
        }
        updatePercentages();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonClear = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        panelPlantControls = new javax.swing.JPanel();
        checkBoxGenerateTilledDirt = new javax.swing.JCheckBox();
        fieldName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        checkBoxOnlyValidBlocks = new javax.swing.JCheckBox();
        paintPicker1 = new org.pepsoft.worldpainter.layers.renderers.PaintPicker();

        buttonClear.setText("\u6E05\u9664");
        buttonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });

        jLabel1.setText("\u540D\u79F0:");

        panelPlantControls.setLayout(new javax.swing.BoxLayout(panelPlantControls, javax.swing.BoxLayout.LINE_AXIS));

        checkBoxGenerateTilledDirt.setSelected(true);
        checkBoxGenerateTilledDirt.setText("\u5C06\u519C\u4F5C\u7269\u4E0B\u7684\u6CE5\u571F\u6216\u8349\u65B9\u5757\u66FF\u6362\u4E3A\u8015\u5730");
        checkBoxGenerateTilledDirt.setEnabled(false);

        fieldName.setColumns(20);
        fieldName.setText("jTextField1");

        jLabel2.setText("\u753B\u7B14:");

        checkBoxOnlyValidBlocks.setSelected(true);
        checkBoxOnlyValidBlocks.setText("\u4EC5\u653E\u7F6E\u4E8E\u6709\u6548\u65B9\u5757\u4E0A");
        checkBoxOnlyValidBlocks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxOnlyValidBlocksActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPlantControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkBoxGenerateTilledDirt)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxOnlyValidBlocks))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxGenerateTilledDirt)
                    .addComponent(checkBoxOnlyValidBlocks))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPlantControls, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
        clear();
    }//GEN-LAST:event_buttonClearActionPerformed

    private void checkBoxOnlyValidBlocksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxOnlyValidBlocksActionPerformed
        if (! checkBoxOnlyValidBlocks.isSelected()) {
            final ExportSettings exportSettings = context.getDimension().getExportSettings();
            if ((exportSettings == null) || ((exportSettings instanceof JavaExportSettings) && ((JavaExportSettings) exportSettings).isRemovePlants())) {
                DesktopUtils.beep();
                showInfo(SwingUtilities.windowForComponent(this), "\u4F60\u8FD8\u5FC5\u987B\u5728\u5BFC\u51FA\u754C\u9762\u7684\u540E\u5904\u7406\u6807\u7B7E\u4E2D\u5173\u95ED\u201C\u690D\u7269\uFF1A\u4ECE\u65E0\u6548\u65B9\u5757\u4E2D\u79FB\u9664\u201D\u9009\u9879\uFF01\n\u5426\u5219\uFF0C\u540E\u5904\u7406\u8FC7\u7A0B\u4E2D\u65E0\u6548\u65B9\u5757\u4E0A\u7684\u690D\u7269\u5C06\u88AB\u79FB\u9664\u3002",
                        "\u63D0\u9192\uFF1A\u5173\u95ED\u79FB\u9664\u690D\u7269\u9009\u9879");
            }
        }
    }//GEN-LAST:event_checkBoxOnlyValidBlocksActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClear;
    private javax.swing.JCheckBox checkBoxGenerateTilledDirt;
    private javax.swing.JCheckBox checkBoxOnlyValidBlocks;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private org.pepsoft.worldpainter.layers.renderers.PaintPicker paintPicker1;
    private javax.swing.JPanel panelPlantControls;
    // End of variables declaration//GEN-END:variables

    private final JSpinner[] spinners = new JSpinner[ALL_PLANTS.length];
    private final JLabel[] plantLabels = new JLabel[ALL_PLANTS.length], percentageLabels = new JLabel[ALL_PLANTS.length];
    private final JSpinner[] growthFromSpinners = new JSpinner[ALL_PLANTS.length], growthToSpinners = new JSpinner[ALL_PLANTS.length];
    private long totalOccurrence;
    private boolean cropsSelected, initialised;
    private Font normalFont, boldFont;

    private final ChangeListener percentageListener = e -> updatePercentages();

    private static final Map<String, BufferedImage> icons = new HashMap<>();
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PlantLayerEditor.class);
}