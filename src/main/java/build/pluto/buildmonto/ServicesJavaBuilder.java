package build.pluto.buildmonto;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildhttp.HTTPDownloader;
import build.pluto.buildhttp.HTTPInput;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.None;
import build.pluto.output.Out;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        GitInput gitInput = GitSettings.toInput();
        BuildRequest<?, ?, ?, ?> gitRequest =
            new BuildRequest<>(GitRemoteSynchronizer.factory, gitInput);
        this.requireBuild(gitRequest);

        //compile services-base-java and build jar
        ServicesBaseJavaInput baseInput = new ServicesBaseJavaInput(
                GitSettings.baseSrc,
                input.baseTarget,
                input.baseJar,
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
                        MavenDependencies.COMMONS_CLI)).build();
        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);
        ArrayList<File> classpath = this.requireBuild(mavenRequest).val();

        //get antlr-4.4-complete
        HTTPInput httpInput = new HTTPInput(
                 "http://www.antlr.org/download/antlr-4.4-complete.jar",
                 new File("lib/antlr-4.4-complete.jar"),
                 RemoteRequirement.NEVER_CHECK);
        BuildRequest<?, ?, ?, ?> httpRequest =
            new BuildRequest<>(HTTPDownloader.factory, httpInput);
        this.requireBuild(httpRequest);

        //compile src
        classpath.add(new File("lib/antlr-4.4-complete.jar"));
        classpath.add(input.baseJar);
        List<BuildRequest<?, ?, ?, ?>> requiredUnits =
            Arrays.asList(baseRequest, mavenRequest, httpRequest);
        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                input.srcDir,
                input.targetDir,
                classpath,
                requiredUnits);
        this.requireBuild(javaRequest);

        //build jar
        File manifest = new File("sj-manifest.txt");
        this.require(manifest);
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator mfGenerator = new ManifestFileGenerator(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.java8.JavaServices",
                classpath,
                false);
        mfGenerator.generate();
        BuildRequest<?, ?, ?, ?>[] requiredUnitsForJar = { javaRequest };
        BuildRequest<?, ?, ?, ?> jarRequest = JavaUtil.createJar(
                input.targetDir,
                input.jarLocation,
                manifest,
                requiredUnitsForJar);
        this.requireBuild(jarRequest);
        return None.val;
    }
}
