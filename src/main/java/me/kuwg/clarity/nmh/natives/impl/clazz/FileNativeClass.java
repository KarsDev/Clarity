package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.nmh.natives.aclass.NativeClass;
import me.kuwg.clarity.register.Register;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SuppressWarnings("CallToPrintStackTrace")
public class FileNativeClass extends NativeClass {
    public FileNativeClass() {
        super("File");
    }

    @Override
    public Object handleCall(final String name, final List<Object> params, final Context context) throws Exception {
        if (!params.isEmpty() && !(params.get(0) instanceof String)) {
            throw new IllegalArgumentException("First parameter must be a String representing the file path. Got: " + params.get(0));
        }

        final String pathString = (String) context.getVariable("path");

        final Path path = Paths.get(pathString);

        switch (name) {
            case "exists":
                checkParams(name, params, 0);
                return Files.exists(path);

            case "delete":
                checkParams(name, params, 0);
                try {
                    return Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return false;
                }

            case "renameTo":
                checkParams(name, params, 1);
                String newName = (String) params.get(0);
                Path newPath = path.resolveSibling(newName);
                try {
                    Files.move(path, newPath);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return false;
                }

            case "size":
                checkParams(name, params, 0);
                return Files.size(path);

            case "read":
                checkParams(name, params, 0);
                try {
                    return new String(Files.readAllBytes(path));
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return "";
                }

            case "write":
                checkParams(name, params, 1);
                String content = (String) params.get(0);
                try {
                    Files.write(path, content.getBytes());
                    return VOID;
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return VOID;
                }

            case "append":
                checkParams(name, params, 1);
                String appendContent = (String) params.get(0);
                try {
                    Files.write(path, appendContent.getBytes(), java.nio.file.StandardOpenOption.APPEND);
                    return VOID;
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return VOID;
                }

            case "createNewFile":
                checkParams(name, params, 0);
                try {
                    if (!Files.exists(path)) {
                        Files.createFile(path);
                        return true;
                    }
                    return false;
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return false;
                }

            case "isDirectory":
                checkParams(name, params, 0);
                return Files.isDirectory(path);

            case "listFiles":
                checkParams(name, params, 0);
                File[] files = path.toFile().listFiles();
                if (files != null) {
                    String[] fileNames = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        fileNames[i] = files[i].getName();
                    }
                    return fileNames;
                }
                return new String[0];

            case "lastModified":
                checkParams(name, params, 0);
                return Files.getLastModifiedTime(path).toMillis();

            case "mkdir":
                checkParams(name, params, 0);
                try {
                    return Files.createDirectory(path).toFile().exists();
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return false;
                }

            case "mkdirs":
                checkParams(name, params, 0);
                try {
                    return Files.createDirectories(path).toFile().exists();
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return false;
                }

            case "canRead":
                checkParams(name, params, 0);
                return Files.isReadable(path);

            case "canWrite":
                checkParams(name, params, 0);
                return Files.isWritable(path);

            case "canExecute":
                checkParams(name, params, 0);
                return Files.isExecutable(path);

            case "getPath":
                checkParams(name, params, 0);
                return path.toString();

            case "getName":
                checkParams(name, params, 0);
                return path.getFileName().toString();

            case "getParent":
                checkParams(name, params, 0);
                return path.getParent() != null ? path.getParent().toString() : null;

            case "setReadOnly":
                checkParams(name, params, 0);
                try {
                    Files.setAttribute(path, "dos:readonly", true);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace(); // Log the error
                    return false;
                }

            default:
                Register.throwException("Invalid native method name: " + name);
                return null;
        }
    }

    private void checkParams(String methodName, List<Object> params, int expectedSize) {
        if (params.size() != expectedSize) {
            throw new IllegalArgumentException("Invalid parameters for " + methodName + ". Expected " + expectedSize + " parameter(s), got " + params.size() + " with types " + getParamTypes(params));
        }
    }

    private String getParamTypes(List<Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(param == null ? "null" : param.getClass().getSimpleName());
        }
        return sb.toString();
    }
}