package com.web.ui;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import com.bt.app.anglebroking.constants.AGlobal;
import com.bt.app.database.DataBaseOpr;
import com.bt.app.guptainvestor.service.GuptaInvestorService;
import com.bt.app.moneybhai.Constants;
import com.bt.libraries.Common;
import com.bt.libraries.RestConnector;
import com.bt.log.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.web.constants.Constant;
import com.web.pojo.ApplicationBean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("login")  // make sure the name matches EL expression
@RequestScoped
public class Login implements Serializable {

	private static final long serialVersionUID = 1L;
	static RestConnector restCon = null;
	private Long scld;
	private String usrName;

	Common com = new Common();
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private String DB_URL = "";

	private String USER = "";
	private String PASS = "";
	private ArrayList<String> clientData = new ArrayList<String>();

	Connection conn = null;
	Statement stmt = null;
	DataBaseOpr dataBaseOpr = new DataBaseOpr();
	public String getUsrName() {
		return usrName;
	}

	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	private String pwd;

	public String submit() {
		Object obj = null;
		try {
			obj = getClientJsonData(usrName);
			
		} catch (Exception e1) {
			Log.error("Exception inside getting client json data "+e1.getMessage());
		}
		int uid = -1;
		String role = "";
		if (obj != null || getUsrName().equalsIgnoreCase("admin")) {
			try {

				DB_URL = ApplicationBean.getInstance().getUrl();
				USER = ApplicationBean.getInstance().getUserName();
				PASS = ApplicationBean.getInstance().getPassword();

				Class.forName("com.mysql.jdbc.Driver");

				// STEP 3: Open a connection
				Log.info(
						"--------------------------------------------------------------------------------------------------------------------------------");
				Log.info(
						"-------------------------------------------Connecting to a selected database----------------------------------------------------");
				Log.info(
						"--------------------------------------------------------------------------------------------------------------------------------");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Log.info(
						"--------------------------------------------------------------------------------------------------------------------------------");
				Log.info(
						"------------------------------------------------Connected database successfully-------------------------------------------------");
				Log.info(
						"--------------------------------------------------------------------------------------------------------------------------------");

				// STEP 4: Execute a query
				Log.info(
						"--------------------------------------------------------------------------------------------------------------------------------");
				Log.info(
						"------------------------------------------------Creating statement--------------------------------------------------------------");
				Log.info(
						"--------------------------------------------------------------------------------------------------------------------------------");
				stmt = conn.createStatement();

				String sql = "SELECT * FROM user_mst where  user_name ='" + usrName + "' and pwd='" + pwd + "'";
				ResultSet rs = stmt.executeQuery(sql);
				if (!rs.next()) {
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Username or Password is incorrect !"));
					FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

					return "login.xhtml?faces-redirect=true";

				} else {
					uid = rs.getInt("id");
					role = rs.getString("role");
				}
				
				rs.close();
				HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
						.getSession(true);
				session.setAttribute("user_id", uid);
				session.setAttribute("user_name", getUsrName());
				session.setAttribute("role", role);
				initiliazeCommodityFrontEnd();
				initiliazeUserList(getUsrName());
				
				if (com.checkTimeBeforeMarketHour(Constants.schedular_end)
						&& !getUsrName().equalsIgnoreCase("otheruser") && checkNoRecordForSchedular(getUsrName())
						&& !getUsrName().equalsIgnoreCase("admin") && !getUsrName().equalsIgnoreCase("default")
						&& !getUsrName().equalsIgnoreCase("dummy")) {
					try {
						Log.info("Setting driver path "+ dataBaseOpr.fetchRecordGeneric("select driver from rms.user_mst where user_name ='"+getUsrName()+"'"));
						Constants.WEBDRIVERPATH = dataBaseOpr.fetchRecordGeneric("select driver from rms.user_mst where user_name ='"+getUsrName()+"'");
						initiliazeAngelApiData(getUsrName());
						if (scld == null) {
							scld = (long) 10000;
						}
						validateClientSetting();
						GuptaInvestorService gis = new GuptaInvestorService();
						gis.updateOrderGeneric(
								" update guptainv_AlgoTrading.LicenceInfo set counter = counter+1, lasttime = now() where USERID ='"
										+ getUsrName() + "'");

					} catch (Exception e) {
						Log.error(" Exception inside starting schedular " + e);
					}
				}
				Log.info("Logged in user " + Constants.userID);
				return "index.xhtml?faces-redirect=true";

			} catch (SQLException se) {
				Log.error("SE Exception " + se.getMessage());
			} catch (Exception e) {
				Log.error("Exception " + e.getMessage());
			} finally {
				
				try {
					if (stmt != null)
						conn.close();
				} catch (SQLException se) {
				} // do nothing
				try {
					if (conn != null)
						conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				} 
			}
			return "login";
		} else {

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "License is not valid"));
			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);

			return "login.xhtml?faces-redirect=true";

		}
	}

	private void validateClientSetting() {
		if (!Constants.autoSL.equalsIgnoreCase("Y"))
			updatePermission("autoSL1");

		if (!Constants.autoTarget.equalsIgnoreCase("Y"))
			updatePermission("autoSL");

		if (!Constants.robo.equalsIgnoreCase("Y"))
			updatePermission("robo");

		if (!Constants.buySLAuto.equalsIgnoreCase("Y"))
			updatePermission("buySL");

		if (!Constants.mcx.equalsIgnoreCase("Y"))
			updatePermission("mcx");

	}

	private void updatePermission(String column) {
		Statement stmt = null;
		java.sql.Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			stmt = conn.createStatement();
			String sql = "";
			sql = "update rms.clientsetting  set " + column + "= 'N'  where code = '" + Constants.userID + "'";
			Log.info("updatePermission " + sql);
			// if (high > 0)
			stmt.executeUpdate(sql);

		} catch (SQLException | ClassNotFoundException e) {

			Log.error("Exception inside putOrder " + e.getMessage(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}

	}

	private boolean checkNoRecordForSchedular(String userID) {

		boolean status = true;
		Statement stmt = null;

		java.sql.Connection conn = null;
		java.sql.ResultSet rs = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
			String query = "SELECT count(*) as count FROM tradedata.killingthread  where name in ('com.bt.app.scheduler.PutSLOrderSchedular','com.bt.app.scheduler.TrackSLOrderExecution',"
					+ "'com.bt.app.scheduler.TrackTargetExecution', 'com.bt.app.scheduler.TrackRoboOrderExecution') and userID ='"
					+ userID + "'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (rs.getInt(1) > 0) {
					status = false;
				}
			}
			Constant.clientData = clientData;
		} catch (Exception e) {

		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
		return status;
	}

	private void initiliazeUserList(String userName) {
		Statement stmt = null;

		java.sql.Connection conn = null;
		java.sql.ResultSet rs = null;
		java.sql.ResultSet rs1 = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			stmt = conn.createStatement();
			String query = "SELECT UserName FROM trade_data.abdata ";
			if (!userName.equalsIgnoreCase("admin") && !userName.equalsIgnoreCase("otheruser")
					&& !userName.equalsIgnoreCase("master"))
				query = query + " where UserName='" + userName + "'";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				clientData.add(rs.getString(1));
			}
			Constant.clientData = clientData;
			query = "SELECT scld, maxloss FROM rms.clientsetting ";
			if (!userName.equalsIgnoreCase("admin") && !userName.equalsIgnoreCase("otheruser")
					&& !userName.equalsIgnoreCase("master"))
				query = query + " where code='" + userName + "'";

			rs1 = stmt.executeQuery(query);
			while (rs1.next()) {
				Constant.maxLoss = rs1.getDouble(2);
				scld = rs1.getLong(1);
			}

		} catch (Exception e) {

		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
			if (rs1 != null) {
				try {
					rs1.close();
				} catch (SQLException e) {

					e.printStackTrace();
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
				}
			}
		}
	}

	public String logoutUser() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

		session.invalidate();
		return "login";
	}

	private void initiliazeAngelApiData(String userName) {
		if (!userName.equalsIgnoreCase("ADMIN") && !userName.equalsIgnoreCase("OTHERUSER")
				&& !userName.equalsIgnoreCase("DEFAULT") && !userName.equalsIgnoreCase("DUMMY")
				&& !userName.equalsIgnoreCase("MASTER")) {
			Statement stmt = null;
			java.sql.Connection conn = null;
			java.sql.ResultSet rs = null;
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				stmt = conn.createStatement();
				rs = stmt.executeQuery(
						"SELECT UserName, Password, APIKey, SecretKey, JWTToken, RefreshToken, FeedToken, IsDoAlgoTrading, autoSL, scld, alert  FROM trade_data.abdata where userName ='"
								+ userName + "'");
				while (rs.next()) {
					AGlobal.userName = rs.getString(1);
					AGlobal.userPassword = rs.getString(2);
					AGlobal.apiKey = rs.getString(3);
					AGlobal.secretKey = rs.getString(4);
					AGlobal.jwtToken = rs.getString(5);
					AGlobal.refreshToken = rs.getString(6);
					AGlobal.feedToken = rs.getString(7);
					AGlobal.alert = rs.getString(8);
					// scld = rs.getLong(10);
				}
			} catch (Exception e) {
				Log.error("exception inside intiliazing Angel key " + e);
			} finally {
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {

						e.printStackTrace();
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException ex) {
					}
				}
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException ex) {
					}
				}
			}
		} else {
			AGlobal.userName = Constants.clientID;
			AGlobal.userPassword = "Feb.2022@1";
			AGlobal.apiKey = "gYmSfTLF";
			AGlobal.secretKey = "a49168d8-3ae5-460e-93b5-8fb08005bf05";
			AGlobal.jwtToken = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VybmFtZSI6IlZJWVYxMDAxIiwicm9sZXMiOjAsInVzZXJ0eXBlIjoiVVNFUiIsImlhdCI6MTY0NTAyMjQzMiwiZXhwIjoxNzMxNDIyNDMyfQ.6r7hhM0KnNsQ4Fg9FwMTK9u82CkjUfzU4XPGT_FPs89KCnupo2Q8F2hgswDtujFHJ-22jxr7RDGoOnQVVM4Lmw";
			AGlobal.refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlbiI6IlJFRlJFU0gtVE9LRU4iLCJpYXQiOjE2NDUwMjI0MzJ9.Z-8x9YoQHxuaDrkkQ7lJtoZK-_KT1DDr6E6h3DczdRsZyFhoMsfuvy-5RQEM3Co_na-0mCtZ4UB0vFskK32uwA";
			AGlobal.feedToken = "96826949";
			AGlobal.alert = Constants.ankurID;
		}
	}

	public Object getClientJsonData(String userID) throws Exception {
		HashMap<String, String> ClientLicense = new HashMap<String, String>();
		RestConnector restCon = RestConnector.getInstance().init(false);
		restCon.disableWaitBeforeURLHit();
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Accept", "application/json");
		String url = Constant.url + userID;
		HttpResponse res = restCon.httpGet(url, null, headers);
		if (HttpURLConnection.HTTP_OK != res.getStatusLine().getStatusCode()) {
			return null;
		}

		Log.info("Get all Order Response Code : " + res.getStatusLine().getStatusCode());
		String outputContent = EntityUtils.toString(res.getEntity());
		// Log.info(outputContent);
		EntityUtils.consume(res.getEntity());

		JsonElement jsonElement = new JsonParser().parse(outputContent);
		JsonObject jsonObject = jsonElement.getAsJsonObject();

		boolean status = jsonObject.get("result").getAsBoolean();
		if (!status) {
			return null;
		}
		JsonElement jsonDataElement = jsonObject.get("data");
		if (!jsonDataElement.isJsonNull()) {
			JsonArray jsonDataArray = jsonDataElement.getAsJsonArray();
			for (int i = 0; i < jsonDataArray.size(); i++) {
				JsonObject userlicense = jsonDataArray.get(i).getAsJsonObject();
				JsonElement USER = userlicense.get("USERID");
				ClientLicense.put("USERID", USER.getAsString());
				Constants.userID = USER.getAsString();

				JsonElement autoSL = userlicense.get("ISAUTOSL");
				ClientLicense.put("autoSL", autoSL.getAsString());
				Constants.autoSL = autoSL.getAsString();

				JsonElement autoTarget = userlicense.get("ISAUTOTARGET");
				ClientLicense.put("autoTarget", autoTarget.getAsString());
				Constants.autoTarget = autoTarget.getAsString();

				JsonElement robo = userlicense.get("ISROBO");
				ClientLicense.put("robo", robo.getAsString());
				Constants.robo = robo.getAsString();

				JsonElement buySL = userlicense.get("ISBUYSL");
				ClientLicense.put("buySL", buySL.getAsString());
				Constants.buySL = buySL.getAsString();

				JsonElement updateSL = userlicense.get("ISUPDATE");
				ClientLicense.put("updateSL", updateSL.getAsString());
				Constants.updateSL = updateSL.getAsString();

				JsonElement buySLAuto = userlicense.get("ISBUYSLAUTO");
				ClientLicense.put("buySLAuto", buySLAuto.getAsString());
				Constants.buySLAuto = buySLAuto.getAsString();

				JsonElement ISPSL = userlicense.get("ISPSL");
				ClientLicense.put("ISPSL", ISPSL.getAsString());
				Constants.psl = ISPSL.getAsString();

				JsonElement ISPBK = userlicense.get("ISPBK");
				ClientLicense.put("ISPBK", ISPBK.getAsString());
				Constants.pbook = ISPBK.getAsString();

				JsonElement ISMCX = userlicense.get("ISMCX");
				ClientLicense.put("ISMCX", ISMCX.getAsString());
				Constants.mcx = ISMCX.getAsString();

				JsonElement ISLIVE = userlicense.get("ISLIVE");
				ClientLicense.put("ISLIVE", ISLIVE.getAsString());
				Constants.islive = ISLIVE.getAsString();
			}
		}
		
		return ClientLicense;

	}
	
	
	public void initiliazeCommodityFrontEnd()
	{
		DataBaseOpr dbo = new DataBaseOpr();
		try {
			Constants.copper = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.copperSymbol+"%' limit 1");
			Constants.natural = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.naturalSymbol+"%' limit 1");
			Constants.gold = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.goldSymbol+"%' limit 1");
			Constants.zinc = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.zincSymbol+"%' limit 1");
			Constants.crude = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.crudeSymbol+"%' limit 1");
			Constants.silver = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.silverSymbol+"%' limit 1");
			Constants.alum = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.alumSymbol+"%' limit 1");
			Constants.lead = dbo.fetchRecordGeneric("select name from fibo where name like '"+Constants.leadSymbol+"%' limit 1");
		} catch (Exception e) {
			Log.error("Exception inside setting commodity level "+e.getMessage());
		}
		
	}
}