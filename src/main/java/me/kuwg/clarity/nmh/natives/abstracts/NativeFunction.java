package me.kuwg.clarity.nmh.natives.abstracts;

import me.kuwg.clarity.library.natives.ClarityNativeFunction;

abstract class NativeFunction<R> extends ClarityNativeFunction<R> {
    protected NativeFunction(final String name) {
        super(name);
    }
}
