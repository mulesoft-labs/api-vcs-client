package org.mule.api.vcs.cli;

import org.mule.api.vcs.client.ApiVCSClient;
import org.mule.api.vcs.client.BranchInfo;
import org.mule.api.vcs.client.ValueResult;
import org.mule.api.vcs.client.service.UserInfoProvider;
import org.mule.api.vcs.client.service.impl.ApiRepositoryFileManager;
import org.mule.designcenter.resource.projects.model.ProjectsGETHeader;
import org.mule.designcenter.responses.ApiDesignerXapiResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Option;

@Command(description = "Clones a project in the given location",
        name = "clone", mixinStandardHelpOptions = true, version = "checksum 0.1")
public class CloneCommand extends BaseCommand implements Callable<Integer> {
    @Parameters(description = "The project to clone.", arity = "1", index = "0")
    String projectName;

    @Parameters(description = "Target directory name", arity = "0..1", index = "1")
    String targetDirectory;


    @Option(names = {"-b", "--branch"}, description = "The branch name.")
    String branch;


    @Override
    public Integer call() throws Exception {
        final UserInfoProvider accessTokenProvider = getAccessTokenProvider();

        String projectId = null;
        final ApiDesignerXapiResponse<List<org.mule.designcenter.resource.projects.model.Project>> xapiResponse = ApiClientFactory.apiDesigner().projects.get(new ProjectsGETHeader(getAccessTokenProvider().getOrgId(), getAccessTokenProvider().getUserId()), getAccessTokenProvider().getAccessToken());
        final List<org.mule.designcenter.resource.projects.model.Project> body = xapiResponse.getBody();
        for (org.mule.designcenter.resource.projects.model.Project project : body) {
            if (project.getName().equals(projectName)) {
                projectId = project.getId();
            }
        }

        String folderName;
        if (projectId != null) {
            if (targetDirectory == null) {
                folderName = projectName;
            } else {
                folderName = targetDirectory;
            }
        } else {
            System.err.println("[Error] Unable to find project with name `" + projectName + "`");
            return -1;
        }

        final File workingDirectory = new File(folderName);
        if (workingDirectory.exists()) {
            System.err.println("[Error] Target directory already exists.");
            return -1;
        }

        final boolean mkdirs = workingDirectory.mkdirs();
        if (!mkdirs) {
            System.err.println("[Error] Unable to create target directory.");
            return -1;
        }

        final ApiVCSClient apiVCSClient = new ApiVCSClient(workingDirectory, new ApiRepositoryFileManager(accessTokenProvider));
        final ValueResult master = apiVCSClient.clone(new BranchInfo(projectId, Optional.ofNullable(branch).orElse("master")));
        if (master.isFailure()) {
            if (master.getMessage().isPresent())
                System.err.println("[Error] " + master.getMessage().get());
            return -1;
        } else {
            return 1;
        }

    }
}
