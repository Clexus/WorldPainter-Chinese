/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter;

/**
 *
 * @author pepijn
 */
public enum Generator {
    DEFAULT("\u9ED8\u8BA4"), FLAT("\u8D85\u5E73\u5766"), LARGE_BIOMES("\u5927\u578B\u751F\u7269\u7FA4\u7CFB"), AMPLIFIED("\u653E\u5927\u5316"), BUFFET("\u81EA\u9009"), CUSTOM("\u81EA\u5B9A\u4E49"), CUSTOMIZED("\u81EA\u5B9A\u4E49"), UNKNOWN("\u672A\u77E5"), NETHER("\u4E0B\u754C"), END("\u672B\u5730");

    Generator(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private final String displayName;
}