import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    private static final int DEFAULT_PORT = 21;
    private static String hostname = null;
    private static int argPointer = 0;
    private static String cmd;
    private static String arg1;
    private static String arg2;

    private static final List<String> twoArgCmds = List.of("cp", "mv");
    private static boolean useTwoArgs;


    public static void main(String[] args) throws URISyntaxException {
        if (args.length < 2) {
            throw new IllegalStateException("Args needs to have a command and at least 1 parameter.");
        }
        cmd  = args[argPointer++]; // cmd will be provided
        arg1 = args[argPointer++]; // arg1 will be provided

        // some commands need multiple args, we decide that here.
        useTwoArgs = twoArgCmds.contains(cmd);

        URI external = null;
        File local = null;
        boolean externalArg1 = arg1.contains("ftp://");

        if (externalArg1) { // discern what arg1 will be
            System.out.print("Server file ");
            external = new URI(arg1);
        } else {
            System.out.print("local file ");
            local = new File(arg1);
        }

        FTPConnection conn = new FTPConnection();
        if (useTwoArgs) { // command uses two arguments
            arg2 = args[argPointer++]; // we know arg2 exists now

            if (externalArg1) { // our first file is on the server
                System.out.println("to device");
                local = new File(arg2); // then the next file must be local
                conn.execIn(cmd, local, external);
            } else {   // our first file is local
                System.out.println("to server");
                external = new URI(arg2); // then the next file must be on the server
                 conn.execOut(cmd, local, external);
            }
        } else { // command takes 1 argument
            System.out.print("\n");
            if (externalArg1) {
                conn.execOut(cmd, external);
              //  System.out.println("Server file");
            } else {
                conn.execIn(cmd, local);
              //  System.out.println("local file");
            }
        }
        conn.shutDown();
    }
}
