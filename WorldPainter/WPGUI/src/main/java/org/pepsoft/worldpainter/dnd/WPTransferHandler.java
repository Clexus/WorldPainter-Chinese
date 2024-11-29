package org.pepsoft.worldpainter.dnd;

import com.google.common.collect.ImmutableSet;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;

@SuppressWarnings("unchecked") // Guaranteed by java.awt.datatransfer package
public class WPTransferHandler extends TransferHandler {
    public WPTransferHandler(App app) {
        this.app = app;
    }

    public boolean canImport(TransferHandler.TransferSupport support) {
        if (! support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            return false;
        }
        if (! support.isDrop()) {
            return false;
        }
        if (! ((COPY & support.getSourceDropActions()) == COPY)) {
            return false;
        }
        support.setDropAction(COPY);
        return true;
    }

    public boolean importData(TransferHandler.TransferSupport support) {
        if (! canImport(support)) {
            return false;
        }

        try {
            final List<File> list = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (list.size() > 1) {
                beepAndShowError(app, "\u4E00\u6B21\u8BF7\u4EC5\u62D6\u5165\u4E00\u4E2A\u6587\u4EF6.", "\u6587\u4EF6\u6570\u91CF\u8FC7\u591A");
                return false;
            }
            final File file = list.get(0);
            final String name = file.getName();
            final int p = name.lastIndexOf('.');
            final String extension = (p != -1) ? name.substring(p + 1).toLowerCase() : null;
            if ((! "world".equals(extension)) && (! IMAGE_FILE_EXTENSIONS.contains(extension))) {
                beepAndShowError(app, "\u8BE5\u6587\u4EF6\u4E0D\u662FWorldPainter\u7684.world\u683C\u5F0F\u6587\u4EF6\u6216\u56FE\u7247\u6587\u4EF6.", "\u4E0D\u652F\u6301\u8BE5\u6587\u4EF6\u7C7B\u578B");
                return false;
            }
            try {
                if (IMAGE_FILE_EXTENSIONS.contains(extension)) {
                    final int action = JOptionPane.showOptionDialog(app,
                            "\u56FE\u7247\u6587\u4EF6\u5DF2\u88AB\u62D6\u5165 WorldPainter.\n\u8BF7\u9009\u62E9\u4F60\u7684\u5BFC\u5165\u65B9\u5F0F:",
                            "\u9009\u62E9\u6587\u4EF6\u5BFC\u5165\u65B9\u5F0F",
                            DEFAULT_OPTION,
                            QUESTION_MESSAGE,
                            null,
                            new String[] { "\u65B0\u4E16\u754C", "\u9AD8\u5EA6\u56FE", "\u906E\u7F69\u5C42", "\u53D6\u6D88" },
                            "\u53D6\u6D88");
                    switch (action) {
                        case 0:
                            app.importHeightMap(file);
                            break;
                        case 1:
                            app.importHeightMapIntoCurrentDimension(file);
                            break;
                        case 2:
                            app.importMask(file);
                            break;
                    }
                } else {
                    app.open(file, true);
                }
            } catch (RuntimeException e) {
                ExceptionHandler.handleException(e, app);
                return false;
            }
            return true;
        } catch (UnsupportedFlavorException | IOException e) {
            logger.error("{} while obtaining drag and drop transfer data", e.getClass().getSimpleName(), e);
            return false;
        }
    }

    private final App app;

    private static final Logger logger = LoggerFactory.getLogger(WPTransferHandler.class);

    private static final Set<String> IMAGE_FILE_EXTENSIONS = ImmutableSet.copyOf(ImageIO.getReaderFileSuffixes());
}