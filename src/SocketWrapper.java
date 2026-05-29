import java.io.*;
import java.net.Socket;

/**
 * This class serves to wrap the TCP sockets that connect to servers, and provides neat abstractions to reduce
 * boilerplate. SocketWrappers behave almost like an enhanced Socket.
 */
public class SocketWrapper {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    /**
        Constructs a SocketWrapper instance.
     */
    public SocketWrapper(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * sends string data to the server
     * @param data
     */
    public void sendln(String data) {
        out.println(data);
    }

    /**
     * Reads raw bytes into the provided buffer.
     */
    public int readBytes(byte[] buf) {
        try {
            InputStream s = socket.getInputStream();
            return s.read(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    /**
        Sends raw bytes to the internal output stream.
     */
    public void sendBytes(byte[] buf, int length) {
        try {
            OutputStream s = socket.getOutputStream();
            s.write(buf, 0, length);
            s.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Interprets a single line from the input stream as text.
     */
    public String receiveLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads all data present in the input stream as text.
     */
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

    /**
     * closes the underlying socket.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
