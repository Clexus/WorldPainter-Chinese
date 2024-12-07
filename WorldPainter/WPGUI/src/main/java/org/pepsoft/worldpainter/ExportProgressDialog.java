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

import org.pepsoft.minecraft.ChunkFactory;
import org.pepsoft.util.*;
import org.pepsoft.util.Version;
import org.pepsoft.util.ProgressReceiver.OperationCancelled;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.exporting.WorldExportSettings;
import org.pepsoft.worldpainter.exporting.WorldExporter;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.util.FileInUseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Map;

import static org.pepsoft.minecraft.Constants.*;
import static org.pepsoft.util.ExceptionUtils.chainContains;
import static org.pepsoft.worldpainter.Constants.V_1_17;
import static org.pepsoft.worldpainter.DefaultPlugin.*;

/**
 *
 * @author pepijn
 */
public class ExportProgressDialog extends MultiProgressDialog<Map<Integer, ChunkFactory.Stats>> implements WindowListener {
    /** Creates new form ExportWorldDialog */
    public ExportProgressDialog(Window parent, World2 world, WorldExportSettings exportSettings, File baseDir, String name, String acknowledgedWarnings) {
        super(parent, "\u5BFC\u51FA\u4E2D");
        this.world = world;
        this.baseDir = baseDir;
        this.name = name;
        this.exportSettings = exportSettings;
        this.acknowledgedWarnings = acknowledgedWarnings;
        addWindowListener(this);

        JButton minimiseButton = new JButton("\u6700\u5C0F\u5316");
        minimiseButton.addActionListener(e -> App.getInstance().setState(Frame.ICONIFIED));
        addButton(minimiseButton);
    }

    public boolean isAllowRetry() {
        return allowRetry;
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
        return "\u5BFC\u51FA";
    }

