package tui.crossterm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;

class NativeLoader {
    public static void load(String nativeLibrary) throws Exception {
        try {
            System.loadLibrary(nativeLibrary);
        } catch (UnsatisfiedLinkError e) {
            loadPackaged(nativeLibrary);
        }
    }

    static String withPlatformName(String lib) throws IOException, InterruptedException {
        if (System.getProperty("org.graalvm.nativeimage.imagecode") != null)
            return "/" + lib;
        else {
            String plat = getPlatform();
            return "/native/" + plat + "/" + lib;
        }
    }

    static void loadPackaged(String nativeLibrary) throws Exception {
        String lib = System.mapLibraryName(nativeLibrary);
        var resourcePath = withPlatformName(lib);
        var resourceStream = NativeLoader.class.getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new UnsatisfiedLinkError(
                    "Native library " + lib + " (" + resourcePath + ") cannot be found on the classpath."
            );
        }

        Path tmp = Files.createTempDirectory("jni-");
        Path extractedPath = tmp.resolve(lib);

        try {
            Files.copy(resourceStream, extractedPath);
        } catch (Exception ex) {
            throw new UnsatisfiedLinkError("Error while extracting native library: " + ex.getMessage());
        }

        System.load(extractedPath.toAbsolutePath().toString());
    }

    private static String getPlatform() {
        if (System.getenv().containsKey("TUI_SCALA_PLATFORM")) {
            return System.getenv().get("TUI_SCALA_PLATFORM");
        }
        String arch = System.getProperty("os.arch");
        String name = System.getProperty("os.name");
        String nameLower = name.toLowerCase();
        boolean isAmd64 = arch.equals("x86_64") || arch.equals("amd64");
        boolean isArm64 = arch.equals("aarch64") || arch.equals("arm64");

        if (isAmd64 && nameLower.contains("win")) return "x86_64-windows";
        if (isAmd64 && nameLower.contains("lin")) return "x86_64-linux";
        if (isAmd64 && nameLower.contains("mac")) return "x86_64-darwin";
        if (isArm64 && nameLower.contains("mac")) return "arm64-darwin";
        throw new RuntimeException(
                "Platform detection does not understand os.name = " + name + " and os.arch = " + arch + ". " +
                        "You can set environment variable TUI_SCALA_PLATFORM to x86_64-windows, x86_64-linux, x86_64-darwin, arm64-darwin to override. " +
                        "Open an issue at https://github.com/oyvindberg/tui-scala/issues ."
        );
    }
}