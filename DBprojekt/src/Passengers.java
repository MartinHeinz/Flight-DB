import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import org.postgresql.geometric.PGpoint;

/**
 * 
 * Row data gateway pre tabulku passengers
 *
 */
public class Passengers {
	String first_name;
	String last_name;
	int id;
	String email;
	
	/**
	 * Insert this do tabulky passengers
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO passengers (first_name, last_name, email) VALUES (?, ?, ?)");
		s.setString(1, first_name);
		s.setString(2, last_name);
		s.setString(3, email);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Update this v tabulke passengers, kde id == this.id
	 * @throws SQLException
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE passengers SET first_name = ?, last_name = ?, email = ? WHERE id = ?");
		s.setString(1, first_name);
		s.setString(2, last_name);
		s.setString(3, email);
		s.setInt(4, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Update this z tabulky passengers, kde id == this.id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM passengers WHERE id = ?"); // CASCADE??
		s.setInt(1, id);
		s.executeUpdate(); // close????
	}
	
	
	/**
	 * vrati istanciu triedy Passenger, taku ze id == passenger.id
	 * @param id - passenger id
	 * @return Passenger
	 * @throws SQLException
	 */
	public static Passengers findById(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM passengers WHERE id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Passengers passenger = new Passengers();
		passenger.id = id;
		passenger.first_name = r.getString("first_name");
		passenger.last_name = r.getString("last_name");
		passenger.email = r.getString("email");
		
		// r.close(); s.close; ????
		return passenger;
	}
	
	/**
	 * Vrati instanciu triedy Passenger, taku, ze first_name == passenger.first_name && last_name == passenger.last_name
	 * @param first_name
	 * @param last_name
	 * @return
	 * @throws SQLException
	 */
	static Passengers findByNames(String first_name, String last_name) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM passengers WHERE first_name = ? AND last_name = ?");
		s.setString(1, first_name);
		s.setString(2, last_name);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Passengers passenger = new Passengers();
		passenger.id = r.getInt("id");
		passenger.first_name = r.getString("first_name");
		passenger.last_name = r.getString("last_name");
		passenger.email = r.getString("email");
		
		// r.close(); s.close; ????
		return passenger;
	}
}


/**
 * Row data gateway pre tabulku loyalty_cards
 * 
 *
 */
class Loyalty_cards {
	int passenger_id;
	BigDecimal discount;
	
	
	/**
	 * Insert this do tabulky loyalty_cards
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO loyalty_cards (passenger_id, discount) VALUES (?, ?)");
		s.setInt(1, passenger_id);
		s.setBigDecimal(2, discount);
		s.executeUpdate(); // close????	
	}
	
	
	/**
	 * Update this v tabulke loyalty_cards, kde passenger_id == this.passenger_id
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE loyalty_cards SET discount = ? WHERE passenger_id = ?");
		s.setBigDecimal(1, discount);
		s.setInt(2, passenger_id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Delete this z tabulky loayalty_cards, kde passenger_id == this. passeneger_id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM loyalty_cards WHERE passenger_id = ?"); // CASCADE??
		s.setInt(1, passenger_id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Vrati instanciu Loyalty cards, param == loyalty_cards.passenger_id
	 * @param passenger_id
	 * @return Loyalty_cards
	 * @throws SQLException
	 */
	public static Loyalty_cards findById(int passenger_id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM loyalty_cards WHERE passenger_id = ?");
		s.setInt(1, passenger_id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Loyalty_cards lc = new Loyalty_cards();
		lc.passenger_id = passenger_id;
		lc.discount = r.getBigDecimal("discount");
		
		// r.close(); s.close; ????
		return lc;
	}
}

/**
 * 
 * Row data gateway pre tabulku tickets
 *
 */
class Tickets {
	int id;
	int passenger_id;
	int flight_id;
	BigDecimal price;
	String travel_class;
	
	
	/**
	 * Insert this do tabulky tickets
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO tickets (passenger_id, flight_id, price, travel_class) VALUES (?, ?, ?, ?)");
		s.setInt(1, passenger_id);
		s.setInt(2, flight_id);
		s.setBigDecimal(3, price);
		s.setString(4, travel_class);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Update this v tabulke tickets, kde this.id == tickets.id
	 * @throws SQLException
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE tickets SET price = ?, travel_class = ?, passenger_id = ?, flight_id = ? WHERE id = ?");
		s.setBigDecimal(1, price);
		s.setString(2, travel_class);
		s.setInt(3, passenger_id);
		s.setInt(4, flight_id);
		s.setInt(5, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Delete this z tabulky tickets, kde this.id == tickets.id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM tickets WHERE id = ?"); // CASCADE??
		s.setInt(1, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Vrati instanciu Tickets, taku ze param == tickets.id
	 * @param id
	 * @return Tickets
	 * @throws SQLException
	 */
	public static Tickets findById(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM tickets WHERE id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Tickets tickets = new Tickets();
		tickets.id = id;
		tickets.passenger_id = r.getInt("passenger_id");
		tickets.flight_id = r.getInt("flight_id");
		tickets.price = r.getBigDecimal("price");
		tickets.travel_class = r.getString("travel_class");
		
		// r.close(); s.close; ????
		return tickets;
	}
	
