package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class ReflectedNativeFunctionDeclaration extends ASTNode {

    private String name;
    private String typeDefault;
    private String fileName;
    private List<ParameterNode> params;
    private boolean isStatic, isConst, isLocal, isAsync;

    public ReflectedNativeFunctionDeclaration(final String name, final String typeDefault, final String className, final List<ParameterNode> params, final boolean isStatic, final boolean isConst, final boolean isLocal, final boolean isAsync) {
        this.name = name;
        this.typeDefault = typeDefault;
        this.fileName = className;
        this.params = params;
        this.isStatic = isStatic;
        this.isConst = isConst;
        this.isLocal = isLocal;
        this.isAsync = isAsync;
    }

    public ReflectedNativeFunctionDeclaration() {
    }

    public final String getName() {
        return name;
    }

    public final String getTypeDefault() {
        return typeDefault;
    }

    public final String getFileName() {
        return fileName;
    }

    public final List<ParameterNode> getParams() {
        return params;
    }

    public final boolean isStatic() {
        return isStatic;
    }

    public final boolean isConst() {
        return isConst;
    }

    public final boolean isLocal() {
        return isLocal;
    }

    public final boolean isAsync() {
        return isAsync;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Reflected Native Declaration: ").append(name).append(typeDefault != null ? "(" + typeDefault + ")\n" : "\n");

        if (isConst) {
            sb.append(indent).append("  Type: Constant\n");
        }
        if (isStatic) {
            sb.append(indent).append("  Type: Static\n");
        }
        if (isLocal) {
            sb.append(indent).append("  Type: Local\n");
        }
        if (isAsync) {
            sb.append(indent).append("  Type: Async\n");
        }

        sb.append(indent).append("  File: ").append(fileName);
        sb.append("\n");
        if (!params.isEmpty()) {
            sb.append(indent).append("  Params: ").append("{").append("\n");
            final String paramIndent = indent + "    ";
            for (final ParameterNode param : params) {
                param.print(sb, paramIndent);
            }
            sb.append(indent).append("  ").append("}");
        }
    }

    @Override
    protected void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(name);
        out.writeString(String.valueOf(typeDefault));
        out.writeString(fileName);
        out.writeNodeList(params, version);
        out.writeBoolean(isStatic);
        out.writeBoolean(isConst);
        out.writeBoolean(isLocal);
        out.writeBoolean(isAsync);
    }

    @Override
    protected void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.name = in.readString();
        this.typeDefault = in.readString();
        if (this.typeDefault.equals("null")) this.typeDefault = null;
        this.fileName = in.readString();
        this.params = in.readNodeListNoCast(version);
        this.isStatic = in.readBoolean();
        this.isConst = in.readBoolean();
        this.isLocal = in.readBoolean();
        this.isAsync = in.readBoolean();
    }

    @Override
    public String toString() {
        return "ReflectedNativeFunctionDeclaration{" +
                "fileName='" + fileName + '\'' +
                ", name='" + name + '\'' +
                ", typeDefault='" + typeDefault + '\'' +
                ", params=" + params +
                ", isStatic=" + isStatic +
                ", isConst=" + isConst +
                ", isLocal=" + isLocal +
                ", isAsync=" + isAsync +
                '}';
    }
}
