package life.dashyeah.ATMSysSim.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Service implements Runnable {
	private Socket socket;
	private String account;
	
	public Service(Socket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		Message msgHello = new Message(System.currentTimeMillis(), "*", "*", -1, 0,"*");
		Message msgError = new Message(System.currentTimeMillis(), "*", "*", 99, 0,"*");
		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter os = new PrintWriter(socket.getOutputStream());
			
			long last = System.currentTimeMillis();
			long current = last;
			int hello = 0;
			while(true){
				String data = "";
				if(is.ready()){ 
					data = is.readLine();
					System.out.println("[MSG] received: " + data);

					Scanner scan = new Scanner(data);
					Message msg = new Message(scan.nextLong(), scan.next(), scan.next(), scan.nextInt(),
							scan.nextDouble(), scan.next());
					scan.close();
					System.out.println(" Message: " + msg.toString());
					
					switch(msg.getOperation()){
					case 0: // login
						if(login(msg)){
							Message msgOK = new Message(System.currentTimeMillis(),msg.getAccountNumber(),"*", 0, 0,"*");
							os.println(msgOK.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 1: // deposit
						if(deposit(msg)){
							Message msgOK = new Message(System.currentTimeMillis(),msg.getAccountNumber(),"*", 1, 0,"*");
							os.println(msgOK.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 2: // withdraw
						if(withdraw(msg)){
							Message msgOK = new Message(System.currentTimeMillis(),msg.getAccountNumber(),"*", 2, 0,"*");
							os.println(msgOK.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 3: // transfer
						if(teansfer(msg)){
							Message msgOK = new Message(System.currentTimeMillis(),msg.getAccountNumber(),"*", 3, 0,"*");
							os.println(msgOK.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 9: //logout
						if(logout(msg)){
							Message msgOK = new Message(System.currentTimeMillis(),msg.getAccountNumber(),"*", 9, 0,"*");
							os.println(msgOK.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case -1: // pulse message
						hello --;
						break;
					default:
						System.err.println("[Warning] received wrong operation code.");
					}
				}
				
				current = System.currentTimeMillis();
				if(current - last > 5000){
					msgHello.setTimeStamp(System.currentTimeMillis());
					os.println(msgHello.toString());
					os.flush();
					hello ++;
				}
				
				if(!socket.isConnected() || hello > 3) throw new Exception("connection lost!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean teansfer(Message msg) {
		return false;
	}

	private boolean withdraw(Message msg) {
		return false;
	}

	private boolean deposit(Message msg) {
		return false;
	}

	private boolean login(Message msg) {
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying login: "+account);
		
		String sql = "select password from accounts where sn='"+msg.getAccountNumber()+"';";
		System.out.println("  sql: "+sql);
		try {
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if(rs.next()){
				String pass = rs.getString("password");
				if(pass.equals(msg.getPassword())){
					account = msg.getAccountNumber();
					System.out.println("  Login Accepted.");
					return true;
				}else{
					System.out.println("  Login Denied.");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	private boolean logout(Message msg) {
		account = "";
		return true;
	}

}
