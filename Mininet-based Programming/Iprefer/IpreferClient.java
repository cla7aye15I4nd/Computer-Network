import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class IpreferClient{
    int port;
    String host;
    double time;

    public IpreferClient(int port, String host, double time) {
        this.port = port;
        this.host = host;
        this.time = time;
    }

    public void run() {
        try {
            Socket socket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            byte[] packet= new byte[1000];

            long total = (long) (this.time * 1000000000);
            long save = System.nanoTime();            
            
            int counter = 0;
            while (System.nanoTime() - save < total) {
                out.write(packet, 0, packet.length);
                counter++;
            }            
            
            out.close();
            socket.close();	
            
            double rate = counter * 8 / 1000 / this.time;
            System.out.printf("sent=%dKB rate=%f Mbps\n", counter, rate);
        } catch (IOException e) {
            System.out.println("IpreferClient run error.");
        }
    }
};
