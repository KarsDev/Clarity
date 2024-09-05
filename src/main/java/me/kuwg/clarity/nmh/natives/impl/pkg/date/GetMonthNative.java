package me.kuwg.clarity.nmh.natives.impl.pkg.date;

import java.time.LocalDate;
import java.util.List;

public class GetMonthNative extends DateNativeFunction<Integer> {
    public GetMonthNative() {
        super("getMonth");
    }

    @Override
    public Integer call(final List<Object> params) {
        return LocalDate.now().getMonthValue() - 1;
    }
}
