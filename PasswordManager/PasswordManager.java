

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Password 관리 
 * @author darkkazma
 *
 */
public class PasswordManager {

	private Scanner scanner = null;
	private ArrayList<USER> UserLIST = new ArrayList<USER>();
	public static void main(String[] args){ new PasswordManager(); }
	
	/**
	 * Console의 PWD를 관리하고 처리 할 수 있도록 한다.
	 * 1) 현재 사용자 정보를 출력한다.
	 * 2) 사용자를 추가 한다.
	 * 3) 사용자를 수정 한다.
	 * 4) 사용자를 삭제 한다.
	 * 5) 사용자의 Lock을 해제 한다.
	 * 6) PWD를 Encryption한다.
	 * 7) PWD를 Decryption한다. 
	 */
	public PasswordManager(){
		
		scanner = new Scanner(System.in);
		boolean process = true;
		try{
			
			while( process ){
				PrintMainMessage();
				
				String command = scanner.nextLine();
				int command_ = 0;
				
				try{ command_ = Integer.parseInt(command); }catch(NumberFormatException ne){ command_ = -1; }
				
				try{
					switch( command_ ){
						case 1:
							PrintUserInfo();
							break;
						case 2:
							UserAdd();
							break;
						case 3:
							UserMod();
							break;
						case 4:
							UserDel();
							break;
						case 5:
							UserUnLock();
							break;
						case 6:
							Encryption();
							break;
						case 7:
							Decryption();
							break;
						case 0 : 
							process = false;
							break;
						default : 
							System.err.println("Wrong Command input.");
							break;
					}
				}catch(Exception ea){ System.out.println("");}
			}
		}catch(Exception ea){
			ea.printStackTrace();
		}
	}
	
