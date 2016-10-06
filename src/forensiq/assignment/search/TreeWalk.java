package forensiq.assignment.search;

import forensiq.assignment.data.Either;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.newDirectoryStream;

/**
 * Visit the directory tree, never throw an IOException. Make a tree foldable :)
 * We are not interested in stack traces when an I/O error happens, we only want a message to potentially show the user.
 */
public class TreeWalk {

    public static Stream<Either<Path, String>> filesFirst(Path path) {
        Stream<Either<Path, String>> dir_stream;
        Stream<Either<Path, String>> file_stream;
        try {
            // NOTE: we do not check for being readable in the dir filter, but in the map.
            Function<Path, Either<Path, String>> is_readable = (Path name) -> (Files.isReadable(name) ?
                    Either.succeed(name) :
                    Either.fail("Not readable: " + name));
            file_stream = StreamSupport.stream(
                    newDirectoryStream(path, file -> Files.isRegularFile(file)).spliterator(), false)
                    .map(is_readable);
            dir_stream = StreamSupport.stream(
                    newDirectoryStream(path, file_path -> Files.isDirectory(file_path)).spliterator(), false)
                    .map(is_readable);
            Function<Either<Path, String>, Stream<Either<Path, String>>> to_substream = (path_result) ->
                    path_result.is_success ? filesFirst(path_result.asSuccess()) :
                            streamOfOne(path_result);
            return dir_stream.collect(Collectors.reducing(file_stream, to_substream, Stream::concat));
        } catch (IOException e) {
            return streamOfOne(Either.fail("Failed to list files in " + path + ": " + e));
        }
    }

    static <T> Stream <T> streamOfOne(T element) {
        return StreamSupport.stream(Collections.singletonList(element).spliterator() , false);
    }
}


