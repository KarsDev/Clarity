package me.kuwg.clarity.installer;

import java.io.*;

public class WindowsClarityInstaller extends ClarityInstaller {
    protected WindowsClarityInstaller(final String path) throws Exception {
        super(path);
    }

    @Override
    public void install() throws IOException, InterruptedException {
        createBatchFiles();
        setUserPath();
        setLanguageLogo();
        exe("del " + new File(path, "installer.bat"));
    }


    private void createBatchFiles() throws IOException {
        cloneResource("windows/clarity.bat", "clarity.bat");
        cloneResource("windows/clr.bat", "clarity.bat");
        cloneResource("windows/installer.bat", "installer.bat");
    }

    private void setUserPath() throws IOException, InterruptedException {
        final String newPath = new File(path).getAbsolutePath();

        // Get current PATH
        final String currentPath = System.getenv("PATH");
        if (currentPath.contains(newPath)) {
            System.out.println("The PATH environment variable already contains the specified directory.");
            return;
        }

        final String updatedPath = currentPath + File.pathSeparator + newPath;

        final String command = "setx PATH \"" + updatedPath + "\"";

        if (exe(command) != 0) {
            throw new IOException("Failed to update PATH environment variable.");
        }

        System.out.println("PATH environment variable updated. Please restart your command prompt for changes to take effect.");
    }

    public void setLanguageLogo() throws IOException, InterruptedException {
        cloneResource("windows/logo.ico", "logo.ico");
        System.out.println("Setting image logo and opener...");
        exe(new File(path, "installer.bat").getAbsolutePath());
    }

}