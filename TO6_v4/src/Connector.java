

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class Connector {
	private String ssid, host, dbUsername, dbPassword, applicatie, port, connection;
	private String businessTrigger, businessEvent, columnName, businessValue, rowID, rowID2, ruleCode, ruleName, operatorName = null;
	private String tSQL1, appName1, tCode, cdName, entName, appName, tSQL, tName, tEvent, tTrigger, tTable, tColumnID, tOperatorID, tValueID;
	private Connection con, con2;

	public Connector() {}
	
	public void connect(String ssid, String prt, String hst, String dbU, String dbP) throws SQLException{
		this.ssid = ssid; 
		this.port = prt;
		this.host = hst;
		this.dbUsername = dbU;
		this.dbPassword = dbP;
		this.connection = "jdbc:oracle:thin:@" + host + ":" + port + "/" + ssid;

		con = DriverManager.getConnection(connection, dbUsername, dbPassword);
		System.out.println("--- Connection Succes! ---");
	}

	// VANAF HIER WORDEN DE STUKKEN CODE UIT DE DATABASE GEHAALD DIE NODIG ZIJN VOOR SQL-CODE GENERATIE //

	//Hier wordt de triggerNaam gemaakt.
	//Hieronder is nodig om de naam van de applicatie te generen.
	public String getApplication(String i) throws SQLException{
		rowID = i;
		//Hier opzoek naar de applicatie ID
		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			appName = result.getString("APPLICATION_ID");
		}
		
		result.close();

		//Vanaf hier opzoek naar de applicatie gegevens. 
		String sql1 = "Select * from APPLICATION where ID ='" + appName + "'";

		PreparedStatement statement1 = con.prepareStatement(sql1);

		ResultSet result1 = statement1.executeQuery();

		while(result1.next()){
			appName1 = result1.getString("HOST");
		}
		
		result1.close();

		return appName1.substring(0,2) + appName1.substring(appName1.length()-1, appName1.length());
	}

	public String getEntiteit(String i) throws SQLException{
		rowID = i;

		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			cdName = result.getString("CODE");
		}		

		// Om de juiste kolom te vinden
		String sql1 = "Select * from COLUMN1 where BUSINESSRULE_ID ='" + rowID + "'";
		String fk = null;
		String tableName = null;
		PreparedStatement statement1 = con.prepareStatement(sql1);

		ResultSet result1 = statement1.executeQuery();

		while(result1.next()){
			fk = result1.getString("FK_TABLE1");
		}
		
		result1.close();

		// Om de juiste tabel te vinden
		String sql2 = "Select * from TABLE1 where ID ='" + fk + "'";
		PreparedStatement statement2 = con.prepareStatement(sql2);

		ResultSet result2 = statement2.executeQuery();

		while(result2.next()){
			entName = result2.getString("NAME");
		}
		
		result2.close();

		return entName.substring(0,2) + entName.substring(entName.length()-1, entName.length()) + "_" + cdName;
	}

	public String generateName(String i) throws SQLException{
		rowID = i;
		String nameGenerated = null;

		nameGenerated = "BRG_" + this.getApplication(rowID) + "_" + this.getEntiteit(rowID) + "_1";

		return nameGenerated.toUpperCase();
	}

	// ^ Einde naam generatie ^

	// Hier wordt de eventName opgehaald. BEFORE, AFTER
	public String getEventName(String i) throws SQLException{
		rowID = i;
		String eventName = null;

		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			eventName = result.getString("EVENT");
		}
		return eventName;
	}


	//Hier wordt de triggerName opgehaald: UPDATE, DELETE, INSERT
	public String getTriggerName(String i) throws SQLException{
		rowID = i;
		String triggerName = null;

		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			triggerName = result.getString("RULETRIGGER");
		}
		result.close();
		return triggerName;
	}

	//Hier wordt de tableName opgehaald.
	public String getTableName(String id, int index) throws SQLException{
		rowID = id;
		int columnIndex = index-1;

		// Om de juiste kolom te vinden
		String sql1 = "Select * from COLUMN1 where BUSINESSRULE_ID ='" + rowID + "' ORDER BY ID ASC";
		PreparedStatement statement1 = con.prepareStatement(sql1);

		ResultSet result1 = statement1.executeQuery();
		ArrayList<String> foreignkeys = new ArrayList<String>();

		while(result1.next()){
			foreignkeys.add(result1.getString("FK_TABLE1"));
		}
		
		result1.close();

		ArrayList<String> bla = new ArrayList<String>();

		// Om de juiste tabel te vinden
		for(int k = 0; k < foreignkeys.size(); k++){
			String sql2 = "Select * from TABLE1 where ID ='" + foreignkeys.get(k) + "'";
			PreparedStatement statement2 = con.prepareStatement(sql2);

			ResultSet result2 = statement2.executeQuery();

			while(result2.next()){
				bla.add(result2.getString("NAME"));
			}
			
			result2.close();
		}
		String tableName = bla.get(columnIndex);
		//		System.out.println(tableName);
		return tableName;
	}

	//Hier wordt de columnName opgehaald.
	public String getColumnName(String i, int j) throws SQLException{
		rowID = i;
		int columnIndex = j-1;

		String sql = "Select * from COLUMN1 where BUSINESSRULE_ID ='" + rowID + "' ORDER BY ID ASC";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();
		ResultSet rs = result;
		ArrayList<String> bla = new ArrayList<String>();

		while (rs.next()) {
			String columnName = result.getString("NAME");
			bla.add(columnName);
		}

		return bla.get(columnIndex);
	}

	//Hier wordt de operator opgehaald
	public String getOperatorName(String i) throws SQLException{
		rowID = i;
		String operatorID = null;
		String operatorName = null;

		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			operatorID = result.getString("OPERATOR_ID");
		}

		if(operatorID == null || operatorID.equals("")){
			operatorName = "";
		}
		else{
			String sql1 = "Select * from OPERATORTYPE where ID ='" + operatorID + "'";getClass();

			PreparedStatement statement1 = con.prepareStatement(sql1);

			ResultSet result1 = statement1.executeQuery();

			while(result1.next()){
				operatorName = result1.getString("NAME");
			}
		}
		return operatorName;
	}

	// ^ het einde van het ophalen van de operator ^

	//Hier wordt de valueName opgehaald.
	public String getValueName(String i) throws SQLException{
		rowID = i;
		ArrayList<Integer> resultList = new ArrayList<Integer>();
		String sql = "Select * from VALUE where BUSINESSRULE_ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while (result.next()) {
			int result1 = result.getInt(2); 
			resultList.add(result1);
		}
		String listString = "(";

		for (int s = 0; s < resultList.size() -1; s++)
		{
			listString += "'" + resultList.get(s) + "', ";
		}
		for (int s1 = resultList.size()-1; s1 < resultList.size(); s1++){
			listString += "'" + resultList.get(s1) + "')";
		}

		return listString;	   
	}

	//Hier worden de values voor een ARNG rule toegevoegd.
	public String getValues(String id, int index) throws SQLException{
		rowID = id;
		int columnIndex = index-1;		
		String sql = "Select * from VALUE where BUSINESSRULE_ID ='" + rowID + "' ORDER BY ID ASC";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();
		ArrayList<String> bla = new ArrayList<String>();

		while (result.next()) {
			bla.add(result.getString("VALUE"));
		}
		return bla.get(columnIndex);
	}

	//Hier wordt de errorMessage opgehaald

	public String getErrorMessage(String i) throws SQLException{
		rowID = i;
		String errorMessage = null;

		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			errorMessage = result.getString("ERRORMESSAGE");
		}
		
		result.close();
		
		return errorMessage;
	}

	//Hier wordt de code van opgehaald
	public String getCode(String i) throws SQLException{
		rowID = i;
		String brCode = null;

		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			brCode = result.getString("CODE");
		}
		return brCode;
	}


	//HIER BOVEN IS NODIG OM ALLE GEGEVENS OP TE HALEN VOOR DE GENERATIE VAN DE SQL TRIGGER.


	//Hier vind de generatie van de trigger plaats. 
	public void generateTriggerTXT(String n, String loc2, String loc) throws SQLException, IOException{
		TemplateReader tr = new TemplateReader();
		String fileName = loc;
		String fileName2 = loc2;
		tr.readTemplate(fileName2, fileName);
		String tValue = null;
		String tValue2 = null;
		String tCode = this.getCode(n);
		String tName = this.generateName(n);
		String tEvent = this.getEventName(n);
		String tTrigger = this.getTriggerName(n);
		String tTable = this.getTableName(n,1);
		String tTable2 = this.getTableName(n,2);
		String tColumn = this.getColumnName(n,1);
		String tColumn2 = this.getColumnName(n,2);
		String tColumn3 = this.getColumnName(n,3);
		String tOperator = this.getOperatorName(n);

		if(tCode.equals("ACMP")){
			tValue = this.getValueName(n);
		}
		else if(tCode.equals("ALIS")){
			tValue = this.getValueName(n);	
		}
		else if(tCode.equals("ARNG")){
			tValue = this.getValues(n, 1);
			tValue2 = this.getValues(n, 2);
		}
		else if(tCode.equals("ICMP")){
			tValue = this.getValues(n, 1);
		}
		else if(tCode.equals("AOTH")){
			tValue = this.getValues(n, 1);
			tValue2 = this.getValues(n, 2);
		}
		else if(tCode.equals("MODI")){
			tValue = this.getValues(n, 1);
		}
		String tError = this.getErrorMessage(n);


		tr.changeNames(fileName, "%TRIGGERNAME%", tName);
		tr.changeNames(fileName, "%EVENTNAME%", tEvent);
		tr.changeNames(fileName, "%TRIGGER%", tTrigger);
		tr.changeNames(fileName, "%TABLENAME%", tTable);
		tr.changeNames(fileName, "%TABLENAME2%", tTable2);
		tr.changeNames(fileName, "%COLUMNNAME%", tColumn);
		tr.changeNames(fileName, "%COLUMNNAME2%", tColumn2);
		tr.changeNames(fileName, "%COLUMNNAME3%", tColumn3);
		tr.changeNames(fileName, "%OPERATORNAME%", tOperator);
		tr.changeNames(fileName, "%VALUENAME%", tValue);
		tr.changeNames(fileName, "%VALUENAME2%", tValue2);
		tr.changeNames(fileName, "%ERROR%", tError);

		System.out.println("--- Trigger Generated! ---");
	}


	// De trigger toevoegen aan de target database.
	public void executeTrigger(String n, String loc, String loc2) throws SQLException, IOException{

		this.generateTriggerTXT(n, loc2, loc);


		rowID = n;

		String applicationID = null;
		String ssId = null;
		String port = null;
		String host = null;
		String dbUsername = null;
		String dbPassword = null;

		//Om de applicatie id te vinden,
		String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";

		PreparedStatement statement = con.prepareStatement(sql);

		ResultSet result = statement.executeQuery();

		while(result.next()){
			applicationID = result.getString("APPLICATION_ID");
		}	

		//de juiste applicatie gegevens vinden bij de applicatieid
		String sql1 = "Select * from APPLICATION where ID ='" + applicationID + "'";

		PreparedStatement statement1 = con.prepareStatement(sql1);

		ResultSet result1 = statement1.executeQuery();

		while(result1.next()){
			dbUsername = result1.getString("DBUSERNAME");
			dbPassword = result1.getString("dbPassword");
			host = result1.getString("HOST");
			ssId = result1.getString("SSID");
			port = result1.getString("PORT");
		}			

		connection = "jdbc:oracle:thin:@" + host + ":" + port + "/" + ssId;

		con2 = DriverManager.getConnection(connection, dbUsername, dbPassword);
		System.out.println("--- Connection Target Succes! ---");

		//Hieronder wordt de trigger aan de target toegevoegd
		Path path2 = Paths.get(loc);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path2), charset);
		//		System.out.println(content);
		Statement statement2 = con2.prepareStatement(content);
		statement2.execute(content);
		statement2.close();
	}
}