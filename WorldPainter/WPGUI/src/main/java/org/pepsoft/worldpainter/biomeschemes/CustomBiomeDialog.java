/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.biomeschemes;

import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.WorldPainterDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.pepsoft.worldpainter.Platform.Capability.NAMED_BIOMES;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"}) // Managed by Netbeans
public class CustomBiomeDialog extends WorldPainterDialog {
    /**
     * Creates new form CustomBiomeDialog
     */
    public CustomBiomeDialog(Window parent, CustomBiome customBiome, boolean _new, Platform platform) {
        super(parent);
        this.customBiome = customBiome;
        initComponents();
        paintPicker1.setOpacity(0.5f);
        paintPicker1.setOpacityEnabled(false);
        spinnerID.setValue(customBiome.getId());
        fieldName.setText(customBiome.getName());
        if (platform.capabilities.contains(NAMED_BIOMES)) {
            remove(jLabel2);
            remove(spinnerID);
            jLabel3.setText("ID:");
            if (_new) {
                fieldName.requestFocusInWindow();
                fieldName.selectAll();
            }
        } else {
            if (!_new) {
                spinnerID.setEnabled(false);
            } else {
                ((SpinnerNumberModel) spinnerID.getModel()).setMinimum(customBiome.getId());
            }
        }
        if (customBiome.getPattern() != null) {
            paintPicker1.setPaint(customBiome.getPattern());
        } else {
            paintPicker1.setPaint(new Color(customBiome.getColour()));
        }

        rootPane.setDefaultButton(buttonOK);
        scaleToUI();
        pack();
        setLocationRelativeTo(parent);
    }

    @Override
    public void ok() {
        customBiome.setId((Integer) spinnerID.getValue());
        customBiome.setName(fieldName.getText().trim());
        final Object paint = paintPicker1.getPaint();
        if (paint instanceof Color) {
            customBiome.setColour(((Color) paint).getRGB());
            customBiome.setPattern(null);
        } else {
            customBiome.setPattern((BufferedImage) paint);
        }
        super.ok();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"}) // Managed by Netbeans
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        spinnerID = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        paintPicker1 = new org.pepsoft.worldpainter.layers.renderers.PaintPicker();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u914D\u7F6E\u81EA\u5B9A\u4E49\u751F\u7269\u7FA4\u7CFB");

        jLabel1.setText("\u5728\u6B64\u914D\u7F6E\u81EA\u5B9A\u4E49\u751F\u7269\u7FA4\u7CFB:");

        jLabel2.setText("ID:");

        spinnerID.setModel(new javax.swing.SpinnerNumberModel(40, 40, 255, 1));

        jLabel3.setText("\u540D\u79F0:");

        fieldName.setColumns(20);

        jLabel4.setText("\u753B\u7B14");

        buttonCancel.setText("\u53d6\u6d88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("\u786e\u8ba4");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(spinnerID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(spinnerID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(paintPicker1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        ok();
    }//GEN-LAST:event_buttonOKActionPerformed

    private final CustomBiome customBiome;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private org.pepsoft.worldpainter.layers.renderers.PaintPicker paintPicker1;
    private javax.swing.JSpinner spinnerID;
    // End of variables declaration//GEN-END:variables

    private static final long serialVersionUID = 1L;
}