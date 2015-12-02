package build.pluto.buildmonto;

import build.pluto.buildmaven.input.ArtifactConstraint;
import build.pluto.buildmaven.input.Dependency;
import build.pluto.dependency.RemoteRequirement;

public class MavenDependencies {
    public static final Dependency JSON = 
        new Dependency(
                new ArtifactConstraint(
                    "com.googlecode.json-simple",
                    "json-simple",
                    "1.1.1",
                    null,
                    null),
        		RemoteRequirement.CHECK_NEVER);
    public static final Dependency JEROMQ =
        new Dependency(
            new ArtifactConstraint(
                    "org.zeromq",
                    "jeromq",
                    "0.3.4",
                    null,
                    null),
    		RemoteRequirement.CHECK_NEVER);
    public static final Dependency ANTLR =
        new Dependency(
            new ArtifactConstraint(
                    "org.antlr",
                    "antlr4-runtime",
                    "4.5",
                    null,
                    null),
    		RemoteRequirement.CHECK_NEVER);
    public static final Dependency COMMONS_CLI =
        new Dependency(
            new ArtifactConstraint(
                    "commons-cli",
                    "commons-cli",
                    "1.3.1",
                    null,
                    null),
    		RemoteRequirement.CHECK_NEVER);
}
