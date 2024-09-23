package me.kuwg.clarity.nmh.natives.impl.clazz;

import me.kuwg.clarity.interpreter.context.Context;
import me.kuwg.clarity.nmh.natives.abstracts.NativeClass;
import me.kuwg.clarity.register.Register;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

            case "compressZip":
                checkParams("compressZip", params, 1);
                return compressFileZip(new File(pathString), String.valueOf(params.get(0)));

            case "decompressZip":
                checkParams("decompressZip", params, 1);
                return decompressFileZip(new File(pathString), String.valueOf(params.get(0)));

            default:
                Register.throwException("Invalid native method name: " + name);
                return null;
        }
    }

    private File compressFileZip(final File original, final String zipFileName) throws IOException {
        File zipFile = new File(zipFileName);

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(original)) {

            ZipEntry zipEntry = new ZipEntry(original.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }

        return zipFile;
    }

    public File decompressFileZip(final File zipFile, final String outputDir) throws IOException {
        File destDir = new File(outputDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, zipEntry.getName());

                new File(newFile.getParent()).mkdirs();

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) >= 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                zis.closeEntry();
            }
        }

        return destDir;
    }

    private void checkParams(String methodName, List<Object> params, int expectedSize) {
        if (params.size() != expectedSize) {
            throw new IllegalArgumentException("Invalid parameters for " + methodName + ". Expected " + expectedSize + " parameter(s), got " + params.size() + " with types " + getParamTypes(params));
        }
    }

}
