/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A WorldPainter dialog window. Provides the following features:
 *
 * <ul><li>Always application modal
 * <li>A {@link #cancel()} method which dismisses the dialog programmatically
 * <li>The dialog is also cancelled if the user presses the {@code Esc} key
 * <li>An {@link #isCancelled()} method which indicates whether the dialog has
 * been cancelled or closed in any other way besides invoking the {@link #ok()}
 * method
 * <li>An {@link #ok()} method which dismisses the dialog programmatically and
 * clears the {@code cancelled} property
 * <li>[SAFE MODE] automatically appended to title if WorldPainter is running in
 * safe mode</ul>
 *
 * @author pepijn
 */
public class WorldPainterDialog extends JDialog {
    public WorldPainterDialog(Window parent) {
        this(parent, true);
    }

    public WorldPainterDialog(Window parent, boolean enableHelpKey) {
        super(parent, ModalityType.APPLICATION_MODAL);

        if (parent instanceof App) {
            ((App) parent).reset3DViewAlwaysOnTop();
            ((App) parent).pauseAutosave();
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    ((App) parent).resumeAutosave();
                }
            });
        }

        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("\u53d6\u6d88") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }

            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

        if (enableHelpKey) {
            getRootPane().putClientProperty(App.KEY_HELP_KEY, "Dialog/" + getClass().getSimpleName());
            actionMap.put("help", new AbstractAction("\u5E2E\u52A9") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    App.getInstance().showHelp(WorldPainterDialog.this);
                }

                private static final long serialVersionUID = 1L;
            });
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "help");
        }
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setTitle(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(title);
        if (Version.isSnapshot()) {
            sb.append(" [\u5FEB\u7167]");
        }
        if (Configuration.getInstance().isSafeMode()) {
            sb.append(" [\u5B89\u5168\u6A21\u5F0F]");
        }
        super.setTitle(sb.toString());
    }

    protected void ok() {
        cancelled = false;
        dispose();
    }
    
    protected void cancel() {
        dispose();
    }

    /**
     * Goes through all child components and applies any additional UI scaling
     * that is necessary because it is not possible via the UIManager defaults.
     *
     * <p>Should be called by subclasses <em>after</em> they have added all
     * their components.
     */
    protected final void scaleToUI() {
        GUIUtils.scaleToUI(this);
    }

    /**
     * Adjusts the size of the window to the UI scale. The window will not be scaled down, nor be made larger than the
     * available space on the screen.
     *
     * <p>Should be called by subclasses <em>after</em> they have added and scaled all their components.
     */
    protected final void scaleWindowToUI() {
        GUIUtils.scaleWindow(this);
    }

    private boolean cancelled = true;
    
    private static final long serialVersionUID = 1L;
}