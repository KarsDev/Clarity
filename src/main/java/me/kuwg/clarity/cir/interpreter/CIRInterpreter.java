package me.kuwg.clarity.cir.interpreter;

import me.kuwg.clarity.cir.interpreter.definition.CIRFunctionDefinition;

import java.util.*;

import static me.kuwg.clarity.interpreter.types.Null.NULL;

public class CIRInterpreter {

    private final String[] lines;
    private final Map<String, Object> variables;
    private final Map<String, Object> registers;
    private final Map<String, CIRFunctionDefinition> functionMap;

    public CIRInterpreter(final String[] lines) {
        this.lines = lines;
        this.variables = new HashMap<>();
        this.registers = new HashMap<>();
        this.functionMap = new HashMap<>();
    }

    public void interpret() {
        parseFunctions();
        final Stack<Object> EMPTY = new Stack<>();

        for (String instruction : functionMap.get("main").getInstructions()) {
            executeInstruction(instruction, EMPTY);
        }
    }

    private void parseFunctions() {
        String currentFunction = null;
        final List<String> params = new ArrayList<>();
        final List<String> instructions = new ArrayList<>();

        for (final String line : lines) {
            final String[] parts = line.trim().split("\\s+");

            if (parts.length == 0) continue;

            final String keyword = parts[0];

            switch (keyword) {
                case "FUNC":
                    params.clear();
                    currentFunction = parts[1].replace(":", "");
                    break;
                case "CONST":
                    instructions.add("CONST " + joinParts(parts, 1));
                    break;
                case "CALL":
                    instructions.add("CALL " + joinParts(parts, 1));
                    break;
                case "STORE":
                    instructions.add("STORE " + parts[1] + " " + joinParts(parts, 2));
                    break;
                case "LOAD":
                    params.add(parts[1]);
                    instructions.add("LOAD " + parts[1] + " " + joinParts(parts, 2));
                    break;
                case "OP":
                    instructions.add("OP " + joinParts(parts, 1));
                    break;
                case "RETURN":
                    instructions.add("RETURN " + joinParts(parts, 1));
                    break;
                case "SYSCALL":
                    instructions.add("SYSCALL " + joinParts(parts, 1));
                    break;
                case "STOP":
                    functionMap.put(currentFunction, new CIRFunctionDefinition(currentFunction, params, new ArrayList<>(instructions)));
                    currentFunction = null;
                    instructions.clear();
                    break;
            }
        }

        if (currentFunction != null) {
            throw new RuntimeException("Did not end function at EOF.");
        }
    }

