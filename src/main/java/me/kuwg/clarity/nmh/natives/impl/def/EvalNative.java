package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.ast.AST;
import me.kuwg.clarity.interpreter.Interpreter;
import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;
import me.kuwg.clarity.parser.ASTParser;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.token.Tokenizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvalNative extends DefaultNativeFunction<Object> {
    public EvalNative() {
        super("eval");
    }

    @Override
    public Object call(final List<Object> params) {
        try {
            final String code = (String) params.get(0);

            final ASTParser parser = new ASTParser("none", "none", Tokenizer.tokenize(code));

            final AST ast = parser.parse();

            final Interpreter interpreter = new Interpreter(ast);
            return interpreter.interpretBlock(ast.getRoot(), interpreter.general());
        } catch (final Exception e) {
            Register.throwException("An error occurred: could not eval");
            return VOID;
        }
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.size() == 1 && params.get(0) instanceof String;
    }

    @Override
    public void help() {
        final Map<String, String> map = new HashMap<>();

        map.put("code", "str");

        System.out.println(formatHelp(map));
    }
}
