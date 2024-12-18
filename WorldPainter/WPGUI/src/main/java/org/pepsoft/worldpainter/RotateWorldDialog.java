/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RotateWorldDialog.java
 *
 * Created on Apr 14, 2012, 3:57:24 PM
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.SubProgressReceiver;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.history.HistoryEntry;

import java.awt.*;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.pepsoft.util.AwtUtils.doOnEventThread;
import static org.pepsoft.util.mdc.MDCUtils.decorateWithMdcContext;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowWarning;
import static org.pepsoft.worldpainter.Dimension.Role.DETAIL;
import static org.pepsoft.worldpainter.ExceptionHandler.handleException;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class RotateWorldDialog extends WorldPainterDialog implements ProgressReceiver {
    /** Creates new form RotateWorldDialog */
    public RotateWorldDialog(Window parent, World2 world, Anchor anchor) {
        super(parent);
        this.world = world;
        this.anchor = anchor;
        affectedDimensions = world.getDimensions().stream()
                .filter(dimension -> dimension.getAnchor().dim == anchor.dim)
                .collect(toList());

        initComponents();
        setTitle("\u65CB\u8F6C " + new Anchor(anchor.dim, DETAIL, false, 0).getDefaultName() + " \u7EF4\u5EA6");

        getRootPane().setDefaultButton(buttonRotate);

        scaleToUI();
        pack();
        setLocationRelativeTo(parent);
    }

    // ProgressReceiver

    @Override
    public synchronized void setProgress(final float progress) {
        doOnEventThread(() -> jProgressBar1.setValue((int) (progress * 100)));
    }

    @Override
    public synchronized void exceptionThrown(final Throwable exception) {
        // Make sure to capture the MDC context from the current thread
        final Throwable exceptionWithContext = decorateWithMdcContext(exception);
        doOnEventThread(() -> {
            handleException(exceptionWithContext, RotateWorldDialog.this);
            cancel();
        });
    }

    @Override
    public synchronized void done() {
        doOnEventThread(() -> {
            if (affectedDimensions.stream().flatMap(dimension -> dimension.getOverlays().stream()).anyMatch(overlay -> ! overlay.getFile().canRead())) {
                beepAndShowWarning(this, "\u4E00\u4E2A\u6216\u591A\u4E2A\u906E\u7F69\u56FE\u65E0\u6CD5\u8BFB\u53D6,\n\u56E0\u6B64\u4E5F\u672A\u88AB\u8C03\u6574.\n\u4F60\u9700\u8981\u624B\u52A8\u8C03\u6574\u5B83\u4EEC.", "\u90E8\u5206\u906E\u7F69\u56FE\u672A\u8C03\u6574");
            } else if (affectedDimensions.stream().anyMatch(dimension -> ! dimension.getOverlays().isEmpty())) {
                beepAndShowWarning(this, "\u906E\u7F69\u56FE\u5DF2\u88AB\u79FB\u52A8\u5230\u6B63\u786E\u4F4D\u7F6E, \u4F46\u672A\u88AB\u65CB\u8F6C.\n\u4F60\u5FC5\u987B\u5728 WorldPainter \u5916\u624B\u52A8\u65CB\u8F6C\u8FD9\u4E9B\u56FE\u7247.", "\u906E\u7F69\u56FE\u672A\u65CB\u8F6C");
            }
            ok();
        });
    }

    @Override
    public synchronized void setMessage(final String message) {
        doOnEventThread(() -> labelProgressMessage.setText(message));
    }

    @Override
    public synchronized void checkForCancellation() {
        // Do nothing
    }

    @Override
    public void reset() {
        doOnEventThread(() -> jProgressBar1.setValue(0));
    }

    @Override
    public void subProgressStarted(SubProgressReceiver subProgressReceiver) {
        // Do nothing
    }

    private void rotate() {
        buttonRotate.setEnabled(false);
        buttonCancel.setEnabled(false);
        final CoordinateTransform transform;
        final int degrees;
        if (jRadioButton1.isSelected()) {
            transform = CoordinateTransform.ROTATE_CLOCKWISE_90_DEGREES;
            degrees = 90;
        } else if (jRadioButton2.isSelected()) {
            transform = CoordinateTransform.ROTATE_180_DEGREES;
            degrees = 180;
        } else {
            transform = CoordinateTransform.ROTATE_CLOCKWISE_270_DEGREES;
            degrees = 270;
        }
        new Thread("World Rotator") {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < affectedDimensions.size(); i++) {
                        final Dimension dimension = affectedDimensions.get(i);
                        world.transform(dimension.getAnchor(), transform, new SubProgressReceiver(RotateWorldDialog.this, (float) i / affectedDimensions.size(), 1.0f / affectedDimensions.size()));
                        world.addHistoryEntry(HistoryEntry.WORLD_DIMENSION_ROTATED, dimension.getName(), degrees);
                    }
                    done();
                } catch (Throwable t) {
                    exceptionThrown(t);
                }
            }
        }.start();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        buttonCancel = new javax.swing.JButton();
        buttonRotate = new javax.swing.JButton();
        labelProgressMessage = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("\u65CB\u8F6C\u4E16\u754C");
        setResizable(false);

        jLabel1.setText("\u9009\u62E9\u4E00\u4E2A\u65CB\u8F6C\u89D2\u5EA6\u540E\u6309\u4E0B\u6309\u94AE\u65CB\u8F6C\u4E16\u754C:");

        buttonCancel.setText("\u53d6\u6d88");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonRotate.setText("\u65CB\u8F6C");
        buttonRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRotateActionPerformed(evt);
            }
        });

        labelProgressMessage.setText(" ");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("90 \u5EA6\u987A\u65F6\u9488");

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("180 \u5EA6");

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("90 \u9006\u65F6\u9488");

        jLabel2.setText("<html><em>\u8BE5\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500!</em>   </html>");

        jLabel7.setText("<html><i>\u6240\u6709\u76F8\u5173\u7684\u7EF4\u5EA6\uFF0C\u6BD4\u5982\u9876\u5C42\u7EF4\u5EA6\u3001\u81EA\u5B9A\u4E49\u6D1E\u7A74/\u901A\u9053\u7EF4\u5EA6\u7B49\u90FD\u4F1A\u4E00\u8D77\u65CB\u8F6C.</i></html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(buttonRotate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(labelProgressMessage)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton3)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton3)
                .addGap(18, 18, 18)
                .addComponent(labelProgressMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonRotate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonRotateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRotateActionPerformed
        rotate();
    }//GEN-LAST:event_buttonRotateActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonRotate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JLabel labelProgressMessage;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final Anchor anchor;
    private final List<Dimension> affectedDimensions;

    private static final long serialVersionUID = 1L;
}