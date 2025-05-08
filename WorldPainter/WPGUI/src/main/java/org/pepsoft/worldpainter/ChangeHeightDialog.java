/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChangeHeightDialog.java
 *
 * Created on 30-jan-2012, 17:54:20
 */
package org.pepsoft.worldpainter;

import org.pepsoft.minecraft.SuperflatGenerator;
import org.pepsoft.minecraft.SuperflatPreset;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.util.WorldUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.util.swing.ProgressDialog.NOT_CANCELABLE;
import static org.pepsoft.worldpainter.Constants.V_1_17;
import static org.pepsoft.worldpainter.DefaultPlugin.*;
import static org.pepsoft.worldpainter.Dimension.Role.DETAIL;
import static org.pepsoft.worldpainter.Dimension.Role.MASTER;
import static org.pepsoft.worldpainter.history.HistoryEntry.*;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef", "unused", "FieldCanBeLocal"})
public class ChangeHeightDialog extends WorldPainterDialog {
    /** Creates new form ChangeHeightDialog */
    @SuppressWarnings("OptionalGetWithoutIsPresent") // Expected
    public ChangeHeightDialog(Window parent, World2 world) {
        super(parent);
        this.world = world;
        final Set<Dimension> dimensions = world.getDimensions().stream()
                .filter(dimension -> (dimension.getAnchor().role == DETAIL || dimension.getAnchor().role == MASTER) && (! dimension.getAnchor().invert))
                .collect(toSet());
        lowestHeight = dimensions.stream().mapToInt(Dimension::getLowestIntHeight).min().getAsInt();
        highestHeight = dimensions.stream().mapToInt(Dimension::getHighestIntHeight).max().getAsInt();

        initComponents();
        labelOldExtents.setText(lowestHeight + " - " + highestHeight);
        supportedPlatforms.addAll(PlatformManager.getInstance().getAllPlatforms());
        final List<Platform> allPlatforms = new ArrayList<>(supportedPlatforms);
        final Platform platform = world.getPlatform();
        if (! allPlatforms.contains(platform)) {
            allPlatforms.add(0, platform);
        }
        comboBoxPlatform.setModel(new DefaultComboBoxModel<>(allPlatforms.toArray(new Platform[allPlatforms.size()])));
        comboBoxPlatform.setSelectedItem(platform);
        setPlatform(platform);

        final int minHeight = world.getMinHeight(), maxHeight = world.getMaxHeight();
        labelCurrentMinHeight.setText(Integer.toString(minHeight));
        comboBoxNewMinHeight.setSelectedItem(minHeight);
        labelCurrentMaxHeight.setText(Integer.toString(maxHeight));
        comboBoxNewMaxHeight.setSelectedItem(maxHeight);

        getRootPane().setDefaultButton(buttonOK);

        initialising = false;
        updateLabels();
        setControlStates();

        scaleToUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void setPlatform(Platform platform) {
        comboBoxNewMinHeight.setModel(new DefaultComboBoxModel<>(stream(platform.minHeights).boxed().toArray(Integer[]::new)));
        comboBoxNewMinHeight.setEnabled(platform.minHeights.length > 1);
        final int desiredMinHeight = (platform.minZ < world.getMinHeight()) ? platform.minZ : world.getMinHeight();
        int matchingMinHeight = Integer.MIN_VALUE;
        for (int minHeight : platform.minHeights) {
            if (minHeight <= desiredMinHeight) {
                matchingMinHeight = minHeight;
                break;
            }
        }
        if (matchingMinHeight == Integer.MIN_VALUE) {
            matchingMinHeight = platform.minMinHeight;
        }
        comboBoxNewMinHeight.setSelectedItem(matchingMinHeight);
        comboBoxNewMaxHeight.setModel(new DefaultComboBoxModel<>(stream(platform.maxHeights).boxed().toArray(Integer[]::new)));
        comboBoxNewMaxHeight.setEnabled(platform.maxHeights.length > 1);
        final int desiredMaxHeight = (platform.standardMaxHeight > world.getMaxHeight()) ? platform.standardMaxHeight : world.getMaxHeight();
        int matchingMaxHeight = Integer.MIN_VALUE;
        for (int maxHeight : platform.maxHeights) {
            if (maxHeight >= desiredMaxHeight) {
                matchingMaxHeight = maxHeight;
                break;
            }
        }
        if (matchingMaxHeight == Integer.MIN_VALUE) {
            matchingMaxHeight = platform.maxMaxHeight;
        }
        comboBoxNewMaxHeight.setSelectedItem(matchingMaxHeight);
        updateLabels();
        pack();
        setControlStates();
    }

    private void updateLabels() {
        final HeightTransform transform = getTransform();
        final int newLowestHeight = transform.transformHeight(lowestHeight), newHighestHeight = transform.transformHeight(highestHeight);
        final int newMinHeight = (int) comboBoxNewMinHeight.getSelectedItem(), newMaxHeight = (int) comboBoxNewMaxHeight.getSelectedItem();
        boolean activateWarning = false;
        final StringBuilder label = new StringBuilder("<html>");
        if (newLowestHeight < newMinHeight) {
            label.append("<b><color=red>" + newLowestHeight + "</color></b>");
            activateWarning = true;
        } else {
            label.append(newLowestHeight);
        }
        label.append(" - ");
        if (newHighestHeight > newMaxHeight) {
            label.append("<b><color=red>" + newHighestHeight + "</color></b>");
            activateWarning = true;
        } else {
            label.append(newHighestHeight);
        }
        label.append("</html>");
        labelNewExtents.setText(label.toString());
        labelCutOffWarning.setVisible(activateWarning);
        labelPlatformWarning.setVisible(! supportedPlatforms.contains(comboBoxPlatform.getSelectedItem()));
    }

    private void setControlStates() {
        final Platform oldPlatform = world.getPlatform(), newPlatform = (Platform) comboBoxPlatform.getSelectedItem();
        final int oldMinHeight = world.getMinHeight(), newMinHeight = (Integer) comboBoxNewMinHeight.getSelectedItem();
        final int oldMaxHeight = world.getMaxHeight(), newMaxHeight = (Integer) comboBoxNewMaxHeight.getSelectedItem();
        final boolean translate = checkBoxTranslate.isSelected(), scale = checkBoxScale.isSelected();
        buttonOK.setEnabled((oldPlatform != newPlatform) || (oldMinHeight != newMinHeight) || (oldMaxHeight != newMaxHeight) || (translate && ((Integer) spinnerTranslateAmount.getValue() != 0)) || (scale && ((Integer) spinnerScaleAmount.getValue() != 100)));
        spinnerTranslateAmount.setEnabled(translate);
        spinnerScaleAmount.setEnabled(scale);
        if ((newPlatform == DefaultPlugin.JAVA_MCREGION) && (newMaxHeight != DEFAULT_MAX_HEIGHT_MCREGION)) {
            labelWarning.setText("\u53EA\u6709\u5728\u6709mod\u7684\u60C5\u51B5\u4E0B\u53EF\u884C!");
            labelWarning.setVisible(true);
        } else if (((newMinHeight < oldMinHeight) || (newMaxHeight > oldMaxHeight)) && (newPlatform.getAttribute(ATTRIBUTE_MC_VERSION).isAtLeast(V_1_17)) && ((newMaxHeight - newMinHeight) > 384)) {
            labelWarning.setText("\u53EF\u80FD\u5F71\u54CD\u6027\u80FD");
            labelWarning.setVisible(true);
        } else {
            labelWarning.setVisible(false);
        }
        checkBoxAdjustLayers.setEnabled((newMinHeight != oldMinHeight) || (newMaxHeight != oldMaxHeight) || translate || scale);
        pack();
    }

    private void doResize() {
        // TODO warn about platform incompatibility?
        final Platform oldPlatform = world.getPlatform(), newPlatform = (Platform) comboBoxPlatform.getSelectedItem();
        final int oldMaxHeight = world.getMaxHeight(), oldMinHeight = world.getMinHeight();
        final int newMaxHeight = (Integer) comboBoxNewMaxHeight.getSelectedItem(), newMinHeight = (Integer) comboBoxNewMinHeight.getSelectedItem();
        if (((newPlatform != oldPlatform) || (newMinHeight != oldMinHeight) || (newMaxHeight != oldMaxHeight)) && (world.getImportedFrom() != null) && (JOptionPane.showConfirmDialog(this, "<html>\u8BE5\u4E16\u754C\u5BFC\u5165\u4E8E\u4E00\u4E2A\u5DF2\u5B58\u5728\u7684\u4E16\u754C!<br>\u4F60<i>\u786E\u5B9A</i>\u8981\u91CD\u5B9A\u5411\u5B83\u5417?<br>\u4F60\u5C06\u65E0\u6CD5\u518D\u5C06\u5176\u5408\u5E76\u56DE\u73B0\u6709\u5730\u56FE!</html>", "\u5BFC\u5165\u4E8E\u5DF2\u6709\u5730\u56FE", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)) {
            return;
        }
        changePlatform(newPlatform, checkBoxAdjustLayers.isSelected());
        ProgressDialog.executeTask(this, new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "\u4FEE\u6539\u4E16\u754C\u9AD8\u5EA6";
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                WorldUtils.resizeWorld(world, getTransform(), newMinHeight, newMaxHeight, checkBoxAdjustLayers.isSelected(), progressReceiver);
                return null;
            }
        }, NOT_CANCELABLE);
        if (newMinHeight != oldMinHeight) {
            world.addHistoryEntry(WORLD_MIN_HEIGHT_CHANGED, newMinHeight);
        }
        if (newMaxHeight != oldMaxHeight) {
            world.addHistoryEntry(WORLD_MAX_HEIGHT_CHANGED, newMaxHeight);
        }
        if (checkBoxTranslate.isSelected()) {
            for (Dimension dimension: world.getDimensions()) {
                world.addHistoryEntry(WORLD_DIMENSION_SHIFTED_VERTICALLY, dimension.getName(), (Integer) spinnerTranslateAmount.getValue());
            }
        }
    }

