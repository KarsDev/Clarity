package me.kuwg.clarity.interpreter.nmh.natives.io;

import me.kuwg.clarity.interpreter.nmh.natives.NativeMethod;

import java.util.List;
import java.util.Scanner;

public class InputNative extends NativeMethod<String> {

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

    protected void finalize() throws Throwable {
        scanner.close();
    }
}
