package build.pluto.buildmonto;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
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

public class ServicesBaseJavaBuilder extends Builder<ServicesBaseJavaInput, None> {

    public static BuilderFactory<ServicesBaseJavaInput, None, ServicesBaseJavaBuilder> factory
        = BuilderFactoryFactory.of(ServicesBaseJavaBuilder.class, ServicesBaseJavaInput.class);

    public ServicesBaseJavaBuilder(ServicesBaseJavaInput input) {
        super(input);
    }

    @Override
    public File persistentPath(ServicesBaseJavaInput input) {
        return new File(input.target, "services-base-java.dep");
    }

    @Override
    protected String description(ServicesBaseJavaInput input) {
        return "Build monto:services-base-java";
    }

    @Override
    protected None build(ServicesBaseJavaInput input) throws Throwable {
        //resolve maven dependencies
        MavenInput mavenInput = new MavenInput.Builder(
                    new File("lib"),
                    Arrays.asList(
                        MavenDependencies.JEROMQ,
                        MavenDependencies.JSON)).build();

        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);
        ArrayList<File> classpath =  this.requireBuild(mavenRequest).val();

        //compile src
        List<BuildRequest<?, ?, ?, ?>> requiredUnits;
        if(input.requiredUnits != null) {
            requiredUnits = new ArrayList<>(input.requiredUnits);
            requiredUnits.add(mavenRequest);
        } else {
            requiredUnits = Arrays.asList(mavenRequest);
        }
        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                input.src,
                input.target,
                classpath,
                requiredUnits);
        this.requireBuild(javaRequest);

        //build jar
        File manifest = new File("sbj-manifest.txt");
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator mfGenerator= new ManifestFileGenerator(
                currentWorkingDir,
                manifest,
                "1.0",
                null,
                classpath,
                false);
        mfGenerator.generate();
        BuildRequest<?, ?, ?, ?>[] requiredUnitsForJar = { javaRequest };
        BuildRequest<?, ?, ?, ?> jarRequest = JavaUtil.createJar(
                input.target,
                input.jarLocation,
                manifest,
                requiredUnitsForJar);
        this.requireBuild(jarRequest);
        return None.val;
    }
}
