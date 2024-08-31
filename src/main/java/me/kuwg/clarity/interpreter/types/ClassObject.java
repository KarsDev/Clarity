package me.kuwg.clarity.interpreter.types;

import me.kuwg.clarity.interpreter.context.Context;

public class ClassObject {

    private final String name;
    private final ClassObject inherited;
    private final Context context;

    public ClassObject(final String name, final ClassObject inherited, final Context context) {
        this.name = name;
        this.inherited = inherited;
        this.context = context;
    }

    public final String getName() {
        return name;
    }

    public final ClassObject getInherited() {
        return inherited;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "ClassObject@" + name;
    }

    public boolean isInstance(final String value) {
        if (name.equals(value)) return true;
        ClassObject inherited = this.inherited;
        while (true) {
            if (inherited == null) return false;
            if (inherited.getName().equals(value)) return true;
            inherited = inherited.getInherited();
        }
    }
}
