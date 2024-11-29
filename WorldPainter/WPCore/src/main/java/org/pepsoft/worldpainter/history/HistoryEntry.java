package org.pepsoft.worldpainter.history;

import org.pepsoft.worldpainter.Version;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Created by Pepijn Schmitz on 07-07-15.
 */
public class HistoryEntry implements Serializable {
    public HistoryEntry(int key, Serializable... args) {
        this.key = key;
        timestamp = System.currentTimeMillis();
        this.args = (args.length > 0) ? args : null;
    }

    public String getText() {
        switch (key) {
            case WORLD_LEGACY_PRE_0_2:
                return "\u4E16\u754C\u521B\u5EFA\u4E8E WorldPainter 0.2 \u4E4B\u524D";
            case WORLD_LEGACY_PRE_2_0_0:
                return "\u4E16\u754C\u521B\u5EFA\u4E8E WorldPainter 2.0.0 \u4E4B\u524D";
            case WORLD_CREATED:
                return MessageFormat.format("\u4E16\u754C\u7531 WorldPainter {0} \u521B\u5EFA", wpVersion);
            case WORLD_IMPORTED_FROM_MINECRAFT_MAP:
                return MessageFormat.format("\u4F4D\u4E8E{1}\u7684\u4E16\u754C{0}\u7531 WorldPainter {2} \u5BFC\u5165", args[0], args[1], wpVersion);
            case WORLD_IMPORTED_FROM_HEIGHT_MAP:
                return MessageFormat.format("\u6765\u6E90\u4E8E\u9AD8\u5EA6\u56FE\u7684\u4E16\u754C{0}\u7531 WorldPainter {1} \u5BFC\u5165", args[0], wpVersion);
            case WORLD_RECOVERED:
                return MessageFormat.format("\u5D29\u6E83\u7684\u4E16\u754C\u88AB WorldPainter {0} \u6062\u590D", wpVersion);
            case WORLD_LOADED:
                return MessageFormat.format("\u4ECE {0} \u4E2D\u52A0\u8F7D\u4E86\u4E16\u754C", args[0]);
            case WORLD_SAVED:
                return MessageFormat.format("\u4E16\u754C\u88AB\u4FDD\u5B58\u5230 {0}", args[0]);
            case WORLD_EXPORTED_FULL:
                return MessageFormat.format("\u4E16\u754C {0} \u88AB WorldPainter {2} \u5B8C\u6574\u5BFC\u51FA\u5230 {1}", args[0], args[1], wpVersion);
            case WORLD_EXPORTED_PARTIAL:
                return MessageFormat.format("\u4E16\u754C {0} \u88AB WorldPainter {2} \u90E8\u5206\u5BFC\u51FA\u5230 {1}", args[0], args[1], wpVersion);
            case WORLD_MERGED_FULL:
                return MessageFormat.format("\u4E16\u754C\u4E0E\u6765\u81EA\u4E8E {1} \u7684\u4E16\u754C {0} \u7531 WorldPainter {2} \u5B8C\u6574\u5408\u5E76", args[0], args[1], wpVersion);
            case WORLD_MERGED_PARTIAL:
                return MessageFormat.format("\u4E16\u754C\u4E0E\u6765\u81EA\u4E8E {1} \u7684\u4E16\u754C {0} \u7531 WorldPainter {2} \u90E8\u5206\u5408\u5E76", args[0], args[1], wpVersion);
            case WORLD_DIMENSION_ADDED:
                return MessageFormat.format("\u7EF4\u5EA6 {0} \u88AB\u6DFB\u52A0\u5230\u4E16\u754C", args[0]);
            case WORLD_DIMENSION_REMOVED:
                return MessageFormat.format("\u7EF4\u5EA6 {0} \u4ECE\u4E16\u754C\u4E2D\u79FB\u9664", args[0]);
            case WORLD_TILES_ADDED:
                return MessageFormat.format("{0} \u4E2A\u65B9\u5757\u5B9E\u4F53\u88AB\u6DFB\u52A0\u5230\u4E86\u7EF4\u5EA6 {1}", args[1], args[0]);
            case WORLD_TILES_REMOVED:
                return MessageFormat.format("\u4ECE\u7EF4\u5EA6 {1} \u4E2D\u79FB\u9664\u4E86 {0} \u4E2A\u5206\u533A", args[1], args[0]);
            case WORLD_DIMENSION_SHIFTED_HORIZONTALLY:
                return MessageFormat.format("\u7EF4\u5EA6 {0} \u5411\u4E1C\u79FB\u52A8\u4E86 {1} \u683C\uFF0C\u5411\u5357\u79FB\u52A8\u4E86 {2} \u683C", args[0], args[1], args[2]);
            case WORLD_DIMENSION_SHIFTED_VERTICALLY:
                return MessageFormat.format("\u7EF4\u5EA6 {0} \u5411\u4E0A\u79FB\u52A8\u4E86 {1} \u683C", args[0], args[1]);
            case WORLD_DIMENSION_ROTATED:
                return MessageFormat.format("\u7EF4\u5EA6 {0} \u65CB\u8F6C\u4E86 {1} \u5EA6", args[0], args[1]);
            case WORLD_MIN_HEIGHT_CHANGED:
                return MessageFormat.format("\u4E16\u754C\u6700\u4F4E\u9AD8\u5EA6\u5DF2\u53D8\u4E3A {0}", args[0]);
            case WORLD_MAX_HEIGHT_CHANGED:
                return MessageFormat.format("\u4E16\u754C\u6700\u9AD8\u9AD8\u5EA6\u5DF2\u53D8\u4E3A {0}", args[0]);
            case WORLD_HEIGHT_MAP_IMPORTED_TO_DIMENSION:
                return MessageFormat.format("\u9AD8\u5EA6\u56FE {1} \u5DF2\u88AB\u5BFC\u5165\u7EF4\u5EA6 {0}", args[0], args[1]);
            case WORLD_MASK_IMPORTED_TO_DIMENSION:
                return MessageFormat.format("\u906E\u7F69 {1} \u5DF2\u4EE5\u8986\u76d6\u5c42 {2} \u5BFC\u5165\u5230\u7EF4\u5EA6 {0}", args[0], args[1], args[2]);
            case WORLD_RECOVERED_FROM_AUTOSAVE:
                return MessageFormat.format("WorldPainter {0} \u5DF2\u5C06\u4E16\u754C\u4ECE\u81EA\u52A8\u4FDD\u5B58\u7684\u5185\u5BB9\u4E2D\u6062\u590D", wpVersion);
            case WORLD_RETARGETED:
                return MessageFormat.format("\u4E16\u754C\u5DF2\u4ECE\u5730\u56FE\u683C\u5F0F {0} \u91CD\u5B9A\u5411\u5230 {1}", args[0], args[1]);
            case WORLD_DIMENSION_SCALED:
                return MessageFormat.format("\u7EF4\u5EA6 {0} \u5DF2\u7F29\u653E {1}%", args[0], args[1]);
            default:
                return MessageFormat.format("WorldPainter {1} ({2}) \u4EA7\u751F\u4E86\u672A\u77E5\u7684\u4E8B\u4EF6ID {0} ", key, wpVersion, wpBuild);
        }
    }

