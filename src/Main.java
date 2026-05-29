import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length < 2) {
            throw new IllegalStateException("Args needs to have a command and at least 1 parameter.");
        }

        // dynamic index variable
        int argPointer = 0;
        String cmd = args[argPointer++]; // cmd guaranteed
        String arg1 = args[argPointer++]; // arg1 guaranteed

        // some commands need multiple args, we decide that here.
        boolean useTwoArgs = List.of("cp", "mv").contains(cmd);

        // if we see the remote path first, then we know we are downloading, otherwise its an upload
        boolean shouldDownload = arg1.contains("ftp://");

        // some commands dont have a second arg, so it may be null.
        String arg2 = useTwoArgs ? args[argPointer] : null;
       //shouldDownload ? new FTPConnection(arg1) : new FTPConnection(arg2); // one must be the connection

        if (!shouldDownload) {
            // swap the args here, as dispatch takes them in a specific order: remote, then local
            String temp = arg1;
            arg1 = arg2;
            arg2 = temp;
        }
        FTPConnection conn = new FTPConnection();
        // by this point, args will look ideal for dispatch()
        conn.dispatch(cmd, arg1, arg2, shouldDownload);
        // end the connection to the server
        conn.shutDown();
    }
}
