package me.kuwg.clarity.installer.sys;

import me.kuwg.clarity.Clarity;

import java.io.*;

public final class LinuxClarityInstaller extends ClarityInstaller {
    LinuxClarityInstaller(final String path) throws Exception {
        super(path);
    }

    @Override
    public void install() throws IOException, InterruptedException {
        createShellScripts();
        setUserPath();
        setLanguageLogo();
        exe("rm " + new File(path, "installer.bat"));
    }

    private void createShellScripts() throws IOException, InterruptedException {
        System.out.println("Creating shell scripts...");
        cloneResource("linux/clarity.sh", "clarity");
        cloneResource("linux/clr.sh", "clarity");
        cloneResource("linux/installer.sh", "installer.sh");

        exe("chmod +x " + new File(path, "clarity").getAbsolutePath());
        exe("chmod +x " + new File(path, "installer.sh").getAbsolutePath());
    }

    // Updates the PATH environment variable for Linux
    private void setUserPath() throws IOException {

        System.out.println("Setting user path...");

        final String newPath = new File(path).getAbsolutePath();

        // Get current PATH from the user's shell configuration file
        final File bashProfile = new File(Clarity.USER_HOME, ".bash_profile");
        final File bashrc = new File(Clarity.USER_HOME, ".bashrc");
        final File zshrc = new File(Clarity.USER_HOME, ".zshrc");
        File shellConfig = bashProfile.exists() ? bashProfile : (bashrc.exists() ? bashrc : zshrc);

        if (!shellConfig.exists()) {
            shellConfig.createNewFile();
        }

        String currentPath = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(shellConfig))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("export PATH=")) {
                    currentPath = line.substring("export PATH=".length()).replace("\"", "");
                    break;
                }
            }
        }

        if (currentPath.contains(newPath)) {
            System.out.println("The PATH environment variable already contains the specified directory.");
            return;
        }

        final String updatedPath = currentPath + ":" + newPath;

        // Add the new path to the shell configuration file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(shellConfig, true))) {
            writer.write("export PATH=\"" + updatedPath + "\"");
            writer.newLine();
        }

        System.out.println("PATH environment variable updated. Please restart your terminal for changes to take effect.");
    }

    public void setLanguageLogo() throws IOException, InterruptedException {
        cloneResource("linux/logo.png", "logo.png");
        System.out.println("Setting image logo and opener...");
        exe(new File(path, "installer.sh").getAbsolutePath());
    }
}
