package forensiq.assignment.search;

import forensiq.assignment.data.Either;
import forensiq.assignment.data.Pair;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static forensiq.assignment.data.Either.fail;
import static forensiq.assignment.data.Either.succeed;

/**
 * Match regexps over one file.
 */
public class SearchInFile {

    // Note: we cannot use Files.lines(path), because streams cannot support line numbers (think parallel execution),
    // and .lines is all or nothing. We'd like to look at the readable initial portion of a file even if it becomes
    // unavailable in the process, e.g. moved by log rotation.
    // So actual file lines reading is done by an iterator.

    private final String file_name;
    private final Path path;

    public SearchInFile(Path path) {
        this.path = path;
        file_name = path.toString();
    }

    // TODO: handle IO errors
    public Stream<Either<Hit, String>> forRegexp(final Pattern regexp) {
        final File file = path.toFile();
        return numberedLines(file).flatMap(maybe_line -> {
            return maybe_line.is_success ? findInLine(maybe_line.asSuccess(), regexp) :
                    Stream.of(fail(maybe_line.asFailure()));  // We want errors to bubble up.
        });
    }

    private Stream<Either<Pair<Integer, String>, String>> numberedLines(File file) {
        try {
            final LineNumberReader numbering_reader = new LineNumberReader(new FileReader(file));
            Runnable close_reader = () -> { try { numbering_reader.close(); } catch (IOException ignore) { } };
            return StreamSupport.stream(Spliterators.spliterator(
                    new LineIterator(numbering_reader), 1, Spliterator.IMMUTABLE | Spliterator.NONNULL), false)
                    .onClose(close_reader);  // We can Stream.close() and the reader and file will close, too.
        } catch (FileNotFoundException e) {
            return Stream.of(fail("Cannot start reading " + file_name + ": " + e));
        }
    }

    private Stream<Either<Hit, String>> findInLine(final Pair<Integer, String> numbered_line, Pattern regexp) {
        // We assume that we have enough RAM to store all the hits in a line.
        Stream.Builder<Either<Hit, String>> sb = Stream.builder();
        final String line = numbered_line.second;
        Matcher matcher = regexp.matcher(line);
        int offset = 0;
        final int max_offset = line.length() - 1;
        while (offset < max_offset && matcher.find(offset)) {
            sb.add(succeed(new Hit(file_name, line, numbered_line.first, matcher.start(), matcher.end())));
            offset = matcher.end() + 1;
        }
        return sb.build();
    }

    // TODO: think about bolting line numbering to Files.lines() stream.
    private class LineIterator implements Iterator<Either<Pair<Integer, String>, String>> {
        private final LineNumberReader reader;
        private Either<Pair<Integer, String>, String> latest_item;
        private boolean still_going = true;

        LineIterator(LineNumberReader numbering_reader) {
            reader = numbering_reader;
        }

        private void proceed() {
            // Try to fetch a line.
            try {
                String line = reader.readLine();
                if (line != null) {
                    latest_item = succeed(new Pair<>(reader.getLineNumber(), line));
                    still_going = true;  // Not needed, stays here for symmetry.
                } else {
                    latest_item = fail("Iterating past end, if you see it it's a bug");
                    still_going = false;
                    reader.close();
                }
            } catch (IOException e) {
                latest_item = fail("Error reading line " + reader.getLineNumber() + ": " + e);
                still_going = true;  // We want this error result to be included.
                try {
                    reader.close();   // Try our best to free resources, but ignore further I/O errors.
                } catch (IOException ignore) { }

            }
        }

        @Override
        public boolean hasNext() {
            if (still_going && latest_item == null) {
                    proceed();
            }
            return still_going;
        }

        @Override
        public Either<Pair<Integer, String>, String> next() {
            if (hasNext()) {
                final Either<Pair<Integer, String>, String> result = latest_item;
                latest_item = null; // Allow to proceed.
                if (! result.is_success) {
                    // The last fetch ended up in an error. Stop proceeding.
                    still_going = false;
                }
                return result;
            } else throw new NoSuchElementException();
        }
    }
}