	/**
	 * Vrati vsetky listky na let s id == param
	 * @param id
	 * @return ResultSet Tickets
	 * @throws SQLException
	 */
	public static ResultSet findByFlightId(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM tickets WHERE flight_id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		return r;
	}
}


/**
 * 
 * Row data gateway pre tabulku pilots
 *
 */
class Pilots {
	String first_name;
	String last_name;
	int id;
	
	/**
	 * Insert this fo tabulky pilots
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO pilots (first_name, last_name) VALUES (?, ?)");
		s.setString(1, first_name);
		s.setString(2, last_name);
		s.executeUpdate(); // close????	
	}
	
	
	/**
	 * Update this v tabulke pilots, kde this.id == pilots.id
	 * @throws SQLException
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE pilots SET first_name = ?, last_name = ? WHERE id = ?");
		s.setString(1, first_name);
		s.setString(2, last_name);
		s.setInt(3, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Delete this v tabulke pilots, kde this.id = pilots.id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM pilots WHERE id = ?"); // CASCADE??
		s.setInt(1, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Vrati instanciu Pilots, taku ze param == pilots.id
	 * @param id
	 * @return Pilots
	 * @throws SQLException
	 */
	public static Pilots findById(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM pilots WHERE id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Pilots pilots = new Pilots();
		pilots.id = id;
		pilots.first_name = r.getString("first_name");
		pilots.last_name = r.getString("last_name");
		
		// r.close(); s.close; ????
		return pilots;
	}
}


/**
 * 
 * Row data gateway pre tabulku destinations
 *
 */
class Destinations {
	int id;
	PGpoint location;
	String name;
	
	/**
	 * Insert this do tabulky destinations
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO destinations (location, name) VALUES (point(?, ?), ?)");
		s.setDouble(1, location.x);
		s.setDouble(2, location.y);
		s.setString(3, name);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Update this v tabulke destinations, kde this.id == destinations.id
	 * @throws SQLException
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE destinations SET location = ?, name = ? WHERE id = ?");
		s.setObject(1, location);
		s.setString(2, name);
		s.setInt(3, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Delete this z tabulky destiantions, kde this.id == destinations.id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM destinations WHERE id = ?"); // CASCADE??
		s.setInt(1, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Vrati instanciu Destinations, take ze param == destinations.id
	 * @param id
	 * @return Destinations
	 * @throws SQLException
	 */
	public static Destinations findById(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM destinations WHERE id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Destinations destinations = new Destinations();
		destinations.id = id;
		destinations.location = (PGpoint) r.getObject("location"); // cast?
		destinations.name = r.getString("name");
		
		// r.close(); s.close; ????
		return destinations;
	}
	
