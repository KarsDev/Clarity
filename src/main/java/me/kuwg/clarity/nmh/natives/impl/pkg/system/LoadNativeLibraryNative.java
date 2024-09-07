package me.kuwg.clarity.nmh.natives.impl.pkg.system;

import me.kuwg.clarity.ClarityNativeFunction;
import me.kuwg.clarity.VoidObject;
import me.kuwg.clarity.nmh.NativeMethodHandler;
import me.kuwg.clarity.register.Register;
import me.kuwg.clarity.ClarityNativeLibrary;  // Make sure this import matches your actual classpath

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class LoadNativeLibraryNative extends SystemNativeFunction<Integer> {

    private static final File LIBRARIES = new File(System.getProperty("user.home") + "/Clarity/libraries/");

    public LoadNativeLibraryNative() {
        super("loadNativeLibrary");
    }

    @Override
    public Integer call(final List<Object> params) {
        if (!(params.get(0) instanceof String)) {
            Register.throwException("Native lib must be string");
            return 5;
        }

        final String path = (String) params.get(0);

        try {
            final File jarFile = new File(LIBRARIES,  path + ".jar");

            if (!jarFile.exists()) {
                Register.throwException("Native lib not found: " + jarFile.getAbsolutePath());
                return 4;
            }

            final URL jarUrl = jarFile.toURI().toURL();
            final URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, this.getClass().getClassLoader());

            final String classPath = readLibOption(classLoader, jarFile);

            if (classPath == null) {
                Register.throwException("Class path not found in library.lib.");
                return 3;
            }

            final Class<?> clazz = classLoader.loadClass(classPath);

            if (!ClarityNativeLibrary.class.isAssignableFrom(clazz)) {
                Register.throwException("Class does not extend ClarityNativeLibrary: " + classPath);
                return 2;
            }

            final ClarityNativeLibrary libraryInstance = (ClarityNativeLibrary) clazz.getDeclaredConstructor().newInstance();

            NativeMethodHandler.loadLibrary(libraryInstance);
            return 0;
        } catch (final Exception e) {
            return 1;
        }
    }

    private String readLibOption(final URLClassLoader classLoader, final File jarFile) throws IOException {
        InputStream inputStream = classLoader.getResourceAsStream("library.lib");

        if (inputStream == null) {
            Register.throwException("Resource file 'library.lib' not found in the JAR: " + jarFile.getAbsolutePath());
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("lib: ")) {
                    return line.substring(5).trim();
                }
            }
        }
        Register.throwException("Option \"lib\" not found in library.lib.");
        return null;
    }
}
