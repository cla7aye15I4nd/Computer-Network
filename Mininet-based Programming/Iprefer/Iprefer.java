import java.net.Socket;
import java.net.ServerSocket;

public class Iprefer{
    static final int CLIENT = 1;
    static final int SERVER = 2;
    
    public static void main(String args[]) {
		Argument arg = parseArgument(args);
		if (arg.type == CLIENT) {
			IpreferClient client = new IpreferClient(arg.port, arg.host, arg.time);
			client.run();
		} else {
			IpreferServer server = new IpreferServer(arg.port);
			server.run();
		}	    
    }    

    static Argument parseArgument(String args[]) {
		Argument arg = new Argument();

		int mode = 0;
		for (int i = 0; i < args.length; i++) {
			if (mode == 1) {
				mode = 0;
				arg.port = Integer.parseInt(args[i]);				
			}else if (mode == 2) {
				mode = 0;
				arg.host = args[i];
			}else if (mode == 3) {
				mode = 0;
				arg.time = Double.parseDouble(args[i]);				
			}else if (args[i].equals("-c"))
				arg.type = CLIENT;
			else if (args[i].equals("-s"))
				arg.type = SERVER;
			else if (args[i].equals("-p"))
				mode = 1;
			else if (args[i].equals("-h"))
				mode = 2;
			else if (args[i].equals("-t"))
				mode = 3;
			
		}

		if (arg.type == 0) {
			System.out.println("Error: missing or additional arguments");
			System.exit(-1);
		}
		if (arg.port < 1024 || arg.port > 65535) {
			System.out.println("Error: port number must be in the range 1024 to 65535");
			System.exit(-1);
		}

		
		return arg;
    }
}

class Argument{
    int type;
    int port;
    String host;
    double time;

    public Argument() {
		type = 0;
		port = 0;
		host = "unknown";
		time = -1;
    }
};

