package life.dashyeah.ATMSysSim.Server;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class Cfg {
	private static String bankCode;
	private static String bankName;
	private static String branchCode;
	private static String branchName;
	private static String relayAddr;
	
	private static String databaseUrl;
	private static String databaseUser;
	private static String databasePass;
	
	private static JSONObject serverList;

	static{
		Reader r = null;
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			r = new FileReader("./cfg/server.cfg.json");
			obj = (JSONObject) parser.parse(r);
			
			bankCode = (String) obj.get("bank");
			bankName = (String) obj.get("name");
			branchCode = (String) obj.get("branchCode");
			branchName = (String) obj.get("branchName");
			relayAddr = (String) obj.get("relay");
			
			databaseUrl = (String) obj.get("databaseUrl");
			databaseUser = (String) obj.get("databaseUser");
			databasePass = (String) obj.get("databasePass");
			
			//System.out.println(" cfg: "+obj.toJSONString());
			
			r.close();
			r = new FileReader("./cfg/server_list.json");
			serverList = (JSONObject) parser.parse(r);
			
			r.close();
		} catch (IOException e) {
			System.err.println("[ERROR] Cann't open file: server.cfg.json");
		} catch (ParseException e) {
			System.err.println("[ERROR] Json parsing failed.");
		}
	}
	
	public static JSONObject getServerList() {
		return serverList;
	}

	public static String getBankCode() {
		return bankCode;
	}

	public static String getBankName() {
		return bankName;
	}

	public static String getBranchCode() {
		return branchCode;
	}

	public static String getBranchName() {
		return branchName;
	}

	public static String getRelayAddr() {
		return relayAddr;
	}

	public static String getDatabaseUrl() {
		return databaseUrl;
	}

	public static String getDatabaseUser() {
		return databaseUser;
	}

	public static String getDatabasePass() {
		return databasePass;
	}
	
}
