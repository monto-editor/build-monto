package build.pluto.buildmonto;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class ServicesJavaInput implements Serializable {
    public final String servicesBaseJavaGitURL = "https://github.com/monto-editor/services-base-java";
    public final File servicesBaseJavaDir = new File("services-base-java");
    public final File srcDir;
    public final File targetDir;
    public final File jarLocation = new File("services-java.jar");

    public ServicesJavaInput(File srcDir, File targetDir) {
        this.srcDir = srcDir;
        this.targetDir = targetDir;
    }

    public static void main(String[] args) throws IOException {
        ServicesJavaInput input = new ServicesJavaInput(
                new File("services-java"),
                new File("targetsj"));
        BuildManagers.build(new BuildRequest<>(ServicesJavaBuilder.factory, input));
    }
}
