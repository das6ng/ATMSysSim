package life.dashyeah.ATMSysSim.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	public static void main(String[] args) {
		System.out.println("[MSG] Current Server: "+Cfg.getBankCode()+"-"+
				Cfg.getBranchCode()+" "+Cfg.getBankName()+Cfg.getBranchName());
		System.out.println("Service strating...");
		serve();
	}
	
	@SuppressWarnings("resource")
	public static void serve(){
		ServerSocket server = null;
		ArrayList<Thread> ser = new ArrayList<>();
		
		try {
			server = new ServerSocket(Integer.parseInt(Cfg.getServicePort()));
		} catch (IOException e) {
			System.err.println("[ERROR] cannot cerate ServerSocket.");
			e.printStackTrace();
			return;
		}
		
		Socket socket = null;
		while (true) {
			try {
				socket = server.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			if(socket != null){
				Thread t = new Thread(new Service(socket));
				ser.add(t);
				t.start();
				//System.out.println("  new thread: "+t.getId());
				socket = null;
			}
		}
	}
}