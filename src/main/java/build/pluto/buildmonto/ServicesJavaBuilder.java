package build.pluto.buildmonto;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildhttp.HTTPDownloader;
import build.pluto.buildhttp.HTTPInput;
import build.pluto.buildjava.JavaBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.compiler.JavacCompiler;
import build.pluto.buildjava.util.FileExtensionFilter;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestWriter;
import build.pluto.output.None;
import build.pluto.output.Out;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.sugarj.common.FileCommands;


public class ServicesJavaBuilder extends Builder<ServicesJavaInput, None> {

    public static BuilderFactory<ServicesJavaInput, None, ServicesJavaBuilder> factory
        = BuilderFactoryFactory.of(ServicesJavaBuilder.class, ServicesJavaInput.class);

    public ServicesJavaBuilder(ServicesJavaInput input) {
        super(input);
    }

    @Override
    public File persistentPath(ServicesJavaInput input) {
        return new File(input.targetDir, "services-java.dep");
    }

    @Override
    protected String description(ServicesJavaInput input) {
        return "Build monto:services-java";
    }

    @Override
    protected None build(ServicesJavaInput input) throws Throwable {
        //get services-base-java src from git
        GitInput gitInput = new GitInput.Builder(input.servicesBaseJavaDir, input.servicesBaseJavaGitURL)
                .build();
        BuildRequest<?, ?, ?, ?> gitRequest =
            new BuildRequest<>(GitRemoteSynchronizer.factory, gitInput);
        this.requireBuild(gitRequest);

        //compile services-base-java
        File sbjJar = new File("services-base-java.jar");
        ServicesBaseJavaInput baseInput = new ServicesBaseJavaInput(
                input.servicesBaseJavaDir,
                new File("targetsb"),
                sbjJar,
                Arrays.asList(gitRequest));

        BuildRequest<?, ?, ?, ?> baseRequest =
            new BuildRequest<>(ServicesBaseJavaBuilder.factory, baseInput);
        this.requireBuild(baseRequest);
        //resolve maven dependencies
        MavenInput mavenInput = new MavenInput.Builder(
                    new File("lib"),
                    Arrays.asList(
                        MavenDependencies.JEROMQ,
                        MavenDependencies.JSON,
                        MavenDependencies.COMMONS_CLI))
            .build();
        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);

        ArrayList<File> classPath = this.requireBuild(mavenRequest).val();

        //get antlr-4.4-complete
        HTTPInput httpInput = new HTTPInput(
                 "http://www.antlr.org/download/antlr-4.4-complete.jar",
                 new File("lib"),
                 "antlr-4.4-complete.jar",
                 0);//never check for new update

        BuildRequest<?, ?, ?, ?> httpRequest =
            new BuildRequest<>(HTTPDownloader.factory, httpInput);
        this.requireBuild(httpRequest);

        classPath.add(new File("lib/antlr-4.4-complete.jar"));
        classPath.add(sbjJar);

        //compile src
        List<BuildRequest<?, ?, ?, ?>> requiredUnits =
            Arrays.asList(baseRequest, mavenRequest, httpRequest);

        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                input.srcDir,
                input.targetDir,
                classPath,
                requiredUnits);
        this.requireBuild(javaRequest);
        //build jar
        BuildRequest<?, ?, ?, ?>[] requiredUnitsForJar = { javaRequest };
        BuildRequest<?, ?, ?, ?> jarRequest = JavaUtil.createJar(
                input.targetDir,
                input.jarLocation,
                requiredUnitsForJar);
        this.requireBuild(jarRequest);
        return None.val;
    }
}
