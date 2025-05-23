/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.groundcover;

import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author pepijn
 */
public class GroundCoverLayer extends CustomLayer {
    public GroundCoverLayer(String name, MixedMaterial material, Object paint) {
        super(name, "\u9876\u90E8\u4E3A\u4E00\u5C42" + material.getName(), DataSize.BIT, 30, paint);
        mixedMaterial = material;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        setDescription(thickness + "\u683C\u9AD8\u7684" + name + "\u8986\u76D6\u5C42");
    }

    @Override
    public String getType() {
        return "\u5730\u8868\u8986\u76D6\u5C42";
    }

    public MixedMaterial getMaterial() {
        return mixedMaterial;
    }

    public void setMaterial(MixedMaterial material) {
        mixedMaterial = material;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        setDescription(thickness + "\u683C\u9AD8\u7684" + getName() + "\u8986\u76D6\u5C42");
    }

    public int getEdgeWidth() {
        return edgeWidth;
    }

    public void setEdgeWidth(final int edgeWidth) {
        this.edgeWidth = edgeWidth;
    }

    public EdgeShape getEdgeShape() {
        return edgeShape;
    }

    public void setEdgeShape(EdgeShape edgeShape) {
        if (edgeShape == null) {
            throw new NullPointerException();
        }
        this.edgeShape = edgeShape;
    }

    public NoiseSettings getNoiseSettings() {
        return noiseSettings;
    }

    public void setNoiseSettings(NoiseSettings noiseSettings) {
        this.noiseSettings = noiseSettings;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public LayerAnchor getLayerAnchor() {
        return layerAnchor;
    }

    public void setLayerAnchor(LayerAnchor layerAnchor) {
        this.layerAnchor = layerAnchor;
    }

    @Override
    public Class<? extends LayerExporter> getExporterType() {
        return GroundCoverLayerExporter.class;
    }

    @Override
    public GroundCoverLayerExporter getExporter(Dimension dimension, Platform platform, ExporterSettings settings) {
        return new GroundCoverLayerExporter(dimension, platform, this);
    }

    // Cloneable

    @Override
    public GroundCoverLayer clone() {
        GroundCoverLayer clone = (GroundCoverLayer) super.clone();
        clone.mixedMaterial = mixedMaterial.clone();
        MixedMaterialManager.getInstance().register(clone.mixedMaterial);
        if (noiseSettings != null) {
            clone.noiseSettings = noiseSettings.clone();
        }
        return clone;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // Legacy support
        if (colour != 0) {
            setPaint(new Color(colour));
            colour = 0;
        }
        if (thickness == 0) {
            thickness = 1;
        }
        if (mixedMaterial == null) {
            mixedMaterial = MixedMaterial.create(DefaultPlugin.JAVA_ANVIL, material);
            material = null;
        }
        if (edgeShape == null) {
            edgeWidth = 1;
            if (thickness == 1) {
                edgeShape = EdgeShape.SHEER;
            } else {
                edgeShape = EdgeShape.ROUNDED;
            }
        }
        if (layerAnchor == null) {
            layerAnchor = LayerAnchor.BEDROCK;
        }
    }
    
    @Deprecated
    private Material material;
    @Deprecated
    private int colour;
    private int thickness = 1;
    private int edgeWidth = 1;
    private MixedMaterial mixedMaterial;
    private EdgeShape edgeShape = EdgeShape.SHEER;
    private NoiseSettings noiseSettings;
    private boolean smooth;
    private LayerAnchor layerAnchor = LayerAnchor.TERRAIN;

    private static final long serialVersionUID = 1L;
    
    public enum EdgeShape {SHEER, LINEAR, SMOOTH, ROUNDED}
    
    /**
     * The anchor relative to which the layers of a layered material should be
     * placed. Only applies to layered materials.
     */
    public enum LayerAnchor {
        /**
         * Layers relative to height 0. This is the default and previously the
         * only option.
         */
        BEDROCK,
        
        /**
         * The bottom of the layers should be aligned with the height of the
         * terrain.
         */
        TERRAIN,
        
        /**
         * The top of the layers should be aligned with the top of the ground
         * cover layer.
         */
        TOP_OF_LAYER
    }
}