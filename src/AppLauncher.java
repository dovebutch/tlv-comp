// This is a legacy class, only kept around to keep the build flow happy.

//import com.jdotsoft.jarloader.JarClassLoader;
import tlv.Main;

public class AppLauncher {

	public static void main(String[] args) {
            Main.main(args);
	    //JarClassLoader jcl = new JarClassLoader();
	    //try {
	    //	jcl.invokeMain("tlv.Main", args);
	    //} catch (Throwable e) {
	    //	e.printStackTrace();
	    //}
	} // main()

} // class MyAppLauncher
