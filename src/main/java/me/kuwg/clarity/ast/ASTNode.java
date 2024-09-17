package me.kuwg.clarity.ast;

import me.kuwg.clarity.compiler.ASTNodeCompiler;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public abstract class ASTNode implements ASTNodeCompiler {

    public ASTNode(){
    }

    private int line;

    public final int getLine() {
//        if (line == 0) throw new UnsupportedOperationException("Unsupported line: 0, in class " + getClass().getSimpleName());
        return line;
    }

    @SuppressWarnings("unchecked")
    public final <T extends ASTNode> T setLine(final int line) {
        this.line = line;
        return (T) this;
    }

    public abstract void print(final StringBuilder sb, final String indent);

    protected abstract void save0(final ASTOutputStream out) throws IOException;

    @Override
    public final void save(final ASTOutputStream out) throws IOException {
        out.writeVarInt(line);
        save0(out);
    }

    protected abstract void load0(ASTInputStream in) throws IOException;

    @Override
    public final void load(final ASTInputStream in) throws IOException {
        this.line = in.readVarInt();
        load0(in);
    }
}
