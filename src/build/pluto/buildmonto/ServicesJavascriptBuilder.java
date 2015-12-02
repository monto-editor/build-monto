package build.pluto.buildmonto;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.output.None;
import build.pluto.output.Out;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServicesJavascriptBuilder extends Builder<ServicesJavascriptInput, None> {

    public static BuilderFactory<ServicesJavascriptInput, None, ServicesJavascriptBuilder> factory
        = BuilderFactoryFactory.of(ServicesJavascriptBuilder.class, ServicesJavascriptInput.class);

    public ServicesJavascriptBuilder(ServicesJavascriptInput input) {
        super(input);
    }

    @Override
    public File persistentPath(ServicesJavascriptInput input) {
        return new File(input.targetDir, "services-javascript.dep");
    }

    @Override
    protected String description(ServicesJavascriptInput input) {
        return "Build monto:services-javascript";
    }

    @Override
    protected None build(ServicesJavascriptInput input) throws Throwable {
        //get services-base-java src from git
        GitInput gitInput = GitSettings.toInput();
        BuildRequest<?, ?, ?, ?> gitRequest =
            new BuildRequest<>(GitRemoteSynchronizer.factory, gitInput);
        this.requireBuild(gitRequest);

        //compile services-base-java
        ServicesBaseJavaInput baseInput = new ServicesBaseJavaInput(
                GitSettings.baseSrc,
                input.baseTarget,
                input.baseJar,
                Arrays.<BuildRequest<?, ?, ?, ?>>asList(gitRequest));

        BuildRequest<?, ?, ?, ?> baseRequest =
            new BuildRequest<>(ServicesBaseJavaBuilder.factory, baseInput);
        this.requireBuild(baseRequest);

        //resolve maven dependencies
        MavenInput mavenInput = new MavenInput.Builder(
                    new File("lib"),
                    Arrays.asList(
                        MavenDependencies.JEROMQ,
                        MavenDependencies.JSON,
                        MavenDependencies.COMMONS_CLI,
                        MavenDependencies.ANTLR)).build();
        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);
        ArrayList<File> classpath = this.requireBuild(mavenRequest).val();

        //compile src
        classpath.add(input.baseJar);
        List<BuildRequest<?, ?, ?, ?>> requiredUnits =
            Arrays.<BuildRequest<?, ?, ?, ?>>asList(baseRequest, mavenRequest);
        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                input.srcDir,
                input.targetDir,
                classpath,
                requiredUnits);
        this.requireBuild(javaRequest);

        //build jar
        File manifest = new File("sjs-manifest.txt");
        this.require(manifest);
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator mfGenerator = new ManifestFileGenerator(
                currentWorkingDir,
                manifest,
                "1.0",
                "monto.service.ecmascript.ECMAScriptServices",
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
