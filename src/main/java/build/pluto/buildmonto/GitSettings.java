package build.pluto.buildmonto;

import build.pluto.buildgit.FastForwardMode;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.bound.BranchBound;
import build.pluto.buildgit.bound.UpdateBound;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GitSettings {

    public static final String baseURL =
        "https://github.com/monto-editor/services-base-java";
    public static final File baseSrc = new File("services-base-java");

    public static final List<String> branchesToClone = null;
    public static final boolean cloneSubmodules = false;
    public static final FastForwardMode ffMode = FastForwardMode.FF_ONLY;
    // public static final MergeStrategy mergeStrategy;
    public static final boolean createMergeCommit = true;
    public static final boolean squashCommit = false;
    public static final UpdateBound bound = new BranchBound("master", baseURL);
    public static long consistencyCheckInterval = TimeUnit.HOURS.toMillis(2);

    public static GitInput toInput() {
        GitInput.Builder builder = new GitInput.Builder(baseSrc, baseURL);
        if (branchesToClone != null) {
            for (String branch : branchesToClone) {
                builder = builder.addBranchToClone(branch);
            }
        }
        builder = builder.setCloneSubmodules(cloneSubmodules)
                         .setFfMode(ffMode)
                         .setCreateMergeCommit(createMergeCommit)
                         .setSquashCommit(squashCommit)
                         .setBound(bound)
                         .setConsistencyCheckInterval(consistencyCheckInterval);
        return builder.build();

    }
}
