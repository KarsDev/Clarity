package me.kuwg.clarity.installer.sys;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class ClarityInstaller {

    protected final String path;

    protected ClarityInstaller(final String path) throws IOException, InterruptedException {
        this.path = path;
        cloneJar();
        install();
    }

    public abstract void install() throws IOException, InterruptedException;


    private void cloneJar() throws IOException {
        final String jarPath = new File(WindowsClarityInstaller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath().replaceAll("%20", " ");
        final File destFile = new File(path, "clarity.jar");

        try (InputStream in = Files.newInputStream(Paths.get(jarPath));
             OutputStream out = Files.newOutputStream(destFile.toPath())) {
            final byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    protected final void cloneResource(final String inputName, final String outputName) throws IOException {
        final String resPath = "installer/" + inputName;
        final File output = new File(path, outputName);
        try (final InputStream is = getClass().getClassLoader().getResourceAsStream(resPath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resPath);
            }

            Files.createDirectories(output.getParentFile().toPath());

            try (FileOutputStream os = new FileOutputStream(output)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    protected final int exe(final String cmd) throws IOException, InterruptedException {
        switch (OS.CURRENT_OS) {
            case WINDOWS: {
                return Runtime.getRuntime().exec("cmd /c " + cmd).waitFor();
            }
            case MAC:
            case LINUX: {
                return Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd}).waitFor();
            }
            default: {
                return throwOSException();
            }
        }
    }


    protected static <T> T throwOSException() {
        throw new UnsupportedOperationException("Unsupported OS in installing: " + OS.CURRENT_OPERATING_SYSTEM_NAME);
    }

    public static void install(final String dest) {
        try {
            switch (OS.CURRENT_OS) {
                case WINDOWS: {
                    System.out.println("Installing Windows version...");
                    new WindowsClarityInstaller(dest);
                    break;
                }
                case MAC: {
                    System.out.println("Installing MacOS version...");
                    new MacClarityInstaller(dest);
                    break;
                }
                case LINUX: {
                    System.out.println("Installing Linux version...");
                    new LinuxClarityInstaller(dest);
                    break;
                }
                case OTHER: {
                    throwOSException();
                    break;
                }
            }
        } catch (final Exception e) {
            System.err.println("An exception occurred while installing:");
            e.printStackTrace(System.err);
        }
    }
}