    @Override
    protected String getResultsReport(Map<Integer, ChunkFactory.Stats> result, long duration) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\u4E16\u754C\u5DF2\u5BFC\u51FA\u4E3A\u6587\u4EF6\u5939 ").append(new File(baseDir, FileUtils.sanitiseName(name)));
        int hours = (int) (duration / 3600);
        duration = duration - hours * 3600L;
        int minutes = (int) (duration / 60);
        int seconds = (int) (duration - minutes * 60);
        sb.append("<br>\u5BFC\u51FA\u82B1\u8D39\u65F6\u95F4: ").append(hours).append(":").append((minutes < 10) ? "0" : "").append(minutes).append(":").append((seconds < 10) ? "0" : "").append(seconds);
        final Platform platform = world.getPlatform();
        final Version mcVersion = platform.getAttribute(ATTRIBUTE_MC_VERSION);
        if ((platform == JAVA_MCREGION) && (world.getMaxHeight() != DEFAULT_MAX_HEIGHT_MCREGION)) {
            sb.append("<br><br>\u8BF7\u6CE8\u610F: \u8BE5\u5730\u56FE\u7684\u9AD8\u5EA6<b>\u4E0D\u6807\u51C6!</b> <br>\u5982\u9700\u6E38\u73A9\u8BE5\u5730\u56FE\uFF0C\u4F60\u9700\u8981\u4E00\u4E2A\u5408\u9002\u7684\u9AD8\u5EA6\u4FEE\u6539mod!");
        } else if ((mcVersion.isAtLeast(V_1_17)) && ((world.getMaxHeight() - world.getMinHeight()) > 384)) {
            sb.append("<br><br>\u8BF7\u6CE8\u610F: \u8BE5\u5730\u56FE\u9AD8\u5EA6<b>\u9AD8\u4E8E 384 \u683C</b>.<br>\u8FD9\u53EF\u80FD\u5728\u4F4E\u7AEF\u7535\u8111\u4E0A\u5E26\u6765\u6027\u80FD\u95EE\u9898.");
        }
        if ((platform == JAVA_ANVIL_1_17) && ((world.getMinHeight() != DEFAULT_MIN_HEIGHT) || (world.getMaxHeight() != DEFAULT_MAX_HEIGHT_ANVIL))) {
            sb.append("<br><br>\u8BF7\u6CE8\u610F: \u8BE5\u5730\u56FE\u4F7F\u7528\u4E86<b>\u4FEE\u6539\u5EFA\u7B51\u9AD8\u5EA6\u7684\u6570\u636E\u5305</b>.<br>\u8BE5\u6570\u636E\u5305\u53EA\u652F\u6301 Minecraft 1.17.<br>\u4E0D\u652F\u6301\u66F4\u65B0\u7248\u7684 Minecraft.");
        } else if (((platform == JAVA_ANVIL_1_18) || (platform == JAVA_ANVIL_1_19)) && ((world.getMinHeight() != DEFAULT_MIN_HEIGHT_1_18) || (world.getMaxHeight() != DEFAULT_MAX_HEIGHT_1_18))) {
            sb.append("<br><br>\u8BF7\u6CE8\u610F: \u8BE5\u5730\u56FE\u4F7F\u7528\u4E86<b>\u4FEE\u6539\u5EFA\u7B51\u9AD8\u5EA6\u7684\u6570\u636E\u5305</b>.<br>\u8BE5\u6570\u636E\u5305\u53EA\u652F\u6301 Minecraft 1.18.2-1.20.1.<br>\u53EF\u80FD\u4E0D\u652F\u6301\u66F4\u65B0\u7248\u7684 Minecraft.");
        }
        if (result.size() == 1) {
            ChunkFactory.Stats stats = result.get(result.keySet().iterator().next());
            sb.append("<br><br>\u7EDF\u8BA1\u6570\u636E:<br>");
            dumpStats(sb, stats, world.getMaxHeight() - world.getMinHeight());
        } else {
            for (Map.Entry<Integer, ChunkFactory.Stats> entry: result.entrySet()) {
                final int dim = entry.getKey();
                final int height;
                ChunkFactory.Stats stats = entry.getValue();
                switch (dim) {
                    case Constants.DIM_NORMAL:
                        sb.append("<br><br>\u4E3B\u4E16\u754C\u7EDF\u8BA1\u6570\u636E:<br>");
                        height = world.getMaxHeight() - world.getMinHeight();
                        break;
                    case Constants.DIM_NETHER:
                        sb.append("<br><br>\u4E0B\u754C\u7EDF\u8BA1\u6570\u636E:<br>");
                        height = DEFAULT_MAX_HEIGHT_NETHER;
                        break;
                    case Constants.DIM_END:
                        sb.append("<br><br>\u672B\u5730\u7EDF\u8BA1\u6570\u636E:<br>");
                        height = DEFAULT_MAX_HEIGHT_END;
                        break;
                    default:
                        sb.append("<br><br>\u7EF4\u5EA6 " + dim + " \u7684\u7EDF\u8BA1\u6570\u636E:<br>");
                        height = world.getMaxHeight() - world.getMinHeight();
                        break;
                }
                dumpStats(sb, stats, height);
            }
        }
        if (backupDir.isDirectory()) {
            sb.append("<br>\u5DF2\u5728<br>").append(backupDir).append("<br>\u521B\u5EFA\u5F53\u524D\u5730\u56FE\u7684\u5907\u4EFD");
        }
        if ((acknowledgedWarnings != null) && (! acknowledgedWarnings.trim().isEmpty())) {
            sb.append("<br><br><em>\u4E4B\u524D\u88AB\u786E\u8BA4\u7684\u8B66\u544A:</em>");
            sb.append(acknowledgedWarnings);
        }
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    protected String getCancellationMessage() {
        return "\u5BFC\u51FA\u5DF2\u88AB\u7528\u6237\u53D6\u6D88.\n\n\u88AB\u90E8\u5206\u5BFC\u51FA\u7684\u5730\u56FE\u5F88\u6709\u53EF\u80FD\u5DF2\u635F\u574F!\n\u4F60\u5E94\u8BE5\u5220\u9664\u8BE5\u5730\u56FE\u6216\u91CD\u65B0\u5BFC\u51FA\u4E00\u904D." + (backupDir.isDirectory() ? ("\n\n\u5F53\u524D\u5730\u56FE\u7684\u5907\u4EFD\u5DF2\u521B\u5EFA\u4E8E:\n" + backupDir) : "");
    }

