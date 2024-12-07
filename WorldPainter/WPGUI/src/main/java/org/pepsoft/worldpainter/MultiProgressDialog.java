/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportWorldDialog.java
 *
 * Created on Mar 29, 2011, 5:09:50 PM
 */

package org.pepsoft.worldpainter;

import org.pepsoft.minecraft.exception.IncompatibleMaterialException;
import org.pepsoft.util.SubProgressReceiver;
import org.pepsoft.util.swing.ProgressComponent.Listener;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.merging.InvalidMapException;
import org.pepsoft.worldpainter.util.FileInUseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import static org.pepsoft.util.AwtUtils.doLaterOnEventThread;
import static org.pepsoft.util.ExceptionUtils.chainContains;
import static org.pepsoft.util.ExceptionUtils.getFromChainOfType;
import static org.pepsoft.util.GUIUtils.scaleToUI;
import static org.pepsoft.util.swing.MessageUtils.*;
import static org.pepsoft.worldpainter.ExceptionHandler.handleException;

/**
 *
 * @author pepijn
 */
public abstract class MultiProgressDialog<T> extends javax.swing.JDialog implements Listener<T>, ComponentListener {
    /** Creates new form ExportWorldDialog */
    public MultiProgressDialog(Window parent, String title) {
        super(parent, ModalityType.APPLICATION_MODAL);
        initComponents();
        setTitle(title);

        scaleToUI(this);
        setLocationRelativeTo(parent);
        
        addComponentListener(this);
    }

    /**
     * Get the unconjugated verb describing the operation, starting with a
     * capital letter.
     * 
     * @return The unconjugated verb describing the operation.
     */
    protected abstract String getVerb();
    
    /**
     * Transform the results object into a text describing the results, suitable
     * for inclusion in a {@link JOptionPane}. HTML is allowed, and must be
     * enclosed in &lt;html&gt;&lt;/html&gt; tags
     * 
     * @param results The result returned by the task.
     * @param duration The duration in ms.
     * @return A text containing a report of the results.
     */
    protected abstract String getResultsReport(T results, long duration);
    
    /**
     * Get the message to show to the user in a {@link JOptionPane} after they
     * cancel the operation.
     * 
     * @return The message to show to the user in a {@link JOptionPane} after
     * they cancel the operation.
     */
    protected abstract String getCancellationMessage();
    
    /**
     * Get the task to perform. The task may use nested
     * {@link SubProgressReceiver}s to report progress, which will be reported
     * separately on the screen.
     * 
     * @return The task to perform.
     */
    protected abstract ProgressTask<T> getTask();

    /**
     * Add a {@link JButton} to the panel, to the left of the Cancel button.
     *
     * @param button The button to add.
     */
    protected void addButton(JButton button) {
        multiProgressComponent1.addButton(button);
    }

    // ProgressComponent.Listener
    
    @Override
    public void exceptionThrown(Throwable exception) {
        doLaterOnEventThread(() -> {
            if (chainContains(exception, FileInUseException.class)) {
                beepAndShowError(MultiProgressDialog.this, "\u65E0\u6CD5" + getVerb().toLowerCase() + "\u4E16\u754C\uFF0C\u56E0\u4E3A\u73B0\u6709\u4E16\u754C\u76EE\u5F55\u88AB\u5360\u7528.\n\u8BF7\u5173\u95ED Minecraft \u548C\u5176\u4ED6\u6240\u6709\u7A97\u53E3\u518D\u8BD5.", "\u5730\u56FE\u4F7F\u7528\u4E2D");
            } else if (chainContains(exception, MissingCustomTerrainException.class)) {
                beepAndShowError(MultiProgressDialog.this, "\u81EA\u5B9A\u4E49\u65B9\u5757 " + (getFromChainOfType(exception, MissingCustomTerrainException.class)).getIndex() + " \u672A\u914D\u7F6E!\n" +
                        "\u8BF7\u5728\u81EA\u5B9A\u4E49\u65B9\u5757\u754C\u9762\u914D\u7F6E.\n" +
                        "\n" +
                        "\u4EC5\u88AB\u90E8\u5206\u5904\u7406\u7684\u5730\u56FE\u53EF\u80FD\u5DF2\u635F\u574F.\n" +
                        "\u4F60\u9700\u8981\u4ECE\u5907\u4EFD\u6587\u4EF6\u4E2D\u5C06\u5176\u66FF\u6362\u6216\u91CD\u65B0" + getVerb().toLowerCase() + "\u5730\u56FE.", "\u672A\u914D\u7F6E\u7684\u65B9\u5757\u7C7B\u578B");
            } else if (chainContains(exception, InvalidMapException.class)) {
                beepAndShowError(MultiProgressDialog.this, getFromChainOfType(exception, InvalidMapException.class).getMessage(), "\u65E0\u6548\u5730\u56FE");
            } else if (chainContains(exception, IncompatibleMaterialException.class)) {
                beepAndShowError(MultiProgressDialog.this, getFromChainOfType(exception, IncompatibleMaterialException.class).getMessage(), "\u4E0D\u517C\u5BB9\u7684\u6750\u8D28");
            } else {
                handleException(exception, MultiProgressDialog.this);
            }
            close();
        });
    }

    @Override
    public void done(T result) {
        long end = System.currentTimeMillis();
        long duration = (end - start) / 1000;
        doLaterOnEventThread(() -> {
            String resultsReport = getResultsReport(result, duration);
            showInfo(this, resultsReport, "\u6210\u529F");
            close();
        });
    }

    @Override
    public void cancelled() {
        logger.info(getVerb() + "\u5DF2\u88AB\u7528\u6237\u53D6\u6D88");
        doLaterOnEventThread(() -> {
            showWarning(this, getCancellationMessage(), getVerb() + "\u5DF2\u53D6\u6D88");
            close();
        });
    }

    // ComponentListener
    
    @Override
    public void componentShown(ComponentEvent e) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        multiProgressComponent1.setTask(getTask());
        multiProgressComponent1.setListener(this);
        start = System.currentTimeMillis();
        multiProgressComponent1.start();
    }

    @Override public void componentResized(ComponentEvent e) {}
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}

    // Implementation details
    
    private void close() {
        dispose();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        multiProgressComponent1 = new org.pepsoft.util.swing.MultiProgressComponent();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(multiProgressComponent1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(multiProgressComponent1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.pepsoft.util.swing.MultiProgressComponent<T> multiProgressComponent1;
    // End of variables declaration//GEN-END:variables

    private long start;

    private static final Logger logger = LoggerFactory.getLogger(MultiProgressDialog.class);
    private static final long serialVersionUID = 1L;
}