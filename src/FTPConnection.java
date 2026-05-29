import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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

    public FTPConnection() { }


    public void execOut(String cmd, File local, URI external) {
        verify(local);
        establishConnection(external);
    }

    public void execOut(String cmd, URI external) {
        establishConnection(external);
    }

    public void execIn(String cmd, File local, URI external) {
        verify(local);
        establishConnection(external);
    }

    public void execIn(String cmd, File local) {
        verify(local);
    }


    private void verify(File local) {
        assert(local.exists());
    }

    public void shutDown() {
        controlChannel.send("QUIT\r\n");
        controlChannel.close();
        dataChannel.close();
    }

    private void establishConnection(URI externalHandle) {
        port = externalHandle.getPort();
        if (port < 0) { port = controlChannelDefaultPort; }

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


        System.out.println(user);
        System.out.println(pass);
        System.out.println(host);
        System.out.println(port);
        System.out.println(path);
        try {
            openControlChannel();
            openDataChannel();
        } catch (IOException e) {
            System.out.println("An error occurred when opening communication channels.");
            throw new RuntimeException(e);
        }


    }

    private void openControlChannel() throws IOException {
        System.out.println("opening control channel...");
        controlChannel = new SocketWrapper(host, port);
        List<String> inits = List.of(
                String.format("USER %s\r\n", user),
                String.format("PASS %s\r\n", pass),
                "TYPE I\r\n",
                "STRU F\r\n"
        );
        String m = controlChannel.receive();
        System.out.println(m);
        for (String msg : inits) {
            controlChannel.send(msg);
            System.out.println(controlChannel.receive());
        }
    }

    private void openDataChannel() throws IOException {
        System.out.println("opening data channel...");
        controlChannel.send("PASV");
        String channelInfo = controlChannel.receive();
        channelInfo = channelInfo.split(" ")[4];
        channelInfo = channelInfo.substring(1, channelInfo.length() - 2);
        System.out.println(channelInfo);
        String[] segments = channelInfo.split(","); // 6 #s
        //new ArrayList<>(List.of(segments)).subList(0, 4).;

        int dataPort = (Integer.parseInt(segments[4]) << 8) + Integer.parseInt(segments[5] /* << 0 */);
        String ip = segments[0] + "." + segments[1] + "." + segments[2] + "." + segments[3];
        System.out.println(ip);

        dataChannel = new SocketWrapper(ip, dataPort);
    }

}
