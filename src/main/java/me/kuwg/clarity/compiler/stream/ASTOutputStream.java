package me.kuwg.clarity.compiler.stream;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ASTData;
import me.kuwg.clarity.compiler.ASTNodeCompiler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static me.kuwg.clarity.compiler.ASTData.CONTINUE_BIT;
import static me.kuwg.clarity.compiler.ASTData.SEGMENT_BITS;

public class ASTOutputStream extends DataOutputStream {
    public ASTOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Writes a {@link me.kuwg.clarity.ast.ASTNode} to the output stream
     */
    public void writeNode(ASTNodeCompiler node) throws IOException {
        // Get node id
        int id = ASTData.getNodeId(node == null ? null : node.getClass());

        if (id == -1) {
            throw new IOException((node == null ? null : node.getClass().getName()) + " has no node id. Please add to ASTData");
        }

        // Write short id
        writeVarInt(id);
        if (node != null) {
            node.save(this);
        }
    }

    /**
     * Writes a list of {@link me.kuwg.clarity.ast.ASTNode} to the output stream
     */
    public void writeNodeList(List<? extends ASTNode> list) throws IOException {
        writeVarInt(list.size());
        for (ASTNode astNode : list) {
            writeNode(astNode);
        }
    }

    /**
     * Writes a UTF-8 encode string to the output stream
     */
    public void writeString(String string) throws IOException {
        writeVarInt(string.length());
        write(string.getBytes(StandardCharsets.UTF_8));
    }

    /***
     * Writes a VarInt, a dynamic sized integer
     * From <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#VarInt_and_VarLong">wiki.vg</a>
     */
    public void writeVarInt(int value) throws IOException {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                writeByte(value);
                return;
            }

            writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }

    /***
     * Writes a VarLong, a dynamic sized long
     * From <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#VarInt_and_VarLong">wiki.vg</a>
     */
    public void writeVarLong(long value) throws IOException {
        while (true) {
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                writeByte((int) value);
                return;
            }

            writeByte((int) ((value & SEGMENT_BITS) | CONTINUE_BIT));

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }
    }
}