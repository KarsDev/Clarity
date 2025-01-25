package me.kuwg.clarity.nmh.natives.abstracts;

import java.util.Map;

import static me.kuwg.clarity.util.ConsoleColors.*;

public abstract class DefaultNativeFunction<R> extends NativeFunction<R> {
    protected DefaultNativeFunction(final String name) {
        super(name);
    }

    // Prints help to console
    public abstract void help();

    protected String formatHelp(final Map<String, String> params) {
        final StringBuilder help = new StringBuilder(WHITE + super.getName() + YELLOW + "(");

        int count = 0;
        final int size = params.size();

        for (final Map.Entry<String, String> entry : params.entrySet()) {
            final String param = entry.getKey();
            final String type = entry.getValue();
            help.append(GREEN).append(param).append(RED).append(":").append(CYAN).append(type);

            count++;
            if (count < size) {
                help.append(", ");
            }
        }

        help.append(YELLOW + ")" + RESET);

        return help.toString();
    }
}
