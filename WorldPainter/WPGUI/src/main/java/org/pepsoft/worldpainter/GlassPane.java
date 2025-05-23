/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * GlassPane.java
 *
 * Created on Apr 28, 2011, 4:29:56 PM
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.IconUtils;
import org.pepsoft.worldpainter.layers.Biome;
import org.pepsoft.worldpainter.layers.Layer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;

import static java.awt.BorderLayout.CENTER;
import static org.pepsoft.util.GUIUtils.getUIScale;

/**
 *
 * @author pepijn
 */
public class GlassPane extends javax.swing.JPanel {
    /** Creates new form GlassPane */
    public GlassPane() {
        initComponents();
        if (getUIScale() != 1.0f) {
            jLabel1.setIcon(IconUtils.loadScaledIcon("org/pepsoft/worldpainter/scale_bar.png")); // NOI18N
            jLabel2.setIcon(IconUtils.loadScaledIcon("org/pepsoft/worldpainter/north_arrow_up.png")); // NOI18N
        }
//        jPanel2.add(miniMap, BorderLayout.CENTER);
    }
    
    public void setScale(float scale) {
        int scaleBarSize = (int) (100 / scale * getUIScale());
        jLabel1.setText(SCALE_FORMAT.format(scaleBarSize));
        repaint();
    }

    public void setHiddenLayers(Set<Layer> hiddenLayers) {
        hiddenLayers.stream().filter(layer -> (! layer.equals(Biome.INSTANCE)) && (layer.getIcon() != null) && (! this.hiddenLayers.containsKey(layer))).forEach(layer -> {
            JLabel label = createLabel(layer);
            this.hiddenLayers.put(layer, label);
        });
        this.hiddenLayers.entrySet().removeIf(entry -> !hiddenLayers.contains(entry.getKey()));
        updateIcons();
    }
    
    public void setSoloLayer(Layer soloLayer) {
        if ((soloLayer != null) && (soloLayer.getIcon() != null)) {
            soloLayerLabel = createSoloLabel(soloLayer);
        } else {
            soloLayerLabel = null;
        }
        updateIcons();
    }

    public void pushPanelComponent(Component component) {
        panelComponentStack.add(component);
        jPanel2.removeAll();
        jPanel2.add(component, CENTER);
        jPanel2.revalidate();
    }

    public Component popPanelComponent() {
        if (panelComponentStack.isEmpty()) {
            return null;
        } else {
            jPanel2.removeAll();
            final Component component = panelComponentStack.remove(panelComponentStack.size() - 1);
            if (! panelComponentStack.isEmpty()) {
                jPanel2.add(panelComponentStack.get(panelComponentStack.size() - 1), CENTER);
            }
            jPanel2.revalidate();
            return component;
        }
    }

    public void removePanelComponents() {
        panelComponentStack.clear();
        jPanel2.removeAll();
        jPanel2.revalidate();
    }

    private void updateIcons() {
        jPanel1.removeAll();
        if (soloLayerLabel != null) {
            jPanel1.add(soloLayerLabel);
        } else {
            hiddenLayers.values().forEach(jPanel1::add);
        }
        jPanel1.revalidate();
    }
    
    private JLabel createLabel(Layer layer) {
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(Math.round(20 * getUIScale()), Math.round(20 * getUIScale()), Transparency.TRANSLUCENT);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.drawImage(PROHIBITED_SIGN_BACKGROUND, 0, 0, null);
            g2.drawImage(layer.getIcon(), Math.round(2 * getUIScale()), Math.round(2 * getUIScale()), null);
            g2.drawImage(PROHIBITED_SIGN_FOREGROUND, 0, 0, null);
        } finally {
            g2.dispose();
        }
        JLabel label = new JLabel(new ImageIcon(image));
        label.setBorder(new EmptyBorder(1, 1, 1, 1));
        label.setToolTipText(layer.getName() + " \u8986\u76D6\u5C42\u88AB\u9690\u85CF");
        return label;
    }

    private JLabel createSoloLabel(Layer layer) {
        BufferedImage image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(Math.round(20 * getUIScale()), Math.round(20 * getUIScale()), Transparency.TRANSLUCENT);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.drawImage(layer.getIcon(), Math.round(2 * getUIScale()), Math.round(2 * getUIScale()), null);
        } finally {
            g2.dispose();
        }
        JLabel label = new JLabel(new ImageIcon(image));
        label.setBorder(new EmptyBorder(1, 1, 1, 1));
        label.setToolTipText("\u53EA\u663E\u793A " + layer.getName() + " \u8986\u76D6\u5C42");
        return label;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();

        setOpaque(false);

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/scale_bar.png"))); // NOI18N
        jLabel1.setText("100 blocks");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/north_arrow_up.png"))); // NOI18N

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jPanel2.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

    private WorldPainter view;
//    private final MiniMap miniMap = new MiniMap();
    private final Map<Layer, JLabel> hiddenLayers = new HashMap<>();
    private JLabel soloLayerLabel;
    private List<Component> panelComponentStack = new ArrayList<>();

    private static final NumberFormat SCALE_FORMAT = new DecimalFormat("#,##0.# \u683C");
    private static final BufferedImage PROHIBITED_SIGN_BACKGROUND = IconUtils.loadScaledImage("org/pepsoft/worldpainter/icons/prohibited_sign_background.png");
    private static final BufferedImage PROHIBITED_SIGN_FOREGROUND = IconUtils.loadScaledImage("org/pepsoft/worldpainter/icons/prohibited_sign_foreground.png");
    private static final long serialVersionUID = 1L;
}