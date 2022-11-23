package tui.crossterm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;

enum NativeLoader {
    instance;
    ;

    void load(String nativeLibrary) throws Exception {
        try {
            System.loadLibrary(nativeLibrary);
        } catch (UnsatisfiedLinkError e) {
            loadPackaged(nativeLibrary);
        }
    }

    void loadPackaged(String nativeLibrary) throws Exception {
        String lib = System.mapLibraryName(nativeLibrary);
        String plat = getPlatform();
        String resourcePath = "/native/" + plat + "/" + lib;

        var resourceStream = getClass().getResourceAsStream(resourcePath);
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

    private static String getPlatform() throws IOException, InterruptedException {
        var process = new ProcessBuilder(asList("uname", "-sm")).start();
        var ret = process.waitFor();
        if (ret != 0) {
            throw new RuntimeException("Error running `uname` command: " + ret);
        }
        String uname = new String(process.getInputStream().readAllBytes());
        String line = uname.split("\n")[0];
        String[] parts = line.split(" ");
        if (parts.length != 2) {
            throw new RuntimeException("Could not determine platform: 'uname -sm' returned unexpected string: " + line);
        }
        String arch = parts[1].toLowerCase().replaceAll("\\s", "");
        String kernel = parts[0].toLowerCase().replaceAll("\\s", "");
        return arch + "-" + kernel;
    }
}