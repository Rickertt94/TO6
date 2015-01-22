import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Main {
	public static void main(String[] args) throws SQLException, IOException{
		Connector con = new Connector();
		String fileName = "C:/Users/gebruiker/Desktop/ACMP_BR1.txt";
		String fileName2 = "C:/Users/gebruiker/Desktop/ACMP_Template.txt";
		
		try {
			con.connect("cursus01.hu.nl", "8521", "ondora01.hu.nl", "tho6_2014_2c_team2", "tho6_2014_2c_team2");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		con.executeTrigger("60", fileName, fileName2);
		
	}
}