	/**
	 * Vrati instanciu Destinations, taku ze param == dsetinations.name
	 * @param name
	 * @return Destinations
	 * @throws SQLException
	 */
	public static Destinations findByName(String name) throws SQLException  {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM destinations WHERE name = ?");
		s.setString(1, name);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Destinations destinations = new Destinations();
		destinations.id = r.getInt("id");
		destinations.location = (PGpoint) r.getObject("location"); // cast?
		destinations.name = name;
		
		// r.close(); s.close; ????
		return destinations;
	}
}

/**
 * 
 * Row data gateway pre tabulku planes
 *
 */
class Planes {
	int id;
	int seat_num;
	String code_name;
	int age;
	int distance_flown;
	Date last_check_date;
	int condition;
	
	/**
	 * Insert this do tabulky planes, kde this.id == planes.id
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO planes (seat_num, code_name, age, distance_flown, last_check_date) VALUES (?, ?, ?, ?, ?, ?)");
		s.setInt(1, seat_num);
		s.setString(2, code_name);
		s.setInt(3, age);
		s.setInt(4, distance_flown);
		s.setDate(5, last_check_date);
		s.setInt(6, condition);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Update this v tabulke planes, kde this.id == planes.id
	 * @throws SQLException
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE planes SET seat_num = ?, code_name = ?, age = ?, distance_flown = ?, last_check_date = ?, condition = ? WHERE id = ?");
		s.setInt(1, seat_num);
		s.setString(2, code_name);
		s.setInt(3, age);
		s.setInt(4, distance_flown);
		s.setDate(5, last_check_date);
		s.setInt(6, condition);
		s.setInt(7, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Delete this z tabulky planes, kde this.id == planes.id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM planes WHERE id = ?"); // CASCADE??
		s.setInt(1, id);
		s.executeUpdate(); // close????	
	}
	
	
	/**
	 * Vrati instanciu Planes, taku ze param == planes.id
	 * @param id
	 * @return Planes
	 * @throws SQLException
	 */
	public static Planes findById(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM planes WHERE id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Planes planes = new Planes();
		planes.id = id;
		planes.seat_num = r.getInt("seat_num"); // cast?
		planes.code_name = r.getString("code_name");
		planes.age = r.getInt("age");
		planes.distance_flown = r.getInt("distance_flown");
		planes.last_check_date = r.getDate("last_check_date");
		planes.condition = r.getInt("condition");
		// r.close(); s.close; ????
		return planes;
	}
	
	
	/**
	 * Vrati vsetky id lietadiel.
	 * @return ResultSet
	 * @throws SQLException
	 */
	public static ResultSet getAll_IDs() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT id FROM planes");
		ResultSet r = s.executeQuery();
		return r;
	}
}

/**
 * 
 * Row data gateway pre tabulku flights
 *
 */
class Flights {
	int id;
	Timestamp take_off_time;
	Timestamp landing_time;
	Date date;
	int from_id;
	int to_id;
	int pilot_id;
	int plane_id;
	