    @Override
    protected ProgressTask<Map<Integer, ChunkFactory.Stats>> getTask() {
        return new ProgressTask<Map<Integer, ChunkFactory.Stats>>() {
            @Override
            public String getName() {
                return "\u6B63\u5728\u5BFC\u51FA\u4E16\u754C " + name;
            }

            @Override
            public Map<Integer, ChunkFactory.Stats> execute(ProgressReceiver progressReceiver) throws OperationCancelled {
                progressReceiver = new TaskbarProgressReceiver(App.getInstance(), progressReceiver);
                progressReceiver.setMessage("\u6B63\u5728\u5BFC\u51FA\u4E16\u754C " + name);
                WorldExporter exporter = PlatformManager.getInstance().getExporter(world, exportSettings);
                try {
                    backupDir = exporter.selectBackupDir(baseDir, name);
                    return exporter.export(baseDir, name, backupDir, progressReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("I/O error while exporting world", e);
                } catch (RuntimeException e) {
                    if (chainContains(e, FileInUseException.class)) {
                        allowRetry = true;
                    }
                    throw e;
                }
            }
        };
    }

    private void dumpStats(final StringBuilder sb, final ChunkFactory.Stats stats, final int height) {
        final NumberFormat formatter = NumberFormat.getIntegerInstance();
        final long duration = stats.time / 1000;
        if (stats.landArea > 0) {
            sb.append("\u9646\u5730\u9762\u79EF: " + formatter.format(stats.landArea) + " \u683C<br>");
        }
        if (stats.waterArea > 0) {
            sb.append("\u6C34\u57DF\u6216\u5CA9\u6D46\u9762\u79EF: " + formatter.format(stats.waterArea) + " \u683C<br>");
            if (stats.landArea > 0) {
                sb.append("\u603B\u8BA1\u9762\u79EF: " + formatter.format(stats.landArea + stats.waterArea) + " \u683C<br>");
            }
        }
        final long totalBlocks = stats.surfaceArea * height;
        if (duration > 0) {
            sb.append("\u751F\u6210\u4E86 " + formatter.format(totalBlocks) + " \u4E2A\u65B9\u5757, \u901F\u5EA6\u4E3A " + formatter.format(totalBlocks / duration) + " \u4E2A\u65B9\u5757\u6BCF\u79D2<br>");
            if (stats.size > 0) {
                final long kbPerSecond = stats.size / duration / 1024;
                sb.append("\u5730\u56FE\u5927\u5C0F: " + formatter.format(stats.size / 1024 / 1024) + " MB, \u901F\u5EA6\u4E3A " + ((kbPerSecond < 1024) ? (formatter.format(kbPerSecond) + " KB") : (formatter.format(kbPerSecond / 1024) + " MB")) + " \u6BCF\u79D2<br>");
            }
        } else {
            sb.append("\u751F\u6210\u4E86 " + formatter.format(totalBlocks) + " \u4E2A\u65B9\u5757<br>");
            if (stats.size > 0) {
                sb.append("\u5730\u56FE\u5927\u5C0F: " + formatter.format(stats.size / 1024 / 1024) + " MB<br>");
            }
        }
    }
    
    private final World2 world;
    private final String name, acknowledgedWarnings;
    private final File baseDir;
    private final WorldExportSettings exportSettings;
    private volatile File backupDir;
    private volatile boolean allowRetry = false;
    
    private static final long serialVersionUID = 1L;
}