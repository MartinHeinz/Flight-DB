import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.postgresql.geometric.PGpoint;

/**
 * 
 * Transaction Script pre tabulku flights
 *
 */
public class FlightModifications {
	
	/**
	 * Prida let do tabulky flights.
	 * @param date
	 * @param fromDest
	 * @param toDest
	 * @param planeId
	 * @param pilotId
	 * @param takeOff
	 * @param landing
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	void addFlight(Date date, String fromDest, String toDest, int planeId, int pilotId, Timestamp takeOff, Timestamp landing) throws SQLException, DBExceptions {
		
		Connection c = DBContext.getConnection();
		c.setAutoCommit(false);
		
		Destinations from = Destinations.findByName(fromDest);
		Destinations to = Destinations.findByName(toDest);
		if (!date.after(Date.valueOf(LocalDate.now()))) {
			throw new DBExceptions("Invalid date.");
		}
		if(from == null || to == null) {
			throw new DBExceptions("Missing Destination.");
		}
		
		ResultSet r = Flights.findBy_planeId_date(planeId, date);
		if (r.next() != false) {
			throw new DBExceptions("Plane not available.");
		}
		
		r = Flights.findBy_pilotId_date(pilotId, date);
		if (r.next() != false) {
			throw new DBExceptions("Pilot not available.");
		}
		if (landing != null && !takeOff.before(landing)) {
			throw new DBExceptions("Invalid times.");
		}
		Flights input = new Flights();
		input.date = date;
		input.from_id = from.id;
		input.to_id = to.id;
		input.pilot_id = pilotId;
		input.plane_id = planeId;
		input.take_off_time = takeOff;
		input.landing_time = landing;
		input.insert();
		
		c.commit();
		DBContext.close();
	}
	
	/**
	 * Zmaze let a vsetky naviazane listky
	 * @param flight_id
	 * @throws SQLException
	 */
	public static void deleteFlight(int flight_id) throws SQLException {
		Connection c = DBContext.getConnection();
		c.setAutoCommit(false);
		
		ResultSet r = Tickets.findByFlightId(flight_id);
		while (r.next()) {
			Tickets t = new Tickets();
			t.id = r.getInt("id");
			t.delete();
		}
		
		Flights f = new Flights();
		f.id = flight_id;
		f.delete();

		
		c.commit();
		DBContext.close();
	}
	
	
	/**
	 * Zmeni lietadlo pre dany let flight_id
	 * @param flight_id
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public static void changePlane(int flight_id) throws SQLException, DBExceptions {
		Connection c = DBContext.getConnection();
		c.setAutoCommit(false);
		
		ResultSet ids = Planes.getAll_IDs();
		Flights f = Flights.findById(flight_id);
		ResultSet ids_by_date = Flights.getPlaneByDate(f.date);
		ArrayList<Integer> plane_ids = new ArrayList<Integer>();
		ArrayList<Integer> plane_ids_on_date = new ArrayList<Integer>();
		while(ids.next()) {
			plane_ids.add(ids.getInt(1));
		}
		while(ids_by_date.next()) {
			plane_ids_on_date.add(ids_by_date.getInt(1));
		}
		int res = plane_ids.get(0);
		boolean flag = false;
		for (int i = 1; i < plane_ids.size(); i++) {
			if (plane_ids_on_date.contains(res)) {
				res = plane_ids.get(i);
			}
			else {
				flag = true;
				break;
			}
		}
		if (!flag) {
			throw new DBExceptions("No plane available.");
		}
		f.plane_id = res;
		f.update();
		
		c.commit();
		DBContext.close();
	}
	
}

/**
 * Transaction Script pre tabulku passengers
 *
 */
class PassengerModifications {
	
	/**
	 * Da vernostnu kartu cestujucemu "passengerId"
	 * @param passengerId
	 * @param discount
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	void giveLoyaltyCard(int passengerId, BigDecimal discount) throws SQLException, DBExceptions {
		Passengers curr = Passengers.findById(passengerId);
		if (curr == null) {
			throw new DBExceptions("Invalid Passenger ID.");
		}
		
		Loyalty_cards input = new Loyalty_cards();
		input.discount = discount;
		input.passenger_id = passengerId;
		input.insert();
	}
}

/**
 * Transaction Script pre tabulku tickets
 *
 */
class TicketModifications {
	
	BigDecimal economy = new BigDecimal(50);
	BigDecimal business = new BigDecimal(100);
	BigDecimal first = new BigDecimal(200);
	