    private void changePlatform(Platform newPlatform, boolean transformLayers) {
        final Platform oldPlatform = world.getPlatform();
        if (newPlatform != oldPlatform) {
            world.setPlatform(newPlatform);
            if (transformLayers) {
                for (Dimension dim: world.getDimensions()) {
                    if (dim.getGenerator() instanceof SuperflatGenerator) {
                        // Patch some block names TODO are there more (that are commonly used in Superflat presets)?
                        final SuperflatPreset settings = ((SuperflatGenerator) dim.getGenerator()).getSettings();
                        // No idea how this could be null, but it has been observed in the wild:
                        if (settings != null) {
                            if (((oldPlatform == JAVA_MCREGION) || (oldPlatform == JAVA_ANVIL)) && (newPlatform != JAVA_MCREGION) && (newPlatform != JAVA_ANVIL)) {
                                for (SuperflatPreset.Layer layer: settings.getLayers()) {
                                    switch (layer.getMaterialName()) {
                                        case "minecraft:grass":
                                            layer.setMaterialName(MC_GRASS_BLOCK);
                                            break;
                                        case "minecraft:snow_layer":
                                            layer.setMaterialName(MC_SNOW);
                                            break;
                                    }
                                }
                            } else if ((oldPlatform != JAVA_MCREGION) && (oldPlatform != JAVA_ANVIL) && ((newPlatform == JAVA_MCREGION) || (newPlatform == JAVA_ANVIL))) {
                                for (SuperflatPreset.Layer layer: settings.getLayers()) {
                                    switch (layer.getMaterialName()) {
                                        case MC_GRASS_BLOCK:
                                            layer.setMaterialName("minecraft:grass");
                                            break;
                                        case MC_SNOW:
                                            layer.setMaterialName("minecraft:snow_layer");
                                            break;
                                    }
                                }

                            }
                        }
                    }
                }
            }
            world.addHistoryEntry(WORLD_RETARGETED, oldPlatform.displayName, newPlatform.displayName);
        }
    }

