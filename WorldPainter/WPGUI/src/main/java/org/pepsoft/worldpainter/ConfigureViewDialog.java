/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConfigureViewDialog.java
 *
 * Created on 3-dec-2011, 23:13:02
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.swing.TiledImageViewer;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;
import org.pepsoft.worldpainter.layers.renderers.VoidRenderer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.event.MouseEvent.BUTTON1;
import static org.pepsoft.util.GUIUtils.getUIScale;
import static org.pepsoft.worldpainter.util.ImageUtils.loadImage;
import static org.pepsoft.worldpainter.util.ImageUtils.selectImageForOpen;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ConfigureViewDialog extends WorldPainterDialog implements WindowListener {
    /** Creates new form ConfigureViewDialog */
    public ConfigureViewDialog(Frame parent, Dimension dimension, WorldPainter view) {
        this(parent, dimension, view, false);
    }
    
    /** Creates new form ConfigureViewDialog */
    public ConfigureViewDialog(Frame parent, Dimension dimension, WorldPainter view, boolean enableOverlay) {
        super(parent);
        this.dimension = dimension;
        this.view = view;
        this.enableOverlay = enableOverlay;
        initComponents();
        checkBoxGrid.setSelected(view.isPaintGrid());
        spinnerGridSize.setValue(view.getGridSize());
        checkBoxImageOverlay.setSelected(dimension.isOverlaysEnabled());
        overlaysTableModel = new OverlaysTableModel(dimension);
        tableOverlays.setModel(overlaysTableModel);
        checkBoxContours.setSelected(view.isDrawContours());
        spinnerContourSeparation.setValue(view.getContourSeparation());
        checkBoxBackgroundImage.setSelected(view.getBackgroundImage() != null);
        config = Configuration.getInstance();
        spinnerRenderDistance.setValue(config.getViewDistance() / 16);

        if (config.getBackgroundImage() != null) {
            fieldBackgroundImage.setText(config.getBackgroundImage().getAbsolutePath());
        }
        comboBoxBackgroundImageMode.setModel(new DefaultComboBoxModel<>(TiledImageViewer.BackgroundImageMode.values()));
        comboBoxBackgroundImageMode.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TiledImageViewer.BackgroundImageMode) {
                    switch ((TiledImageViewer.BackgroundImageMode) value) {
                        case CENTRE:
                            setText("\u5C45\u4E2D");
                            break;
                        case CENTRE_REPEAT:
                            setText("\u5C45\u4E2D(\u91CD\u590D)");
                            break;
                        case FIT:
                            setText("\u9002\u5E94");
                            break;
                        case FIT_REPEAT:
                            setText("\u9002\u5E94(\u91CD\u590D)");
                            break;
                        case REPEAT:
                            setText("\u91CD\u590D");
                            break;
                        case STRETCH:
                            setText("\u62C9\u4F38");
                            break;
                    }
                }
                return this;
            }
        });
        comboBoxBackgroundImageMode.setSelectedItem(config.getBackgroundImageMode());
        checkBoxShowBiomes.setSelected(config.isShowBiomes());
        checkBoxShowBorders.setSelected(config.isShowBorders());

        fieldBackgroundImage.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateBackgroundImageFile(); }
            @Override public void removeUpdate(DocumentEvent e) { updateBackgroundImageFile(); }
            @Override public void changedUpdate(DocumentEvent e) { updateBackgroundImageFile(); }
        });
        colourEditor1.setColour(view.getBackground().getRGB());
        colourEditor1.addPropertyChangeListener("colour", event -> {
            int newColour = (Integer) event.getNewValue();
            if (newColour == VoidRenderer.getColour()) {
                config.setBackgroundColour(-1);
            } else {
                config.setBackgroundColour(newColour);
            }
            view.setBackground(new Color(newColour));
        });
        if (BiomeSchemeManager.getAvailableBiomeAlgorithms().isEmpty()) {
            checkBoxShowBiomes.setSelected(false);
            checkBoxShowBiomes.setEnabled(false);
        }
        tableOverlays.getSelectionModel().addListSelectionListener(e -> setControlStates());
        tableOverlays.getColumnModel().getColumn(0).setMaxWidth((int) (50 * getUIScale()));
        setControlStates();
        scaleToUI();
        pack();
        setLocationRelativeTo(parent);

        if (enableOverlay) {
            addWindowListener(this);
        }
        programmaticChange = false;
    }

    @Override
    protected void cancel() {
        if (dimension.getOverlays().stream().noneMatch(Overlay::isEnabled)) {
            dimension.setOverlaysEnabled(false);
        }
        super.cancel();
    }

    // WindowListener

    @Override
    public void windowOpened(WindowEvent e) {
        if (enableOverlay) {
            enableOverlay();
        }
    }

    @Override public void windowClosing(WindowEvent e) {}
    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    private void setControlStates() {
        spinnerGridSize.setEnabled(checkBoxGrid.isSelected());
        boolean imageOverlayEnabled = checkBoxImageOverlay.isSelected();
        spinnerContourSeparation.setEnabled(checkBoxContours.isSelected());
        boolean backgroundImageEnabled = checkBoxBackgroundImage.isSelected();
        fieldBackgroundImage.setEnabled(backgroundImageEnabled);
        buttonSelectBackgroundImage.setEnabled(backgroundImageEnabled);
        comboBoxBackgroundImageMode.setEnabled(backgroundImageEnabled);
        tableOverlays.setEnabled(imageOverlayEnabled);
        buttonAddOverlay.setEnabled(imageOverlayEnabled);
        buttonDeleteOverlay.setEnabled(imageOverlayEnabled && (tableOverlays.getSelectedRowCount() > 0));
        buttonEditOverlay.setEnabled(imageOverlayEnabled && (tableOverlays.getSelectedRowCount() > 0));
    }

    private void updateBackgroundImageFile() {
        File file = new File(fieldBackgroundImage.getText());
        if (! config.isSafeMode()) {
            BufferedImage image = loadImage(this, file);
            if (image != null) {
                // The loading succeeded
                config.setBackgroundImage(file);
                view.setBackgroundImage(image);
            }
        } else {
            // Don't try to load the image, as that may have been what was
            // crashing, but do store the new value in the configuration, as
            // long as the file exists and is readable, so that it is still
            // possible to change the configured background image in safe mode
            if (file.isFile() && file.canRead()) {
                logger.info("[SAFE MODE] Not loading background image");
                config.setBackgroundImage(file);
            }
        }
    }

    private void selectBackgroundImage() {
        final File selectedFile = selectImageForOpen(this, "\u80CC\u666F\u56FE\u6587\u4EF6", new File(fieldBackgroundImage.getText()));
        if (selectedFile != null) {
            fieldBackgroundImage.setText(selectedFile.getAbsolutePath());
        }
    }

    private void enableOverlay() {
        if (! checkBoxImageOverlay.isSelected()) {
            checkBoxImageOverlay.setSelected(true);
            dimension.setOverlaysEnabled(true);
            setControlStates();
            if (dimension.getOverlays().isEmpty()) {
                addOverlay();
            }
        }
    }

    private void addOverlay() {
        final File imageFile = selectImageForOpen(this, "\u906e\u7f69\u56fe\u6587\u4EF6", config.getOverlaysDirectory());
        if (imageFile != null) {
            final Overlay overlay = new Overlay(imageFile);
            final int rowIndex = overlaysTableModel.addOverlay(overlay);
            final ConfigureOverlayDialog dialog = new ConfigureOverlayDialog(this, overlay, dimension);
            dialog.setVisible(true);
            if (! dialog.isCancelled()) {
                overlaysTableModel.overlayChanged(rowIndex);
                config.setOverlaysDirectory(imageFile.getParentFile());
            } else {
                overlaysTableModel.removeOverlay(rowIndex);
            }
        }
    }

    private void deleteOverlay() {
        overlaysTableModel.removeOverlay(tableOverlays.getSelectedRow());
    }

    private void editOverlay() {
        final int selectedRow = tableOverlays.getSelectedRow();
        final Overlay overlay = overlaysTableModel.getOverlay(selectedRow);
        final ConfigureOverlayDialog dialog = new ConfigureOverlayDialog(this, overlay, dimension);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            overlaysTableModel.overlayChanged(selectedRow);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({"DataFlowIssue", "Convert2Lambda", "Anonymous2MethodRef"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        checkBoxGrid = new javax.swing.JCheckBox();
        spinnerGridSize = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        checkBoxImageOverlay = new javax.swing.JCheckBox();
        buttonClose = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        checkBoxContours = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        spinnerContourSeparation = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        colourEditor1 = new org.pepsoft.worldpainter.ColourEditor();
        checkBoxBackgroundImage = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        fieldBackgroundImage = new javax.swing.JTextField();
        buttonSelectBackgroundImage = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        comboBoxBackgroundImageMode = new javax.swing.JComboBox<>();
        checkBoxShowBiomes = new javax.swing.JCheckBox();
        checkBoxShowBorders = new javax.swing.JCheckBox();
        buttonResetBackgroundColour = new javax.swing.JButton();
        buttonAddOverlay = new javax.swing.JButton();
        buttonDeleteOverlay = new javax.swing.JButton();
        buttonEditOverlay = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableOverlays = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        spinnerRenderDistance = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u914D\u7F6E\u89C6\u56FE");

        checkBoxGrid.setText("\u7F51\u683C");
        checkBoxGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxGridActionPerformed(evt);
            }
        });

        spinnerGridSize.setModel(new javax.swing.SpinnerNumberModel(128, 2, 9999, 1));
        spinnerGridSize.setEnabled(false);
        spinnerGridSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerGridSizeStateChanged(evt);
            }
        });

        jLabel1.setText("\u7F51\u683C\u5C3A\u5BF8:");

        checkBoxImageOverlay.setSelected(true);
        checkBoxImageOverlay.setText("\u663E\u793A\u906E\u7F69\u56FE");
        checkBoxImageOverlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxImageOverlayActionPerformed(evt);
            }
        });

        buttonClose.setText("\u5173\u95ED");
        buttonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCloseActionPerformed(evt);
            }
        });

        jLabel9.setText("\u683C");

        checkBoxContours.setSelected(true);
        checkBoxContours.setText("\u7B49\u9AD8\u7EBF");
        checkBoxContours.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxContoursActionPerformed(evt);
            }
        });

        jLabel10.setText("\u5206\u5272\u95F4\u9694:");

        spinnerContourSeparation.setModel(new javax.swing.SpinnerNumberModel(10, 2, 999, 1));
        spinnerContourSeparation.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerContourSeparationStateChanged(evt);
            }
        });

        jLabel11.setText("\u683C");

        jLabel12.setText("\u80CC\u666F\u8272:");

        checkBoxBackgroundImage.setText("\u80CC\u666F\u56FE");
        checkBoxBackgroundImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBackgroundImageActionPerformed(evt);
            }
        });

        jLabel13.setText("\u56FE\u50CF:");

        fieldBackgroundImage.setEnabled(false);

        buttonSelectBackgroundImage.setText("...");
        buttonSelectBackgroundImage.setEnabled(false);
        buttonSelectBackgroundImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectBackgroundImageActionPerformed(evt);
            }
        });

        jLabel14.setText("\u5951\u5408\u65B9\u5F0F:");

        comboBoxBackgroundImageMode.setEnabled(false);
        comboBoxBackgroundImageMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxBackgroundImageModeActionPerformed(evt);
            }
        });

        checkBoxShowBiomes.setText("\u663E\u793A Minecraft \u751F\u7269\u7FA4\u7CFB (\u53EF\u7528\u65F6)");
        checkBoxShowBiomes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxShowBiomesActionPerformed(evt);
            }
        });

        checkBoxShowBorders.setText("\u663E\u793A\u8FB9\u754C");
        checkBoxShowBorders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxShowBordersActionPerformed(evt);
            }
        });

        buttonResetBackgroundColour.setText("\u91CD\u7F6E");
        buttonResetBackgroundColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetBackgroundColourActionPerformed(evt);
            }
        });

        buttonAddOverlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_add.png"))); // NOI18N
        buttonAddOverlay.setToolTipText("\u6DFB\u52A0\u4E00\u5F20\u906E\u7F69\u56FE");
        buttonAddOverlay.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonAddOverlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddOverlayActionPerformed(evt);
            }
        });

        buttonDeleteOverlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_delete.png"))); // NOI18N
        buttonDeleteOverlay.setToolTipText("\u79FB\u9664\u9009\u4E2D\u906E\u7F69\u56FE");
        buttonDeleteOverlay.setEnabled(false);
        buttonDeleteOverlay.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonDeleteOverlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteOverlayActionPerformed(evt);
            }
        });

        buttonEditOverlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_edit.png"))); // NOI18N
        buttonEditOverlay.setToolTipText("\u7F16\u8F91\u9009\u4E2D\u906E\u7F69\u56FE");
        buttonEditOverlay.setEnabled(false);
        buttonEditOverlay.setMargin(new java.awt.Insets(2, 2, 2, 2));
        buttonEditOverlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonEditOverlayActionPerformed(evt);
            }
        });

        tableOverlays.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableOverlays.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tableOverlaysMousePressed(evt);
            }
        });
        jScrollPane2.setViewportView(tableOverlays);

        jLabel2.setText("\u6E32\u67D3\u8DDD\u79BB:");

        spinnerRenderDistance.setModel(new javax.swing.SpinnerNumberModel(12, 1, 32, 1));
        spinnerRenderDistance.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerRenderDistanceStateChanged(evt);
            }
        });

        jLabel3.setText("\u4E2A\u533A\u5757");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colourEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonResetBackgroundColour)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerRenderDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(buttonAddOverlay, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(buttonDeleteOverlay, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(buttonEditOverlay, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel13)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fieldBackgroundImage))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel1)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(spinnerGridSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(jLabel9))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel10)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(spinnerContourSeparation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(jLabel11))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(jLabel14)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(comboBoxBackgroundImageMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 0, Short.MAX_VALUE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkBoxGrid)
                                            .addComponent(checkBoxContours)
                                            .addComponent(checkBoxBackgroundImage)
                                            .addComponent(checkBoxShowBorders)
                                            .addComponent(checkBoxShowBiomes)
                                            .addComponent(checkBoxImageOverlay))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectBackgroundImage)))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(buttonClose)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(spinnerGridSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addComponent(checkBoxContours)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(spinnerContourSeparation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addComponent(checkBoxImageOverlay)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonAddOverlay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonDeleteOverlay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonEditOverlay))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(checkBoxShowBorders)
                .addGap(18, 18, 18)
                .addComponent(checkBoxShowBiomes)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(colourEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonResetBackgroundColour))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxBackgroundImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(fieldBackgroundImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectBackgroundImage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(comboBoxBackgroundImageMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinnerRenderDistance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addComponent(buttonClose)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkBoxGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxGridActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                setControlStates();
                boolean gridEnabled = checkBoxGrid.isSelected();
                view.setPaintGrid(gridEnabled);
                dimension.setGridEnabled(gridEnabled);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_checkBoxGridActionPerformed

    private void checkBoxImageOverlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxImageOverlayActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                setControlStates();
                final boolean overlaysEnabled = checkBoxImageOverlay.isSelected();
                dimension.setOverlaysEnabled(overlaysEnabled);
                if (dimension.getOverlays().isEmpty()) {
                    addOverlay();
                }
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_checkBoxImageOverlayActionPerformed

    private void spinnerGridSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerGridSizeStateChanged
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                int gridSize = ((Number) spinnerGridSize.getValue()).intValue();
                view.setGridSize(gridSize);
                dimension.setGridSize(gridSize);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_spinnerGridSizeStateChanged

    private void buttonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCloseActionPerformed
        if (dimension.getOverlays().stream().noneMatch(Overlay::isEnabled)) {
            dimension.setOverlaysEnabled(false);
        }
        ok();
    }//GEN-LAST:event_buttonCloseActionPerformed

    private void checkBoxContoursActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxContoursActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                setControlStates();
                boolean contoursEnabled = checkBoxContours.isSelected();
                view.setDrawContours(contoursEnabled);
                dimension.setContoursEnabled(contoursEnabled);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_checkBoxContoursActionPerformed

    private void spinnerContourSeparationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerContourSeparationStateChanged
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                int contourSeparation = ((Number) spinnerContourSeparation.getValue()).intValue();
                view.setContourSeparation(contourSeparation);
                dimension.setContourSeparation(contourSeparation);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_spinnerContourSeparationStateChanged

    private void checkBoxBackgroundImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBackgroundImageActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                if (checkBoxBackgroundImage.isSelected()) {
                    if (fieldBackgroundImage.getText().trim().isEmpty()) {
                        selectBackgroundImage();
                    } else {
                        updateBackgroundImageFile();
                    }
                } else {
                    config.setBackgroundImage(null);
                    view.setBackgroundImage(null);
                }
                setControlStates();
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_checkBoxBackgroundImageActionPerformed

    private void buttonSelectBackgroundImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectBackgroundImageActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                selectBackgroundImage();
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_buttonSelectBackgroundImageActionPerformed

    private void checkBoxShowBordersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxShowBordersActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                boolean showBorders = checkBoxShowBorders.isSelected();
                config.setShowBorders(showBorders);
                view.setDrawBorders(showBorders);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_checkBoxShowBordersActionPerformed

    private void checkBoxShowBiomesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxShowBiomesActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                boolean showBiomes = checkBoxShowBiomes.isSelected();
                config.setShowBiomes(showBiomes);
                view.setDrawBiomes(showBiomes);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_checkBoxShowBiomesActionPerformed

    private void comboBoxBackgroundImageModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxBackgroundImageModeActionPerformed
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                TiledImageViewer.BackgroundImageMode mode = (TiledImageViewer.BackgroundImageMode) comboBoxBackgroundImageMode.getSelectedItem();
                config.setBackgroundImageMode(mode);
                view.setBackgroundImageMode(mode);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_comboBoxBackgroundImageModeActionPerformed

    private void buttonResetBackgroundColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetBackgroundColourActionPerformed
        colourEditor1.setColour(VoidRenderer.getColour());
        view.setBackground(new Color(VoidRenderer.getColour()));
        config.setBackgroundColour(-1);
    }//GEN-LAST:event_buttonResetBackgroundColourActionPerformed

    private void buttonAddOverlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddOverlayActionPerformed
        addOverlay();
    }//GEN-LAST:event_buttonAddOverlayActionPerformed

    private void buttonDeleteOverlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeleteOverlayActionPerformed
        deleteOverlay();
    }//GEN-LAST:event_buttonDeleteOverlayActionPerformed

    private void buttonEditOverlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditOverlayActionPerformed
        editOverlay();
    }//GEN-LAST:event_buttonEditOverlayActionPerformed

    private void tableOverlaysMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableOverlaysMousePressed
        if ((evt.getButton() == BUTTON1) && (evt.getClickCount() == 2) && tableOverlays.isEnabled()) {
            editOverlay();
        }
    }//GEN-LAST:event_tableOverlaysMousePressed

    private void spinnerRenderDistanceStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerRenderDistanceStateChanged
        if (! programmaticChange) {
            programmaticChange = true;
            try {
                int renderDistance = ((Number) spinnerRenderDistance.getValue()).intValue();
                view.setViewDistance(renderDistance * 16);
                config.setViewDistance(renderDistance * 16);
            } finally {
                programmaticChange = false;
            }
        }
    }//GEN-LAST:event_spinnerRenderDistanceStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAddOverlay;
    private javax.swing.JButton buttonClose;
    private javax.swing.JButton buttonDeleteOverlay;
    private javax.swing.JButton buttonEditOverlay;
    private javax.swing.JButton buttonResetBackgroundColour;
    private javax.swing.JButton buttonSelectBackgroundImage;
    private javax.swing.JCheckBox checkBoxBackgroundImage;
    private javax.swing.JCheckBox checkBoxContours;
    private javax.swing.JCheckBox checkBoxGrid;
    private javax.swing.JCheckBox checkBoxImageOverlay;
    private javax.swing.JCheckBox checkBoxShowBiomes;
    private javax.swing.JCheckBox checkBoxShowBorders;
    private org.pepsoft.worldpainter.ColourEditor colourEditor1;
    private javax.swing.JComboBox<TiledImageViewer.BackgroundImageMode> comboBoxBackgroundImageMode;
    private javax.swing.JTextField fieldBackgroundImage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner spinnerContourSeparation;
    private javax.swing.JSpinner spinnerGridSize;
    private javax.swing.JSpinner spinnerRenderDistance;
    private javax.swing.JTable tableOverlays;
    // End of variables declaration//GEN-END:variables

    private final Dimension dimension;
    private final WorldPainter view;
    private final boolean enableOverlay;
    private final OverlaysTableModel overlaysTableModel;
    private final Configuration config;
    /**
     * The (unscaled) image that is currently selected.
     */
    private BufferedImage overlayImage;
    private boolean programmaticChange = true;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigureViewDialog.class);
    private static final long serialVersionUID = 1L;
}