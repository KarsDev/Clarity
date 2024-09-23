package me.kuwg.clarity.nmh.natives.abstracts;

import me.kuwg.clarity.library.cnf.ClarityNativeFunction;

abstract class NativeFunction<R> extends ClarityNativeFunction<R> {
    protected NativeFunction(final String name) {
        super(name);
    }
}
