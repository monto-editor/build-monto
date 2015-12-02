package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import build.pluto.builder.BuildRequest;

public class ServicesJavaInput implements Serializable {
    private static final long serialVersionUID = 1952189069839703973L;

    public final File targetDir;
    public final File jarLocation;
    public List<BuildRequest<?, ?, ?, ?>> requiredUnits;
    
    public ServicesJavaInput(File targetDir, File jarLocation, List<BuildRequest<?, ?, ?, ?>> requiredUnits) {
        this.targetDir = targetDir;
        this.jarLocation = jarLocation;
        this.requiredUnits = requiredUnits;
    }
}
