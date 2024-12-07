/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.SubProgressReceiver;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.history.HistoryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowWarning;
import static org.pepsoft.util.swing.ProgressDialog.NOT_CANCELABLE;
import static org.pepsoft.worldpainter.App.INT_NUMBER_FORMAT;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Dimension.Role.DETAIL;
import static org.pepsoft.worldpainter.util.MinecraftUtil.blocksToWalkingTime;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class ScaleWorldDialog extends WorldPainterDialog {

    /**
     * Creates new form ScaleWorldDialog
     */
    public ScaleWorldDialog(Window parent, World2 world, Anchor anchor) {
        super(parent);
        this.world = world;
        this.anchor = anchor;
        affectedDimensions = world.getDimensions().stream()
                .filter(dimension -> dimension.getAnchor().dim == anchor.dim)
                .collect(toList());

        initComponents();
        setTitle("Scale " + new Anchor(anchor.dim, DETAIL, false, 0).getDefaultName() + " Dimension");
        final Dimension dimension = world.getDimension(anchor);
        final int width = dimension.getWidth() * TILE_SIZE, height = dimension.getHeight() * TILE_SIZE;
        labelCurrentSize.setText(format("%s x %s blocks", INT_NUMBER_FORMAT.format(width), INT_NUMBER_FORMAT.format(height)));
        labelCurrentWalkingTime.setText(getWalkingTime(width, height));
        updateNewSize();

        getRootPane().setDefaultButton(buttonScale);

        setLocationRelativeTo(parent);
    }

    private void scale() {
        final int percentage = (int) spinnerScaleFactor.getValue();
        if (percentage == 100) {
            beepAndShowError(this, "\u9009\u62E9\u4E00\u4E2A 100% \u4EE5\u5916\u7684\u7F29\u653E\u7CFB\u6570", "\u9009\u62E9\u7F29\u653E\u7CFB\u6570");
            return;
        } else if (JOptionPane.showConfirmDialog(this, "\u4F60\u786E\u5B9A\u8981\u5C06\u8BE5\u7EF4\u5EA6\u7F29\u653E\u81F3" + percentage + "%\u5417?\n\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!", "\u786E\u8BA4\u7F29\u653E", YES_NO_OPTION) != OK_OPTION) {
            return;
        }
        final CoordinateTransform transform = CoordinateTransform.getScalingInstance(percentage / 100f);
        ProgressDialog.executeTask(this, new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "\u7F29\u653E\u7EF4\u5EA6";
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
                for (int i = 0; i < affectedDimensions.size(); i++) {
                    final Dimension dimension = affectedDimensions.get(i);
                    world.transform(dimension.getAnchor(), transform, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, (float) i / affectedDimensions.size(), 1.0f / affectedDimensions.size()) : null);
                    world.addHistoryEntry(HistoryEntry.WORLD_DIMENSION_SCALED, dimension.getName(), percentage);
                }
                return null;
            }
        }, NOT_CANCELABLE);
        if (affectedDimensions.stream().flatMap(dimension -> dimension.getOverlays().stream()).anyMatch(overlay -> ! overlay.getFile().canRead())) {
            beepAndShowWarning(this, "\u4E00\u4E2A\u6216\u591A\u4E2A\u906E\u7F69\u56FE\u65E0\u6CD5\u8BFB\u53D6,\n\u56E0\u6B64\u4E5F\u672A\u88AB\u8C03\u6574.\n\u4F60\u9700\u8981\u624B\u52A8\u8C03\u6574\u5B83\u4EEC.", "\u90E8\u5206\u906E\u7F69\u56FE\u672A\u8C03\u6574");
        }
        ok();
    }

    private void updateNewSize() {
        final Dimension dimension = world.getDimension(anchor);
        final float scale = (int) spinnerScaleFactor.getValue() / 100f;
        final int newWidth = Math.round(dimension.getWidth() * TILE_SIZE * scale), newHeight = Math.round(dimension.getHeight() * TILE_SIZE * scale);
        labelNewSize.setText(format("%s x %s \u683C", INT_NUMBER_FORMAT.format(newWidth), INT_NUMBER_FORMAT.format(newHeight)));
        labelNewWalkingTime.setText(getWalkingTime(newWidth, newHeight));
    }

    private void setControlStates() {
        buttonScale.setEnabled((int) spinnerScaleFactor.getValue() != 100);
    }

    private String getWalkingTime(int width, int height) {
        if (width == height) {
            return blocksToWalkingTime(width);
        } else {
            String westEastTime = blocksToWalkingTime(width);
            String northSouthTime = blocksToWalkingTime(height);
            if (westEastTime.equals(northSouthTime)) {
                return westEastTime;
            } else {
                return "west to east: " + westEastTime + ", north to south: " + northSouthTime;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelCurrentSize = new javax.swing.JLabel();
        labelCurrentWalkingTime = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        spinnerScaleFactor = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonScale = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        labelNewSize = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        labelNewWalkingTime = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u7F29\u653E\u4E16\u754C");

        jLabel1.setText("\u4F7F\u7528\u8FD9\u4E9B\u9009\u9879\u7F29\u653E\u4E16\u754C:");

        jLabel2.setText("\u5F53\u524D\u5927\u5C0F:");

        jLabel3.setText("\u4ECE\u4E00\u8FB9\u5230\u53E6\u4E00\u8FB9\u7684\u884C\u8D70\u65F6\u95F4:");

        labelCurrentSize.setText("jLabel4");

        labelCurrentWalkingTime.setText("jLabel4");

        jLabel4.setLabelFor(spinnerScaleFactor);
        jLabel4.setText("\u7F29\u653E\u7CFB\u6570:");

        spinnerScaleFactor.setModel(new javax.swing.SpinnerNumberModel(100, 10, 1000, 1));
        spinnerScaleFactor.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerScaleFactorStateChanged(evt);
            }
        });

        jLabel5.setText("%");

        buttonCancel.setText("\u53d6\u6d88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonScale.setText("\u7F29\u653E");
        buttonScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonScaleActionPerformed(evt);
            }
        });

        jLabel6.setText("\u7F29\u653E\u540E\u5927\u5C0F:");

        labelNewSize.setText("jLabel7");

        jLabel7.setText("\u7F29\u653E\u540E\u884C\u8D70\u65F6\u95F4:");

        labelNewWalkingTime.setText("jLabel8");

        jLabel8.setText("<html>\n\u6CE8\u610F:\n<ul>\n<li>\u5730\u5F62\u9AD8\u5EA6\u4EE5\u53CA\u7C7B\u4F3C\u4E8E\u6811\u3001\u81EA\u5B9A\u4E49\u5BF9\u8C61\u7B49\u7684\u8986\u76D6\u5C42<br>\n\u4F1A\u4F7F\u7528\u53CC\u4E09\u6B21\u63D2\u503C\u5E73\u6ED1\u7F29\u653E. \u7136\u800C\uFF0C\u4EBA\u5DE5\u4FEE\u590D\u4ECD\u7136\u662F\u4E0D\u53EF\u907F\u514D\u7684,<br>\n\u7279\u522B\u662F\u5728\u7F29\u653E\u7CFB\u6570\u5F88\u5927\u7684\u65F6\u5019.\n<li>\u79BB\u6563\u8986\u76D6\u5C42\uFF08\u5982\u6807\u6CE8\u548C\u751F\u7269\u7FA4\u7CFB\uFF09\u4EE5\u53CA\u65B9\u5757\u7C7B\u578B\u90FD\u5C06\u4F7F\u7528\u6700\u8FD1\u90BB\u63D2\u503C\u8FDB\u884C\u7F29\u653E\u3002<br>\n\u5728\u7F29\u653E\u7CFB\u6570\u5F88\u5927\u7684\u65F6\u5019\uFF0C\u8FD9\u4E9B\u8986\u76D6\u5C42\u4F1A\u6709\u660E\u663E\u7684\u5757\u72B6\u8FB9\u7F18.\n<li>\u7F29\u653E\u4F4E\u5206\u8FA8\u7387\u5730\u5F62\uFF08\u4ECEMinecraft\u6216\u4F4E\u5206\u8FA8\u7387\u9AD8\u5EA6\u56FE\u5BFC\u5165\uFF09\u53EF\u80FD\u4F1A\u5BFC\u81F4\u5757\u72B6\u7ED3\u679C.\n<li>\u53EF\u80FD\u4F1A\u5728\u7F29\u653E\u540E\u7684\u8FB9\u754C\u5468\u56F4\u6DFB\u52A0\u989D\u5916\u7684\u571F\u5730\uFF0C\u4EE5\u6EE1\u8DB3\u533A\u5757\u8FB9\u754C\u9700\u6C42.\n<li><strong>\u8B66\u544A:</strong> \u653E\u5927\u4E16\u754C\u65F6\u53EF\u80FD\u4F1A\u56E0\u4E3A\u5185\u5B58\u4E0D\u8DB3\u653E\u5927\u5931\u8D25. \u8BF7\u5148\u5C06\u4F60\u7684\u4E16\u754C\u4FDD\u5B58\u5230\u78C1\u76D8\u4E2D!\n</ul>\n</html>");

        jLabel9.setFont(jLabel9.getFont().deriveFont((jLabel9.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel9.setText("\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!");

        jLabel10.setText("<html><i>\u6240\u6709\u76F8\u5173\u7684\u7EF4\u5EA6\u4E5F\u4F1A\u88AB\u4E00\u8D77\u7F29\u653E\uFF0C\u4F8B\u5982\u9876\u5C42\u7EF4\u5EA6\u3001\u6D1E\u7A74/\u901A\u9053\u7EF4\u5EA6\u7B49.</i></html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addGap(0, 0, Short.MAX_VALUE)
                                                                .addComponent(buttonScale)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(buttonCancel))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel1)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel2)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelCurrentSize))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel3)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelCurrentWalkingTime))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel4)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(spinnerScaleFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(0, 0, 0)
                                                                                .addComponent(jLabel5))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel6)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelNewSize))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel7)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelNewWalkingTime))
                                                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jLabel9))
                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(labelCurrentSize))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(labelCurrentWalkingTime))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(spinnerScaleFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(labelNewSize))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(labelNewWalkingTime))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonCancel)
                                        .addComponent(buttonScale))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScaleActionPerformed
        scale();
    }//GEN-LAST:event_buttonScaleActionPerformed

    private void spinnerScaleFactorStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerScaleFactorStateChanged
        setControlStates();
        updateNewSize();
    }//GEN-LAST:event_spinnerScaleFactorStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonScale;
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
    private javax.swing.JLabel labelCurrentSize;
    private javax.swing.JLabel labelCurrentWalkingTime;
    private javax.swing.JLabel labelNewSize;
    private javax.swing.JLabel labelNewWalkingTime;
    private javax.swing.JSpinner spinnerScaleFactor;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final Anchor anchor;
    private final List<Dimension> affectedDimensions;
}