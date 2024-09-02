package me.kuwg.clarity.compiler.stream;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.ast.nodes.function.declare.FunctionDeclarationNode;
import me.kuwg.clarity.compiler.ASTData;
import me.kuwg.clarity.compiler.ASTNodeCompiler;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static me.kuwg.clarity.compiler.ASTData.CONTINUE_BIT;
import static me.kuwg.clarity.compiler.ASTData.SEGMENT_BITS;

public class ASTInputStream extends DataInputStream {
    public ASTInputStream(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Reads a UTF-8 string from the stream
     */
    public String readString() throws IOException  {
        final int length = readVarInt();
        byte[] bytes = new byte[length];

        // noinspection ResultOfMethodCallIgnored
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads an {@link me.kuwg.clarity.ast.ASTNode} from the stream
     */
    public ASTNode readNode() throws IOException {
        int id = readVarInt();
        Class<? extends ASTNodeCompiler> clazz = ASTData.getClassFromId(id);
        if (clazz == null) {
            return null;
        }

        ASTNodeCompiler node;
        try {
            node = clazz.newInstance();
        } catch (final IllegalAccessException | InstantiationException | NullPointerException e) {
            throw new IOException("Failed to create class: " + clazz);
        }

        node.load(this);

        return (ASTNode) node;
    }

    /**
     * Reads a list of {@link me.kuwg.clarity.ast.ASTNode} from the stream
     */
    public List<? extends ASTNode> readNodeList() throws IOException {
        int length = readVarInt();
        List<ASTNode> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(readNode());
        }
        return list;
    }

    /**
     * Reads a list of {@link me.kuwg.clarity.ast.ASTNode} from the stream without type casting
     */
    @SuppressWarnings("unchecked")
    public <T extends ASTNode> List<T> readNodeListNoCast() throws IOException {
        return (List<T>) readNodeList();
    }

    /**
     * Reads a VarInt, a dynamic sized integer from the stream
     * From <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#VarInt_and_VarLong">wiki.vg</a>
     */
    public int readVarInt() throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;

            if (position >= 32) throw new IOException("VarInt is too big");
        }

        return value;
    }

    /**
     * Reads a VarLong, a dynamic sized long from the stream
     * From <a href="https://wiki.vg/index.php?title=Protocol&oldid=7368#VarInt_and_VarLong">wiki.vg</a>
     */
    public long readVarLong() throws IOException {
        long value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = readByte();
            value |= (long) (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;

            if (position >= 64) throw new IOException("VarLong is too big");
        }

        return value;
    }
}
