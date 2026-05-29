import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FTPConnection {
    private SocketWrapper controlChannel; // FTP Requests *sending* such as USER <username>\r\n
    private SocketWrapper dataChannel; //
    private static final int controlChannelDefaultPort = 21;
    private String user;
    private String pass;
    private String host;
    private String path;
    private int port;
    private static final boolean print = false;
    private int[] bytesOut;
    private int[] bytesIn;

    public FTPConnection() {
    }


    public void serverExec(String cmd, URI external, File local) {
        serverExec(cmd, external, local, false);

    }

    // from           to
    public void serverExec(String cmd, URI external, File local, boolean swapInput) {
        String exPath = external.getPath();
        String locPath = null;
        if (local != null) {
            locPath = local.getPath();
        }

        switch (cmd) {
            case "ls": {
                // LIST
                send("LIST " + exPath);
                int[] out = {-1};
                System.out.println(dataChannel.receiveAll(out)); // ls is string data
                break;
            }
            case "rm": {send("DELE " + exPath);break;}
            case "mkdir": {send("MKD " + exPath);break;}
            case "rmdir": {send("RMD " + exPath);break;}
            case "mv": {
                // down/upload then delete
            }
            case "cp": {
                assert local != null;
                if (swapInput) {
                    // upload op
                   upload(locPath, exPath);
                } else {
                    // download op
                    download(exPath, locPath);
                }

                break;
            }
            default: {
                // nothing
            }
        }
    }


    private void download(String fromPath, String toPath) {
        send("RETR " + fromPath, true); // tell the server we want to download
        try {
            File local = new File(toPath);
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
                throw new RuntimeException("File could not create");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void upload(String filepath, String destination) {
        send("STOR " + path);
        try {
            File local = new File(filepath);
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


    public void execOut(String cmd, File local, URI external) {
        verify(local);
        establishConnection(external);
        serverExec(cmd, external, local, true);
    }

    public void execOut(String cmd, URI external) {
        establishConnection(external);
        serverExec(cmd, external, null);
    }

    public void execIn(String cmd, File local, URI external) {
        verify(local);
        establishConnection(external);
        serverExec(cmd, external, local, false);
    }

    // questionable if needed
    public void execIn(String cmd, File local) {
        verify(local);
    }


    private void verify(File local) {
        assert (local.exists());
    }

    public void shutDown() {
        dataChannel.close();
        send("QUIT");
        controlChannel.close();
    }

    private void establishConnection(URI externalHandle) {
        port = externalHandle.getPort();
        if (port < 0) {
            port = controlChannelDefaultPort;
        }

        String username = externalHandle.getUserInfo();
        if (username == null) { // no user provided, use anonymous with no pass
            user = "anonymous";
            pass = "";
        } else if (username.contains(":")) {   // user and password provided
            user = username.split(":")[0];
            pass = username.split(":")[1];
        } else {    // user with no pass provided
            user = username;
            pass = "";
        }

        host = externalHandle.getHost();
        path = externalHandle.getPath();

//        System.out.println("- - - - - - - - - - - - -");
//        System.out.println(user);
//        System.out.println(pass);
//        System.out.println(host);
//        System.out.println(port);
//        System.out.println(path);
        try {
            //            System.out.println("- - - - - - - - - - - - -");
            openControlChannel();
//            System.out.println("- - - - - - - - - - - - -");
            openDataChannel();
        } catch (IOException e) {
            System.out.println("An error occurred when opening communication channels.");
            throw new RuntimeException(e);
        }


    }

    private void openControlChannel() throws IOException {
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