	/**
	 * Kupi listok na let flight_id pre zakaznika passenger_id
	 * @param passenger_id
	 * @param flight_id
	 * @param input_money
	 * @param travel_class
	 * @throws SQLException
	 * @throws DBExceptions
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	void buyTickets(int passenger_id, int flight_id, BigDecimal input_money, String travel_class) throws SQLException, DBExceptions, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Connection c = DBContext.getConnection();
		c.setAutoCommit(false);
		
		if (Passengers.findById(passenger_id) == null) {
			throw new DBExceptions("Invalid Passenger ID.");
		}
		Flights f = Flights.findById(flight_id);
		if (f == null) {
			throw new DBExceptions("Invalid Flight ID.");
		}
		
		if (f.take_off_time.before(new Timestamp(System.currentTimeMillis()))) {
			throw new DBExceptions("Past Flight ID.");
		}
		Loyalty_cards card = Loyalty_cards.findById(passenger_id);
		Tickets ticket = new Tickets();
		ticket.passenger_id = passenger_id;
		ticket.flight_id = flight_id;
		if (!checkClass(card, input_money, travel_class, ticket)) {
			throw new DBExceptions("Invalid travel class.");
		}
		ticket.travel_class = travel_class;
		ticket.insert();
		
		c.commit();
		DBContext.close();
	}
	
	private Boolean checkClass(Loyalty_cards card, BigDecimal input_money, String travel_class, Tickets ticket) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		if (travel_class.equals("economy") || travel_class.equals("business") || travel_class.equals("first")) {
			Field f = this.getClass().getDeclaredField(travel_class);
			f.setAccessible(true);
			BigDecimal value = (BigDecimal)f.get(this);
			if(card != null) {
				if (input_money.compareTo(value.subtract(value.multiply(card.discount))) > -1) {
					ticket.price = value.subtract(value.multiply(card.discount));
					return true;
				}
			}
			else if (input_money.compareTo(value) > -1) {
				ticket.price = value;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Kupi listok pre noveho zakaznika
	 * @param first_name
	 * @param last_name
	 * @param email
	 * @param flight_id
	 * @param input_money
	 * @param travel_class
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws DBExceptions
	 */
	public void newPassengerTicketOrder(String first_name, String last_name, String email, int flight_id, BigDecimal input_money, String travel_class) throws SQLException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, DBExceptions {
//		Connection c = DBContext.getConnection();
//		c.setAutoCommit(false);
		Passengers p = new Passengers();
		p.email = email;
		p.first_name = first_name;
		p.last_name = last_name;
		p.insert();
		Passengers p0 = Passengers.findByNames(first_name, last_name);
		buyTickets(p0.id, flight_id, input_money, travel_class);
	}
}

/**
 * Transaction Script pre tabulku planes
 *
 */
class PlaneModifications {
	
	static final double _eQuatorialEarthRadius = 6378.1370D;
	
	/**
	 * aktualizuje nalietane kilometre lietadla prisluchajuceho k letu flight_id
	 * @param flight_id
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public void addFlightDistance(int flight_id) throws SQLException, DBExceptions {
		Connection c = DBContext.getConnection();
		c.setAutoCommit(false);
		
		Flights f = Flights.findById(flight_id);
		if (f == null) {
			throw new DBExceptions("Invalid Flight ID.");
		}
		int from = f.from_id;
		int to = f.to_id;
		int plane = f.plane_id;
		Planes p = Planes.findById(plane);
		
		if (p == null) {
			throw new DBExceptions("Invalid Plane ID.");
		}
		
		Destinations fromDest = Destinations.findById(from);
		Destinations toDest = Destinations.findById(to);
		if (fromDest == null || toDest == null) {
			throw new DBExceptions("Invalid Destination ID.");
		}
		PGpoint fromGPS = fromDest.location;
		PGpoint toGPS = toDest.location;
		double dlat = (toGPS.x - fromGPS.x) * (Math.PI / 180D);
        double dlong = (toGPS.y - fromGPS.y) * (Math.PI / 180D);
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(fromGPS.x * (Math.PI / 180D)) * Math.cos(toGPS.x * (Math.PI / 180D)) * Math.pow(Math.sin(dlong / 2D), 2D);
        double x = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        int distance = (int) (_eQuatorialEarthRadius * x);
        
        p.condition = changeCondition(p.distance_flown, p.distance_flown + distance, p.condition);
        p.distance_flown += distance;
        p.update();
        
		c.commit();
		DBContext.close();
	}
	

	/**
	 * Skontroluje stav lietadla, v pripade nevyhovujuceho stavu zrusi prisluchajuce lety.
	 * @param id
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public void checkPlane(int id) throws SQLException, DBExceptions {
		Connection c = DBContext.getConnection();
		c.setAutoCommit(false);
		
		Planes p = Planes.findById(id);
		if (p == null) {
			throw new DBExceptions("Plane not found.");
		}
		
		if (p.condition > 10) {
			p.delete(); // delete aj lety a tickety
			ResultSet r = Flights.getByPlaneId(id);
			while (r.next()) {
				FlightModifications.deleteFlight(r.getInt("id"));
			}
		}
		else {
			p.condition = 0;
			p.last_check_date = Date.valueOf(LocalDate.now());
			p.update();
		}
		
		c.commit();
		DBContext.close();
	}
	
	/**
	 * zmeni stav lietadla podla nalietanych kilometrov
	 * @param currDist
	 * @param newDist
	 * @param condition
	 * @return new condition
	 */
	public int changeCondition(int currDist, int newDist, int condition) {
		if (newDist % 10000 < currDist % 10000) {
			condition++;
		}
		return condition;
	}
}

/**
 * Transaction Script pre pocitanie statistik
 *
 */
class Statistics {
	
