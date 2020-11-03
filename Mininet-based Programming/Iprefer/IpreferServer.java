import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

class IpreferServer{
    int port;

    public IpreferServer(int port) {
	    this.port = port;
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            Socket client = server.accept();

            byte[] buffer = new byte[1000];
            DataInputStream in = new DataInputStream(client.getInputStream());

            long bytes = 0, length;
            long save = System.nanoTime();
            while (true) {
                length = in.read(buffer, 0, buffer.length);
                if (length < 0)
                    break;
                bytes += length;
            }

            double total = (System.nanoTime() - save) / 1000000000.0;
            double rate = bytes / 1000000 * 8 / total;

            in.close();        
            client.close();   
            server.close();

            System.out.printf("received=%d KB rate=%f Mbps\n", bytes / 1000, rate);
        }catch (IOException e) {
            System.out.println("IpreferServer run error");
        }
    }
};