    private HeightTransform getTransform() {
        boolean scale = checkBoxScale.isSelected();
        int scaleAmount = (Integer) spinnerScaleAmount.getValue();
        boolean translate = checkBoxTranslate.isSelected();
        int translateAmount = (Integer) spinnerTranslateAmount.getValue();
        return HeightTransform.get(scale ? scaleAmount : 100, translate ? translateAmount : 0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        labelCurrentMaxHeight = new javax.swing.JLabel();
        comboBoxNewMaxHeight = new javax.swing.JComboBox<>();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        spinnerTranslateAmount = new javax.swing.JSpinner();
        label = new javax.swing.JLabel();
        spinnerScaleAmount = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        checkBoxScale = new javax.swing.JCheckBox();
        checkBoxTranslate = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        labelWarning = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        comboBoxPlatform = new javax.swing.JComboBox<>();
        labelCurrentMinHeight = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        comboBoxNewMinHeight = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        checkBoxAdjustLayers = new javax.swing.JCheckBox();
        labelCutOffWarning = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        labelOldExtents = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        labelNewExtents = new javax.swing.JLabel();
        labelPlatformWarning = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u66F4\u6539\u5730\u56FE\u683C\u5F0F");

        jLabel1.setText("\u5F53\u524D\u5EFA\u7B51\u4E0A\u9650:");

        jLabel2.setText("\u65B0\u5EFA\u7B51\u4E0A\u9650:");

        labelCurrentMaxHeight.setText("jLabel3");

        comboBoxNewMaxHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxNewMaxHeightActionPerformed(evt);
            }
        });

        buttonCancel.setText("\u53D6\u6D88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("\u786E\u8BA4");
        buttonOK.setEnabled(false);
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        jLabel5.setText("\u65B9\u5757\u548C\u6C34\u5E73\u9762\u9AD8\u5EA6:");

        spinnerTranslateAmount.setModel(new javax.swing.SpinnerNumberModel(0, -127, 127, 1));
        spinnerTranslateAmount.setEnabled(false);
        spinnerTranslateAmount.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerTranslateAmountStateChanged(evt);
            }
        });

        label.setText("\u683C");

        spinnerScaleAmount.setModel(new javax.swing.SpinnerNumberModel(100, 1, 9999, 1));
        spinnerScaleAmount.setEnabled(false);
        spinnerScaleAmount.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerScaleAmountStateChanged(evt);
            }
        });

        jLabel7.setText("%");

        checkBoxScale.setText("\u7F29\u653E");
        checkBoxScale.setToolTipText("<html>\u6309\u7279\u5B9A\u767E\u5206\u6BD4\u7F29\u653E\u9AD8\u5EA6;<br>n\u8FC7\u9AD8\u6216\u8FC7\u4F4E\u7684\u9AD8\u5EA6\u4F1A\u88AB\u524A\u51CF.</html>");
        checkBoxScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxScaleStateChanged(evt);
            }
        });
        checkBoxScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxScaleActionPerformed(evt);
            }
        });

        checkBoxTranslate.setText("\u79FB\u52A8");
        checkBoxTranslate.setToolTipText("<html>\u5C06\u9AD8\u5EA6\u4E0A\u4E0B\u79FB\u52A8;<br>n\u8D1F\u6570\u5373\u4E3A\u5411\u4E0B; \u8FC7\u9AD8\u6216\u8FC7\u4F4E\u7684\u90E8\u5206\u4F1A\u88AB\u88C1\u5207.</html>");
        checkBoxTranslate.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkBoxTranslateStateChanged(evt);
            }
        });
        checkBoxTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxTranslateActionPerformed(evt);
            }
        });

        jLabel6.setText("<html><b>\u6CE8\u610F:</b> \u8BE5\u64CD\u4F5C\u65E0\u6CD5\u64A4\u9500!</html>");

        jLabel8.setText("(\u5982\u679C\u4E24\u8005\u90FD\u5F00\u542F,\u7F29\u653E");

        labelWarning.setFont(labelWarning.getFont().deriveFont(labelWarning.getFont().getStyle() | java.awt.Font.BOLD));
        labelWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelWarning.setText("\u53EF\u80FD\u5F71\u54CD\u6027\u80FD");

        jLabel3.setText("\u5730\u56FE\u683C\u5F0F:");

        comboBoxPlatform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxPlatformActionPerformed(evt);
            }
        });

        labelCurrentMinHeight.setText("jLabel4");

        jLabel9.setText("\u9AD8\u5EA6\u4E0B\u9650");

        comboBoxNewMinHeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxNewMinHeightActionPerformed(evt);
            }
        });

        jLabel10.setText("\u9AD8\u5EA6\u4E0A\u9650");

        checkBoxAdjustLayers.setSelected(true);
        checkBoxAdjustLayers.setText("\u540C\u65F6\u5E94\u7528\u4E3B\u9898\u548C\u8986\u76D6\u5C42\u8BBE\u7F6E");

        labelCutOffWarning.setFont(labelCutOffWarning.getFont().deriveFont(labelCutOffWarning.getFont().getStyle() | java.awt.Font.BOLD));
        labelCutOffWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelCutOffWarning.setText("\u8D85\u51FA\u9650\u5236\u7684\u90E8\u5206\u5DF2\u88AB\u88C1\u5207!");

        jLabel11.setText("\u4F1A\u5148\u5E94\u7528\uFF0C\u7136\u540E\u518D\u79FB\u52A8.)");

        jLabel4.setText("\u5F53\u524D\u4E16\u754C\u9AD8\u5EA6\u4E0A\u4E0B\u9650:");

        labelOldExtents.setText("-999 - -999");

        jLabel12.setText("\u4FEE\u6539\u540E\u4E0A\u4E0B\u9650:");

        labelNewExtents.setText("<html><b>-999 - 999</b></html>");

        labelPlatformWarning.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/error.png"))); // NOI18N
        labelPlatformWarning.setText("<html><b>\u672A\u77E5\u683C\u5F0F; \u65E0\u6CD5\u5BFC\u51FA</b></html>");
        labelPlatformWarning.setToolTipText("<html>\u8BE5\u5730\u56FE\u683C\u5F0F\u4F4D\u7F6E\u56E0\u6B64\u65E0\u6CD5\u5BFC\u51FA. \u5F88\u6709\u53EF\u80FD\u662F\u56E0\u4E3A\u652F\u6301\u6539\u683C\u5F0F\u7684\u63D2\u4EF6<br>n\u672A\u5B89\u88C5\u6216\u65E0\u6CD5\u52A0\u8F7D.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(labelCutOffWarning)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxAdjustLayers)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(labelCurrentMinHeight)
                                    .addComponent(comboBoxNewMinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(comboBoxNewMaxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelWarning))
                                    .addComponent(labelCurrentMaxHeight)
                                    .addComponent(jLabel10)))
                            .addComponent(jLabel5)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBoxTranslate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinnerScaleAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel7))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(spinnerTranslateAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(label)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel8)))
                            .addComponent(checkBoxScale)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelNewExtents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelOldExtents)))
                            .addComponent(labelPlatformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(comboBoxPlatform, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelPlatformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(labelCurrentMaxHeight)
                    .addComponent(labelCurrentMinHeight))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(comboBoxNewMaxHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelWarning)
                    .addComponent(comboBoxNewMinHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxScale)
                    .addComponent(spinnerScaleAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxTranslate)
                    .addComponent(spinnerTranslateAmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(labelOldExtents))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(labelNewExtents, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(checkBoxAdjustLayers)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK)
                    .addComponent(labelCutOffWarning))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void comboBoxNewMaxHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxNewMaxHeightActionPerformed
        if (initialising) {
            return;
        }
        updateLabels();
        setControlStates();
    }//GEN-LAST:event_comboBoxNewMaxHeightActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void checkBoxScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkBoxScaleStateChanged
        if (initialising) {
            return;
        }
        setControlStates();
    }//GEN-LAST:event_checkBoxScaleStateChanged

    private void checkBoxTranslateStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkBoxTranslateStateChanged
        if (initialising) {
            return;
        }
        setControlStates();
    }//GEN-LAST:event_checkBoxTranslateStateChanged

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        doResize();
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void spinnerScaleAmountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerScaleAmountStateChanged
        if (initialising) {
            return;
        }
        updateLabels();
        setControlStates();
    }//GEN-LAST:event_spinnerScaleAmountStateChanged

    private void spinnerTranslateAmountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerTranslateAmountStateChanged
        if (initialising) {
            return;
        }
        updateLabels();
        setControlStates();
    }//GEN-LAST:event_spinnerTranslateAmountStateChanged

    private void comboBoxPlatformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxPlatformActionPerformed
        if (initialising) {
            return;
        }
        setPlatform((Platform) comboBoxPlatform.getSelectedItem());
    }//GEN-LAST:event_comboBoxPlatformActionPerformed

    private void comboBoxNewMinHeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxNewMinHeightActionPerformed
        if (initialising) {
            return;
        }
        updateLabels();
        setControlStates();
    }//GEN-LAST:event_comboBoxNewMinHeightActionPerformed

    private void checkBoxScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxScaleActionPerformed
        if (initialising) {
            return;
        }
        updateLabels();
    }//GEN-LAST:event_checkBoxScaleActionPerformed

    private void checkBoxTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxTranslateActionPerformed
        if (initialising) {
            return;
        }
        updateLabels();
    }//GEN-LAST:event_checkBoxTranslateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JCheckBox checkBoxAdjustLayers;
    private javax.swing.JCheckBox checkBoxScale;
    private javax.swing.JCheckBox checkBoxTranslate;
    private javax.swing.JComboBox<Integer> comboBoxNewMaxHeight;
    private javax.swing.JComboBox<Integer> comboBoxNewMinHeight;
    private javax.swing.JComboBox<Platform> comboBoxPlatform;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel label;
    private javax.swing.JLabel labelCurrentMaxHeight;
    private javax.swing.JLabel labelCurrentMinHeight;
    private javax.swing.JLabel labelCutOffWarning;
    private javax.swing.JLabel labelNewExtents;
    private javax.swing.JLabel labelOldExtents;
    private javax.swing.JLabel labelPlatformWarning;
    private javax.swing.JLabel labelWarning;
    private javax.swing.JSpinner spinnerScaleAmount;
    private javax.swing.JSpinner spinnerTranslateAmount;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final int lowestHeight, highestHeight;
    private final List<Platform> supportedPlatforms = new ArrayList<>();
    private boolean initialising = true;

    private static final long serialVersionUID = 1L;
}