	private void UserUnLock() throws Exception {
		
		readUserInfo();
		
		System.out.print("Input UserID for Unlock : ");
		String UserID = scanner.nextLine().trim();
		System.out.println("");
		
		if( UserID != null && UserID.length() > 0 ){
			DBConnectionMgr db = DBConnectionMgr.getInstance();
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				con = db.getConnection();
				String query = "UPDATE USERINFO "
						 + "SET IS_ACCOUNT_LOCK = 'N'"
						 + ", LOGIN_FAIL_COUNT = 0"
						 + " WHERE USERID = ?";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, UserID);
				pstmt.executeUpdate();
				System.out.println("User unlock success...");
				System.out.println("");
			}catch(Exception ea){
				System.err.println("User unlock failed..." + ea.toString());
				throw ea; 
			}
			finally{
				try{ if( db != null ){ db.freeConnection(con, pstmt, rs); } }catch(Exception ea){}finally{ db = null; }
			}
		}
		
	}

	private void UserDel() throws Exception{
		
		readUserInfo();
		
		System.out.print("Input UserID for DELETE : ");
		String UserID = scanner.nextLine().trim();
		System.out.println("");
		
		if( UserID != null && UserID.length() > 0 ){
			DBConnectionMgr db = DBConnectionMgr.getInstance();
			Connection con = null;
			PreparedStatement pstmt = null;
			try{
				con = db.getConnection();
				pstmt = con.prepareStatement("DELETE FROM USERINFO WHERE USERID=?");
				pstmt.setString(1, UserID);
				pstmt.executeUpdate();
				
				System.out.println("");
				System.out.println("");
				pstmt.close(); pstmt = null;
				System.out.println("User delete success...");
				System.out.println("");
			}catch(Exception ea){
				System.err.println("User delete failed..." + ea.toString());
				throw ea; 
			}
			finally{
				try{ if( db != null ){ db.freeConnection(con, pstmt); } }catch(Exception ea){}finally{ db = null; }
			}
		}
		
	}

	public void UserMod() throws Exception{
		readUserInfo();
		
		System.out.print("Input UserID for Modify : ");
		String UserID = scanner.nextLine().trim();
		System.out.println("");
		
		if( UserID != null && UserID.length() > 0 ){
			String UserPWD = null;
			String UserLevel = null;
			String UserEmail = null;
			
			boolean find = false;
			
			DBConnectionMgr db = DBConnectionMgr.getInstance();
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				con = db.getConnection();
				pstmt = con.prepareStatement("SELECT * FROM USERINFO WHERE USERID=?");
				pstmt.setString(1, UserID);
				rs = pstmt.executeQuery();
				
				while( rs.next() ){
					UserPWD = rs.getString("PASSWORD");
					UserLevel = String.valueOf(rs.getInt("AUTHOR"));
					UserEmail = rs.getString("USERMAIL");
					find = true;
				}
				System.out.println("");
				System.out.println("");
				rs.close(); rs = null;
				pstmt.close(); pstmt = null;
			}catch(Exception ea){ throw ea; }
			finally{
				try{ if( db != null ){ db.freeConnection(con, pstmt, rs); } }catch(Exception ea){}finally{ db = null; }
			}
			
			if( find ){
				
				System.out.print("Enter User New Password (default ["+DESUtil.decrypt(UserPWD)+"]) : ");
				String newPWD = scanner.nextLine().trim();
				if( newPWD != null && newPWD.length() == 0 ){ 
					newPWD = DESUtil.decrypt(UserPWD); 
				}else{
					String pwdPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{9,}$";
					boolean tt = Pattern.matches(pwdPattern, newPWD);
					while( !tt ){
						System.err.println("Password must be combination of eng + num + special char with at least 9digits.");
						System.out.println("");
						System.out.print("Enter User New Password : ");
						newPWD = scanner.nextLine().trim();
						if( newPWD != null && newPWD.length() == 0 ){ 
							newPWD = DESUtil.decrypt(UserPWD);
							tt = true;
						}else{
							tt = Pattern.matches(pwdPattern, newPWD);
						}
					}
				}
				
				System.out.print("Enter User New Level (2: Manager, 1: User , default ["+UserLevel+"]) ");
				String newLevel = scanner.nextLine().trim();
				if(newLevel != null && newLevel.length() == 0 ){ newLevel = UserLevel; }
				try{ Integer.parseInt(newLevel); } catch(Exception ea){ newLevel = UserLevel; }
				
				System.out.print("Enter User New Email : ");
				String newEmail = scanner.nextLine().trim();
				if( newEmail != null && newEmail.length() == 0 ){ newEmail = UserEmail; }
				
				System.out.print("newPWD : " + newPWD + " | newLevel : " + ( newLevel.equals("1") ? "User" : "Manager")  + " | UserMail : "+ newEmail+"  y or n (default y)");
				String yesorno = scanner.nextLine().trim();
				if( yesorno != null && yesorno.length() == 0 ){ yesorno = "y"; }
				
				if( yesorno.equalsIgnoreCase("y") ){
					
					try{
						db = DBConnectionMgr.getInstance();
						con = db.getConnection();
						String query = "UPDATE USERINFO SET PASSWORD=?, AUTHOR=?, USERMAIL=? WHERE USERID=?";
						pstmt = con.prepareStatement(query);
						pstmt.setString(1, DESUtil.encrypt(newPWD));
						pstmt.setInt(2, Integer.parseInt(newLevel));
						pstmt.setString(3, UserEmail);
						pstmt.setString(4, UserID);
						pstmt.executeUpdate();
						
						System.out.println("User modify success..");
						System.out.println("");
						
					}catch(Exception ea){ 
						System.out.println("User modify failed.." + ea.toString());
						System.out.println("");
						throw ea; 
					}
					finally{
						try{ if( db != null ){ db.freeConnection(con, pstmt, rs); } }catch(Exception ea){}finally{ db = null; }
					}
					
				}else{
					throw new Exception();
				}
				
			}else{
				System.err.println("Not Found User. ["+UserID+"]");
				throw new Exception();
			}
		}
		
	}
	
	public void Encryption() throws Exception{
		System.out.print("Input normal text : ");
		String text = scanner.nextLine().trim();
		System.out.println("Encryption => ["+ DESUtil.encrypt(text) +"]");
		System.out.println("");
	}
	
	public void Decryption() throws Exception{
		System.out.print("Input encryption text : ");
		String text = scanner.nextLine().trim();
		System.out.println("Decryption => ["+ DESUtil.decrypt(text) +"]");
		System.out.println("");
	}
	
	public void UserAdd() throws Exception{
		
		readUserInfo();
		
		String UserID = null;
		String UserPWD = null;
		String UserLevel = null;
		String UserEmail = null;
		
		System.out.print("Input User ID : ");
		UserID = scanner.nextLine().trim();
		
		for( USER user : UserLIST ){ 
			if( user.getUSERID().equals(UserID) ){
				System.err.println("Duplicate UserID");
				throw new Exception();
			} 
		}
		
		
		System.out.print("Input User PWD : ");
		UserPWD = scanner.nextLine().trim();
		
		String pwdPattern = "^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-z])(?=.*[A-Z]).{9,}$";
		boolean tt = Pattern.matches(pwdPattern, UserPWD);
		while( !tt ){
			System.err.println("Password must be combination of eng + num + special char with at least 9digits.");
			System.out.println("");
			System.out.print("Input User PWD : ");
			UserPWD = scanner.nextLine().trim();
			tt = Pattern.matches(pwdPattern, UserPWD);
		}
		
		
		
		System.out.print("Input User level (2: Manager, 1: User) : ");
		UserLevel = scanner.nextLine().trim();
		
		int level = 0;
		try{ level = Integer.parseInt(UserLevel); }catch(NumberFormatException ne){ level = 1; }
		if( level < 0 || level > 2 ){ level = 1; }
		
		System.out.print("Input User Email : ");
		UserEmail = scanner.nextLine().trim();
		
		System.out.print("UserID : " + UserID + " | UserPWD : " + UserPWD + " | UserLevel : " + ( level == 1 ? "User" : "Manager")  + " | UserMail : "+ UserEmail+"  y or n (default y)");
		String yesorno = scanner.nextLine().trim();
		if( yesorno != null && yesorno.length() == 0 ){ yesorno = "y"; }
		
		if( yesorno.equalsIgnoreCase("y") ){
			
			DBConnectionMgr db = DBConnectionMgr.getInstance();
			Connection con = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try{
				con = db.getConnection();
				String query = "INSERT INTO USERINFO (USERID, USERNAME, PASSWORD, AUTHOR, USERMAIL) VALUES (?,?,?,?,?)";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, UserID);
				pstmt.setString(2, "CMD_USER");
				pstmt.setString(3, DESUtil.encrypt(UserPWD));
				pstmt.setInt(4, level);
				pstmt.setString(5, UserEmail);
				pstmt.executeUpdate();
				
				System.out.println("User add success..");
				System.out.println("");
				
			}catch(Exception ea){ 
				System.out.println("User add failed.." + ea.toString());
				System.out.println("");
				throw ea; 
			}
			finally{
				try{ if( db != null ){ db.freeConnection(con, pstmt, rs); } }catch(Exception ea){}finally{ db = null; }
			}
			
		}else{
			throw new Exception();
		}
		
	}
	
	
	/** 
	 * 유저 정보를 출력 한다. 
	**/
	public void PrintUserInfo() throws Exception{
		
		DBConnectionMgr db = DBConnectionMgr.getInstance();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			con = db.getConnection();
			pstmt = con.prepareStatement("SELECT * FROM USERINFO");
			rs = pstmt.executeQuery();
			
			StringBuffer sb = new StringBuffer();
			sb.append( String.format("%-20s", "UserID"));
			sb.append( String.format("%-40s", "UserPWD"));
			sb.append( String.format("%-10s", "UserLevel"));
			sb.append( String.format("%-20s", "UserMail"));
			sb.append( String.format("%-10s", "UserLock"));
			sb.append( String.format("%-20s", "LastLoginIP"));
			sb.append( String.format("%-20s", "LastLoginDate"));
			System.out.println(sb.toString());
			System.out.println("-----------------------------------------------------------------------------------------------------------------");
			while( rs.next() ){
				sb.setLength(0);
				sb.append( String.format("%-20s", rs.getString("USERID")));
				sb.append( String.format("%-40s", rs.getString("PASSWORD")));
				sb.append( String.format("%-10s", rs.getString("AUTHOR")));
				sb.append( String.format("%-20s", rs.getString("USERMAIL")));
				sb.append( String.format("%-10s", rs.getString("IS_ACCOUNT_LOCK")));
				sb.append( String.format("%-20s", rs.getString("LAST_LOGIN_IP")));
				sb.append( String.format("%-20s", rs.getString("LAST_LOGIN_DATE")));
				System.out.println(sb.toString());
			}
			System.out.println("");
			System.out.println("");
			
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
			
		}catch(Exception ea){ throw ea; }
		finally{
			try{ if( db != null ){ db.freeConnection(con, pstmt, rs); } }catch(Exception ea){}finally{ db = null; }
		}
	}
	
	
	public void PrintMainMessage(){
		System.out.println("###########################################################");
		System.out.println(" BCFSync Console User Info Manager");
		System.out.println("###########################################################");
		System.out.println(" 1) print user info       (사용자 정보를 출력 한다)");
		System.out.println(" 2) add user info         (사용자 정보를 추가 한다)");
		System.out.println(" 3) modify user info      (사용자 정보를 수정 한다)");
		System.out.println(" 4) delete user info      (사용자 정보를 삭제 한다)");
		System.out.println(" 5) unlock user info      (사용자 계정의 Lock을 해제 한다)");
		System.out.println(" 6) Encryption password   (평문을 암호화 한다)");
		System.out.println(" 7) Decryption password   (암호를 복호화 한다)");
		System.out.println(" 0) exit                  (종료 한다)");
		System.out.println("");
	}
	
	
	
	
	public void readUserInfo() throws Exception {

		UserLIST.clear();
		
		DBConnectionMgr db = DBConnectionMgr.getInstance();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			con = db.getConnection();
			pstmt = con.prepareStatement("SELECT * FROM USERINFO");
			rs = pstmt.executeQuery();
			
			while( rs.next() ){
				USER user = new USER();
				user.setUSERID(rs.getString("USERID"));
				user.setUSERNAME(rs.getString("USERNAME"));
				user.setPASSWORD(rs.getString("PASSWORD"));
				user.setAUTHOR(rs.getInt("AUTHOR"));
				user.setUSERMAIL(rs.getString("USERMAIL"));
				user.setIS_ACCOUNT_LOCK(rs.getString("IS_ACCOUNT_LOCK"));
				UserLIST.add(user);
			}
			System.out.println("");
			System.out.println("");
			rs.close(); rs = null;
			pstmt.close(); pstmt = null;
		}catch(Exception ea){ throw ea; }
		finally{
			try{ if( db != null ){ db.freeConnection(con, pstmt, rs); } }catch(Exception ea){}finally{ db = null; }
		}
	}
	
	public class USER{
		String USERID;
		String USERNAME;
		String PASSWORD;
		int AUTHOR;
		String USERMAIL;
		String IS_ACCOUNT_LOCK;
		
		
		
		public String getUSERID() {
			return USERID;
		}
		public void setUSERID(String uSERID) {
			USERID = uSERID;
		}
		public String getUSERNAME() {
			return USERNAME;
		}
		public void setUSERNAME(String uSERNAME) {
			USERNAME = uSERNAME;
		}
		public String getPASSWORD() {
			return PASSWORD;
		}
		public void setPASSWORD(String pASSWORD) {
			PASSWORD = pASSWORD;
		}
		public int getAUTHOR() {
			return AUTHOR;
		}
		public void setAUTHOR(int aUTHOR) {
			AUTHOR = aUTHOR;
		}
		public String getUSERMAIL() {
			return USERMAIL;
		}
		public void setUSERMAIL(String uSERMAIL) {
			USERMAIL = uSERMAIL;
		}
		public String getIS_ACCOUNT_LOCK() {
			return IS_ACCOUNT_LOCK;
		}
		public void setIS_ACCOUNT_LOCK(String iS_ACCOUNT_LOCK) {
			IS_ACCOUNT_LOCK = iS_ACCOUNT_LOCK;
		}
		
		
	}
}
