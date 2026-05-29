import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
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

    public void sendln(String data) {
        out.println(data);
    }
    public void send(String data) {
        out.print(data);
        out.flush();
    }

    public int readBytes(byte[] buf) {
        try {
            InputStream s = socket.getInputStream();
            return s.read(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendBytes(byte[] buf, int length) {
        try {
            OutputStream s = socket.getOutputStream();
            s.write(buf, 0, length);
            s.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String receiveLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String receiveAll() {
        return receiveAll(null);
    }

    public String receiveAll(int[] out) {
        StringBuilder sb = new StringBuilder();
        if (out == null || out.length < 1) {
            out = new int[1];
        }

        try {
            String line;
            out[0] = 0;
            while ((line = in.readLine()) != null) {
                sb.append(line).append("\n");
                out[0] += line.length() + 1; // line length + new line
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
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
