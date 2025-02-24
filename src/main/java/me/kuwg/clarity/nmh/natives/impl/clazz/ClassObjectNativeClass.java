package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.nmh.natives.abstracts.NativeClass;

import java.util.List;

public class ClassObjectNativeClass extends NativeClass {
    public ClassObjectNativeClass() {
        super("ClassObject");
    }

    @Override
    public Object handleCall(final String name, final List<Object> params, final Context context) {
        System.out.println("Here");
        switch (name) {
            case "print": {
                return context.highest().getCurrentClassName();
            }
            case "getClassName": {
                return context.getCurrentClassName();
            }
        }
        throw new RuntimeException();
    }
}
