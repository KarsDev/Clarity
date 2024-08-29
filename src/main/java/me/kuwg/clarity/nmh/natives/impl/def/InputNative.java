package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.nmh.natives.aclass.DefaultNativeFunction;

import java.util.List;
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
}
