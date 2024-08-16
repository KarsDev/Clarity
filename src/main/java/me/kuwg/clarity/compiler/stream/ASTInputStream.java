package me.kuwg.clarity.compiler.stream;

import me.kuwg.clarity.ast.ASTNode;
import me.kuwg.clarity.compiler.ASTData;
import me.kuwg.clarity.compiler.ASTNodeCompiler;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ASTInputStream extends DataInputStream {
    public ASTInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public String readString() throws IOException  {
        final int length = readInt();
        byte[] bytes = new byte[length];

        // noinspection ResultOfMethodCallIgnored
        read(bytes);
        return new String(bytes);
    }

    public ASTNode readNode() throws IOException {
        int id = readInt();
        Class<? extends ASTNodeCompiler> clazz = ASTData.getClassFromId(id);
        if (clazz == null) {
            throw new RuntimeException("Failed to read class id " + id);
        }

        ASTNodeCompiler node;
        try {
            node = clazz.newInstance();
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to create class");
        }

        node.load(this);

        return (ASTNode) node;
    }

    public List<? extends ASTNode> readNodeList() throws IOException {
        int length = readInt();
        List<ASTNode> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            list.add(readNode());
        }
        return list;
    }
}