	/**
	 * Insert do tabulky flights, kde this.id == flights.id
	 * @throws SQLException
	 */
	void insert() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("INSERT INTO flights (take_off_time, landing_time, date, from_id, to_id, pilot_id, plane_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
		s.setTimestamp(1, take_off_time);
		s.setTimestamp(2, landing_time);
		s.setDate(3, date);
		s.setInt(4, from_id);
		s.setInt(5, to_id);
		s.setInt(6, pilot_id);
		s.setInt(7, plane_id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Update this v tabulke flights, kde this.id == flights.id
	 * @throws SQLException
	 */
	void update() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("UPDATE flights SET take_off_time = ?, landing_time = ?, date = ?, from_id = ?, to_id = ?, pilot_id = ?, plane_id = ? WHERE id = ?");
		s.setTimestamp(1, take_off_time);
		s.setTimestamp(2, landing_time);
		s.setDate(3, date);
		s.setInt(4, from_id);
		s.setInt(5, to_id);
		s.setInt(6, pilot_id);
		s.setInt(7, plane_id);
		s.setInt(8, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Delete this z tabulky flights, kde this.id == flights.id
	 * @throws SQLException
	 */
	void delete() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("DELETE FROM flights WHERE id = ?"); // CASCADE??
		s.setInt(1, id);
		s.executeUpdate(); // close????	
	}
	
	/**
	 * Vrati vsetky lety, take ze planeId == flights.plane_id && date == flights.date
	 * @param planeId
	 * @param date
	 * @return ResultSet Flights
	 * @throws SQLException
	 */
	static ResultSet findBy_planeId_date(int planeId, Date date) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM flights WHERE plane_id = ? AND date = ?");
		s.setInt(1, planeId);
		s.setDate(2, date);
		ResultSet r = s.executeQuery();
		return r;
	}
	
	/**
	 * Vrati vsetky lety, take ze pilot_id == flights.pilot_id && date == flights.date
	 * @param pilot_id
	 * @param date
	 * @return ResultSet Flights
	 * @throws SQLException
	 */
	static ResultSet findBy_pilotId_date(int pilot_id, Date date) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM flights WHERE pilot_id = ? AND date = ?");
		s.setInt(1, pilot_id);
		s.setDate(2, date);
		ResultSet r = s.executeQuery();
		return r;
	}
	
	/**
	 * Vrati instanciu Flights, taku ze param == flights.id
	 * @param id
	 * @return Flights
	 * @throws SQLException
	 */
	public static Flights findById(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM flights WHERE id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		if (r.next() == false) return null;
		
		Flights flights = new Flights();
		flights.id = id;
		flights.take_off_time = r.getTimestamp("take_off_time"); // cast?
		flights.landing_time = r.getTimestamp("landing_time");
		flights.date = r.getDate("date");
		flights.from_id = r.getInt("from_id");
		flights.to_id = r.getInt("to_id");
		flights.pilot_id = r.getInt("pilot_id");
		flights.plane_id = r.getInt("plane_id");
		
		// r.close(); s.close; ????
		return flights;
	}
	
	/**
	 * Vrati vsetky lety
	 * @return ResultSet Flights
	 * @throws SQLException
	 */
	static ResultSet getAll() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM flights");
		ResultSet r = s.executeQuery();
		return r;
	}
	
	/**
	 * Vrati lietadlo letiace v dany den date
	 * @param date
	 * @return ResultSet
	 * @throws SQLException
	 */
	static ResultSet getPlaneByDate(Date date) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT plane_id FROM flights WHERE ? = date");
		s.setDate(1, date);
		ResultSet r = s.executeQuery();
		return r;
	}
	
	/**
	 * Vrati vsetky datumy letov
	 * @return ResultSet Flights
	 * @throws SQLException
	 */
	static ResultSet getDates() throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT date FROM flights");
		ResultSet r = s.executeQuery();
		return r;
	}
	
	/**
	 * Vrati vsetky lety, take ze param == flights.plane_id
	 * @return ResultSet Flights
	 * @throws SQLException
	 */
	static ResultSet getByPlaneId(int id) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement("SELECT * FROM flights WHERE plane_id = ?");
		s.setInt(1, id);
		ResultSet r = s.executeQuery();
		return r;
	}
}

/**
 * 
 * Pomocne funkcie(queries) pre statistiky
 *
 */
class SelectStatsData {
	
	/**
	 * Vrati ceny vsetkych listov za mesiac "param"
	 * @param month - format = <"01" - "12">
	 * @return ResultSet
	 * @throws SQLException
	 */
	static ResultSet getPricesDuringMonth(String month) throws SQLException {
		Connection c = DBContext.getConnection();
		PreparedStatement s = c.prepareStatement
				("SELECT f.date, f.to_id, t.price FROM flights AS f JOIN tickets AS t ON f.id = t.flight_id WHERE date_part('month', f.date) = ?::integer");
		s.setString(1, month);
		ResultSet r = s.executeQuery();
		return r;
	}
}




















