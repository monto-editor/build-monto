package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import build.pluto.builder.BuildRequest;

public class ServicesBaseJavaInput implements Serializable {
    private static final long serialVersionUID = -8432928706675953694L;

    public final File target;
    public final File jarLocation;
    public List<BuildRequest<?, ?, ?, ?>> requiredUnits;

    public ServicesBaseJavaInput(
            File target,
            File jarLocation,
            List<BuildRequest<?, ?, ?, ?>> requiredUnits) {
        this.target = target;
        this.jarLocation = jarLocation;
        this.requiredUnits = requiredUnits;
    }
}
