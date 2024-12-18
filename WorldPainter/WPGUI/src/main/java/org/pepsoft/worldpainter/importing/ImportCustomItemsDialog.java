/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.importing;

import com.jidesoft.swing.CheckBoxTreeCellRenderer;
import com.jidesoft.swing.CheckBoxTreeSelectionModel;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.World2;
import org.pepsoft.worldpainter.WorldPainterDialog;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

/**
 *
 * @author Pepijn Schmitz
 */
public class ImportCustomItemsDialog extends WorldPainterDialog implements TreeSelectionListener {
    /**
     * Creates new form ImportCustomItemsDialog
     */
    public ImportCustomItemsDialog(Window parent, World2 world, ColourScheme colourScheme, CustomItemsTreeModel.ItemType itemType) {
        super(parent);
        
        initComponents();
        ((CheckBoxTreeCellRenderer) treeCustomItems.getCellRenderer()).setActualTreeRenderer(new CustomItemsTreeCellRenderer(colourScheme));
        treeModel = new CustomItemsTreeModel(world, itemType);
        treeCustomItems.getCheckBoxTreeSelectionModel().setModel(treeModel);
        treeCustomItems.setModel(treeModel);
        expandAll(treeCustomItems);
        CheckBoxTreeSelectionModel checkBoxTreeSelectionModel = treeCustomItems.getCheckBoxTreeSelectionModel();
        checkBoxTreeSelectionModel.addSelectionPath(new TreePath(treeModel.getRoot()));
        checkBoxTreeSelectionModel.addTreeSelectionListener(this);
        labelWorld.setText(world.getName());

        getRootPane().setDefaultButton(buttonOK);
        scaleToUI();
        pack();
        scaleWindowToUI();
        setLocationRelativeTo(parent);
    }

    public List<Object> getSelectedItems() {
        return treeModel.getSelectedItems(treeCustomItems.getCheckBoxTreeSelectionModel().getSelectionPaths());
    }

    // TreeSelectionListener

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        setControlStates();
    }

    private void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    private void setControlStates() {
        buttonOK.setEnabled(treeCustomItems.getCheckBoxTreeSelectionModel().getSelectionCount() > 0);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        labelWorld = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        treeCustomItems = new com.jidesoft.swing.CheckBoxTree();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u5BFC\u5165\u81EA\u5B9A\u4E49\u9879\u76EE");

        jLabel1.setText("\u9009\u4E2D\u4E16\u754C:");

        labelWorld.setText("jLabel2");

        jLabel3.setText("\u9009\u5165\u4F60\u8981\u5BFC\u5165\u7684\u9879\u76EE:");

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

        treeCustomItems.setSelectionModel(null);
        jScrollPane1.setViewportView(treeCustomItems);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelWorld))
                            .addComponent(jLabel3))
                        .addGap(0, 91, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(labelWorld))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                .addGap(18, 18, 18)
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelWorld;
    private com.jidesoft.swing.CheckBoxTree treeCustomItems;
    // End of variables declaration//GEN-END:variables

    private final CustomItemsTreeModel treeModel;
}