	/**
	 * Vrati utriedeny list hodnot.
	 * @param map
	 * @return sortedEntries
	 */
	public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

		Collections.sort(sortedEntries, 
				new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		}
				);

		return sortedEntries;
	}
	
	/**
	 * Vrati najcastejsie destinacie(prvych 10) letov utriedene zostupne
	 * @return LinkedHashMap<String, Integer>
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public LinkedHashMap<String, Integer> mostFrequentDest() throws SQLException, DBExceptions {
		ResultSet r = Flights.getAll();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		java.util.Date lastMonth = cal.getTime();
		TreeMap<Integer, Integer> destCount = new TreeMap<Integer, Integer>();
		while(r.next()) {
			if (r.getDate(4).after(lastMonth)) { // date
				int id = r.getInt(6); // to_id
				if (destCount.containsKey(id)) {
					destCount.put(id, destCount.get(id)+1);
				}
				else {
					destCount.put(id, 1);
				}
			}
		}
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		List<Entry<Integer, Integer>> destSorted = entriesSortedByValues(destCount);
		for (Entry<Integer, Integer> e : destSorted) {
			if (result.size() == 10) {
				break;
			}
			result.put(Destinations.findById(e.getKey()).name, e.getValue());
		}
		return result;
	}
	
	
	/**
	 * Vrati pocet letov za mesiac utriedene zostupne
	 * @return LinkedHashMap<String, Integer>
	 * @throws SQLException
	 */
	public LinkedHashMap<String, Integer> flightDemandPerMonth() throws SQLException {
		ResultSet r = Flights.getDates();
		TreeMap<String, Integer> months = new TreeMap<String, Integer>();
		
		while(r.next()) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(r.getDate(1).getTime());
			String currMonth = new SimpleDateFormat("MMMM").format(cal.getTime());
			if (months.containsKey(currMonth)) {
				months.put(currMonth, months.get(currMonth) + 1);
			}
			else {
				months.put(currMonth, 1);
			}
		}
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		List<Entry<String, Integer>> monthsSorted = entriesSortedByValues(months);
		for (Entry<String, Integer> e : monthsSorted) {
			result.put(e.getKey(), e.getValue());
		}
		
		return result;
	}
	
	/**
	 * Vrati najziskovejsie destinacie(prvych 10) utriedenych zostupne
	 * @param month
	 * @return LinkedHashMap<String, BigDecimal>
	 * @throws SQLException
	 */
	public LinkedHashMap<String, BigDecimal> mostProfitableDest(String month) throws SQLException {
		ResultSet r = SelectStatsData.getPricesDuringMonth(month);
		TreeMap<Integer, BigDecimal> destinations = new TreeMap<Integer, BigDecimal>();
		while(r.next()) {
			int to_id = r.getInt(2);
			BigDecimal price = r.getBigDecimal(3);
			if (destinations.containsKey(to_id)) {
				destinations.put(to_id, destinations.get(to_id).add(price));
			}
			else {
				destinations.put(to_id, price);
			}
		}
		LinkedHashMap<String, BigDecimal> result = new LinkedHashMap<String, BigDecimal>();
		List<Entry<Integer, BigDecimal>> destinationsSorted = entriesSortedByValues(destinations);
		for (Entry<Integer, BigDecimal> e : destinationsSorted) {
			if (result.size() == 10) {
				break;
			}
			result.put(Destinations.findById(e.getKey()).name, e.getValue());
		}
		return result;
	}
}



















