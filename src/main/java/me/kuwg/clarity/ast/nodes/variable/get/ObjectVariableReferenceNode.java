package me.kuwg.clarity.ast.nodes.variable.get;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;

public class ObjectVariableReferenceNode extends ASTNode {
    private String caller;
    private String called;

    public ObjectVariableReferenceNode(final String caller, final String called) {
        this.caller = caller;
        this.called = called;
    }

    public ObjectVariableReferenceNode(){
    }

    public final String getCaller() {
        return caller;
    }

    public final String getCalled() {
        return called;
    }


    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("ObjectVariableReferenceNode: ").append(caller).append(".").append(called).append("(");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(caller);
        out.writeString(called);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.caller = in.readString();
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
