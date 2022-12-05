package tui.crossterm;

public record KeyModifiers(int bits) {
    public static final int SHIFT = 0b0000_0001;
    public static final int CONTROL = 0b0000_0010;
    public static final int ALT = 0b0000_0100;
    public static final int SUPER = 0b0000_1000;
    public static final int HYPER = 0b0001_0000;
    public static final int META = 0b0010_0000;
    public static final int NONE = 0b0000_0000;

}
