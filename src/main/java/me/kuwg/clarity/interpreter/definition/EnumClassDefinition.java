package me.kuwg.clarity.interpreter.definition;

import me.kuwg.clarity.interpreter.types.VoidObject;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class EnumClassDefinition extends ClassDefinition {

    private final List<EnumValue> enumValues;

    public EnumClassDefinition(final String name, final boolean isConstant, final List<EnumValue> enumValues) {
        super(name, isConstant, null, null, null, false);
        this.enumValues = enumValues;
    }

    public EnumValue getValue(final String name) {
        for (final EnumValue val : enumValues) {
            if (val.name.equals(name)) {
                return val;
            }
        }
        Register.throwException("Accessing an enum value that does not exist");
        throw new RuntimeException(); // unreachable
    }

    public List<EnumValue> getValues() {
        return enumValues;
    }

    public static class EnumValue {
        private final String name;
        private final Object value;

        public EnumValue(final String name, final Object value) {
            this.name = name;
            this.value = value;
        }

        public final String getName() {
            return name;
        }

        public final Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "EnumValue@" + name;
        }
    }
}
