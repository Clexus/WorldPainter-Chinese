/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package org.pepsoft.worldpainter.layers.renderers;

import org.pepsoft.util.ImageUtils;
import org.pepsoft.worldpainter.WorldPainterDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparingInt;
import static org.pepsoft.util.IconUtils.createScaledColourIcon;
import static org.pepsoft.util.ImageUtils.createColourSquare;

/**
 *
 * @author pepijn
 */
public class EditPaintDialog extends WorldPainterDialog {

    /**
     * Creates new form EditPaintDialog
     */
    public EditPaintDialog(Window parent, Object paint, float opacity, boolean opacityEnabled) {
        super(parent);
        if (paint instanceof Color) {
            this.colour = (Color) paint;
        } else if (paint instanceof BufferedImage) {
            this.pattern = ImageUtils.clone((BufferedImage) paint);
        } else {
            throw new IllegalArgumentException("Paint type " + paint.getClass() + " not supported");
        }

        initComponents();
        this.opacity = opacity;
        sliderOpacity.setValue(Math.round(opacity * 100));
        rendererPreviewer1.setOpacity(this.opacity);
        if (! opacityEnabled) {
            sliderOpacity.setEnabled(false);
        }

        getRootPane().setDefaultButton(buttonOk);
        if (colour != null) {
            iconEditor1.setIcon(createColourSquare(16, colour.getRGB()));
            eraseColour = colour.getRGB();
            radioButtonSolidColour.setSelected(true);
        } else {
            iconEditor1.setIcon(pattern);
            eraseColour = findBackgroundColour(pattern);
            radioButtonPattern.setSelected(true);
        }
        iconEditor1.setEraseColour(eraseColour);
        updatePreview();
        iconEditor1.addPropertyChangeListener("icon", evt -> {
            EditPaintDialog.this.colour = null;
            EditPaintDialog.this.pattern = (BufferedImage) evt.getNewValue();
            updatePreview();
        });

        scaleToUI();
        createColourButtons();
        pack();
        setLocationRelativeTo(parent);
        setControlStates();
    }

    /**
     * Returns the selected paint.
     *
     * <p><strong>Note:</strong> only valid after {@link #ok()} has been invoked!
     */
    public Object getSelectedPaint() {
        return (colour != null) ? colour : pattern;
    }

    /**
     * Returns the selected opacity.
     */
    public float getSelectedOpacity() {
        return opacity;
    }

    @Override
    protected void ok() {
        colour = getSolidColour();
        pattern = (colour == null) ? iconEditor1.getIcon() : null;
        super.ok();
    }

    private void setControlStates() {
        boolean patternMode = radioButtonPattern.isSelected();
        buttonSelectSolidColour.setEnabled(! patternMode);
        iconEditor1.setEditable(patternMode);
        buttonSolidColour.setEnabled(patternMode);
        toggleButtonPencil.setEnabled(patternMode);
        toggleButtonEraser.setEnabled(patternMode);
        buttonClear.setEnabled(patternMode);
        for (Component component: panelColours.getComponents()) {
            component.setEnabled(patternMode);
        }
    }

    private void createColourButtons() {
        // Remove the button that is on there just to be able to edit the form at development time
        panelColours.removeAll();
        // Rotate the opacity label (can't do that at development time or it would mess up the layout)
        labelOpacity.setOrientation(SwingConstants.VERTICAL);
        labelOpacity.setClockwise(false);
        for (int ega = 0; ega < 16; ega++) {
            final JToggleButton button = new JToggleButton(createScaledColourIcon(EGA_COLOURS[ega]));
            button.setToolTipText(EGA_NAMES[ega]);
            button.setMargin(new Insets(2, 2, 2, 2));
            final Color colour = new Color(EGA_COLOURS[ega]);
            button.addActionListener(e -> {
                paintColour = colour;
                iconEditor1.setPaintColour(colour.getRGB());
                if (! toggleButtonPencil.isSelected()) {
                    toggleButtonPencil.setSelected(true);
                }
            });
            if (ega == 0) {
                button.setSelected(true);
            }
            buttonGroupColours.add(button);
            panelColours.add(button);
        }
    }

