package red.vuis.vbm.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

public final class VbmUtils {
    private VbmUtils() {}

    public static boolean all(boolean... values) {
        for (boolean value : values) {
            if (!value) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean any(boolean... values) {
        for (boolean value : values) {
            if (value) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyEquals(Object target, Object... values) {
        for (Object value : values) {
            if (Objects.equals(target, value)) {
                return true;
            }
        }
        return false;
    }

    public static boolean allBits(int value, int mask) {
        return (value & mask) == mask;
    }

    @SuppressWarnings("unchecked")
    public static <E> List<E> listOf(E first, E[] after) {
        E[] all = (E[]) Array.newInstance(after.getClass().componentType(), after.length + 1);
        all[0] = first;
        System.arraycopy(after, 0, all, 1, after.length);
        return List.of(all);
    }

    public static Set<ClassNode> readJarClassNodes(Path jar) throws IOException {
        Set<ClassNode> classNodes = new HashSet<>();

        try (FileSystem fileSystem = FileSystems.newFileSystem(jar)) {
            for (Path root : fileSystem.getRootDirectories()) {
                try (Stream<Path> stream = Files.walk(root)) {
                    stream.forEach(path -> {
                        if (!path.toString().endsWith(".class")) return;
                        try {
                            classNodes.add(readClassNode(path));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }

        return classNodes;
    }

    public static ClassNode readClassNode(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        return node;
    }

    public static String javaName(String rawName) {
        StringBuilder result = new StringBuilder(rawName.length());
        for (int i = 0; i < rawName.length(); i++) {
            char c = rawName.charAt(i);
            boolean alpha = Character.isAlphabetic(c);
            boolean digit = Character.isDigit(c);
            if (i == 0 && digit) {
                result.append('_');
            }
            if (alpha || digit) {
                result.append(Character.toUpperCase(c));
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    public static Frame<SourceValue>[] getMethodFrames(String className, MethodNode method) {
        try {
            return new Analyzer<>(new SourceInterpreter()).analyze(className, method);
        } catch (AnalyzerException e) {
            throw new RuntimeException(e);
        }
    }
}
