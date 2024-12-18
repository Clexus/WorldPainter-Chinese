/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.bo2.Bo2LayerExporter;
import org.pepsoft.worldpainter.layers.bo2.Bo2ObjectProvider;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author pepijn
 */
public class Bo2Layer extends CustomLayer {
    public Bo2Layer(Bo2ObjectProvider objectProvider, String description, Object paint) {
        super(objectProvider.getName(), description, DataSize.NIBBLE, 50, paint);
        this.objectProvider = objectProvider;
    }

    public Bo2ObjectProvider getObjectProvider() {
        return objectProvider;
    }

    public void setObjectProvider(Bo2ObjectProvider objectProvider) {
        this.objectProvider = objectProvider;
        setName(objectProvider.getName());
        setDescription("\u81EA\u5B9A\u4E49 " + objectProvider.getName() + " \u5BF9\u8C61");

        // Legacy
        files = Collections.emptyList();
    }

    public List<File> getFiles() {
        return files;
    }

    @Override
    public Class<? extends LayerExporter> getExporterType() {
        return Bo2LayerExporter.class;
    }

    @Override
    public Bo2LayerExporter getExporter(Dimension dimension, Platform platform, ExporterSettings settings) {
        return new Bo2LayerExporter(dimension, platform, this);
    }

    public int getDensity() {
        return density;
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public int getRandomDisplacement() {
        return randomDisplacement;
    }

    public void setRandomDisplacement(int randomDisplacement) {
        this.randomDisplacement = randomDisplacement;
    }

    @Override
    public String getType() {
        return "\u81EA\u5B9A\u4E49\u5BF9\u8C61";
    }

    // Cloneable

    @Override
    public Bo2Layer clone() {
        Bo2Layer clone = (Bo2Layer) super.clone();
        clone.objectProvider = objectProvider.clone();
        return clone;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Legacy support
        if (colour != 0) {
            setPaint(new Color(colour));
            colour = 0;
        }
        if (density == 0) {
            density = 20;
        }
        if (gridX == 0) {
            gridX = 1;
            gridY = 1;
        }
    }
    
    private Bo2ObjectProvider objectProvider;
    @Deprecated
    private int colour;
    @Deprecated
    private List<File> files = Collections.emptyList();
    private int density = 20;
    private int gridX = 1, gridY = 1, randomDisplacement = 0;

    private static final long serialVersionUID = 1L;
}