    private void updatePreview() {
        if (colour != null) {
            rendererPreviewer1.setColour(colour);
        } else if (pattern != null) {
            rendererPreviewer1.setPattern(pattern);
        }
    }

    /**
     * If the icon editor is currently completely a solid colour, return it. Otherwise return {@code null}.
     */
    private Color getSolidColour() {
        final BufferedImage pattern = iconEditor1.getIcon();
        int solidColour = pattern.getRGB(0, 0);
        for (int x = 0; x < pattern.getWidth(); x++) {
            for (int y = 0; y < pattern.getHeight(); y++) {
                if (pattern.getRGB(x, y) != solidColour) {
                    return null;
                }
            }
        }
        return new Color(solidColour);
    }

    /**
     * Guesstimate the background colour by finding the most prevalent colour, where the outer rings of the image are
     * weighted heavier.
     */
    private int findBackgroundColour(BufferedImage image) {
        final Map<Integer, Integer> weightedCounts = new HashMap<>();
        final int w = image.getWidth(), h = image.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int rgb = image.getRGB(x, y);
                final int distanceFromEdge = Math.min(Math.min(x, w - 1 - x), Math.min(y, h - 1 - y));
                final int weight = Math.max(3 - distanceFromEdge, 1);
                int weightedCount = weightedCounts.getOrDefault(rgb, 0);
                weightedCounts.put(rgb, weightedCount + weight);
            }
        }
        final List<Map.Entry<Integer, Integer>> entries = new ArrayList<>(weightedCounts.entrySet());
        entries.sort(comparingInt(Map.Entry::getValue));
        return entries.get(entries.size() - 1).getValue();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupTools = new javax.swing.ButtonGroup();
        buttonGroupColours = new javax.swing.ButtonGroup();
        buttonGroupType = new javax.swing.ButtonGroup();
        iconEditor1 = new org.pepsoft.worldpainter.util.IconEditor();
        buttonSolidColour = new javax.swing.JButton();
        toggleButtonPencil = new javax.swing.JToggleButton();
        toggleButtonEraser = new javax.swing.JToggleButton();
        buttonCancel = new javax.swing.JButton();
        buttonOk = new javax.swing.JButton();
        panelColours = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        rendererPreviewer1 = new org.pepsoft.worldpainter.layers.renderers.RendererPreviewer();
        buttonClear = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        sliderOpacity = new javax.swing.JSlider();
        labelOpacity = new com.jidesoft.swing.JideLabel();
        radioButtonSolidColour = new javax.swing.JRadioButton();
        radioButtonPattern = new javax.swing.JRadioButton();
        buttonSelectSolidColour = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u7F16\u8F91\u753B\u7B14");

        iconEditor1.setEditable(false);

        buttonSolidColour.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/fill.png"))); // NOI18N
        buttonSolidColour.setText("\u7EAF\u8272\u586B\u5145");
        buttonSolidColour.setToolTipText("<html>\u9009\u4E00\u4E2A\u989C\u8272\u5B8C\u5168\u586B\u5145\u753B\u5E03<br>\n\u9009\u4E2D\u7684\u989C\u8272\u5C06\u6210\u4E3A\u80CC\u666F\u8272</html>");
        buttonSolidColour.setEnabled(false);
        buttonSolidColour.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonSolidColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSolidColourActionPerformed(evt);
            }
        });

        buttonGroupTools.add(toggleButtonPencil);
        toggleButtonPencil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/pencil.png"))); // NOI18N
        toggleButtonPencil.setSelected(true);
        toggleButtonPencil.setText("\u94C5\u7B14");
        toggleButtonPencil.setToolTipText("<html>\u5DE6\u952E\u4F7F\u7528\u9009\u4E2D\u989C\u8272\u7ED8\u753B<br>\n\u53F3\u952E\u4F7F\u7528\u80CC\u666F\u989C\u8272\u7ED8\u753B</html>");
        toggleButtonPencil.setEnabled(false);
        toggleButtonPencil.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        toggleButtonPencil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleButtonPencilActionPerformed(evt);
            }
        });

        buttonGroupTools.add(toggleButtonEraser);
        toggleButtonEraser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/sponge.png"))); // NOI18N
        toggleButtonEraser.setText("\u6A61\u76AE");
        toggleButtonEraser.setToolTipText("\u5C06\u989C\u8272\u91CD\u7F6E\u4E3A\u900F\u660E");
        toggleButtonEraser.setEnabled(false);
        toggleButtonEraser.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        toggleButtonEraser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleButtonEraserActionPerformed(evt);
            }
        });

        buttonCancel.setText("\u53d6\u6d88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOk.setText("\u786e\u8ba4");
        buttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOkActionPerformed(evt);
            }
        });

        panelColours.setLayout(new java.awt.GridLayout(0, 4));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/shovel-icon_16.png"))); // NOI18N
        jButton1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        panelColours.add(jButton1);

        rendererPreviewer1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        rendererPreviewer1.setToolTipText("\u6837\u5F0F\u9884\u89C8");

        buttonClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/clear_selection.png"))); // NOI18N
        buttonClear.setText("\u6E05\u9664");
        buttonClear.setToolTipText("<html>\u6E05\u9664\u753B\u5E03<br>\n\u5C06\u80CC\u666F\u8272\u8BBE\u7F6E\u4E3A\u900F\u660E</html>");
        buttonClear.setEnabled(false);
        buttonClear.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        buttonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont().deriveFont((jLabel1.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel1.setText("\u5DE6\u952E\u4F7F\u7528\u9009\u62E9\u989C\u8272\u7ED8\u753B; \u53F3\u952E\u4F7F\u7528\u80CC\u666F\u989C\u8272\u7ED8\u753B");

        sliderOpacity.setOrientation(javax.swing.JSlider.VERTICAL);
        sliderOpacity.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderOpacityStateChanged(evt);
            }
        });

        labelOpacity.setText("\u4E0D\u900F\u660E\u5EA6");

        buttonGroupType.add(radioButtonSolidColour);
        radioButtonSolidColour.setSelected(true);
        radioButtonSolidColour.setText("\u7EAF\u8272");
        radioButtonSolidColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonSolidColourActionPerformed(evt);
            }
        });

        buttonGroupType.add(radioButtonPattern);
        radioButtonPattern.setText("\u6837\u5F0F:");
        radioButtonPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonPatternActionPerformed(evt);
            }
        });

        buttonSelectSolidColour.setText("...");
        buttonSelectSolidColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectSolidColourActionPerformed(evt);
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
                        .addComponent(iconEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(buttonSolidColour, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(toggleButtonPencil, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(toggleButtonEraser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonClear, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(panelColours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                                        .addComponent(labelOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sliderOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(rendererPreviewer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonOk)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioButtonPattern)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(radioButtonSolidColour)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonSelectSolidColour)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioButtonSolidColour)
                    .addComponent(buttonSelectSolidColour))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonPattern)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(buttonSolidColour)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(toggleButtonPencil)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(toggleButtonEraser)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonClear)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(panelColours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(labelOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(sliderOpacity, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rendererPreviewer1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(iconEditor1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOk)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSolidColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSolidColourActionPerformed
        final Color selectedColour = JColorChooser.showDialog(this, "\u9009\u62E9\u4E00\u79CD\u989C\u8272", paintColour);
        if (selectedColour != null) {
            iconEditor1.fill(selectedColour.getRGB());
            colour = selectedColour;
            eraseColour = selectedColour.getRGB();
            iconEditor1.setEraseColour(eraseColour);
            pattern = null;
            updatePreview();
        }
    }//GEN-LAST:event_buttonSolidColourActionPerformed

    private void toggleButtonPencilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleButtonPencilActionPerformed
        iconEditor1.setPaintColour(paintColour.getRGB());
        jLabel1.setText("\u5DE6\u952E\u5355\u51FB\u4EE5\u4F7F\u7528\u6240\u9009\u989C\u8272\u7ED8\u5236\uFF1B\u53F3\u952E\u5355\u51FB\u4EE5\u663E\u793A\u80CC\u666F\u989C\u8272");
    }//GEN-LAST:event_toggleButtonPencilActionPerformed

    private void toggleButtonEraserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleButtonEraserActionPerformed
        iconEditor1.setPaintColour(0x00ffffff);
        jLabel1.setText("\u70B9\u51FB\u4EE5\u64E6\u9664\u4E3A\u900F\u660E");
    }//GEN-LAST:event_toggleButtonEraserActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOkActionPerformed
        ok();
    }//GEN-LAST:event_buttonOkActionPerformed

    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
        iconEditor1.fill(0x00ffffff);
        colour = null;
        eraseColour = 0x00ffffff;
        iconEditor1.setEraseColour(eraseColour);
        pattern = iconEditor1.getIcon();
        updatePreview();
    }//GEN-LAST:event_buttonClearActionPerformed

    private void sliderOpacityStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderOpacityStateChanged
        opacity = sliderOpacity.getValue() / 100.0f;
        rendererPreviewer1.setOpacity(opacity);
    }//GEN-LAST:event_sliderOpacityStateChanged

    private void radioButtonSolidColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonSolidColourActionPerformed
        final Color solidColour = getSolidColour();
        if (solidColour != null) {
            setControlStates();
        } else {
            final Color selectedColour = JColorChooser.showDialog(this, "Choose A Colour", paintColour);
            if (selectedColour != null) {
                iconEditor1.fill(selectedColour.getRGB());
                colour = selectedColour;
                eraseColour = selectedColour.getRGB();
                iconEditor1.setEraseColour(eraseColour);
                pattern = null;
                updatePreview();
                setControlStates();
            } else {
                radioButtonPattern.setSelected(true);
            }
        }
    }//GEN-LAST:event_radioButtonSolidColourActionPerformed

    private void buttonSelectSolidColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectSolidColourActionPerformed
        buttonSolidColourActionPerformed(evt);
    }//GEN-LAST:event_buttonSelectSolidColourActionPerformed

    private void radioButtonPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonPatternActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonPatternActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonClear;
    private javax.swing.ButtonGroup buttonGroupColours;
    private javax.swing.ButtonGroup buttonGroupTools;
    private javax.swing.ButtonGroup buttonGroupType;
    private javax.swing.JButton buttonOk;
    private javax.swing.JButton buttonSelectSolidColour;
    private javax.swing.JButton buttonSolidColour;
    private org.pepsoft.worldpainter.util.IconEditor iconEditor1;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private com.jidesoft.swing.JideLabel labelOpacity;
    private javax.swing.JPanel panelColours;
    private javax.swing.JRadioButton radioButtonPattern;
    private javax.swing.JRadioButton radioButtonSolidColour;
    private org.pepsoft.worldpainter.layers.renderers.RendererPreviewer rendererPreviewer1;
    private javax.swing.JSlider sliderOpacity;
    private javax.swing.JToggleButton toggleButtonEraser;
    private javax.swing.JToggleButton toggleButtonPencil;
    // End of variables declaration//GEN-END:variables

    private Color colour, paintColour = Color.BLACK;
    private int eraseColour;
    private BufferedImage pattern;
    private float opacity;

    private static final int[] EGA_COLOURS = {
            0x000000,
            0x0000AA,
            0x00AA00,
            0x00AAAA,
            0xAA0000,
            0xAA00AA,
            0xAA5500,
            0xAAAAAA,
            0x555555,
            0x5555FF,
            0x55FF55,
            0x55FFFF,
            0xFF5555,
            0xFF55FF,
            0xFFFF55,
            0xFFFFFF
    };

    private static final String[] EGA_NAMES = {
            "\u9ED1\u8272",
            "\u84DD\u8272",
            "\u7EFF\u8272",
            "\u9752\u8272",
            "\u7EA2\u8272",
            "\u6D0B\u7EA2\u8272",
            "\u68D5\u8272",
            "\u6D45\u7070\u8272",
            "\u7070\u8272",
            "\u4EAE\u84DD\u8272",
            "\u4EAE\u7EFF\u8272",
            "\u4EAE\u9752\u8272",
            "\u4EAE\u7EA2\u8272",
            "\u4EAE\u6D0B\u7EA2\u8272",
            "\u4EAE\u9EC4\u8272",
            "\u4EAE\u767D\u8272"
    };
}