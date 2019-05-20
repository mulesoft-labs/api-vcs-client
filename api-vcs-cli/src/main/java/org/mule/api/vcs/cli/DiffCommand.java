package org.mule.api.vcs.cli;

import org.mule.api.vcs.client.ApiVCSClient;
import org.mule.api.vcs.client.ValueResult;
import org.mule.api.vcs.client.diff.Diff;
import org.mule.api.vcs.client.service.impl.ApiRepositoryFileManager;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;

@Command(description = "Show diffs",
        name = "diff", mixinStandardHelpOptions = true, version = "checksum 0.1")
public class DiffCommand extends BaseCommand implements Callable<Integer> {


    @Override
    public Integer call() throws Exception {
        final ApiVCSClient apiVCSClient = new ApiVCSClient(new File("."), new ApiRepositoryFileManager(getAccessTokenProvider()));
        final ValueResult<List<Diff>> mayBeDiffs = apiVCSClient.diff();
        if (mayBeDiffs.isFailure()) {
            System.err.println(mayBeDiffs.getMessage().get());
            return -1;
        }
        if (mayBeDiffs.doGetValue().isEmpty()) {
            System.out.println("No differences found.");
        } else {
            for (Diff diff : mayBeDiffs.doGetValue()) {
                diff.print(new PrintWriter(System.out));
            }
        }
        return 1;
    }
}
