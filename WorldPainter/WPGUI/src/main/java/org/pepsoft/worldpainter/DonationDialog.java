/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DonationDialog.java
 *
 * Created on 5-nov-2011, 17:24:59
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.worldpainter.vo.EventVO;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

import static org.pepsoft.util.swing.MessageUtils.showInfo;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"ConstantConditions", "Convert2Lambda", "Anonymous2MethodRef", "unused", "FieldCanBeLocal"}) // Managed by NetBeans
public final class DonationDialog extends WorldPainterDialog {
    /** Creates new form DonationDialog */
    public DonationDialog(Window parent) {
        super(parent);
        config = Configuration.getInstance();
        
        initComponents();
        
        // Fix JTextArea font, which uses a butt ugly non-proportional font by default on Windows
        jTextArea1.setFont(UIManager.getFont("TextField.font").deriveFont(UIManager.getFont("TextField.font").getSize() + 6f));
        
        rootPane.setDefaultButton(buttonDonate);
        pack();
    }

    public static boolean maybeShowDonationDialog(Window parent) {
        final Configuration config = Configuration.getInstance();
        // Ask again every so often
        if (config.getLaunchCount() >= config.getShowDonationDialogAfter()) {
            DonationDialog dialog = new DonationDialog(parent);
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            if (dialog.isCancelled()) {
                config.logEvent(new EventVO(Constants.EVENT_KEY_DONATION_CLOSED).addTimestamp());
            }
            return true;
        } else {
            return false;
        }
    }
    
