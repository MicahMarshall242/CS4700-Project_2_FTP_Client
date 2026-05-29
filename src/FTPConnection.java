import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class FTPConnection {
    private SocketWrapper controlChannel; // FTP Requests *sending* such as USER <username>\r\n
    private SocketWrapper dataChannel; //
    private static final int controlChannelDefaultPort = 21;

    private static final boolean print = true;


    public void dispatch(String cmd, String remoteURL, String localPath, boolean download) {
        URI handle;
        try {
            handle = establishConnection(remoteURL);
        } catch (URISyntaxException e) {
            System.out.println("Bad path.");
            throw new RuntimeException(e);
        }
        String remotePath = handle.getPath();

        switch (cmd) {
            case "ls": {
                // LIST
                send("LIST " + remotePath);
                int[] out = {-1};
                System.out.println(dataChannel.receiveAll(out)); // ls is string data
                break;
            }
            case "rm": {send("DELE " + remotePath);break;}
            case "mkdir": {send("MKD " + remotePath);break;}
            case "rmdir": {send("RMD " + remotePath);break;}
            case "mv": {
                // down/upload then delete
            }
            case "cp": {
                if (download) {
                    download(remotePath, localPath);
                } else {
                    upload(remotePath, localPath);
                }
                break;
            }
            default: {
                // nothing
            }
        }
    }


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

    private void upload(String remotePath, String localPath) {
        send("STOR " + remotePath);
        try {
            File local = new File(localPath);
            FileInputStream fi = new FileInputStream(local);
            byte[] buffer = new byte[8192]; // large enough      (-_-(-_-)-_-)
            int bytesRead;
            while ((bytesRead = fi.read(buffer)) != -1) {
                dataChannel.sendBytes(buffer, bytesRead);
            }

            fi.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutDown() {
        dataChannel.close();
        send("QUIT");
        controlChannel.close();
    }

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
//            System.out.println("- - - - - - - - - - - - -");
            openControlChannel(host, port, user, pass);
//            System.out.println("- - - - - - - - - - - - -");
            openDataChannel();
        } catch (IOException e) {
            System.out.println("An error occurred when opening communication channels.");
            throw new RuntimeException(e);
        }
        return externalHandle;

    }

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

    private void openDataChannel() throws IOException {
        // System.out.println("opening data channel...");
        controlChannel.sendln("PASV");
        String channelInfo = controlChannel.receiveLine();
        channelInfo = channelInfo.split(" ")[4];
        channelInfo = channelInfo.substring(1, channelInfo.length() - 2);
        // System.out.println(channelInfo);
        String[] segments = channelInfo.split(","); // 6 #s
        //new ArrayList<>(List.of(segments)).subList(0, 4).;

        int dataPort = (Integer.parseInt(segments[4]) << 8) + Integer.parseInt(segments[5] /* << 0 */);
        String ip = segments[0] + "." + segments[1] + "." + segments[2] + "." + segments[3];
        // System.out.println(ip);

        dataChannel = new SocketWrapper(ip, dataPort);
    }

    private void send(String msg, boolean print) {
        controlChannel.sendln(msg + "\r\n");
        String fb = controlChannel.receiveLine();
        if (print) System.out.println(fb);
    }

    private void send(String msg) {
        controlChannel.sendln(msg + "\r\n");
        String fb = controlChannel.receiveLine();
        if (print) System.out.println(fb);
    }


}
