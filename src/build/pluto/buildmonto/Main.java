package build.pluto.buildmonto;

import java.io.File;

import org.sugarj.common.Log;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;

public class Main {

	public static void main(String[] args) throws Throwable {
		Log.log.setLoggingLevel(Log.DETAIL);
		
		ServicesJava.Input javaInput = new ServicesJava.Input(
      		  new File ("target/services-java"),
      		  new File ("target/services-java.jar"),
      		  null);
        BuildManagers.build(new BuildRequest<>(ServicesJava.factory, javaInput));
        
		ServicesJavascript.Input jsInput = new ServicesJavascript.Input(
    		  new File ("target/services-javascript"),
    		  new File ("target/services-javascript.jar"),
    		  null);
      BuildManagers.build(new BuildRequest<>(ServicesJavascript.factory, jsInput));
	}

}
