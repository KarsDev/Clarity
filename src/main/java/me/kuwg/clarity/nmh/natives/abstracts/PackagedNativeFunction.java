package me.kuwg.clarity.nmh.natives.abstracts;

import me.kuwg.clarity.library.natives.ClarityPackagedNativeFunction;

public abstract class PackagedNativeFunction<R> extends ClarityPackagedNativeFunction<R> {
    protected PackagedNativeFunction(final String name) {
        super(name);
    }
}