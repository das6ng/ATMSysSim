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
		long last = System.currentTimeMillis();
		long current = last;
		int hello = 0;
		Message msgHello = new Message(System.currentTimeMillis(), "*", "*", Message.KEEPALIVE_NO, 0,"*");
		Message msgError = new Message(System.currentTimeMillis(), "*", "*", Message.ERROR_NO, 0,"*");
		try {
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter os = new PrintWriter(socket.getOutputStream());
			
			System.out.println("[MSG] a new ATM got online.");
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
					case 6: // login
						if(login(msg)){
							inquire(msg);
							os.println(msg.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 1: // deposit
						if(deposit(msg)){
							os.println(msg.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 2: // withdraw
						if(withdraw(msg)){
							os.println(msg.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 3: // transfer
						if(transfer(msg)){
							os.println(msg.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 4: //inquire
						if(inquire(msg)){
							os.println(msg.toString());
							os.flush();
						}else{
							msgError.setTimeStamp(System.currentTimeMillis());
							os.println(msgError.toString());
							os.flush();
						}
						break;
					case 9: //logout
						if(logout(msg)){
							os.println(msg.toString());
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
				
//				current = System.currentTimeMillis();
//				if(current - last > 5000){
//					msgHello.setTimeStamp(System.currentTimeMillis());
//					os.println(msgHello.toString());
//					os.flush();
//					hello ++;
//					last = current;
//					
//					System.out.println("^_^");
//				}
				
//				if(!socket.isConnected() || hello > 3) throw new Exception("connection lost!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean inquire(Message msg) {
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying inquire: "+account);
		
		String sql = "select balance from accounts where sn='"+account+"';";
		System.out.println("  sql: "+sql);
		
		try {
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if(rs.next()){
				double balance = rs.getDouble("balance");
				msg.setDeal(balance);
			}
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean transfer(Message msg) {
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying transfer: "+account);
		
		Message tmp = new Message(0, "*", "*", 0, 0, "*");
		inquire(tmp);
		double balance = tmp.getDeal();
		double deal = msg.getDeal();
		String target = msg.getOtherAccount();
		
		
		if(balance >= deal){
			
		}else{
			return false;
		}
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean withdraw(Message msg) {
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying withdraw: "+account);
		
		Message tmp = new Message(0, "*", "*", 0, 0, "*");
		inquire(tmp);
		double balance = tmp.getDeal();
		if(balance < msg.getDeal()){
			return false;
		}else{
			balance -= msg.getDeal();
		}
		String sql = "update accounts set balance="+balance+
				     " where sn='"+account+"'";
		System.out.println("  sql: "+sql);
		try {
			conn.createStatement().executeUpdate(sql);
			msg.setDeal(balance);
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	private boolean deposit(Message msg) {
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying deposit: "+account);
		
		Message tmp = new Message(0, "*", "*", 0, 0, "*");
		inquire(tmp);
		double balance = tmp.getDeal();
		balance += msg.getDeal();
		String sql = "update accounts set balance="+balance+
				     " where sn='"+account+"'";
		System.out.println("  sql: "+sql);
		try {
			conn.createStatement().executeUpdate(sql);
			msg.setDeal(balance);
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	private boolean login(Message msg) {
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying login: ");
		
		String sql = "select password from accounts where sn='"+msg.getAccountNumber()+"';";
		System.out.println("  sql: "+sql);
		try {
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if(rs.next()){
				String pass = rs.getString("password");
				if(pass.equals(msg.getPassword())){
					account = msg.getAccountNumber();
					System.out.println("  Login Accepted.");
					
					conn.close();
					return true;
				}else{
					System.out.println("  Login Denied.");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean logout(Message msg) {
		account = "";
		return true;
	}

}
