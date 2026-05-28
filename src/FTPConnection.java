import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.Socket;

public class FTPConnection {
    Socket controlChannel; // FTP Requests *sending* such as USER <username>\r\n
    Socket dataChannel; //
    private static final int controlChannelPort = 21;

    public void init() {
        SSLSocket sslSock = (SSLSocket) SSLSocketFactory.getDefault().createSocket(hostname, controlChannelPort);
        // send a passV through control channel to open data channel. server response:
        // 227 Entering Passive Mode (192,168,150,90,195,149).
        // first 4 # are the IP, final 2 are port
        // -> open data channel with these #



      //  sslSock.startHandshake();
    }



}
