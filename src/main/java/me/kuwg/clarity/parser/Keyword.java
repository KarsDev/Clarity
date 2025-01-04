package me.kuwg.clarity.parser;

import me.kuwg.clarity.token.Token;

import java.util.HashMap;
import java.util.Map;

public enum Keyword {
    CLASS {
        @Override
        public String[] usage() {
            return new String[] {
                    format("[modifier...] class <name> [inherits <inherited>] {...}", "Defines a class with a given name, if constant, it cannot be inherited."),

            };
        }
    },
    VAR {
        @Override
        public String[] usage() {
            return new String[] {
                    format("[modifier...] var <name>", "Creates a variable without giving it a value."),
                    format("[modifier...] var <name> = <value>", "Creates a variable with a give value."),

            };
        }
    },
    CONSTRUCTOR {
        @Override
        public String[] usage() {
            return new String[] {
                    format("constructor([params...]) {...}", "Defines a constructor in a class."),

            };
        }
    },
    LOCAL {
        @Override
        public String[] usage() {
            return new String[] {
                    format("local [modifier...] <type> [= <value>]", "Creates a local variable in a class."),
                    format("local [modifier...] fn [-> <type>] {...}", "Creates a local function in a class."),
                    format("local.var", "References a variable in the class."),
                    format("local.var = <value>", "Changes the value of a variable in the class"),

            };
        }
    },
    FN {
        @Override
        public String[] usage() {
            return new String[] {
                    format("[modifier...] fn <name>([params...]) [-> <type>] {...}", "Defines a function with optional parameters and return type."),

            };
        }
    },
    NATIVE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("native.<name>([params...]", "Calls a native function with optional parameters."),
                    format("[modifier...] native fn <name>([params...]) [-> <type>]", "Defines a native function. This must be defined in a native class and requires Java code to be written."),
                    format("[modifier...] native class <name> [inherits <inherited>]", "Defines a native class that can contain native functions. This requires Java code to be written."),

            };
        }
    },
    IF {
        @Override
        public String[] usage() {
            return new String[] {
                    format("if <condition> {...} [else if <condition> {...}] [else {...}]", "Defines if block with optional else if and else blocks."),

            };
        }
    },
    ELSE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("else [if <condition>] {...}", "Defines else (if) block."),

            };
        }
    },
    RETURN {
        @Override
        public String[] usage() {
            return new String[] {
                    format("return <value>", "Returns value in a function."),
                    format("return void", "Stops executing a function and returns void."),
                    format("return <value> when <condition>", "Returns a value when condition applies."),

            };
        }
    },
    NEW {
        @Override
        public String[] usage() {
            return new String[] {
                    format("new <class>([params...])", "Instantiates a new class (and returns the object) with given parameters."),

            };
        }
    },
    VOID {
        @Override
        public String[] usage() {
            return new String[] {
                    format("void", "Type in clarity language."),

            };
        }
    },
    INCLUDE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("include <name>", "Includes a clr file into the project (note: included into the project and not into the single file)"),
                    format("include native <name>", "Includes a native clr file into the project (note: included into the project and not into the single file)"),
                    format("include compiled <name>", "Includes a compiled clr file into the project (note: included into the project and not into the single file)."),

            };
        }
    },
    STATIC {
        @Override
        public String[] usage() {
            return new String[] {
                    format("static", "Modifier in clarity language."),
                    format("static [async] {...}", "Defines static initializer."),

            };
        }
    },
    CONST {
        @Override
        public String[] usage() {
            return new String[] {
                    format("const", "Modifier in clarity language."),

            };
        }
    },
    COMPILED {
        @Override
        public String[] usage() {
            return new String[] {
                    format("compiled", "Used to include compiled clarity files (<name>.cclr)."),

            };
        }
    },
    NULL {
        @Override
        public String[] usage() {
            return new String[] {
                    format("null", "Represents null (empty) object."),

            };
        }
    },
    FOR {
        @Override
        public String[] usage() {
            return new String[] {
                    format("for <declaration>, <condition>, <incrementation> {...}", "Defines a iteration loop."),
                    format("for <name> : <num> {...}", "Defines a for in range loop."),
                    format("for <name> : <arr> {...}", "Defines a for each loop."),

            };
        }
    },
    WHILE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("while <condition> {...}", "Defines a while loop."),

            };
        }
    },
    SELECT {
        @Override
        public String[] usage() {
            return new String[] {
                    format("select <param> {[whens...] [default]}", "Defines a select (switch) statement."),

            };
        }
    },
    WHEN {
        @Override
        public String[] usage() {
            return new String[] {
                    format("when <value> {...}", "Defines a when (case) statement in select."),

            };
        }
    },
    DEFAULT {
        @Override
        public String[] usage() {
            return new String[] {
                    format("default {...}", "Defines a default statement in select."),

            };
        }
    },
    BREAK {
        @Override
        public String[] usage() {
            return new String[] {
                    format("break", "Stops the current loop."),

            };
        }
    },
    CONTINUE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("continue", "Continues the iteration of the current loop."),

            };
        }
    },
    FLOAT {
        @Override
        public String[] usage() {
            return new String[] {
                    format("float", "Type in clarity language."),
                    format("float <name>", "Creates a float variable without value."),
                    format("float <name> = <value>", "Creates a float variable with a given value."),


            };
        }
    },
    INT {
        @Override
        public String[] usage() {
            return new String[] {
                format("int", "Type in clarity language."),
                format("int <name>", "Creates a integer variable without value."),
                format("int <name> = <value>", "Creates a integer variable with a given value."),
            };
        }
    },
    INHERITS {
        @Override
        public String[] usage() {
            return new String[] {
                    format("<class definition> inherits <name>", "Inherits a class with all of its properties."),

            };
        }
    },
    ASSERT {
        @Override
        public String[] usage() {
            return new String[] {
                    format("assert <condition>", "Asserts a condition, and if false raises a default exception."),
                    format("assert <condition> else <value>", "Asserts a condition, and if false raises en exception with given value."),

            };
        }
    },
    IS {
        @Override
        public String[] usage() {
            return new String[] {
                    format("<value> is <type>", "Checks if a given value is instance of the type (can also be a class)."),

            };
        }
    },
    ARR {
        @Override
        public String[] usage() {
            return new String[] {
                    format("arr", "Type in clarity language."),
                    format("arr <name>", "Creates an array variable without value."),
                    format("int <name> = <value>", "Creates an array variable with a given value."),

            };
        }
    },
    STR {
        @Override
        public String[] usage() {
            return new String[] {
                    format("str", "Type in clarity language."),
                    format("str <name>", "Creates a string variable without value."),
                    format("str <name> = <value>", "Creates a string variable with a given value."),

            };
        }
    },
    ENUM {
        @Override
        public String[] usage() {
            return new String[] {
                    format("enum <name> {...}", "Defines a enum."),

            };
        }
    },
    BOOL {
        @Override
        public String[] usage() {
            return new String[] {
                    format("bool", "Type in clarity language."),
                    format("bool <name>", "Creates a boolean variable without value."),
                    format("bool <name> = <value>", "Creates a boolean variable with a given value."),

            };
        }
    },
    ASYNC {
        @Override
        public String[] usage() {
            return new String[] {
                    format("async", "Modifier for classes and static blocks."),

            };
        }
    },
    RAISE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("raise <value>", "Raises an exception with given value."),

            };
        }
    },
    TRY {
        @Override
        public String[] usage() {
            return new String[] {
                    format("try {...} except [name] {...}", "Defines a try except block."),

            };
        }
    },
    EXCEPT {
        @Override
        public String[] usage() {
            return new String[] {
                    format("except", "Used in try-except block."),

            };
        }
    },
    LAMBDA {
        @Override
        public String[] usage() {
            return new String[] {
                    format("lambda", "Type in clarity language."),
                    format("lambda([params...]) -> {...}", "Defines a lambda object."),

            };
        }
    },
    DELETE {
        @Override
        public String[] usage() {
            return new String[] {
                    format("delete <name>", "Deletes a variable."),
                    format("<delete <name>(<count>)", "Deletes a function with give params count."),

            };
        }
    },

    ;

    private static final Map<String, Keyword> KEYWORD_MAP = new HashMap<>();

    static {
        for (final Keyword keyword : Keyword.values()) {
            KEYWORD_MAP.put(keyword.toString(), keyword);
        }
    }

    public abstract String[] usage();

    public static Keyword keyword(final Token token) {
        final Keyword keyword = KEYWORD_MAP.get(token.getValue().toLowerCase());
        if (keyword == null) {
            throw new UnsupportedOperationException("Unsupported keyword: " + token.getValue() + " at line " + token.getLine());
        }
        return keyword;
    }

    public static Keyword keyword(final String name) {
        return KEYWORD_MAP.getOrDefault(name.toLowerCase(), null);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    final String format(final String usage, final String desc) {
        return usage + ":\n  " + desc;
    }
}