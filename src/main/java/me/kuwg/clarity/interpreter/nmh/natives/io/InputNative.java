package me.kuwg.clarity.interpreter.nmh.natives.io;

import me.kuwg.clarity.interpreter.nmh.natives.NativeMethod;

import java.util.List;
import java.util.Scanner;

public class InputNative extends NativeMethod<String> {
    public InputNative() {
        super("input");
    }

    @Override
    public String call(List<Object> params) {
        if (!params.isEmpty()) {
            System.out.print(params.get(0));
        }
        Scanner scanner = new Scanner(System.in);
        String nextLine = scanner.nextLine();
        scanner.close();
        return nextLine;
    }

    @Override
    protected boolean applies0(List<Object> params) {
        return true;
    }
}
