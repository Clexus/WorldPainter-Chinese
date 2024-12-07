/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ErrorDialog.java
 *
 * Created on Apr 17, 2011, 8:22:55 PM
 */

package org.pepsoft.worldpainter;

import org.pepsoft.util.mdc.MDCWrappingException;
import org.pepsoft.util.mdc.MDCWrappingRuntimeException;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.vo.AttributeKeyVO;
import org.pepsoft.worldpainter.vo.EventVO;
import org.pepsoft.worldpainter.vo.ExceptionVO;
import org.pepsoft.worldpainter.vo.UsageVO;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.pepsoft.util.AwtUtils.doOnEventThread;
import static org.pepsoft.util.GUIUtils.scaleToUI;
import static org.pepsoft.util.mdc.MDCUtils.gatherMdcContext;
import static org.pepsoft.util.swing.MessageUtils.showInfo;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.util.ThreadUtils.getMostRecentThreadCount;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unused", "Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
public class ErrorDialog extends javax.swing.JDialog {
    /** Creates new form ErrorDialog */
    public ErrorDialog(Window parent) {
        super(parent, APPLICATION_MODAL);
        init(parent);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend") // Readability
    public void setException(Throwable exception) {
        SortedMap<String, String> mdcContextMap = new TreeMap<>(gatherMdcContext(exception));
        if ((exception instanceof MDCWrappingException) || (exception instanceof MDCWrappingRuntimeException)) {
            exception = exception.getCause();
        }

        final UUID uuid = UUID.randomUUID();
        logger.error("[" + uuid + "] " + exception.getClass().getSimpleName() + ": " + exception.getMessage(), exception);

        event = new EventVO(EVENT_KEY_EXCEPTION);
        event.addTimestamp();
        event.setAttribute(ATTRIBUTE_KEY_EXCEPTION, new ExceptionVO(exception));
        event.setAttribute(ATTRIBUTE_KEY_UUID, uuid.toString());

        final Set<Class<? extends Throwable>> exceptionTypes = new HashSet<>();
        exceptionTypes.add(exception.getClass());
        Throwable rootCause = exception;
        String ioExceptionMessage = (exception instanceof IOException) ? exception.getMessage() : null;
        // TODO remove this temporary measure to get to the bottom of something
        boolean forceEmail = forceEmail(exception);
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
            exceptionTypes.add(rootCause.getClass());
            if (rootCause instanceof IOException) {
                ioExceptionMessage = rootCause.getMessage();
            }
            if (forceEmail(rootCause)) {
                forceEmail = true;
            }
        }
        final boolean ioException = ioExceptionMessage != null;

        if (exceptionTypes.contains(OutOfMemoryError.class)) {
            setTitle("\u5185\u5B58\u6EA2\u51FA");
            final Integer threadCount = getMostRecentThreadCount();
            if ((threadCount != null) && (threadCount > 1)) {
                jTextArea1.setText("\u6CA1\u6709\u8DB3\u591F\u7684\u5185\u5B58\u6765\u8FDB\u884C\u8BE5\u64CD\u4F5C!\n\n" +
                    "\u5982\u679C\u8BE5\u9519\u8BEF\u53D1\u751F\u4E8E\u5BFC\u51FA\u6216\u5408\u5E76\u9636\u6BB5,\n" +
                    "\u8BF7\u5C1D\u8BD5\u524D\u5F80\u504F\u597D\u8BBE\u7F6E\u7684\u6027\u80FD\u90E8\u5206,\n"+
                    "\u5C06\u6700\u5927\u7EBF\u7A0B\u6570\u91CF\u8C03\u6574\u4E3A" + (threadCount - 1) + ")");
            } else {
                jTextArea1.setText("\u6CA1\u6709\u8DB3\u591F\u7684\u5185\u5B58\u6765\u8FDB\u884C\u8BE5\u64CD\u4F5C!\n\n"
                    + "WorldPainter\u5DF2\u7ECF\u4F7F\u7528\u4E86\u63A8\u8350\u7684\u6700\u9AD8\u5185\u5B58\n"
                    + "\u4E0D\u63A8\u8350\u4F60\u518D\u5206\u914D\u66F4\u591A\u5185\u5B58\n"
                    + "\u8981\u8FDB\u884C\u8BE5\u64CD\u4F5C\uFF0C\u4F60\u9700\u8981\u5B89\u88C5\u66F4\u591A\u5185\u5B58(\u5E76\u91CD\u88C5WorldPainter)");
            }
            jButton1.setEnabled(false);
            jButton1.setToolTipText("\u6CA1\u5FC5\u8981\u53D1\u9001\u5185\u5B58\u6EA2\u51FA\u6570\u636E\u7684\u8BE6\u7EC6\u4FE1\u606F");
            jButton3.setEnabled(false);
        } else {
            String message = (ioExceptionMessage != null) ? ioExceptionMessage : rootCause.getMessage();
            if ((message != null) && (message.length() > 250)) {
                message = message.substring(0, 247) + "...";
            }
            final String requestedActionLine;
            if ((Main.privateContext != null) && (! forceEmail)) {
                // We can submit the error
                final Configuration config = Configuration.getInstance();
                if ((config != null) && TRUE.equals(config.getPingAllowed()) && (! ioException)) {
                    // Automatic submission is allowed; submit it automatically
                    mode = Mode.SEND_AUTOMATICALLY;
                    requestedActionLine = "\u6B64\u9519\u8BEF\u7684\u8BE6\u7EC6\u4FE1\u606F\u5C06\u81EA\u52A8\u63D0\u4EA4\u7ED9\u6B64\u7A0B\u5E8F\u7684\u521B\u5EFA\u8005.";
                } else {
                    mode = Mode.SEND_MANUALLY;
                    if (ioException) {
                        requestedActionLine = "\u5982\u679C\u60A8\u8BA4\u4E3A\u8FD9\u662F\u4E00\u4E2A\u6F0F\u6D1E\uFF0C\u8BF7\u4F7F\u7528\u4E0B\u9762\u7684\u201C\u53D1\u9001\u62A5\u544A\u201D\u6309\u94AE\u5C06\u6B64\u9519\u8BEF\u7684\u8BE6\u7EC6\u4FE1\u606F\u53D1\u9001\u7ED9\u6B64\u7A0B\u5E8F\u7684\u521B\u5EFA\u8005.";
                    } else {
                        requestedActionLine = "\u8BF7\u4F7F\u7528\u4E0B\u9762\u7684\u201C\u53D1\u9001\u62A5\u544A\u201D\u6309\u94AE\u5C06\u6B64\u9519\u8BEF\u7684\u8BE6\u7EC6\u4FE1\u606F\u53D1\u9001\u7ED9\u6B64\u7A0B\u5E8F\u7684\u521B\u5EFA\u8005\uFF0C\u4EE5\u5E2E\u52A9\u8C03\u8BD5\u95EE\u9898.";
                    }
                }
            } else {
                jButton1.setText("\u90AE\u4EF6\u53D1\u9001\u8BE6\u7EC6\u4FE1\u606F...");
                if ((! Desktop.isDesktopSupported()) || (! Desktop.getDesktop().isSupported(Desktop.Action.MAIL))) {
                    jButton1.setToolTipText("\u8BE5\u7CFB\u7EDF\u4E0D\u652F\u6301\u53D1\u9001\u90AE\u4EF6; \u8BF7\u70B9\u51FB \"\u590D\u5236\u5230\u526A\u8D34\u677F\" \u6309\u94AE\u5E76\u5C06\u8BE6\u7EC6\u4FE1\u606F\u53D1\u9001\u5230 worldpainter@pepsoft.org.");
                } else {
                    jButton1.setEnabled(true);
                }
                mode = Mode.REPORTING_DISABLED;
                if (ioException) {
                    requestedActionLine = "\u5982\u679C\u60A8\u8BA4\u4E3A\u8FD9\u662F\u4E00\u4E2A\u6F0F\u6D1E\uFF0C\u8BF7\u4F7F\u7528\u4E0B\u9762\u7684\u201C\u53D1\u9001\u62A5\u544A\u201D\u6309\u94AE\u5C06\u6B64\u9519\u8BEF\u7684\u8BE6\u7EC6\u4FE1\u606F\u53D1\u9001\u7ED9\u6B64\u7A0B\u5E8F\u7684\u521B\u5EFA\u8005.";
                } else {
                    requestedActionLine = "\u8BF7\u4F7F\u7528\u4E0B\u9762\u7684\u201C\u53D1\u9001\u62A5\u544A\u201D\u6309\u94AE\u5C06\u6B64\u9519\u8BEF\u7684\u8BE6\u7EC6\u4FE1\u606F\u53D1\u9001\u7ED9\u6B64\u7A0B\u5E8F\u7684\u521B\u5EFA\u8005\uFF0C\u4EE5\u5E2E\u52A9\u8C03\u8BD5\u95EE\u9898.";
                }
            }
            final String text;
            if (ioException) {
                text = "\u53D1\u751F\u4E86\u4E00\u4E2A\u8BFB\u5199\u95EE\u9898.\n\n"
                    + "\u9519\u8BEF\u4FE1\u606F: " + message + "\n\n"
                    + requestedActionLine;
            } else {
                text = "\u53D1\u751F\u4E86\u4E00\u4E2A\u672A\u77E5\u7684\u9519\u8BEF.\n\n"
                    + "\u9519\u8BEF\u7C7B\u578B: " + rootCause.getClass().getName() + "\n"
                    + "\u9519\u8BEF\u6D88\u606F: " + message + "\n\n"
                    + requestedActionLine + "\n\n"
                    + "\u7A0B\u5E8F\u73B0\u5728\u53EF\u80FD\u5904\u4E8E\u4E0D\u7A33\u5B9A\u72B6\u6001\u3002\u5EFA\u8BAE\u5C3D\u5FEB\u91CD\u65B0\u542F\u52A8.";
            }
            jTextArea1.setText(text);
        }
        pack();

        StringBuilder sb = new StringBuilder();
        String eol = System.getProperty("line.separator");
        sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage()).append(eol);
        StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, 10); i++) {
            sb.append("\t\u4F4D\u4E8E " + stackTrace[i].getClassName() + '.' + stackTrace[i].getMethodName() + '(' + stackTrace[i].getFileName() + ':' + stackTrace[i].getLineNumber() + ')' + eol);
        }
        sb.append(eol);
        if (rootCause != exception) {
            sb.append("\u6839\u6E90\u539F\u56E0:" + eol);
            sb.append(rootCause.getClass().getName() + ": " + rootCause.getMessage() + eol);
            stackTrace = rootCause.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
                sb.append("\t\u4F4D\u4E8E " + stackTrace[i].getClassName() + '.' + stackTrace[i].getMethodName() + '(' + stackTrace[i].getFileName() + ':' + stackTrace[i].getLineNumber() + ')' + eol);
            }
            sb.append(eol);
        }

        if (! mdcContextMap.isEmpty()) {
            sb.append("\u8BCA\u65AD\u4E0A\u4E0B\u6587:" + eol);
            mdcContextMap.forEach((key, value) -> sb.append("\t" + key + ": " + value + eol));
            sb.append(eol);

            mdcContextMap.forEach((key, value) -> event.setAttribute(new AttributeKeyVO<>(ATTRIBUTE_KEY_MDC_ENTRY + '.' + key), value));
        }

        sb.append("WorldPainter \u7248\u672C: " + Version.VERSION + " (" + Version.BUILD + ")" + eol);
        event.setAttribute(ATTRIBUTE_KEY_VERSION, Version.VERSION);
        event.setAttribute(ATTRIBUTE_KEY_BUILD, Version.BUILD);
        sb.append(eol);
        for (String propertyName: SYSTEM_PROPERTIES) {
            sb.append(propertyName + ": " + System.getProperty(propertyName) + eol);
            event.setAttribute(new AttributeKeyVO<>(ATTRIBUTE_KEY_SYSTEM_PROPERTY + '.' + propertyName), System.getProperty(propertyName));
        }
        sb.append(eol);
        Runtime runtime = Runtime.getRuntime();
        sb.append("\u53EF\u7528\u5185\u5B58: " + runtime.freeMemory() + " \u5B57\u8282" + eol);
        sb.append("\u603B\u5185\u5B58: " + runtime.totalMemory() + " \u5B57\u8282" + eol);
        sb.append("\u6700\u5927\u5185\u5B58: " + runtime.maxMemory() + " \u5B57\u8282" + eol);
        event.setAttribute(ATTRIBUTE_KEY_FREE_MEMORY, runtime.freeMemory());
        event.setAttribute(ATTRIBUTE_KEY_TOTAL_MEMORY, runtime.totalMemory());
        event.setAttribute(ATTRIBUTE_KEY_MAX_MEMORY, runtime.maxMemory());

        // The app may be in an unstable state, so if an exception occurs while
        // interrogating it, swallow it to prevent endless loops
        try {
            App app = App.getInstanceIfExists();
            World2 world = (app != null) ? app.getWorld() : null;
            Dimension dimension = (app != null) ? app.getDimension() : null;
            if ((world != null) && (dimension != null)) {
                sb.append(eol);
                sb.append("\u4E16\u754C\u540D: " + world.getName() + eol);
                sb.append("\u5E73\u53F0: " + world.getPlatform().displayName + " (" + world.getPlatform().id + ')' + eol);
                sb.append("\u79CD\u5B50: " + dimension.getSeed() + eol);
                sb.append("\u754C\u9650: " + dimension.getLowestX() + ", " + dimension.getLowestY() + " => " + dimension.getHighestX() + ", " + dimension.getHighestY() + eol);
                sb.append("\u9AD8\u5EA6: " + world.getMaxHeight() + eol);
                sb.append("\u5206\u533A\u6570\u91CF: " + dimension.getTileCount() + eol);
                Set<Layer> layers = dimension.getAllLayers(false);
                sb.append("\u4F7F\u7528\u4E2D\u7684\u8986\u76D6\u5C42: ");
                boolean first = true;
                for (Layer layer : layers) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(layer.getName());
                }
                sb.append(eol);
                sb.append("\u8FB9\u754C: " + dimension.getBorder() + " @ " + dimension.getBorderLevel() + eol);
                sb.append("\u5730\u4E0B\u6750\u8D28: " + dimension.getSubsurfaceMaterial() + eol);
                TileFactory tileFactory = dimension.getTileFactory();
                if (tileFactory instanceof HeightMapTileFactory) {
                    HeightMapTileFactory heightMapTileFactory = (HeightMapTileFactory) tileFactory;
                    sb.append("\u6C34\u5E73\u9762\u9AD8\u5EA6: " + heightMapTileFactory.getWaterHeight() + eol);
                }
                if (world.getImportedFrom() != null) {
                    sb.append("\u4E16\u754C\u5BFC\u5165\u4E8E " + world.getImportedFrom() + eol);
                }
                if (!world.isAllowMerging()) {
                    sb.append("\u4E16\u754C\u4F7F\u7528\u65E7\u5750\u6807\u7CFB\u7EDF\u521B\u5EFA" + eol);
                }
            }
            if (app != null) {
                sb.append(eol);
                sb.append("\u64CD\u4F5C: " + app.getActiveOperation() + eol);
                sb.append("\u534A\u5F84: " + app.getRadius() + eol);
                //        sb.append("Brush shape: " + app.getBrushShape() + "/" + app.getToolBrushShape() + eol);
                sb.append("\u7B14\u5237: " + app.getBrush() + "/" + app.getToolBrush() + eol);
                sb.append("\u9AD8\u5EA6: " + app.getLevel() + "/" + app.getToolLevel() + eol);
                sb.append("\u7F29\u653E: " + app.getZoom() + eol);
                sb.append("\u9690\u85CF\u7684\u8986\u76D6\u5C42: " + app.getHiddenLayers());
            }
        } catch (Throwable t) {
            logger.error("Secondary exception occurred while interrogating app for exception report", t);
        }

        body = sb.toString();

        if (! "true".equals(System.getProperty("org.pepsoft.worldpainter.devMode"))) {
            logger.error(body);
        }

        if (mode == Mode.SEND_AUTOMATICALLY) {
            submitInBackground();
        }
    }

    private void init(Window parent) {
        initComponents();

        getRootPane().setDefaultButton(jButton2);

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("\u53D6\u6D88") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        scaleToUI(this);
        setLocationRelativeTo(parent);
    }

    private void close() {
        dispose();
    }

    private void email() {
        try {
            URI uri = new URI("mailto", "worldpainter@pepsoft.org?subject=WorldPainter error report&body=" + body, null);
            Desktop desktop = Desktop.getDesktop();
            desktop.mail(uri);
            showInfo(this, "\u73B0\u5728\u5E94\u8BE5\u5DF2\u7ECF\u6253\u5F00\u4E86\u4E00\u5C01\u65B0\u7684\u7535\u5B50\u90AE\u4EF6\u4F9B\u60A8\u53D1\u9001.\n\u5982\u679C\u5B83\u4E0D\u8D77\u4F5C\u7528\uFF0C\u8BF7\u4F7F\u7528\u201C\u590D\u5236\u5230\u526A\u8D34\u677F\u201D\u6309\u94AE\u5E76\u624B\u52A8\u5C06\u4FE1\u606F\u90AE\u5BC4\u5230 worldpainter@pepsoft.org.", "\u5DF2\u521B\u5EFA\u90AE\u4EF6");
        } catch (URISyntaxException e) {
            logger.error("URI syntax error while trying to send email", e);
            JOptionPane.showMessageDialog(this, "\u65E0\u6CD5\u521B\u5EFA\u5E26\u6709\u62A5\u9519\u4FE1\u606F\u7684\u90AE\u4EF6!\n\u8BF7\u4F7F\u7528 \"\u590D\u5236\u5230\u526A\u8D34\u677F\" \u6309\u94AE\u5E76\u5C06\u4FE1\u606F\u53D1\u9001\u5230 worldpainter@pepsoft.org.", "\u65E0\u6CD5\u521B\u5EFA\u90AE\u4EF6", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            logger.error("I/O error while trying to send email", e);
            JOptionPane.showMessageDialog(this, "\u65E0\u6CD5\u521B\u5EFA\u5E26\u6709\u62A5\u9519\u4FE1\u606F\u7684\u90AE\u4EF6!\n\u8BF7\u4F7F\u7528 \"\u590D\u5236\u5230\u526A\u8D34\u677F\" \u6309\u94AE\u5E76\u5C06\u4FE1\u606F\u53D1\u9001\u5230 worldpainter@pepsoft.org.", "\u65E0\u6CD5\u521B\u5EFA\u90AE\u4EF6", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyToClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection data = new StringSelection(body);
        clipboard.setContents(data, data);
        showInfo(this, "\u4FE1\u606F\u5DF2\u590D\u5236\u5230\u526A\u8D34\u677F. \u8BF7\u5C06\u5176\u7C98\u8D34\u5230\u90AE\u4EF6\u4E2D\u5E76\u53D1\u9001\u5230 worldpainter@pepsoft.org.", "\u4FE1\u606F\u5DF2\u590D\u5236");
    }

    private void submitInBackground() {
        jButton1.setText("\u53D1\u9001...");
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        new Thread("Exception Submitter") {
            @Override
            public void run() {
                try {
                    UsageVO usageVO = new UsageVO();
                    usageVO.setEvents(singletonList(event));
                    final Configuration config = Configuration.getInstance();
                    usageVO.setLaunchCount(config.getLaunchCount());
                    usageVO.setInstall(config.getUuid());
                    usageVO.setWPVersion(Version.VERSION);
                    Main.privateContext.submitUsageData(usageVO, true);
                    doOnEventThread(() -> {
                        jButton1.setText("\u62A5\u544A\u5DF2\u53D1\u9001");
                        jButton2.setEnabled(true);
                    });
                } catch (RuntimeException e) {
                    logger.error("{} while trying to submit exception report to server (message: {})", e.getClass().getSimpleName(), e.getMessage(), e);
                    doOnEventThread(() -> {
                        JOptionPane.showMessageDialog(ErrorDialog.this, "\u9519\u8BEF\u62A5\u544A\u53D1\u9001\u5931\u8D25.\n\u8BF7\u70B9\u51FB\u4E0B\u65B9\u7684 \"\u90AE\u5BC4\u8BE6\u7EC6\u5185\u5BB9...\" \u6309\u94AE\u6765\u53D1\u9001\u62A5\u544A.");
                        jButton1.setText("\u90AE\u5BC4\u8BE6\u7EC6\u5185\u5BB9...");
                        if ((! Desktop.isDesktopSupported()) || (! Desktop.getDesktop().isSupported(Desktop.Action.MAIL))) {
                            jButton1.setToolTipText("\u8BE5\u7CFB\u7EDF\u4E0D\u652F\u6301\u53D1\u9001\u90AE\u4EF6; \u8BF7\u70B9\u51FB \"\u590D\u5236\u5230\u526A\u8D34\u677F\" \u6309\u94AE\u5E76\u5C06\u8BE6\u7EC6\u4FE1\u606F\u53D1\u9001\u5230 worldpainter@pepsoft.org.");
                        } else {
                            jButton1.setEnabled(true);
                        }
                        mode = Mode.REPORTING_DISABLED;
                        jButton2.setEnabled(true);
                    });
                }
            }
        }.start();
    }

    private static boolean forceEmail(Throwable exception) {
        final String message = exception.getMessage();
        return (message != null) && (message.contains("terrainRanges contains null value")
                || message.contains("terrainRanges may not contain null values")
                || message.contains("aValue (rowIndex: ")
                || message.contains("Index -4 out of bounds for length 20")
                || ((exception instanceof IllegalArgumentException) && message.equals("Comparison method violates its general contract!")) // Strange Java GUI bug in JFileChooser
                || ((exception instanceof ImagingOpException) && message.equals("Unable to transform src image")) // Observed when rotating icon image
        );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextArea1 = new javax.swing.JTextArea();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("\u610F\u5916\u9519\u8BEF");

        jButton1.setText("\u53D1\u9001\u62A5\u544A");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("\u5173\u95ED");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setFont(getFont());
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(10);
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);

        jButton3.setText("\u5C06\u7EC6\u8282\u590D\u5236\u81F3\u526A\u8D34\u677F");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextArea1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextArea1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        switch (mode) {
            case REPORTING_DISABLED:
                email();
                break;
            case SEND_MANUALLY:
                submitInBackground();
                break;
            default:
                throw new IllegalStateException("mode " + mode);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        close();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        copyToClipboard();
    }//GEN-LAST:event_jButton3ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    private String body;
    private EventVO event;
    private Mode mode = Mode.REPORTING_UNNECESSARY;

    private static final String[] SYSTEM_PROPERTIES = {
        "java.version",
        "java.vendor",
        "java.vm.version",
        "java.vm.vendor",
        "java.vm.name",
        "os.name",
        "os.arch",
        "os.version",
        "user.home",
        "user.dir",
        "user.country",
        "user.language",
    };

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ErrorDialog.class);
    private static final long serialVersionUID = 1L;

    enum Mode { REPORTING_UNNECESSARY, SEND_AUTOMATICALLY, SEND_MANUALLY, REPORTING_DISABLED }
}