package org.pepsoft.worldpainter.tools;

import org.pepsoft.worldpainter.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Pepijn Schmitz on 21-09-16.
 */
public class BiomeAlgorithmListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Integer) {
            switch ((Integer) value) {
                case Constants.BIOME_ALGORITHM_1_1:
                    setText("Minecraft 1.1");
                    break;
                case Constants.BIOME_ALGORITHM_1_2_AND_1_3_DEFAULT:
                    setText("Minecraft 1.6 \u9ED8\u8BA4 (\u6216 1.2 - 1.5)");
                    break;
                case Constants.BIOME_ALGORITHM_1_3_LARGE:
                    setText("Minecraft 1.6 \u5927\u578B\u751F\u7269\u7FA4\u7CFB (\u6216 1.3 - 1.5)");
                    break;
                case Constants.BIOME_ALGORITHM_1_7_DEFAULT:
                    setText("Minecraft 1.10 \u9ED8\u8BA4 (\u6216 1.7 - 1.9)");
                    break;
                case Constants.BIOME_ALGORITHM_1_7_LARGE:
                    setText("Minecraft 1.10 \u5927\u578B\u751F\u7269\u7FA4\u7CFB (\u6216 1.7 - 1.9)");
                    break;
            }
        }
        return this;
    }
}
