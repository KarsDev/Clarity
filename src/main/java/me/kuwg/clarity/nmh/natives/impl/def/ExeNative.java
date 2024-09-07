package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ExeNative extends DefaultNativeFunction<Object> {
    public ExeNative() {
        super("exe");
    }

    @Override
    public Object call(final List<Object> list) {
        return exe((String) list.get(0));
    }

    @Override
    protected boolean applies0(final List<Object> list) {
        return list.size() == 1 && list.get(0) instanceof String;
    }

    /**
     * Executes a given command and returns its output.
     *
     * @param command The command to be executed.
     * @return The output of the command.
     */
    public static String exe(final String command) {
        final StringBuilder output = new StringBuilder();

        final ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);

        try {
            final Process process = processBuilder.start();

            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            try (final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    output.append("ERROR: ").append(errorLine).append(System.lineSeparator());
                }
            }

            final int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command executed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("An error occurred: ").append(e.getMessage());
        }

        return output.toString();
    }

}
