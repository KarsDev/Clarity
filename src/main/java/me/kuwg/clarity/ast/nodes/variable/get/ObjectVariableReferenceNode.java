package me.kuwg.clarity.ast.nodes.variable.get;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ObjectVariableReferenceNode extends ASTNode {
    private ASTNode caller;
    private String called;

    public ObjectVariableReferenceNode(final ASTNode caller, final String called) {
        this.caller = caller;
        this.called = called;
    }

    public ObjectVariableReferenceNode(){
    }

    public final ASTNode getCaller() {
        return caller;
    }

    public final String getCalled() {
        return called;
    }


    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("ObjectVariableReferenceNode: ").append(caller).append(" -> ").append(called).append("\n");
    }

    @Override
    public void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeNode(caller, version);
        out.writeString(called);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.caller = in.readNode(version);
        this.called = in.readString();
    }

    @Override
    public String toString() {
        return "ObjectVariableReferenceNode{" +
                "caller='" + caller + '\'' +
                ", called='" + called + '\'' +
                '}';
    }
}
