package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.Builder;
import build.pluto.builder.BuilderFactory;
import build.pluto.builder.BuilderFactoryFactory;
import build.pluto.buildgit.GitInput;
import build.pluto.buildgit.GitRemoteSynchronizer;
import build.pluto.buildgit.bound.BranchBound;
import build.pluto.buildmaven.MavenDependencyResolver;
import build.pluto.buildmaven.input.MavenInput;
import build.pluto.buildmonto.util.JavaUtil;
import build.pluto.buildmonto.util.ManifestFileGenerator;
import build.pluto.dependency.RemoteRequirement;
import build.pluto.output.None;
import build.pluto.output.Out;

public class ServicesBaseJava extends Builder<ServicesBaseJava.Input, None> {
	
    public static final String REPO_URL = "https://github.com/monto-editor/services-base-java";
//	public static final String REPO_URL = "file:///Users/seba/projects/monto/services-base-java";

    public static class Input implements Serializable {
        private static final long serialVersionUID = -8432928706675953694L;

        public final File target;
        public final File jarLocation;
        public List<BuildRequest<?, ?, ?, ?>> requiredUnits;

        public Input(
                File target,
                File jarLocation,
                List<BuildRequest<?, ?, ?, ?>> requiredUnits) {
            this.target = target;
            this.jarLocation = jarLocation;
            this.requiredUnits = requiredUnits;
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
    	
        //get services-base-java src from git
        GitInput gitInput = 
        		new GitInput.Builder(checkoutDir, REPO_URL)
                .setBound(new BranchBound(REPO_URL, "master"))
                .setConsistencyCheckInterval(RemoteRequirement.CHECK_ALWAYS)
                .build();
        BuildRequest<?,?,?,?> gitRequest = new BuildRequest<>(GitRemoteSynchronizer.factory, gitInput);
        requireBuild(gitRequest);

        //resolve maven dependencies
        MavenInput mavenInput = 
        		new MavenInput.Builder()
        		.addDependency(MavenDependencies.JEROMQ)
        		.addDependency(MavenDependencies.JSON)
        		.build();

        BuildRequest<?, Out<ArrayList<File>>, ?, ?> mavenRequest =
            new BuildRequest<>(MavenDependencyResolver.factory, mavenInput);
        ArrayList<File> classpath =  this.requireBuild(mavenRequest).val();

        //compile src
        List<BuildRequest<?, ?, ?, ?>> requiredForJavac = new ArrayList<>();
        requiredForJavac.add(gitRequest);
        requiredForJavac.add(mavenRequest);
        if(input.requiredUnits != null)
            requiredForJavac.addAll(input.requiredUnits);
        BuildRequest<?, ?, ?, ?> javaRequest = JavaUtil.compileJava(
                checkoutDir,
                input.target,
                classpath,
                requiredForJavac);
        requireBuild(javaRequest);

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
        BuildRequest<?,?,?,?> mfGeneratorReq = new BuildRequest<>(ManifestFileGenerator.factory, mfGeneratorInput);
        
        BuildRequest<?, ?, ?, ?>[] requiredUnitsForJar = { javaRequest, mfGeneratorReq };
        BuildRequest<?, ?, ?, ?> jarRequest = JavaUtil.createJar(
                input.target,
                input.jarLocation,
                manifest,
                requiredUnitsForJar);
        this.requireBuild(jarRequest);
        return None.val;
    }
}
