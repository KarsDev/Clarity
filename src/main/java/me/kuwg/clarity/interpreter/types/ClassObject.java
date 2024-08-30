package me.kuwg.clarity.interpreter.types;

import me.kuwg.clarity.interpreter.context.Context;

public class ClassObject {

    private final String name;
    private final Context context;

    public ClassObject(final String name, final Context context) {
        this.name = name;
        this.context = context;
    }

    public final String getName() {
        return name;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "ClassObject@" + name;
    }
}
