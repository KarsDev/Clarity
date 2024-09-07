package me.kuwg.clarity.nmh.natives.aclass;

import me.kuwg.clarity.ClarityNativeFunction;

abstract class NativeFunction<R> extends ClarityNativeFunction<R> {
    protected NativeFunction(final String name) {
        super(name);
    }
}
