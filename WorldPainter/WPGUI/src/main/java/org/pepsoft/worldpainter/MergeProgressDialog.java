/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.TaskbarProgressReceiver;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.merging.JavaWorldMerger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Pepijn Schmitz
 */
public class MergeProgressDialog extends MultiProgressDialog<Void> implements WindowListener {
    public MergeProgressDialog(Window parent, JavaWorldMerger merger, File backupDir) {
        super(parent, "\u5408\u5E76\u4E2D");
        this.merger = merger;
        this.backupDir = backupDir;
        addWindowListener(this);

        JButton minimiseButton = new JButton("\u6700\u5C0F\u5316");
        minimiseButton.addActionListener(e -> App.getInstance().setState(Frame.ICONIFIED));
        addButton(minimiseButton);
    }

    // WindowListener

    @Override
    public void windowClosed(WindowEvent e) {
        // Make sure to clean up any progress that is still showing
        DesktopUtils.setProgressDone(App.getInstance());
    }

    @Override public void windowClosing(WindowEvent e) {}
    @Override public void windowOpened(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    // MultiProgressDialog

    @Override
    protected String getVerb() {
        return "\u5408\u5E76";
    }

    @Override
    protected String getResultsReport(Void results, long duration) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u4E16\u754C\u4E0E ").append(merger.getMapDir()).append(" \u5408\u5E76");
        int hours = (int) (duration / 3600);
        duration = duration - hours * 3600L;
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration - minutes * 60);
        sb.append("\n\u4E16\u754C\u5408\u5E76\u8017\u65F6 ").append(hours).append(":").append((minutes < 10) ? "0" : "").append(minutes).append(":").append((seconds < 10) ? "0" : "").append(seconds);
        sb.append("\n\n\u5F53\u524D\u4E16\u754C\u5907\u4EFD\u5DF2\u5B58\u4E8E:\n").append(backupDir);
        return sb.toString();
    }

    @Override
    protected String getCancellationMessage() {
        return "\u5408\u5E76\u5DF2\u88AB\u7528\u6237\u53D6\u6D88.\n\n\u90E8\u5206\u5408\u5E76\u7684\u5730\u56FE\u53EF\u80FD\u5DF2\u635F\u574F!\n\u4F60\u9700\u8981\u5220\u9664\u5B83\u5E76\u4F7F\u7528\u8BE5\u6587\u4EF6\u6062\u590D\u4E16\u754C:\n" + backupDir;
    }

    @Override
    protected ProgressTask<Void> getTask() {
        return new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "Merging world " + merger.getWorld().getName();
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
                progressReceiver = new TaskbarProgressReceiver(App.getInstance(), progressReceiver);
                try {
                    merger.merge(backupDir, progressReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("I/O error while merging world " + merger.getWorld().getName() + " with map " + merger.getMapDir(), e);
                }
                return null;
            }
        };
    }

    private final File backupDir;
    private final JavaWorldMerger merger;
}