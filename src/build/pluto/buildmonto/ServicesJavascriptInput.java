package build.pluto.buildmonto;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import build.pluto.builder.BuildRequest;

public class ServicesJavascriptInput implements Serializable {
    private static final long serialVersionUID = 6927714821370770411L;

    public final File targetDir;
    public final File jarLocation;
    public List<BuildRequest<?, ?, ?, ?>> requiredUnits;

    public ServicesJavascriptInput(File targetDir, File jarLocation, List<BuildRequest<?, ?, ?, ?>> requiredUnits) {
        this.targetDir = targetDir;
        this.jarLocation = jarLocation;
        this.requiredUnits = requiredUnits;
    }
}
