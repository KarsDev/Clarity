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

/**
 * @author hi12167pies
 */
public final class ASTOutputStream extends DataOutputStream {

    public ASTOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Writes an {@link ASTNodeCompiler} to the output stream.
     *
     * @param node The ASTNodeCompiler to be written.
     * @throws IOException If an I/O error occurs.
     */
    public void writeNode(ASTNodeCompiler node) throws IOException {
        // Get node id
        int id = ASTData.getIdFromNode(node == null ? null : node.getClass());

        if (id == -1) {
            throw new IOException((node == null ? "null" : node.getClass().getName()) + " has no node id. Please add to ASTData.");
        }

        // Write short id
        writeVarInt(id);

        if (node != null) {
            node.save(this);
        }
    }

    /**
     * Writes a list of {@link ASTNode} to the output stream.
     *
     * @param list The list of ASTNodes to be written.
     * @throws IOException If an I/O error occurs.
     */
    public void writeNodeList(List<? extends ASTNode> list) throws IOException {
        writeVarInt(list.size());
        for (ASTNode astNode : list) {
            writeNode(astNode);
        }
    }

    /**
     * Writes a UTF-8 encoded string to the output stream.
     *
     * @param string The string to be written.
     * @throws IOException If an I/O error occurs.
     */
    public void writeString(String string) throws IOException {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        write(bytes);
    }

    /**
     * Writes a VarInt, a variable-length integer.
     * From <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#VarInt_and_VarLong">wiki.vg</a>
     *
     * @param value The integer value to be written.
     * @throws IOException If an I/O error occurs.
     */
    public void writeVarInt(int value) throws IOException {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                writeByte(value);
                return;
            }
            writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }
    }

    /**
     * Writes a VarLong, a variable-length long.
     * From <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#VarInt_and_VarLong">wiki.vg</a>
     *
     * @param value The long value to be written.
     * @throws IOException If an I/O error occurs.
     */
    public void writeVarLong(long value) throws IOException {
        while (true) {
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                writeByte((int) value);
                return;
            }
            writeByte((int) ((value & SEGMENT_BITS) | CONTINUE_BIT));
            value >>>= 7;
        }
    }

    public void writeOptimalLong(final long value) throws IOException {
        final int lBits = Long.BYTES * 8;
        final String lStrBytes = Long.toString(value);
        final int lStrBits = lStrBytes.getBytes(StandardCharsets.UTF_8).length * 8;

        final int stringBytes = getVarIntBits(lStrBits) + lStrBits;

        if (stringBytes < lBits) {
            writeBoolean(true);
            writeString(lStrBytes);
        } else {
            writeBoolean(false);
            writeLong(value);
        }
    }

    public void writeOptimalDouble(final double value) throws IOException {
        final int dBits = Double.BYTES * 8;
        final String dStrBytes = Double.toString(value);
        final int dStrBits = dStrBytes.getBytes(StandardCharsets.UTF_8).length * 8;

        final int stringBytes = getVarIntBits(dStrBits) + dStrBits;

        if (stringBytes < dBits) {
            writeBoolean(true);
            writeString(dStrBytes);
        } else {
            writeBoolean(false);
            writeDouble(value);
        }
    }

    private static int getVarIntBits(int value) {
        int bits = 0;
        while (true) {
            bits += 7;
            if ((value & ~SEGMENT_BITS) == 0) {
                break;
            }
            value >>>= 7;
        }
        return bits;
    }
}