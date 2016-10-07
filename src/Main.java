import forensiq.assignment.data.Either;
import forensiq.assignment.search.Hit;
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

    private static final String[] help_text = {
            "Usage: <prog> [before <timestamp>] [after <timestamp>] regexp directory_name",
            "",
            "  Timestamps follow format year-month-day:hour:min:sec+offset",
            "e.g. 2016-10-05:10:35:00-0400"
    };

    public static void main(String[] args) {
        if (args.length != 2) {
            // TODO: add help message
            for (String line : help_text) System.out.println(line);
        } else {
            Pattern pattern = null; // TODO: handle parsing errors.
            try {
                pattern = Pattern.compile(args[0]);
            } catch (PatternSyntaxException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            Stream<Either<Path, String>> files = TreeWalk.filesFirst(Paths.get(args[1]));
            final Pattern final_patern = pattern;
            Stream<Either<Hit, String>> hits = files.flatMap(file_entry -> {
                return file_entry.is_success ? new SearchInFile(file_entry.asSuccess()).forRegexp(final_patern) :
                        Stream.of(fail(file_entry.asFailure()));
            });
            Iterator<Either<Hit, String>> hits_iter = hits.iterator();
            while (hits_iter.hasNext()) {
                System.out.println(hits_iter.next());
            }
        }
    }
}
