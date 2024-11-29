package org.pepsoft.worldpainter.selection;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.WorldPainter;
import org.pepsoft.worldpainter.WorldPainterView;
import org.pepsoft.worldpainter.brushes.BrushShape;
import org.pepsoft.worldpainter.operations.MouseOrTabletOperation;
import org.pepsoft.worldpainter.operations.StandardOptionsPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

/**
 * Created by Pepijn Schmitz on 03-11-16.
 */
public class CopySelectionOperation extends MouseOrTabletOperation {
    public CopySelectionOperation(WorldPainterView view) {
        super("Copy Selection", "\u590D\u5236\u9009\u533A-\u5C06\u9009\u533A\u590D\u5236\u5230\u5176\u4ED6\u5730\u65B9", view, "operation.selection.copy", "copy_selection");
    }

    @Override
    public JPanel getOptionsPanel() {
        return optionsPanel;
    }

    @Override
    protected void activate() throws PropertyVetoException {
        final Dimension dimension = getDimension();
        if (dimension == null) {
            throw new PropertyVetoException("Dimension not set", null);
        }
        super.activate();
        selectionHelper = new SelectionHelper(dimension);
        WorldPainter view = (WorldPainter) getView();
        Rectangle bounds = selectionHelper.getSelectionBounds();
        if (bounds == null) {
            super.deactivate();
            throw new PropertyVetoException("No active selection", null);
        }
        bounds.translate(-bounds.x, -bounds.y);
        view.setCustomBrushShape(bounds);
        view.setBrushShape(BrushShape.CUSTOM);
        view.setDrawBrush(true);
    }

    @Override
    protected void deactivate() {
        getView().setDrawBrush(false);
        super.deactivate();
    }

    @Override
    protected void tick(int centreX, int centreY, boolean inverse, boolean first, float dynamicLevel) {
        selectionHelper.setOptions(options);
        final Dimension dimension = getDimension();
        if (dimension == null) {
            // Probably some kind of race condition
            return;
        }
        dimension.setEventsInhibited(true);
        try {
            // TODO: opening the progress dialog (and presumeably any dialog)
            // causes the JIDE docking framework to malfunction
//            ProgressDialog.executeTask(SwingUtilities.getWindowAncestor(getView()), new ProgressTask<Void>() {
//                @Override
//                public String getName() {
//                    return "Copying selection";
//                }
//
//                @Override
//                public Void execute(ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
                    selectionHelper.copySelection(centreX, centreY, null);
//                    return null;
//                }
//            }, false);
        } catch (ProgressReceiver.OperationCancelled e) {
            throw new InternalError(e);
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    private SelectionHelper selectionHelper;
    private final CopySelectionOperationOptions options = new CopySelectionOperationOptions();
    private final StandardOptionsPanel optionsPanel = new StandardOptionsPanel("\u590D\u5236\u9009\u533A", "<p>\u70B9\u51FB\u5C06\u9009\u533A\u590D\u5236\u5230\u6307\u5B9A\u533A\u57DF. \u5728\u4E0B\u65B9\u9009\u62E9\u4E16\u754C\u7684\u54EA\u4E9B\u5185\u5BB9\u9700\u8981\u88AB\u590D\u5236:") {
        @Override
        protected void addAdditionalComponents(GridBagConstraints constraints) {
            add(new CopySelectionOperationOptionsPanel(options), constraints);
        }
    };
}