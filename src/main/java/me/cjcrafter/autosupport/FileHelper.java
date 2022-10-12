package me.cjcrafter.autosupport;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public final class FileHelper {

    private FileHelper() {
        throw new IllegalStateException("Nobody may instantiate " + FileHelper.class);
    }

    public static void forEachResource(URL source, Consumer<Path> consumer) {
        try {
            PathReference pathReference = PathReference.of(source.toURI());

            Files.walkFileTree(pathReference.path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    consumer.accept(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    private record PathReference(Path path, FileSystem fileSystem) {

        public static PathReference of(URI resource) throws IOException {
            try {
                // first try getting a path via existing file systems
                return new PathReference(Paths.get(resource), null);
            } catch (final FileSystemNotFoundException e) {
                // This generally occurs when the file is in a .jar file.
                final Map<String, ?> env = Collections.emptyMap();
                final FileSystem fs = FileSystems.newFileSystem(resource, env);
                return new PathReference(fs.provider().getPath(resource), fs);
            }

        }
    }
}