    public final int key;
    public final long timestamp;
    public final String wpVersion = Version.VERSION, wpBuild = Version.BUILD, userId = System.getProperty("user.name");
    public final Serializable[] args;

    public static final int WORLD_LEGACY_PRE_0_2                   =  1;
    public static final int WORLD_LEGACY_PRE_2_0_0                 =  2;
    public static final int WORLD_CREATED                          =  3;
    public static final int WORLD_IMPORTED_FROM_MINECRAFT_MAP      =  4; // arg 0: level name as String, arg 1: directory as File
    public static final int WORLD_IMPORTED_FROM_HEIGHT_MAP         =  5; // arg 0: height map file as File
    public static final int WORLD_RECOVERED                        =  6;
    public static final int WORLD_LOADED                           =  7; // arg 0: file as File
    public static final int WORLD_SAVED                            =  8; // arg 0: file as File
    public static final int WORLD_EXPORTED_FULL                    =  9; // arg 0: level name as String, arg 1: directory as File
    public static final int WORLD_EXPORTED_PARTIAL                 = 10; // arg 0: level name as String, arg 1: directory as File, arg 2: name(s) of dimension(s) as String
    public static final int WORLD_MERGED_FULL                      = 11; // arg 0: level name as String, arg 1: directory as File
    public static final int WORLD_MERGED_PARTIAL                   = 12; // arg 0: level name as String, arg 1: directory as File, arg 2: name(s) of dimension(s) as String
    public static final int WORLD_DIMENSION_ADDED                  = 13; // arg 0: name of dimension as String
    public static final int WORLD_DIMENSION_REMOVED                = 14; // arg 0: name of dimension as String
    public static final int WORLD_TILES_ADDED                      = 15; // arg 0: name of dimension as String, arg 1: number of tiles added as Integer
    public static final int WORLD_TILES_REMOVED                    = 16; // arg 0: name of dimension as String, arg 1: number of tiles removed as Integer
    public static final int WORLD_DIMENSION_SHIFTED_HORIZONTALLY   = 17; // arg 0: name of dimension as String, arg 1: number of blocks shifted east as Integer, arg 2: number of blocks shifted south as Integer
    public static final int WORLD_DIMENSION_SHIFTED_VERTICALLY     = 18; // arg 0: name of dimension as String, arg 1: number of blocks shifted up as Integer
    public static final int WORLD_DIMENSION_ROTATED                = 19; // arg 0: name of dimension as String, arg 1: number of degrees rotated clockwise as Integer
    public static final int WORLD_MAX_HEIGHT_CHANGED               = 20; // arg 0: new maxHeight as Integer
    public static final int WORLD_HEIGHT_MAP_IMPORTED_TO_DIMENSION = 21; // arg 0: name of dimension as String, arg 1: height map file as File
    public static final int WORLD_MASK_IMPORTED_TO_DIMENSION       = 22; // arg 0: name of dimension as String, arg 1: mask file as File, arg 2: name of aspect to which the mask was applied
    public static final int WORLD_RECOVERED_FROM_AUTOSAVE          = 23;
    public static final int WORLD_MIN_HEIGHT_CHANGED               = 24; // arg 0: new minHeight as Integer
    public static final int WORLD_RETARGETED                       = 25; // arg 0: old platform as String, arg 1: new platform as String
    public static final int WORLD_DIMENSION_SCALED                 = 26; // arg 0: name of dimension as String, arg 1: scaling percentage as Integer

    private static final long serialVersionUID = 1L;
}