/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers;

import java.util.ResourceBundle;

/**
 *
 * @author pepijn
 */
public class GardenCategory extends Layer {
    public GardenCategory() {
        super("\u5206\u7C7B", "\u56ED\u827A\u5206\u7C7B", DataSize.NIBBLE, true, 80);
    }

    public static String getLabel(ResourceBundle resourceBundle, int category) {
        switch(category) {
            case CATEGORY_BUILDING:
                return resourceBundle.getString("structure.building");
            case CATEGORY_FIELD:
                return resourceBundle.getString("structure.field");
            case CATEGORY_ROAD:
                return resourceBundle.getString("structure.road");
            case CATEGORY_STREET_FURNITURE:
                return resourceBundle.getString("structure.street.furniture");
            case CATEGORY_WATER:
                return resourceBundle.getString("structure.water");
            case CATEGORY_TREE:
                return "\u6811";
            case CATEGORY_OBJECT:
                return "\u5BF9\u8C61";
            default:
                return "\u672A\u77E5";
        }
    }
    
    /**
     * Unoccupied land
     */
    public static final int CATEGORY_UNOCCUPIED = 0;
    
    /**
     * Roadway
     */
    public static final int CATEGORY_ROAD = 1;
    
    /**
     * A building
     */
    public static final int CATEGORY_BUILDING = 2;
    
    /**
     * A garden, park or field
     */
    public static final int CATEGORY_FIELD = 3;
    
    /**
     * Street furniture such as awnings, lighting, seats, fountains, wells, etc.
     */
    public static final int CATEGORY_STREET_FURNITURE = 4;
    
    /**
     * Water
     */
    public static final int CATEGORY_WATER = 5;
    
    /**
     * Tree
     */
    public static final int CATEGORY_TREE = 6;

    /**
     * A manually placed custom object
     */
    public static final int CATEGORY_OBJECT = 7;
    
    public static final GardenCategory INSTANCE = new GardenCategory();

    private static final long serialVersionUID = 1L;
}