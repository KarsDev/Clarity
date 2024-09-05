package me.kuwg.clarity.ast.nodes.variable.assign;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ast.stream.ASTInputStream;
import me.kuwg.clarity.compiler.ast.stream.ASTOutputStream;

import java.io.IOException;

public class ObjectVariableReassignmentNode extends ASTNode {
    private String caller;
    private String called;
    private ASTNode value;

    public ObjectVariableReassignmentNode(final String caller, final String called, final ASTNode value) {
        this.caller = caller;
        this.called = called;
        this.value = value;
    }

    public ObjectVariableReassignmentNode() {
    }

    public final String getCaller() {
        return caller;
    }

    public final String getCalled() {
        return called;
    }

    public final ASTNode getValue() {
        return value;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Variable Reassignment:\n");

        sb.append(indent).append("  Caller: ").append(caller).append("\n");
        sb.append(indent).append("  Called: ").append(called).append("\n");

        sb.append(indent).append("  Value:\n");
        value.print(sb, indent + "    ");
    }

    @Override
    public void save0(final ASTOutputStream out) throws IOException {
        out.writeString(caller);
        out.writeString(called);
        out.writeNode(value);
    }

    @Override
    public void load0(final ASTInputStream in) throws IOException {
        this.caller = in.readString();
        this.called = in.readString();
        this.value = in.readNode();
    }

    @Override
    public String toString() {
        return "ObjectVariableReassignmentNode{" +
                "caller='" + caller + '\'' +
                ", called='" + called + '\'' +
                ", value=" + value +
                '}';
    }
}
