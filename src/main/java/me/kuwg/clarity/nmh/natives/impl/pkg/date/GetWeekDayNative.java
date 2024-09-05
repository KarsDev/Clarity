package me.kuwg.clarity.nmh.natives.impl.pkg.date;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class GetWeekDayNative extends DateNativeFunction<Integer> {
    public GetWeekDayNative() {
        super("getWeekDay");
    }

    @Override
    public Integer call(final List<Object> params) {
        return LocalDate.now().getDayOfWeek().ordinal();
    }
}
