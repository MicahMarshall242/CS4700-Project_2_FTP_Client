import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketWrapper {
    private Socket socket;
    private  BufferedReader in;
    private  PrintWriter out;
    private final String host;
    private final int port;

    public SocketWrapper(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = //SSLSocketFactory.getDefault().createSocket(host, port);
                    new Socket(host, port);
            // System.out.println(socket.isConnected());
           // socket.startHandshake();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String data) {
        out.println(data);
    }

    public String receive() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeTCP() {
        try {
            SSLSocketFactory fac =  (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket s = (SSLSocket) fac.createSocket(socket, host, port, true);
            s.startHandshake();

            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            socket = s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }








}
