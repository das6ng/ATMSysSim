package life.dashyeah.ATMSysSim.Server;

import java.util.Scanner;

public class Message {
	private long timeStamp;
	private String accountNumber;
	private String password;
	private int operation; // 1 2 3
	private double deal;
	private String otherAccount;
	
	/**
	 * @param timeStamp
	 * @param accountNumber
	 * @param password
	 * @param operation
	 * @param deal
	 * @param otherAccount
	 */
	public Message(long timeStamp, String accountNumber,
			       String password, int operation,
			       double deal, String otherAccount){
		
		this.accountNumber = accountNumber;
		this.timeStamp = timeStamp;
		this.password = password;
		this.operation = operation;
		this.deal = deal;
		this.otherAccount = otherAccount;
	}
	
	public static Message parse(String src){
		Scanner scan = new Scanner(src);
		Message msg = new Message(scan.nextLong(), scan.next(), scan.next(), scan.nextInt(),
				scan.nextDouble(), scan.next());
		scan.close();
		return msg;
	}
	
	public void setDeal(double deal) {
		this.deal = deal;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public String getPassword() {
		return password;
	}
	public int getOperation() {
		return operation;
	}
	public double getDeal() {
		return deal;
	}
	public String getOtherAccount() {
		return otherAccount;
	}
	
	public String toString(){
		String str = "";
		
		str += timeStamp+"\t";
		str += accountNumber+"\t";
		str += password+"\t";
		str += operation+"\t";
		str += deal+"\t";
		if(otherAccount == null || "".equals(otherAccount)){
			str += "*";
		}else{
			str += otherAccount;
		}
		
		return str;
	}
}