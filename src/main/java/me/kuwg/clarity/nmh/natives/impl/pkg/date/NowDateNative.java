package me.kuwg.clarity.nmh.natives.impl.pkg.date;

import me.kuwg.clarity.interpreter.definition.EnumClassDefinition;
import me.kuwg.clarity.library.objects.types.ClassObject;
import me.kuwg.clarity.util.ClassUtil;

import java.time.LocalDate;
import java.util.List;

public class NowDateNative extends DateNativeFunction<ClassObject> {

    private static final String DATE_CLASS_NAME, DAY_ENUM_CLASS_NAME, MONTH_ENUM_CLASS_NAME, YEAR_CLASS_NAME;

    static {
        DATE_CLASS_NAME = "Date";
        DAY_ENUM_CLASS_NAME = "Day";
        MONTH_ENUM_CLASS_NAME = "Month";
        YEAR_CLASS_NAME = "Year";
    }

    public NowDateNative() {
        super("now");
    }

    @Override
    public ClassObject call(final List<Object> params) {
        final LocalDate now = LocalDate.now();

        final EnumClassDefinition.EnumValue CURRENT_WEEK = ClassUtil.getEnumValue(DAY_ENUM_CLASS_NAME, now.getDayOfWeek().name());
        final int CURRENT_DAY = now.getDayOfMonth();
        final EnumClassDefinition.EnumValue CURRENT_MONTH = ClassUtil.getEnumValue(MONTH_ENUM_CLASS_NAME, now.getMonth().name());
        final ClassObject CURRENT_YEAR = ClassUtil.initClass(YEAR_CLASS_NAME, now.getYear());

        return ClassUtil.initClass(DATE_CLASS_NAME, CURRENT_WEEK, CURRENT_DAY, CURRENT_MONTH, CURRENT_YEAR);
    }
}