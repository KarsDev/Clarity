package me.kuwg.clarity.library.objects.types;

import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.ast.nodes.function.declare.ParameterNode;
import me.kuwg.clarity.interpreter.context.Context;

import java.util.List;

public class LambdaObject {
    private final List<ParameterNode> params;
    private final BlockNode block;
    private final Context context;

    public LambdaObject(final List<ParameterNode> params, final BlockNode block, final Context context) {
        this.params = params;
        this.block = block;
        this.context = context;
    }

    public final BlockNode getBlock() {
        return block;
    }

    public final List<ParameterNode> getParams() {
        return params;
    }

    public final Context getContext() {
        return context;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LambdaObject(");
        for (int i = 0; i < params.size(); i++) {
            final ParameterNode param = params.get(i);
            builder.append(param.getName()).append(param.isLambda() ? " (lambda)" : "").append(i + 1 < params.size() ? ", " : "");
        }
        builder.append(")");
        return builder.toString();
    }
}
