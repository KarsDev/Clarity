package me.kuwg.clarity.installer;

public enum OS {
    WINDOWS,
    MAC,
    LINUX,
    OTHER;

    public static final String CURRENT_OPERATING_SYSTEM_NAME;

    public static final OS CURRENT_OS;

    static {
        CURRENT_OPERATING_SYSTEM_NAME = System.getProperty("os.name");

        final OS result;
        final String osName = CURRENT_OPERATING_SYSTEM_NAME.toLowerCase();

        if (osName.contains("win")) {
            result = OS.WINDOWS;
        } else if (osName.contains("mac")) {
            result = OS.MAC;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            result = OS.LINUX;
        } else {
            result = OS.OTHER;
        }
        CURRENT_OS = result;
    }

}
