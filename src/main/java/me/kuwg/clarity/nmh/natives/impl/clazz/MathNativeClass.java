package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.nmh.natives.abstracts.NativeClass;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class MathNativeClass extends NativeClass {
    public MathNativeClass() {
        super("Math");
    }


    @Override
    public Object handleCall(final String name, final List<Object> params, final Context context) throws Exception {
        switch (name) {
            case "sqrt": {
                check("Invalid parameters for 'sqrt'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                return Math.sqrt(((Number) params.get(0)).doubleValue());
            }
            case "cbrt": {
                check("Invalid parameters for 'cbrt'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                return Math.cbrt(((Number) params.get(0)).doubleValue());
            }
            case "isPrime": {
                check("Invalid parameters for 'isPrime'. Expected 1 Integer, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Integer);
                final int num = (int) params.get(0);
                if (num <= 1) return false;
                if (num <= 3) return true;
                if (num % 2 == 0 || num % 3 == 0) return false;
                for (int i = 5; i * i <= num; i = i + 6) if (num % i == 0 || num % (i + 2) == 0) return false;
                return true;
            }
            case "pow": {
                check("Invalid parameters for 'pow'. Expected 2 Numbers, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 2 && params.get(0) instanceof Number && params.get(1) instanceof Number);
                return Math.pow(((Number) params.get(0)).doubleValue(), ((Number) params.get(1)).doubleValue());
            }
            case "floor": {
                check("Invalid parameters for 'floor'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                return Math.floor(((Number) params.get(0)).doubleValue());
            }
            case "ceil": {
                check("Invalid parameters for 'ceil'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                return Math.ceil(((Number) params.get(0)).doubleValue());
            }
            case "round": {
                check("Invalid parameters for 'round'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                return Math.round(((Number) params.get(0)).doubleValue());
            }
            case "random": {
                check("Invalid parameters for 'random'. Expected no parameters, got " + params.size() + " with types " + getParamTypes(params),
                        params.isEmpty());
                return Math.random();
            }
            case "log": {
                check("Invalid parameters for 'log'. Expected 2 Numbers, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 2 && params.get(0) instanceof Number && params.get(1) instanceof Number);
                double value = ((Number) params.get(0)).doubleValue();
                double base = ((Number) params.get(1)).doubleValue();
                if (base <= 1 || value <= 0) {
                    throw new IllegalArgumentException("Base must be greater than 1 and value must be positive.");
                }
                return Math.log(value) / Math.log(base);
            }
            case "exp": {
                check("Invalid parameters for 'exp'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                return Math.exp(((Number) params.get(0)).doubleValue());
            }
            case "fact": {
                check("Invalid parameters for 'fact'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Integer);
                final int n = (int) params.get(0);

                if (n < 0) Register.throwException("Negative numbers are not allowed.");

                long result = 1;
                for (int i = 2; i <= n; i++) result *= i;
                return result;
            }
            case "sin": {
                check("Invalid parameters for 'sin'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double n = ((Number) params.get(0)).doubleValue();
                return Math.sin(n);
            }
            case "cos": {
                check("Invalid parameters for 'cos'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double n = ((Number) params.get(0)).doubleValue();
                return Math.cos(n);
            }
            case "tan": {
                check("Invalid parameters for 'tan'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double n = ((Number) params.get(0)).doubleValue();
                return Math.tan(n);
            }
            case "asin": {
                check("Invalid parameters for 'asin'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double n = ((Number) params.get(0)).doubleValue();
                return Math.asin(n);
            }
            case "acos": {
                check("Invalid parameters for 'acos'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double n = ((Number) params.get(0)).doubleValue();
                return Math.acos(n);
            }
            case "atan": {
                check("Invalid parameters for 'atan'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double n = ((Number) params.get(0)).doubleValue();
                return Math.atan(n);
            }
            case "log10": {
                check("Invalid parameters for 'log10'. Expected 1 Number, got " + params.size() + " with types " + getParamTypes(params),
                        params.size() == 1 && params.get(0) instanceof Number);
                final double value = ((Number) params.get(0)).doubleValue();
                return Math.log10(value);
            }
        }

        throw new UnsupportedOperationException("Unsupported math native: " + name);
    }
}