import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This class is responsible for communicating with and facilitating data transfers between the client and FTP server.
 */
public class FTPConnection {
    private SocketWrapper controlChannel; // FTP Requests *sending* such as USER <username>\r\n
    private SocketWrapper dataChannel; //
    private static final int controlChannelDefaultPort = 21;
    private static final boolean print = true; // debug

    /**
     * This method takes the provided command and other file path data and executes it, processing all data transfers
     * and queries of the server.
     */
    public void dispatch(String cmd, String remoteURL, String localPath, boolean download) {
        URI handle; // save the handle to get its properties later
        try {
            handle = establishConnection(remoteURL); // connect to the server
        } catch (URISyntaxException e) {
            System.out.println("Bad path.");
            throw new RuntimeException(e);
        }
        String remotePath = handle.getPath();

        // now start processing the different commands. The following branches are self-explanatory
        switch (cmd) {
            case "ls": { // LIST
                send("LIST " + remotePath);
                System.out.println(dataChannel.receiveAll(null)); // get all the incoming data
                break;
            }
            case "rm": {send("DELE " + remotePath);break;}
            case "mkdir": {send("MKD " + remotePath);break;}
            case "rmdir": {send("RMD " + remotePath);break;}
            case "mv": { // transfer the file over, then remove it from its origin.
                if (download) {
                    download(remotePath, localPath);
                    send("DELE " + remotePath);
                } else {
                    upload(remotePath, localPath);
                    try {
                        Files.delete(Path.of(localPath));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case "cp": { // download or upload the file
                if (download) {
                    download(remotePath, localPath);
                } else {
                    upload(remotePath, localPath);
                }
                break;
            }
        }
    }

    // Download a file from the server to the specified local path
    private void download(String remotePath, String localPath) {
        send("RETR " + remotePath, true); // tell the server we want to download
        try {
            File local = new File(localPath);
            boolean success = local.createNewFile(); // make local file
            if (success) {
                // begin reading and writing into the file
                FileOutputStream fo = new FileOutputStream(local);
                byte[] out = new byte[8192];
                int bytesRead;
                while ((bytesRead = dataChannel.readBytes(out)) != -1) {
                    fo.write(out, 0, bytesRead);
                }
                fo.close();
            } else {
                throw new RuntimeException("File could not be created");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // Upload a file to the server, from the specified local path
    private void upload(String remotePath, String localPath) {
        send("STOR " + remotePath);
        try {
            File local = new File(localPath);
            FileInputStream fi = new FileInputStream(local);
            byte[] buffer = new byte[8192]; // large enough      (-_-(-_-)-_-)
            int bytesRead;
            while ((bytesRead = fi.read(buffer)) != -1) {
                dataChannel.sendBytes(buffer, bytesRead); // chuck the bytes into the data channel
            }

            fi.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method ends the FTP connection by closing the control and data channels and sending a QUIT message
     * to the server.
     */
    public void shutDown() {
        dataChannel.close();
        send("QUIT");
        controlChannel.close();
    }

    // This method connects to the server with the provided information and initializes the control and data channels
    private URI establishConnection(String externalPath) throws URISyntaxException {
        URI externalHandle = new URI(externalPath);
        String userpass = externalHandle.getUserInfo();
        String user;
        String pass;
        String host = externalHandle.getHost();

        int port = externalHandle.getPort();
        if (port < 0) {
            port = controlChannelDefaultPort;
        }

        if (userpass == null) { // no user provided, use anonymous with no pass
            user = "anonymous";
            pass = "";
        } else if (userpass.contains(":")) {   // user and password provided
            user = userpass.split(":")[0];
            pass = userpass.split(":")[1];
        } else {    // user with no pass provided
            user = userpass;
            pass = "";
        }

        try {
            openControlChannel(host, port, user, pass);
            openDataChannel();
        } catch (IOException e) {
            System.out.println("An error occurred when opening communication channels.");
            throw new RuntimeException(e);
        }
        return externalHandle;

    }

    // Simply opens the control channel for use
    private void openControlChannel(String host, int port, String user, String pass) throws IOException {
        //System.out.println("opening control channel...");
        controlChannel = new SocketWrapper(host, port);
        List<String> inits = List.of(
                String.format("USER %s\r\n", user),
                String.format("PASS %s\r\n", pass),
                "TYPE I\r\n",
                "STRU F\r\n"
        );
        String m = controlChannel.receiveLine();
        // System.out.println(m);
        for (String msg : inits) {
            send(msg, false);
        }
    }
    // simply opens the data channel for use.
    private void openDataChannel() throws IOException {
        controlChannel.sendln("PASV");
        String channelInfo = controlChannel.receiveLine();
        channelInfo = channelInfo.split(" ")[4];
        channelInfo = channelInfo.substring(1, channelInfo.length() - 2);
        String[] segments = channelInfo.split(","); // 6 #s

        int dataPort = (Integer.parseInt(segments[4]) << 8) + Integer.parseInt(segments[5] /* << 0 */);
        String ip = segments[0] + "." + segments[1] + "." + segments[2] + "." + segments[3];

        dataChannel = new SocketWrapper(ip, dataPort); // boom, a data channel
    }

    // a soft wrapper for sending commands to the server.
    private void send(String msg, boolean print) {
        controlChannel.sendln(msg + "\r\n");
        String fb = controlChannel.receiveLine();
        if (print) System.out.println(fb);
    }

    // a soft wrapper for sending commands to the server.
    private void send(String msg) {
        controlChannel.sendln(msg + "\r\n");
        String fb = controlChannel.receiveLine();
        if (print) System.out.println(fb);
    }
}
