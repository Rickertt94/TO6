

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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class Connector {
	private String SSID, host, dbUsername, dbPassword, applicatie, port, connection;
	private String businessTrigger, businessEvent, columnName, businessValue, rowID, ruleCode, ruleName, operatorName = null;
	private String tSQL1, appName1, tCode, cdName, entName, appName, tSQL, tName, tEvent, tTrigger, tTable, tColumnID, tOperatorID, tValueID;
	private Connection con, con2;
	
	public void connect(String s, String p, String h, String dbU, String dbP) throws SQLException{
		SSID = s; 
		port = p;
		host = h;
		dbUsername = dbU;
		dbPassword = dbP;
		connection = "jdbc:oracle:thin:@" + host + ":" + port + "/" + SSID;
		
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
				
				//Vanaf hier opzoek naar de applicatie gegevens. 
				String sql1 = "Select * from APPLICATION where ID ='" + appName + "'";
				
				PreparedStatement statement1 = con.prepareStatement(sql1);
				
				ResultSet result1 = statement1.executeQuery();
				
				while(result1.next()){
					appName1 = result1.getString("HOST");
				}		
				
				return appName1.substring(0,2) + appName1.substring(appName1.length()-1, appName1.length());
			}
			
			public String getEntiteit(String i) throws SQLException{
				rowID = i;
				
				String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";
				
				PreparedStatement statement = con.prepareStatement(sql);
				
				ResultSet result = statement.executeQuery();
				
				while(result.next()){
					entName = result.getString("TABLE1");
					cdName = result.getString("CODE");
					}		
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
			return triggerName;
		}
	
		//Hier wordt de tableName opgehaald.
		public String getTableName(String i) throws SQLException{
			rowID = i;
			String tableName = null;
			
			String sql = "Select * from BUSINESSRULE where ID ='" + rowID + "'";
				
			PreparedStatement statement = con.prepareStatement(sql);
				
			ResultSet result = statement.executeQuery();
				
			while(result.next()){
				tableName = result.getString("TABLE1");
			}
			return tableName;
		}
		
		//Hier wordt de columnName opgehaald.
		public String getColumnName(String i) throws SQLException{
			rowID = i;
			String columnName = null;
			
			String sql = "Select * from COLUMN1 where BUSINESSRULE_ID ='" + rowID + "'";
				
			PreparedStatement statement = con.prepareStatement(sql);
				
			ResultSet result = statement.executeQuery();
				
			while(result.next()){
				columnName = result.getString("NAME");
			}
			return columnName;
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
			
			String sql1 = "Select * from OPERATORTYPE where ID ='" + operatorID + "'";
			
			PreparedStatement statement1 = con.prepareStatement(sql1);
				
			ResultSet result1 = statement1.executeQuery();
				
			while(result1.next()){
				operatorName = result1.getString("NAME");
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
			String listString = "";

			for (int s = 0; s < resultList.size() -1; s++)
			{
			    listString += "'" + resultList.get(s) + "', ";
			}
			for (int s1 = resultList.size()-1; s1 < resultList.size(); s1++){
				listString += "'" + resultList.get(s1) + "'";
			}
			
		return listString;	   
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
			return errorMessage;
		}
	
		
	//HIER BOVEN IS NODIG OM ALLE GEGEVENS OP TE HALEN VOOR DE GENERATIE VAN DE SQL TRIGGER.
		
		
	//Hier vind de generatie van de trigger plaats. 
		public void generateTriggerTXT(String n, String loc) throws SQLException, IOException{
			TemplateReader tr = new TemplateReader();
			String fileName = loc;
			
			tr.readTemplate(fileName);
			
			String tName = this.generateName(n);
			String tEvent = this.getEventName(n);
			String tTrigger = this.getTriggerName(n);
			String tTable = this.getTableName(n);
			String tColumn = this.getColumnName(n);
			String tOperator = this.getOperatorName(n);
			String tValue = this.getValueName(n);
			String tError = this.getErrorMessage(n);
			

			tr.changeNames(fileName, "%TRIGGERNAME%", tName);
			tr.changeNames(fileName, "%EVENTNAME%", tEvent);
			tr.changeNames(fileName, "%TRIGGER%", tTrigger);
			tr.changeNames(fileName, "%TABLENAME%", tTable);
			tr.changeNames(fileName, "%COLUMNNAME%", tColumn);
			tr.changeNames(fileName, "%OPERATORNAME%", tOperator);
			tr.changeNames(fileName, "%VALUENAME%", tValue);
			tr.changeNames(fileName, "%ERROR%", tError);
			
			System.out.println("--- Trigger Generated! ---");
		}
		
		
		// De trigger toevoegen aan de target database.
		public void executeTrigger(String n, String loc, String loc2) throws SQLException, IOException{
			
			this.generateTriggerTXT(n, loc);
			
			
			rowID = n;
			String applicationID = null, SSID = null, port = null, host = null, dbUsername = null, dbPassword = null;
			
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
				SSID = result1.getString("SSID");
				port = result1.getString("PORT");
			}			
			
			connection = "jdbc:oracle:thin:@" + host + ":" + port + "/" + SSID;
			
			con2 = DriverManager.getConnection(connection, dbUsername, dbPassword);
			System.out.println("--- Connection Target Succes! ---");
			
			//Hieronder wordt de trigger aan de target toegevoegd
			Path path2 = Paths.get(loc);
			Charset charset = StandardCharsets.UTF_8;
			String content = new String(Files.readAllBytes(path2), charset);
			
			Statement statement2 = con2.prepareStatement(content);
			System.out.println(content);
			statement2.execute(content);
	
			System.out.println(content);
		}
}