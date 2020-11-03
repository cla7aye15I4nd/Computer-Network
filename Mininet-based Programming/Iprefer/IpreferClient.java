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
            long save = System.nanoTime(), realtotal;            
            
            int counter = 0;
            while (true) {
                out.write(packet, 0, packet.length);
                counter++;
		        realtotal = System.nanoTime() - save;
		        if (realtotal >= total)
		            break;
            }            
            
            out.close();
            socket.close();

            double rate = counter * 8 / 1000 / (realtotal / 1000000000.0);
            System.out.printf("sent=%dKB rate=%f Mbps\n", counter, rate);
        } catch (IOException e) {
            System.out.println("IpreferClient run error.");
        }
    }
};
