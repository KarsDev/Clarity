package me.kuwg.clarity.nmh.natives.impl.pkg.date;

import java.time.LocalDate;
import java.util.List;

public class GetDayNative extends DateNativeFunction<Integer> {
    public GetDayNative() {
        super("getDay");
    }

    @Override
    public Integer call(final List<Object> params) {
        return LocalDate.now().getDayOfMonth();
    }
}
