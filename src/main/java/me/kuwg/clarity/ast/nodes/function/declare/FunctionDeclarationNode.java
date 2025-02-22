package me.kuwg.clarity.ast.nodes.function.declare;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.PreInterpretable;
import me.kuwg.clarity.ast.nodes.block.BlockNode;
import me.kuwg.clarity.compiler.CompilerVersion;
import me.kuwg.clarity.compiler.stream.ASTInputStream;
import me.kuwg.clarity.compiler.stream.ASTOutputStream;

import java.io.IOException;
import java.util.List;

public class FunctionDeclarationNode extends ASTNode implements PreInterpretable {

    protected String functionName;
    private String typeDefault;
    private boolean isStatic, isConst, isLocal, isAsync;
    protected List<ParameterNode> parameterNodes;
    protected BlockNode block;

    public FunctionDeclarationNode(final String functionName, final String typeDefault, final boolean isStatic, final boolean isConst, final boolean isLocal, final boolean isAsync, final List<ParameterNode> parameterNodes, final BlockNode block) {
        this.functionName = functionName;
        this.typeDefault = typeDefault;
        this.isStatic = isStatic;
        this.isConst = isConst;
        this.isLocal = isLocal;
        this.isAsync = isAsync;
        this.parameterNodes = parameterNodes;
        this.block = block;
    }

    public FunctionDeclarationNode() {
        super();
    }

    public final String getFunctionName() {
        return functionName;
    }

    public final String getTypeDefault() {
        return typeDefault;
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

    public final List<ParameterNode> getParameterNodes() {
        return parameterNodes;
    }

    public final BlockNode getBlock() {
        return block;
    }

    @Override
    public void print(final StringBuilder sb, final String indent) {
        sb.append(indent).append("Function: ").append(functionName).append(typeDefault != null ? " (" + typeDefault + ")" : "").append("\n");
        sb.append(indent).append("Parameters: ");

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

        if (parameterNodes.isEmpty()) {
            sb.append("None\n");
        } else {
            sb.append("\n");
            String paramIndent = indent + "    ";
            for (ParameterNode param : parameterNodes) {
                param.print(sb, paramIndent);
            }
        }

        sb.append(indent).append("Body:\n");
        block.print(sb, indent + "    ");
    }

    @Override
    public void save0(final ASTOutputStream out, final CompilerVersion version) throws IOException {
        out.writeString(functionName);
        out.writeString(String.valueOf(typeDefault));
        out.writeBoolean(isStatic);
        out.writeBoolean(isConst);
        out.writeBoolean(isLocal);
        out.writeBoolean(isAsync);
        out.writeNodeList(parameterNodes, version);
        out.writeNode(block, version);
    }

    @Override
    public void load0(final ASTInputStream in, final CompilerVersion version) throws IOException {
        this.functionName = in.readString();
        this.typeDefault = in.readString();
        if (this.typeDefault.equals("null")) this.typeDefault = null;
        this.isStatic = in.readBoolean();
        this.isConst = in.readBoolean();
        this.isLocal = in.readBoolean();
        this.isAsync = in.readBoolean();
        this.parameterNodes = in.readNodeListNoCast(version);
        this.block = (BlockNode) in.readNode(version);
    }
}
