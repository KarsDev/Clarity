package me.kuwg.clarity.nmh.natives.impl.pkg.date;

import java.time.LocalDate;
import java.util.List;

public class GetYearNative extends DateNativeFunction<Integer> {
    public GetYearNative() {
        super("getYear");
    }

    @Override
    public Integer call(final List<Object> params) {
        return LocalDate.now().getYear();
    }
}
