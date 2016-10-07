import forensiq.assignment.data.Either;
import forensiq.assignment.search.SearchInFile;
import forensiq.assignment.search.TreeWalk;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static forensiq.assignment.data.Either.fail;

/**
 * Command-line entry point.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            // TODO: add help message
            System.out.println("Usage: <prog> regexp directory");
        } else {
            Pattern pattern = null; // TODO: handle parsing errors.
            try {
                pattern = Pattern.compile(args[0]);
            } catch (PatternSyntaxException e) {
                System.err.println(e);  // No, we don't need the stack trace in this case.
                System.exit(1);
            }
            Stream<Either<Path, String>> files = TreeWalk.filesFirst(Paths.get(args[1]));
            final Pattern final_patern = pattern;
            Stream<Either<? extends Object, String>> hits = files.flatMap(file_entry -> {
                return file_entry.is_success ? new SearchInFile(file_entry.asSuccess()).forRegexp(final_patern) :
                        Stream.of(fail(file_entry.asFailure()));
            });
            Iterator<Either<? extends Object, String>> hits_iter = hits.iterator();
            while (hits_iter.hasNext()) {
                System.out.println(hits_iter.next());
            }
        }
    }
}
