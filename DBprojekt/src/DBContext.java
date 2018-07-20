import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.postgresql.jdbc2.optional.ConnectionPool;



/**
 * Trieda pre pripojenie k DB.
 *
 */
public class DBContext {

	public static void main(String[] args) throws SQLException, DBExceptions, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		DBContext DB = new DBContext("serverName", "dbName", "userName", "passwd", 5432, 5, 15); // DB credentials removed for security. Use localhost.
		Statistics stat = new Statistics();
		try {
			System.out.println(stat.mostFrequentDest());
			System.out.println(stat.flightDemandPerMonth());
			System.out.println(stat.mostProfitableDest("05"));
			Generate g = new Generate();
			
			g.generate_flights(1000);
			g.generate_tickets(2000);
			
		} catch (SQLException | DBExceptions e) {
			e.printStackTrace();
	}		
}
	
	static ConnectionPool pool;
	static ThreadLocal<Connection> connection = new ThreadLocal<>();
	
	public DBContext(String serverName, String DBName, String userName, String password, int port, int initialConCount, int maxConCount) {
		pool = new ConnectionPool();
		pool.setServerName(serverName);
		pool.setDatabaseName(DBName);
		pool.setUser(userName);
		pool.setPassword(password);
		pool.setPortNumber(port);
		//pool.setInitialConnections(initialConCount);
		//pool.setMaxConnections(maxConCount);
	}
	
	/**
	 * Ziska Connection z poolu.
	 * @return Connection
	 * @throws SQLException
	 */
	static Connection getConnection() throws SQLException {
		if (connection.get() == null) {
			connection.set(pool.getConnection());
		}
		return connection.get();
	}
	
	/**
	 * uzatvori connection
	 * @throws SQLException
	 */
	static void close() throws SQLException {
		if (connection.get() != null) {
			connection.get().close();
			connection.set(null);
		}
	}
}

/**
 * Trieda pre generovanie testovacich dat.
 *
 */
class Generate {
	
	
	/**
	 * Vygenegruje num letov
	 * @param num
	 * @throws SQLException
	 */
	public void generate_flights(int num) throws SQLException {
		Random rnd = new Random();
		ArrayList<Integer> pl = new ArrayList<>();
		ArrayList<Integer> pi = new ArrayList<>();
		ArrayList<Integer> dest = new ArrayList<>();
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM planes");
		ResultSet planes = s.executeQuery();
		while(planes.next()) {
			pl.add(planes.getInt("id"));
		}
		s = c.prepareStatement("SELECT * FROM pilots");
		ResultSet pilots = s.executeQuery();
		while(pilots.next()) {
			pi.add(pilots.getInt("id"));
		}
		s = c.prepareStatement("SELECT * FROM destinations");
		ResultSet destinations = s.executeQuery();
		while(destinations.next()) {
			dest.add(destinations.getInt("id"));
		}
		Flights f = new Flights();
		LocalDate ld = LocalDate.now();
		LocalDateTime cal = LocalDateTime.now();
		
		for (int i = 0; i < num; i++) {
			
			ld = ld.plusDays(rnd.nextInt(2)+1);
			cal = ld.atStartOfDay();
			cal = cal.plusHours(rnd.nextInt(12));
			f.take_off_time = Timestamp.valueOf(cal);
			f.date = java.sql.Date.valueOf(cal.toLocalDate());
			cal = cal.plusHours(rnd.nextInt(11)+1);
			f.landing_time = Timestamp.valueOf(cal);
			f.from_id = dest.get(rnd.nextInt(dest.size()));
			f.to_id = dest.get(rnd.nextInt(dest.size()));
			while (f.to_id == f.from_id) {
				f.to_id = dest.get(rnd.nextInt(dest.size()));
			}
			f.pilot_id = pi.get(rnd.nextInt(pi.size()));
			f.plane_id = pl.get(rnd.nextInt(pl.size()));
			f.insert();
		}
	}
	
	/**
	 * Vygeneruje num listkov
	 * @param num
	 * @throws SQLException
	 */
	public void generate_tickets(int num) throws SQLException {
		Random rnd = new Random();
		ArrayList<Integer> fl = new ArrayList<>();
		ArrayList<Integer> pas = new ArrayList<>();
		TreeMap<Integer, Integer> freq = new TreeMap<Integer, Integer>();
		String[] classes = {"economy", "business", "first"};
		
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM flights");
		ResultSet flights = s.executeQuery();
		while(flights.next()) {
			fl.add(flights.getInt("id"));
		}
		s = c.prepareStatement("SELECT * FROM passengers");
		ResultSet passengers = s.executeQuery();
		while(passengers.next()) {
			pas.add(passengers.getInt("id"));
		}

		Tickets ticket = new Tickets();
		for (int i = 0; i < num; i++) {
			ticket.price = new BigDecimal(rnd.nextInt(250) + 50);
			ticket.travel_class = classes[rnd.nextInt(3)];
			int fl_id = fl.get(rnd.nextInt(fl.size()));
			while (freq.containsKey(fl_id) && freq.get(fl_id) > 100) {
				fl_id = fl.get(rnd.nextInt(fl.size()));
			}
			ticket.flight_id = fl_id;
			ticket.passenger_id = pas.get(rnd.nextInt(pas.size()));
			ticket.insert();
		}
	}
}














