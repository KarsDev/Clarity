package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InputNative extends DefaultNativeFunction<String> {

    private static final Scanner scanner = new Scanner(System.in);

    public InputNative() {
        super("input");
    }

    @Override
    public String call(List<Object> params) {
        if (!params.isEmpty()) {
            System.out.print(params.get(0));
        }

        return scanner.nextLine();
    }

    @Override
    protected boolean applies0(List<Object> params) {
        return params.size() <= 1;
    }

    @Override
    protected void finalize()  {
        scanner.close();
    }

    @Override
    public void help() {
        final Map<String, String> map = new HashMap<>();

        map.put("[print]", "var");

        System.out.println(formatHelp(map));
    }
}