    // Helper method to join parts of the array
    private String joinParts(String[] parts, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < parts.length; i++) {
            if (i > start) {
                sb.append(" ");
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private void executeInstruction(String instruction, Stack<Object> params) {
        String[] parts = instruction.split(" ", 2);
        String command = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";

        switch (command) {
            case "CONST":
                registers.put("T" + registers.size(), args.replace("\"", ""));
                break;
            case "CALL":
                executeCall(args);
                break;
            case "STORE":
                String[] storeParts = args.split(" ");
                final Object toStore;
                if (params.isEmpty()) {
                    toStore = registers.remove("RET");
                } else {
                    toStore = params.pop();
                }
                variables.put(storeParts[0].trim(), toStore);
                break;
            case "LOAD":
                String[] loadParts = args.split(" ");
                String variable = loadParts[0];
                final Object toLoad;
                if (params.isEmpty()) {
                    return;
                } else {
                    toLoad = params.pop();
                }
                variables.put(variable, toLoad);
                registers.put("T" + registers.size(), toLoad);
                break;
            case "OP":
                handleOperation(args);
                break;
            case "RETURN":
                registers.put("RET", resolveValue(args));
                break;
            case "SYSCALL":
                final String cmd = args.split(" ")[0];
                final String param = args.substring(cmd.length() + 1);
                handleNative(cmd, param);
                break;
            default:
                System.err.println("Unknown instruction: " + command);
        }
    }

    private void executeCall(String args) {
        String[] callParts = args.split(" ");
        String functionName = callParts[0];

        CIRFunctionDefinition func = functionMap.get(functionName);
        if (func != null) {
            Stack<Object> funcParams = new Stack<>();

            for (int i = 1; i < callParts.length; i++) {
                funcParams.add(resolveValue(callParts[i]));
            }

            for (String instruction : func.getInstructions()) {
                executeInstruction(instruction, funcParams);
            }
        } else {
            System.err.println("Function not found: " + functionName);
        }
    }

    private void handleOperation(String args) {
        String[] terms = args.trim().split(",\\s*");

        // Extract terms
        if (terms.length != 3) {
            System.err.println("The input string does not contain enough terms. (" + terms.length + " != 3)");
            System.exit(1);
        }

        final String[] firstTwo = terms[0].split(" ");

        String firstTerm = firstTwo[0].trim();
        String secondTerm = firstTwo[1].trim();
        String thirdTerm = terms[1].trim();
        String fourthTerm = terms[2].trim();

        // Resolve operands
        Object value1 = resolveValue(secondTerm);
        Object value2 = resolveValue(thirdTerm);

        if (value1 == NULL || value2 == NULL) {
            System.err.println("Invalid operands: " + secondTerm + ", " + thirdTerm);
            System.exit(1);
            return;
        }

        String result = performOperation(firstTerm, value2, value1);
        registers.put(fourthTerm, result);
    }

    private Object resolveValue(String operand) {
        if (operand.startsWith("T")) {
            return registers.get(operand);
        }
        if (operand.contains("\"")) return operand.replace("\"", "");
        return variables.getOrDefault(operand, NULL);
    }

    private String performOperation(String op, Object value1, Object value2) {

        if (value1 == null || value2 == null) {
            System.err.printf("Invalid values: %s, %s\n", value1, value2);
            throw new NullPointerException();
        }

        if (value1 instanceof String || value2 instanceof String) {
            if (op.equals("+")) {
                return value1 + value2.toString();
            } else {
                System.err.println("Unsupported operation for strings: " + op);
                return NULL.toString();
            }
        }

        // Otherwise, treat operands as numbers (int or double)
        double num1, num2;
        boolean isIntOperation = true;

        try {
            num1 = parseNumber(value1.toString());
            num2 = parseNumber(value2.toString());
            // Check if the numbers have decimal places to determine if the operation should result in a double
            if (value1.toString().contains(".") || value2.toString().contains(".")) {
                isIntOperation = false;
            }
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format: " + value1 + ", " + value2);
            return NULL.toString();
        }

        double result;

        switch (op) {
            case "+":
                result = num1 + num2;
                break;
            case "-":
                result = num1 - num2;
                break;
            case "*":
                result = num1 * num2;
                break;
            case "/":
                if (num2 == 0) {
                    System.err.println("Division by zero error");
                    return NULL.toString();
                }
                result = num1 / num2;
                isIntOperation = false;
                break;
            case "%":
                if (num2 == 0) {
                    System.err.println("Division by zero error");
                    return NULL.toString();
                }
                result = num1 % num2;
                break;
            case "==":
                return Boolean.toString(num1 == num2);
            case "!=":
                return Boolean.toString(num1 != num2);
            case "<":
                return Boolean.toString(num1 < num2);
            case ">":
                return Boolean.toString(num1 > num2);
            case "<=":
                return Boolean.toString(num1 <= num2);
            case ">=":
                return Boolean.toString(num1 >= num2);
            default:
                System.err.println("Unsupported operation: " + op);
                return NULL.toString();
        }

        if (isIntOperation) {
            return Integer.toString((int) result);
        } else {
            return Double.toString(result);
        }
    }

    private double parseNumber(String value) throws NumberFormatException {
        if (value.contains(".")) {
            return Double.parseDouble(value);
        } else {
            return Integer.parseInt(value);
        }
    }

    private void handleNative(final String cmd, final String args) {
        switch (cmd) {
            case "println": {
                System.out.println(resolveValue(args));
                break;
            }
            case "print": {
                System.out.print(resolveValue(args));
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported native: " + cmd);
            }
        }
    }
}