    private void donate() {
        try {
            DesktopUtils.open(new URL("https://www.worldpainter.net/donate/paypal"));
            config.setDonationStatus(Configuration.DonationStatus.DONATED);
            config.setShowDonationDialogAfter(config.getLaunchCount() + 100);
            showInfo(this, "\u6350\u8D60PayPal\u9875\u9762\u5DF2\u5728\u4F60\u7684\u6D4F\u89C8\u5668\u6253\u5F00.\n\n\u611F\u8C22\u60A8\u7684\u6350\u8D60!", "\u8C22\u8C22");
            config.logEvent(new EventVO(Constants.EVENT_KEY_DONATION_DONATE).addTimestamp());
            ok();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void alreadyDonated() {
        config.setDonationStatus(Configuration.DonationStatus.DONATED);
        config.setShowDonationDialogAfter(config.getLaunchCount() + 100);
        showInfo(this, "\u975E\u5E38\u611F\u8C22\u6350\u8D60!", "\u8C22\u8C22");
        config.logEvent(new EventVO(Constants.EVENT_KEY_DONATION_ALREADY_DONATED).addTimestamp());
        ok();
    }

    private void askLater() {
        config.logEvent(new EventVO(Constants.EVENT_KEY_DONATION_ASK_LATER).addTimestamp());
        ok();
    }

    private void noThanks() {
        config.setDonationStatus(Configuration.DonationStatus.NO_THANK_YOU);
        config.setShowDonationDialogAfter(config.getLaunchCount() + 50);
        showInfo(this, "\u884C\uFF0C\u6211\u4EEC\u4F1A\u5728\u4E00\u6BB5\u65F6\u95F4\u5185\u90FD\u4E0D\u6253\u6270\u60A8\u7684.\n\u5982\u679C\u60A8\u6539\u53D8\u4E3B\u610F\u4E86\uFF0C\u8FD8\u662F\u53EF\u4EE5\u5728\u5173\u4E8E\u9875\u9762\u8FDB\u884C\u6350\u8D60!", "\u6CA1\u95EE\u9898");
        config.logEvent(new EventVO(Constants.EVENT_KEY_DONATION_NO_THANKS).addTimestamp());
        ok();
    }

    private void openMerchStore() {
        try {
            DesktopUtils.open(new URL("https://www.worldpainter.store/"));
            config.logEvent(new EventVO(Constants.EVENT_KEY_MERCH_STORE_OPENED).addTimestamp());
            ok();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextArea1 = new javax.swing.JTextArea();
        buttonDonate = new javax.swing.JButton();
        buttonAlreadyDonated = new javax.swing.JButton();
        buttonAskLater = new javax.swing.JButton();
        buttonNoThanks = new javax.swing.JButton();
        buttonMerchStore = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u8BF7\u652F\u6301WorldPainter");
        setResizable(false);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/resources/about.png"))); // NOI18N
        jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(jTextArea1.getFont().deriveFont(jTextArea1.getFont().getSize()+6f));
        jTextArea1.setLineWrap(true);
        jTextArea1.setText("\u611F\u8C22\u60A8\u4F7F\u7528 WorldPainter!\n\nWorldPainter \u7684\u521B\u4F5C\u548C\u7EF4\u62A4\u9700\u8981\u82B1\u8D39\u5F88\u591A\u7CBE\u529B. \u8BF7\u8003\u8651\u901A\u8FC7\u6350\u8D60\u6216\u8D2D\u4E70\u5468\u8FB9\u5546\u54C1\u8D5E\u52A9\u672C\u9879\u76EE.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);

        buttonDonate.setBackground(new java.awt.Color(255, 196, 58));
        buttonDonate.setFont(buttonDonate.getFont().deriveFont(buttonDonate.getFont().getStyle() | java.awt.Font.BOLD, buttonDonate.getFont().getSize()+3));
        buttonDonate.setMnemonic('d');
        buttonDonate.setText("\u6350\u8D60");
        buttonDonate.setBorderPainted(false);
        buttonDonate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDonateActionPerformed(evt);
            }
        });

        buttonAlreadyDonated.setFont(buttonAlreadyDonated.getFont().deriveFont(buttonAlreadyDonated.getFont().getSize()+3f));
        buttonAlreadyDonated.setMnemonic('a');
        buttonAlreadyDonated.setText("\u6211\u5DF2\u7ECF\u6350\u8D60\u8FC7\u4E86");
        buttonAlreadyDonated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAlreadyDonatedActionPerformed(evt);
            }
        });

        buttonAskLater.setFont(buttonAskLater.getFont().deriveFont(buttonAskLater.getFont().getSize()+3f));
        buttonAskLater.setMnemonic('l');
        buttonAskLater.setText("\u4EE5\u540E\u518D\u8BF4");
        buttonAskLater.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAskLaterActionPerformed(evt);
            }
        });

        buttonNoThanks.setFont(buttonNoThanks.getFont().deriveFont(buttonNoThanks.getFont().getSize()+3f));
        buttonNoThanks.setMnemonic('n');
        buttonNoThanks.setText("\u4E0D\uFF0C\u6211\u4E0D\u60F3\u6350\u8D60");
        buttonNoThanks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonNoThanksActionPerformed(evt);
            }
        });

        buttonMerchStore.setFont(buttonMerchStore.getFont().deriveFont(buttonMerchStore.getFont().getSize()+3f));
        buttonMerchStore.setText("\u5468\u8FB9\u5546\u5E97");
        buttonMerchStore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMerchStoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonDonate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonMerchStore)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAlreadyDonated)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonAskLater)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonNoThanks))
                    .addComponent(jTextArea1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jTextArea1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonDonate)
                    .addComponent(buttonAlreadyDonated)
                    .addComponent(buttonAskLater)
                    .addComponent(buttonNoThanks)
                    .addComponent(buttonMerchStore))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonDonateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDonateActionPerformed
        donate();
    }//GEN-LAST:event_buttonDonateActionPerformed

    private void buttonAlreadyDonatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAlreadyDonatedActionPerformed
        alreadyDonated();
    }//GEN-LAST:event_buttonAlreadyDonatedActionPerformed

    private void buttonAskLaterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAskLaterActionPerformed
        askLater();
    }//GEN-LAST:event_buttonAskLaterActionPerformed

    private void buttonNoThanksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNoThanksActionPerformed
        noThanks();
    }//GEN-LAST:event_buttonNoThanksActionPerformed

    private void buttonMerchStoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMerchStoreActionPerformed
        openMerchStore();
    }//GEN-LAST:event_buttonMerchStoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAlreadyDonated;
    private javax.swing.JButton buttonAskLater;
    private javax.swing.JButton buttonDonate;
    private javax.swing.JButton buttonMerchStore;
    private javax.swing.JButton buttonNoThanks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    private final Configuration config;
    
    private static final long serialVersionUID = 1L;
}