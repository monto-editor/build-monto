package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.List;

import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildgit.bound.BranchBound;
import build.pluto.buildjava.JavaBulkBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.dependency.Origin;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.None;

public class ServicesBaseJava extends Builder<ServicesBaseJava.Input, None> {
	
    public static final String REPO_URL = "https://github.com/monto-editor/services-base-java";
//	public static final String REPO_URL = "file:///Users/seba/projects/monto/services-base-java";

    public static class Input implements Serializable {
        private static final long serialVersionUID = -8432928706675953694L;

        public final File target;
        public final File jarLocation;

        public Input(
                File target,
                File jarLocation) {
            this.target = target;
            this.jarLocation = jarLocation;
        }
    }
    
    public static BuilderFactory<Input, None, ServicesBaseJava> factory = BuilderFactoryFactory.of(ServicesBaseJava.class, Input.class);

    public ServicesBaseJava(Input input) {
        super(input);
    }

    @Override
    public File persistentPath(Input input) {
        return new File(input.target, "services-base-java.dep");
    }

    @Override
    protected String description(Input input) {
        return "Build monto:services-base-java";
    }

    @Override
    protected None build(Input input) throws Throwable {
    	File checkoutDir = new File(input.target + "-src");
    	
    	Origin.Builder requiredForJar = new Origin.Builder();
    	
        //get services-base-java src from git
        GitInput gitInput = 
        		new GitInput.Builder(checkoutDir, REPO_URL)
                .setBound(new BranchBound(REPO_URL, "master"))
                .setConsistencyCheckInterval(RemoteRequirement.CHECK_ALWAYS)
                .build();
        requireBuild(GitRemoteSynchronizer.factory, gitInput);
        Origin sourceOrigin = Origin.from(lastBuildReq());

        //resolve maven dependencies
        MavenInput mavenInput = 
        		new MavenInput.Builder()
        		.addDependency(MavenDependencies.JEROMQ)
        		.addDependency(MavenDependencies.JSON)
        		.build();
        List<File> classpath = requireBuild(MavenDependencyResolver.factory, mavenInput).val();
        Origin classOrigin = Origin.from(lastBuildReq());

        //compile src
        JavaInput javaInput = JavaUtil.compileJava(
                checkoutDir,
                input.target,
                classpath,
                sourceOrigin,
                classOrigin);
        requireBuild(JavaBulkBuilder.factory, javaInput);
        requiredForJar.add(lastBuildReq());

        //build jar
        File manifest = new File(input.target, "manifest.mf");
        File currentWorkingDir = Paths.get("").toFile();
        ManifestFileGenerator.Input mfGeneratorInput= new ManifestFileGenerator.Input(
                currentWorkingDir,
                manifest,
                "1.0",
                null,
                classpath,
                false);
        requiredForJar.add(ManifestFileGenerator.factory, mfGeneratorInput);
        
        JavaJar.Input jarInput = JavaUtil.createJar(
                input.target,
                input.jarLocation,
                manifest,
                requiredForJar.get());
        requireBuild(JavaJar.factory, jarInput);
        
        return None.val;
    }
}
