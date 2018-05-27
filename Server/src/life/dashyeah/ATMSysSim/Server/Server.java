package life.dashyeah.ATMSysSim.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Server {

	public static void main(String[] args) {
		System.out.println("code: "+Cfg.getBankCode()+"\n"+
				"name: "+Cfg.getBankName());
		//System.out.println("Servers: "+Cfg.getServerList().toJSONString());
		
		serve();
	}
	
	@SuppressWarnings("resource")
	public static void serve(){
		ServerSocket server = null;
		ArrayList<Thread> ser = new ArrayList<>();
		
		try {
			server = new ServerSocket(2333);
			//SocketAddress endpoint;
			//server.bind(endpoint);
		} catch (IOException e) {
			System.err.println("[ERROR] cannot cerate ServerSocket.");
			e.printStackTrace();
			return;
		}
		
		Socket socket = null;
		while (true) {
			try {
				socket = server.accept();
				Thread t = new Thread(new Service(socket));
				ser.add(t);
				t.start();
				System.out.println("  new thread: "+t.getId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
		}
	}
}

class Addr extends SocketAddress{
	
}