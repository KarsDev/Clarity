package me.kuwg.clarity.installer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClarityInstaller {

    private final String path;

    public ClarityInstaller(final String dest) throws Exception {
        this.path = dest;
        cloneJar();
        createBatchFiles();
        setUserPath();
        setLanguageLogo();
    }

    private void cloneJar() throws IOException {
        final String jarPath = new File(ClarityInstaller.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath().replaceAll("%20", " ");
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

    private void createBatchFiles() throws IOException {
        final File clarity = new File(path, "clarity.bat");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clarity))) {
            writer.write("@echo off\n");
            writer.write("java -jar \"%~dp0clarity.jar\" %*\n");
        }

        final File clr = new File(path, "clr.bat");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clr))) {
            writer.write("@echo off\n");
            writer.write("java -jar \"%~dp0clarity.jar\" %*\n");
        }

        final File setLogo = new File(path, "setlogo.bat");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(setLogo))) {
            writer.write("@echo off\n");
            writer.write(":: Check if the script is running as administrator\n");
            writer.write(":: If not, relaunch it with admin privileges\n\n");

            writer.write(":: Get the current script path\n");
            writer.write("set \"batchFile=%~f0\"\n\n");

            writer.write(":: Check for admin privileges\n");
            writer.write("net session >nul 2>&1\n");
            writer.write("if %errorLevel% neq 0 (\n");
            writer.write("    echo Requesting administrative privileges...\n");
            writer.write("    :: Relaunch the script with admin privileges\n");
            writer.write("    powershell -Command \"Start-Process cmd -ArgumentList '/c \\\"%batchFile%\\\"' -Verb RunAs\"\n");
            writer.write("    exit /b\n");
            writer.write(")\n\n");

            writer.write(":: Set opening app as clarity.bat");
            writer.write("assoc .clr=clrfile\n");
            writer.write("for /f \"tokens=2 delims==\" %%a in ('assoc .clr') do set FILETYPE=%%a\n");
            writer.write("echo setting %FYLETYPE%'s opening\n");
            writer.write("ftype %FILETYPE%=\"%USERPROFILE%\\Clarity\\clarity.bat\" interpret %*\n");

            writer.write(":: Define variables\n");
            writer.write("set \"ext=.clr\"\n");
            writer.write("set \"iconPath=%userprofile%\\Clarity\\logo.ico\"\n");
            writer.write("set \"fileType=ClrFileType\"\n\n");

            writer.write(":: Create a registry entry for the file extension\n");
            writer.write("reg add \"HKEY_CLASSES_ROOT\\%ext%\" /v \"\" /t REG_SZ /d \"%fileType%\" /f\n\n");

            writer.write(":: Create a registry entry for the file type\n");
            writer.write("reg add \"HKEY_CLASSES_ROOT\\%fileType%\" /v \"\" /t REG_SZ /d \"Clarity File Type\" /f\n\n");

            writer.write(":: Set the icon for the file type\n");
            writer.write("reg add \"HKEY_CLASSES_ROOT\\%fileType%\\DefaultIcon\" /v \"\" /t REG_SZ /d \"%iconPath%\" /f\n\n");

            writer.write(":: Refresh the icon cache (Restart Explorer)\n");
            writer.write("echo Restarting Explorer to apply changes...\n");
            writer.write("taskkill /f /im explorer.exe\n");
            writer.write("start explorer.exe\n\n");

            writer.write("echo Done!\n");
            writer.write("pause");
        }
    }

    private void setUserPath() throws IOException {
        final String newPath = new File(path).getAbsolutePath();

        // Get current PATH
        String currentPath = System.getenv("PATH");
        if (currentPath.contains(newPath)) {
            System.out.println("The PATH environment variable already contains the specified directory.");
            return;
        }

        final String updatedPath = currentPath + File.pathSeparator + newPath;

        final String command = "setx PATH \"" + updatedPath + "\"";

        try {
            if (exe(command) != 0) {
                throw new IOException("Failed to update PATH environment variable.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Error occurred while waiting for the process to complete.", e);
        }

        System.out.println("PATH environment variable updated. Please restart your command prompt for changes to take effect.");
    }

    public void setLanguageLogo() throws Exception {
        final String resPath = "installer/logo.ico";
        final File output = new File(path, "logo.ico");

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

        System.out.println("Setting image logo and opener...");
        final String logoBatFile = new File(path, "setlogo.bat").getAbsolutePath();
        exe(logoBatFile);
        exe("del " + logoBatFile);
    }

    private int exe(final String cmd) throws IOException, InterruptedException {
        return Runtime.getRuntime().exec("cmd /c" + cmd).waitFor();
    }

    public static void install(final String dest) {
        try {
            new ClarityInstaller(dest);
        } catch (final Exception e) {
            System.err.println("An exception occurred while installing:");
            e.printStackTrace(System.err);
        }
    }
}