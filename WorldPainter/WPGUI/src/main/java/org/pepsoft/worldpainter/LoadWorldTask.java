package org.pepsoft.worldpainter;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.swing.ProgressTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.pepsoft.worldpainter.World2.*;

public class LoadWorldTask implements ProgressTask<World2> {
    public LoadWorldTask(Component parent, File file) {
        this.parent = parent;
        this.file = file;
    }

    @Override
    public String getName() {
        return strings.getString("loading.world");
    }

    @Override
    public World2 execute(ProgressReceiver progressReceiver) {
        try {
            WorldIO worldIO = new WorldIO();
            worldIO.load(new FileInputStream(file));
            World2 world = worldIO.getWorld();
            if (logger.isDebugEnabled() && (world.getMetadata() != null)) {
                logMetadataAsDebug(world.getMetadata());
            }
            return world;
        } catch (UnloadableWorldException e) {
            logger.error("Could not load world from file " + file, e);
            if (e.getMetadata() != null) {
                logMetadataAsError(e.getMetadata());
            }
            reportUnloadableWorldException(e);
            return null;
        } catch (IOException e) {
            throw new RuntimeException("I/O error while loading world", e);
        }
    }

    private void appendMetadata(StringBuilder sb, Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry: metadata.entrySet()) {
            switch (entry.getKey()) {
                case METADATA_KEY_WP_VERSION:
                    sb.append("\u4F7F\u7528 WorldPainter ").append(entry.getValue()).append(" \u4FDD\u5B58");
                    String build = (String) metadata.get(METADATA_KEY_WP_BUILD);
                    if (build != null) {
                        sb.append(" (").append(build).append(')');
                    }
                    sb.append('\n');
                    break;
                case METADATA_KEY_TIMESTAMP:
                    sb.append("\u4FDD\u5B58\u4E8E ").append(SimpleDateFormat.getDateTimeInstance().format((Date) entry.getValue())).append('\n');
                    break;
                case METADATA_KEY_PLUGINS:
                    String[][] plugins = (String[][]) entry.getValue();
                    for (String[] plugin: plugins) {
                        sb.append("\u63D2\u4EF6: ").append(plugin[0]).append(" (").append(plugin[1]).append(")\n");
                    }
                    break;
            }
        }
    }

    private void logMetadataAsDebug(Map<String, Object> metadata) {
        StringBuilder sb = new StringBuilder("\u6765\u81EA\u4E8E\u4E16\u754C\u6587\u4EF6\u7684\u5143\u6570\u636E:\n");
        appendMetadata(sb, metadata);
        logger.debug(sb.toString());
    }

    private void logMetadataAsError(Map<String, Object> metadata) {
        StringBuilder sb = new StringBuilder("\u6765\u81EA\u4E8E\u4E16\u754C\u6587\u4EF6\u7684\u5143\u6570\u636E:\n");
        appendMetadata(sb, metadata);
        logger.error(sb.toString());
    }

    private void reportUnloadableWorldException(UnloadableWorldException e) {
        try {
            String text;
            if (e.getMetadata() != null) {
                StringBuilder sb = new StringBuilder("WorldPainter \u65E0\u6CD5\u52A0\u8F7D\u8BE5\u6587\u4EF6\uFF0C\u53EF\u80FD\u662F\u56E0\u4E3A:\n" +
                        "\n" +
                        "* \u8BE5\u6587\u4EF6\u5DF2\u635F\u574F\u6216\u5D29\u6E83\n" +
                        "* \u8BE5\u6587\u4EF6\u7531\u66F4\u65B0\u7684 WorldPainter \u521B\u5EFA\n" +
                        "* \u8BE5\u6587\u4EF6\u4F7F\u7528\u4E86\u4F60\u6CA1\u6709\u7684 WorldPainter \u63D2\u4EF6\n" +
                        "\n");
                appendMetadata(sb, e.getMetadata());
                text = sb.toString();
            } else {
                text = "WorldPainter \u65E0\u6CD5\u52A0\u8F7D\u8BE5\u6587\u4EF6\uFF0C\u53EF\u80FD\u662F\u56E0\u4E3A:\n" +
                        "\n" +
                        "* \u8BE5\u6587\u4EF6\u4E0D\u662F WorldPainter \u4E16\u754C\n" +
                        "* \u8BE5\u6587\u4EF6\u5DF2\u635F\u574F\u6216\u5D29\u6E83\n" +
                        "* \u8BE5\u6587\u4EF6\u7531\u66F4\u65B0\u7684 WorldPainter \u521B\u5EFA\n" +
                        "* \u8BE5\u6587\u4EF6\u4F7F\u7528\u4E86\u4F60\u6CA1\u6709\u7684 WorldPainter \u63D2\u4EF6\n";
            }
            SwingUtilities.invokeAndWait(() -> showMessageDialog(parent, text, strings.getString("file.damaged"), ERROR_MESSAGE));
        } catch (InterruptedException e2) {
            throw new RuntimeException("Thread interrupted while reporting unloadable file " + file, e2);
        } catch (InvocationTargetException e2) {
            throw new RuntimeException("Invocation target exception while reporting unloadable file " + file, e2);
        }
    }

    private final Component parent;
    private final File file;

    private static final Logger logger = LoggerFactory.getLogger(LoadWorldTask.class);
    private static final ResourceBundle strings = ResourceBundle.getBundle("org.pepsoft.worldpainter.resources.strings"); // NOI18N
}