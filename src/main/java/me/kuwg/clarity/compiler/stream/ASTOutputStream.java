package me.kuwg.clarity.compiler.stream;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ASTData;
import me.kuwg.clarity.compiler.ASTNodeCompiler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ASTOutputStream extends DataOutputStream {
    public ASTOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Writes a {@link me.kuwg.clarity.ast.ASTNode} to the output stream
     */
    public void writeNode(ASTNodeCompiler node) throws IOException {
        // Get node id
        int id = ASTData.getNodeId(node.getClass());

        if (id == -1) {
            throw new RuntimeException(node.getClass().getName() + " has no node id. Please add to ASTData");
        }

        // Write short id
        writeInt(id);
        node.save(this);
    }

    /**
     * Writes a list of {@link me.kuwg.clarity.ast.ASTNode} to the output stream
     */
    public void writeNodeList(List<? extends ASTNode> list) throws IOException {
        writeInt(list.size());
        for (ASTNode astNode : list) {
            writeNode(astNode);
        }
    }

    /**
     * Writes a UTF-8 encode string to the output stream
     */
    public void writeString(String string) throws IOException {
        writeInt(string.length());
        write(string.getBytes(StandardCharsets.UTF_8));
    }
}