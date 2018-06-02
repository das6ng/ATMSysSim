package life.dashyeah.ATMSysSim.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

public class Service implements Runnable {
	private Socket socket;
	private String account;
	private BufferedReader is;
	private PrintWriter os;
	private final long timeout = 60000;
	Message msgHello = new Message(System.currentTimeMillis(), "*", "*", Message.KEEPALIVE_NO, 0,"*");
	
	public Service(Socket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		long last = System.currentTimeMillis();
		long current = last;
		int hello = 0;
		try {
			is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os = new PrintWriter(socket.getOutputStream());
			
			System.out.println("[MSG] a new ATM got online.");
			while(true){
				String data = "";
				if(is.ready()){
					data = is.readLine();
					System.out.println("[MSG] received: " + data);
					Message msg = Message.parse(data);
					System.out.println("       Message: " + msg.toString());
					
					switch(msg.getOperation()){
					case Message.LOGIN_NO: // login
						os.println(login(msg).toString());
						os.flush();
						break;
					case Message.DESPOSIT_NO: // deposit
						os.println(deposit(msg).toString());
						os.flush();
						break;
					case Message.WITHDRAW_NO: // withdraw
						os.println(withdraw(msg).toString());
						os.flush();
						break;
					case Message.TRANSFER_NO: // transfer
						os.println(transfer(msg).toString());
						os.flush();
						break;
					case Message.INQUIRE_NO: //inquire
						os.println(inquire(msg).toString());
						os.flush();
						break;
					case Message.EXIT_NO: //logout
						os.println(logout(msg).toString());
						os.flush();
						break;
					case Message.KEEPALIVE_NO: // pulse message
						if(hello>0) hello --;
						break;
					default:
						System.err.println("[Warning] received wrong operation code.");
						os.println(errorMessage().toString());
						os.flush();
					}
				}
				
//				current = System.currentTimeMillis();
//				if(current - last > timeout){
//					msgHello.setTimeStamp(System.currentTimeMillis());
//					os.println(msgHello.toString());
//					os.flush();
//					hello ++;
//					last = current;
//					
//					System.out.println("^_^");
//				}
//				if(hello > 3 && !is.ready()) throw new Exception("connection lost!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Message inquire(Message msg) {
		if(!isLegal(msg.getAccountNumber())) return errorMessage();
		if(!isThisServer(msg.getAccountNumber()))
			return redirect(msg);
		if(!checkPass(msg)) return errorMessage();
		
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying inquire: "+account);
		
		String sql = "select balance from accounts where sn='"+account+"';";
		//System.out.println("  sql: "+sql);
		
		Message result = Message.parse(msg.toString());
		
		try {
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if(rs.next()){
				double balance = rs.getDouble("balance");
				
				result.setDeal(balance);
				result.setTimeStamp(System.currentTimeMillis());
				
				conn.close();
				return result;
			}else{
				
				conn.close();
				return errorMessage();
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return errorMessage();
		}
	}

	private Message withdraw(Message msg) {
		if(!isLegal(msg.getAccountNumber())) return errorMessage();
		if(!isThisServer(msg.getAccountNumber()))
			return redirect(msg);
		if(!checkPass(msg)) return errorMessage();
		
		System.out.println("[MSG] Trying withdraw: "+account+" гд"+msg.getDeal());
		
		Message result = inquire(msg);
		double balance = result.getDeal();
		if(balance < msg.getDeal()){
			return errorMessage();
		}else{
//			long current = System.currentTimeMillis();
//			while(true){
//				try {
//					if(is.ready()){
//						Message tmp = Message.parse(is.readLine());
//						System.out.println("  withdraw: "+tmp.toString());
//						if(tmp.getOperation() == Message.COMMIT_NO) break;
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//					return errorMessage();
//				}
//				if(System.currentTimeMillis() - current > timeout) return errorMessage();
//			}
			balance -= msg.getDeal();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Connection conn = DBConn.getConn();
		String sql = "update accounts set balance="+balance+
				     " where sn='"+account+"'";
		//System.out.println("  sql: "+sql);
		try {
			conn.createStatement().executeUpdate(sql);
			result.setDeal(balance);
			
			conn.close();
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return errorMessage();
		}
	}

	private Message deposit(Message msg) {
		if(!isLegal(msg.getAccountNumber())) return errorMessage();
		if(!isThisServer(msg.getAccountNumber()))
			return redirect(msg);
		
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying deposit: "+account+" гд"+msg.getDeal());
		
		Message result = inquire(msg);
		double balance = result.getDeal();
		balance += msg.getDeal();
		String sql = "update accounts set balance="+balance+
				     " where sn='"+account+"'";
		System.out.println("  sql: "+sql);
		try {
			conn.createStatement().executeUpdate(sql);
			result.setDeal(balance);
			
			conn.close();
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return errorMessage();
		}
	}

	private Message transfer(Message msg) {
		if(!isLegal(msg.getAccountNumber())) return errorMessage();
		if(!isLegal(msg.getOtherAccount())) return errorMessage();
		if(!isThisServer(msg.getAccountNumber()))
			return redirect(msg);
		if(!checkPass(msg)) return errorMessage();
		
		System.out.println("[MSG] Trying transfer from:"+msg.getAccountNumber()+
				           " to:"+msg.getOtherAccount()+
				          " гд"+msg.getDeal());
		
		Message result = inquire(msg);
		if (result.getOperation() != Message.ERROR_NO) {
			if(result.getDeal() < msg.getDeal())
				return errorMessage();
			
			result.setAccountNumber(msg.getOtherAccount());
			result.setDeal(msg.getDeal());
			result.setOperation(Message.DESPOSIT_NO);
			if (isThisServer(msg.getOtherAccount())) {
				result = deposit(result);
			}else{
				result = redirect(result);
			}
			if(result.getOperation() == Message.DESPOSIT_NO){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				result = withdraw(msg);
				return result;
			}else{
				return errorMessage();
			}
		} else {
			return errorMessage();
		}
	}

	private Message login(Message msg) {
		if(!isLegal(msg.getAccountNumber())) return errorMessage();
		if(!isThisServer(msg.getAccountNumber()))
			return redirect(msg);
		
		if(checkPass(msg)){
			msg = inquire(msg);
			account = msg.getAccountNumber();
			return msg;
		}else{
			return errorMessage();
		}
	}

	private Message logout(Message msg) {
		System.out.println("[MSG] Logout: "+account);
		account = "";
		return msg;
	}
	
	private boolean checkPass(Message msg){
		Connection conn = DBConn.getConn();
		System.out.println("[MSG] Trying login: "+msg.getAccountNumber()+" "+msg.getPassword());
		
		String sql = "select password from accounts where sn='"+msg.getAccountNumber()+"';";
		//System.out.println("  sql: "+sql);
		try {
			ResultSet rs = conn.createStatement().executeQuery(sql);
			if(rs.next()){
				String pass = rs.getString("password");
				if(pass.equals(msg.getPassword())){
					account = msg.getAccountNumber();
					System.out.println("  Login Accepted.");
					
					conn.close();
					msg.setTimeStamp(System.currentTimeMillis());
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
	
	private static boolean isThisServer(String sn){
		String targetBank = sn.substring(0, 4);
		String targetBranch = sn.substring(4,8);
		
		//System.out.println("  this:"+Cfg.getBankCode()+Cfg.getBranchCode()+
		//		           "  target:"+targetBank+targetBranch);
		if(targetBank.equals(Cfg.getBankCode()) &&
		   targetBranch.equals(Cfg.getBranchCode()))
			return true;
		else
			return false;
	}
	
	private Message redirect(Message msg){
		JSONObject servers = Cfg.getServerList();
		String sn = msg.getAccountNumber();
		String targetBank = sn.substring(0, 4);
		String targetBranch = sn.substring(4,8);
		System.out.println("[MSG] Redirecting --> targetBank: "+targetBank+" targetBranch: "+targetBranch);
		String addr = (String) ((JSONObject)servers.get(targetBank)).get(targetBranch);
		try {
			@SuppressWarnings("resource")
			Socket socket = new Socket(addr, 2333);
			BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter os = new PrintWriter(socket.getOutputStream());
			os.println(msg.toString());
			os.flush();
			
			long t = System.currentTimeMillis();
			while(true){
				if(is.ready()){
					Message result = Message.parse(is.readLine());
					if(msg.getOperation() == result.getOperation() || 
					   result.getOperation() == Message.ERROR_NO){
						
						is.close();
						os.close();
						socket.close();
						return result;
					}else if(result.getOperation() == Message.KEEPALIVE_NO){
						result.setTimeStamp(System.currentTimeMillis());
						os.println(result.toString());
						os.flush();
					}
				}
				if(System.currentTimeMillis() - t > timeout)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorMessage();
	}
	
	private Message errorMessage(){
		return new Message(System.currentTimeMillis(), "*", "*", Message.ERROR_NO, 0,"*");
	}
	
	private static boolean isLegal(String account){
		String pattern = "^[0-9]{16}$";
		if(!Pattern.matches(pattern, account))
			return false;
		else
			return true;
	}

}
