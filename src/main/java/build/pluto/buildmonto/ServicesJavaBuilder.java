package build.pluto.buildmonto;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.buildmaven.MavenDependencyFetcher;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.output.None;

import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;

import build.pluto.buildjava.JavaBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.output.Out;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sugarj.common.FileCommands;
import build.pluto.buildjava.util.FileExtensionFilter;


public class ServicesJavaBuilder extends Builder<ServicesJavaInput, None> {

    public static BuilderFactory<ServicesJavaInput, None, ServicesJavaBuilder> factory
        = BuilderFactory.of(ServicesJavaBuilder.class, ServicesJavaInput.class);

    public ServicesJavaBuilder(ServicesJavaInput input) {
        super(input);
    }

    @Override
    protected File persistentPath(ServicesJavaInput input) {
        return new File(input.srcDir, "services-java.dep");
    }

    @Override
    protected String description(ServicesJavaInput input) {
        return "Build monto:services-base-java";
    }

    @Override
    protected None build(ServicesJavaInput input) throws Throwable {
        //get services-base-java src from git
        // GitInput.Builder gitInBuilder = new GitInput.Builder(input.servicesBaseJavaDir, input.servicesBaseJavaGitURL);
        // this.requireBuild(GitRemoteSynchronizer.factory, gitInBuilder.build());

        //compile services-base-java
        // ServicesBaseJavaInput sbjInput = null;//new ServicesBaseJavaInput();
        // this.requireBuild(ServicesBaseJavaBuilder.factory, sbjInput);

        //resolve maven dependencies
        MavenInput.Builder mavenInputBuilder = new MavenInput.Builder(
                    new File("lib"),
                    Arrays.asList(
                        MavenDependencies.JEROMQ,
                        MavenDependencies.JSON,
                        MavenDependencies.ANTLR,
                        MavenDependencies.COMMONS_CLI));

        Out<ArrayList<File>> classPath =
            this.requireBuild(MavenDependencyFetcher.factory, mavenInputBuilder.build());
        //compile src

        List<BuildRequest<?, ?, ?, ?>> requiredUnits = new ArrayList();
        requiredUnits.add(new BuildRequest(MavenDependencyFetcher.factory, mavenInputBuilder.build()));

        FileFilter javaFileFilter = new FileExtensionFilter("java");

        List<Path> javaSrcPathList =
            FileCommands.listFilesRecursive(input.srcDir.toPath(), javaFileFilter);

        List<File> javaSrcFileList = new ArrayList<>();
        for(Path p : javaSrcPathList) {
            javaSrcFileList.add(p.toFile());
        }

        List<File> cP = classPath.val();
        cP.add(new File("services-java/lib/antlr-4.4-complete.jar"));
        cP.add(new File("services-java/lib/services-base-java.jar"));
        List<File> sourcePath = Arrays.asList(input.srcDir);
        for (File f : javaSrcFileList) {
            JavaInput javaInput = new JavaInput(
                    f,
                    input.targetDir,
                    sourcePath,
                    cP,
                    null,
                    requiredUnits);
            ArrayList<JavaInput> javaInputList =
                new ArrayList<>(Arrays.asList(javaInput));
            requireBuild(JavaBuilder.factory, javaInputList);
        }
        //build jar
        return None.val;
    }
}
