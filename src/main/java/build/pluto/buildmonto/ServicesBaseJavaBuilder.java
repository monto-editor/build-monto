package build.pluto.buildmonto;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildjava.JavaBuilder;
import build.pluto.buildjava.compiler.JavacCompiler;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildjava.util.FileExtensionFilter;

import build.pluto.buildmaven.MavenDependencyFetcher;
import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.buildmaven.input.MavenInput;

import build.pluto.output.None;

import build.pluto.output.Out;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.sugarj.common.FileCommands;

public class ServicesBaseJavaBuilder extends Builder<ServicesBaseJavaInput, None> {

    public static BuilderFactory<ServicesBaseJavaInput, None, ServicesBaseJavaBuilder> factory
        = BuilderFactoryFactory.of(ServicesBaseJavaBuilder.class, ServicesBaseJavaInput.class);

    public ServicesBaseJavaBuilder(ServicesBaseJavaInput input) {
        super(input);
    }

    @Override
    public File persistentPath(ServicesBaseJavaInput input) {
        return new File(input.src, "services-base-java.dep");
    }

    @Override
    protected String description(ServicesBaseJavaInput input) {
        return "Build monto:services-base-java";
    }

    @Override
    protected None build(ServicesBaseJavaInput input) throws Throwable {
        //resolve maven dependencies
        MavenInput.Builder mavenInputBuilder = new MavenInput.Builder(
                    new File("lib"),
                    Arrays.asList(MavenDependencies.JEROMQ, MavenDependencies.JSON));

        //compile services-base-java
        // ServicesBaseJavaInput sbjInput = null;//new ServicesBaseJavaInput();
        // this.requireBuild(ServicesBaseJavaBuilder.factory, sbjInput);
        Out<ArrayList<File>> mavenOutput =
            this.requireBuild(MavenDependencyFetcher.factory, mavenInputBuilder.build());

        // //compile src
        List<BuildRequest<?, ?, ?, ?>> requiredUnits = new ArrayList();
        requiredUnits.add(
                new BuildRequest(
                    MavenDependencyFetcher.factory,
                    mavenInputBuilder.build()));

        FileFilter javaFileFilter = new FileExtensionFilter("java");

        List<Path> javaSrcPathList =
            FileCommands.listFilesRecursive(input.src.toPath(), javaFileFilter);

        List<File> sourcePath =
            Arrays.asList(new File(input.src, "src"));
        for (Path p : javaSrcPathList) {
            JavaInput javaInput = new JavaInput(
                    p.toFile(),
                    input.target,
                    sourcePath,
                    null,
                    null,
                    requiredUnits,
                    JavacCompiler.instance);
            requireBuild(JavaBuilder.request(javaInput));
        }
        //build jar out of classfiles
        return None.val;
    }
}
