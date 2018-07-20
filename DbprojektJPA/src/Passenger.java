import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder.In;


/**
 * Domain model pre tabulku passengers
 *
 */
@Entity @Table(name = "passengers")
public class Passenger {
	String first_name;
	String last_name;
	@GeneratedValue
	@Id
	int id;
	String email;
	@OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
	@JoinColumn(name = "passenger_id")
	Collection<Ticket> ticket;
	
	/**
	 * Najde zakaznika podla mena a emailu
	 * @param first_name
	 * @param last_name
	 * @param email
	 * @return Passenger
	 */
	public static Passenger find(String first_name, String last_name, String email) {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		TypedQuery<Passenger> query = em.createQuery(
				"SELECT p FROM Passenger p WHERE p.first_name = :first_name AND p.last_name = :last_name AND p.email = :email", Passenger.class);
		query.setParameter("first_name", first_name);
		query.setParameter("last_name", last_name);
		query.setParameter("email", email);
		return query.getSingleResult();
	}
}

/**
 * Domain model pre tabulku loyalty_cards
 *
 */
@Entity @Table(name = "loyalty_cards")
class LoyaltyCards {
	@Column(precision = 3, scale = 2)
	BigDecimal discount;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@Id
	Passenger passenger;
	
	/**
	 * Da vernostnu kartu cestujucemu "passengerId"
	 * @param passengerId
	 * @param discount
	 * @throws DBExceptions
	 */
	public void giveLoyaltyCard(int passengerId, BigDecimal discount) throws DBExceptions {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		Passenger p = em.find(Passenger.class, passengerId);
		if (p == null) {
			throw new DBExceptions("Invalid Passenger ID.");
		}
		LoyaltyCards l = new LoyaltyCards();
		l.discount = discount;
		l.passenger = p;
		em.persist(l);
		em.getTransaction().commit();
		em.close();
	}
	
	
}

/**
 * Domail model pre tabulku tickets
 *
 */
@Entity @Table(name = "tickets")
class Ticket {
	@GeneratedValue
	@Id
	int id;
	@Column(precision = 5, scale = 2)
	BigDecimal price;
	String travel_class;
	@ManyToOne
	Flight flight;
	
	@Transient
	BigDecimal economy = new BigDecimal(50);
	@Transient
	BigDecimal business = new BigDecimal(100);
	@Transient
	BigDecimal first = new BigDecimal(200);
	
	/**
	 * Vrati vsetky listky na let s id == param
	 * @param flight_id
	 * @return List<Ticket>
	 */
	public static List<Ticket> findByFlightId(int flight_id) {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		TypedQuery<Ticket> query = em.createQuery("SELECT t FROM Ticket t WHERE t.flight.id = :flight_id", Ticket.class);
		query.setParameter("flight_id", flight_id);
		return query.getResultList();
	}
	
	/**
	 * Kupi listok na let flight_id pre zakaznika passenger_id
	 * @param passenger_id
	 * @param flight_id
	 * @param input_money
	 * @param travel_class
	 * @throws DBExceptions
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	public void buyTickets(int passenger_id, int flight_id, BigDecimal input_money, String travel_class) throws DBExceptions, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		Passenger p = em.find(Passenger.class, passenger_id);
		if (p == null) {
			throw new DBExceptions("Invalid Passenger ID.");
		}
		Flight f = em.find(Flight.class, flight_id);
		if (f == null) {
			throw new DBExceptions("Invalid Flight ID.");
		}
		if (f.take_off_time.before(new Timestamp(System.currentTimeMillis()))) {
			throw new DBExceptions("Past Flight ID.");
		}
		
		LoyaltyCards lc = em.find(LoyaltyCards.class, passenger_id);
		
		Ticket t = new Ticket();
		t.flight = em.find(Flight.class, flight_id);
		p.ticket.add(t);
		if (!checkClass(lc, input_money, travel_class, t)) {
			throw new DBExceptions("Invalid travel class.");
		}
		t.travel_class = travel_class;
		em.persist(t);
		em.persist(p);
		em.getTransaction().commit();
		em.close();
	}
	
	private Boolean checkClass(LoyaltyCards card, BigDecimal input_money, String travel_class, Ticket ticket) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
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
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws DBExceptions
	 */
	public void newPassengerTicketOrder(String first_name, String last_name, String email, int flight_id, BigDecimal input_money, String travel_class) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, DBExceptions {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		Passenger p = new Passenger();
		p.first_name = first_name;
		p.last_name = last_name;
		p.email = email;
		em.persist(p);
		em.getTransaction().commit();
		
		Passenger p0 = Passenger.find(first_name, last_name, email);
		buyTickets(p0.id, flight_id, input_money, travel_class);
	}
}	

/**
 * Domain Model pre tabulku pilots
 *
 */
@Entity @Table(name = "pilots")
class Pilot {
	String first_name;
	String last_name;
	@GeneratedValue
	@Id
	int id;
	@OneToMany(mappedBy = "pilot", cascade = CascadeType.PERSIST, orphanRemoval = true)
	Collection<Flight> flight;
}

/**
 * Domain Model pre tabulku destinations
 *
 */
@Entity @Table(name = "destinations")
class Destination {
	@GeneratedValue
	@Id
	int id;
	@Column(precision = 10, scale = 8)
	BigDecimal longitude;
	@Column(precision = 10, scale = 8)
	BigDecimal latitude;
	String name;
	
	/**
	 * Najde destianaciu s danym meno.
	 * @param name
	 * @return List<Destination>
	 */
	public static List<Destination> getDestinationByName(String name) {
		EntityManager em = DBFactory.getManager();
		TypedQuery<Destination> query = em.createQuery("SELECT d FROM Destination d WHERE d.name = :name", Destination.class);
		query.setParameter("name", name);
		return query.getResultList();
	}
}

/**
 * Domain model pre tabulku planes
 *
 */
@Entity @Table(name = "planes")
class Plane {
	@GeneratedValue
	@Id
	int id;
	int seat_num;
	String code_name;
	int age;
	int distance_flown;
	Date last_check_date;
	int condition;
	
	@OneToMany(mappedBy = "plane", cascade = CascadeType.PERSIST, orphanRemoval = true)
	Collection<Flight> flight;
	
	@Transient
	static final double _eQuatorialEarthRadius = 6378.1370D;
	
	/**
	 * aktualizuje nalietane kilometre lietadla prisluchajuceho k letu flight_id
	 * @param flight_id
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public void addFlightDistance(int flight_id) throws SQLException, DBExceptions {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		
		Flight f = em.find(Flight.class, flight_id);
		if (f == null) {
			throw new DBExceptions("Invalid Flight ID.");
		}
		Destination from = f.from;
		Destination to = f.to;
		Plane plane = f.plane;
		Plane p = em.find(Plane.class, plane.id);
		
		if (p == null) {
			throw new DBExceptions("Invalid Plane ID.");
		}
		
		if (from == null || to == null) {
			throw new DBExceptions("Invalid Destination ID.");
		}
		double dlat = (to.latitude.doubleValue() - from.latitude.doubleValue()) * (Math.PI / 180D);
        double dlong = (to.longitude.doubleValue() - from.longitude.doubleValue()) * (Math.PI / 180D);
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(from.latitude.doubleValue() * (Math.PI / 180D)) * Math.cos(to.latitude.doubleValue() * (Math.PI / 180D)) * Math.pow(Math.sin(dlong / 2D), 2D);
        double x = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        int distance = (int) (_eQuatorialEarthRadius * x);
        
        p.condition = changeCondition(p.distance_flown, p.distance_flown + distance, p.condition);
        p.distance_flown += distance;
        em.persist(p);
        
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Skontroluje stav lietadla, v pripade nevyhovujuceho stavu zrusi prisluchajuce lety.
	 * @param id
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public void checkPlane(int id) throws SQLException, DBExceptions { // zmenit lietadlo namiesto rusenia letov???
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		
		Plane p = em.find(Plane.class, id);
		if (p == null) {
			throw new DBExceptions("Plane not found.");
		}
		
		if (p.condition > 10) {
			em.remove(p); // delete aj lety a tickety

			List<Flight> flights = Flight.getFlightByPlaneId(id);
			for (Flight f : flights) {
				Flight.deleteFlight(f.id);
			}
		}
		else {
			p.condition = 0;
			p.last_check_date = Date.valueOf(LocalDate.now());
			em.persist(p);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * zmeni stav lietadla podla nalietanych kilometrov
	 * @param currDist
	 * @param newDist
	 * @param condition
	 * @return
	 */
	public int changeCondition(int currDist, int newDist, int condition) {
		if (newDist % 10000 < currDist % 10000) {
			condition++;
		}
		return condition;
	}
	
	
	/**
	 * Vrati vsetky lietadla.
	 * @return List<Plane>
	 * @throws SQLException
	 */
	public static List<Plane> getAll() throws SQLException {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		TypedQuery<Plane> query = em.createQuery("SELECT p FROM Plane p", Plane.class);
		return query.getResultList();
	}
}

/**
 * Domain model pre tabulku flights
 *
 */
@Entity @Table(name = "flights")
class Flight {
	@GeneratedValue
	@Id
	int id;
	Timestamp take_off_time;
	Timestamp landing_time;
	Date date;
	
	@OneToMany(mappedBy = "flight", cascade = CascadeType.PERSIST, orphanRemoval = true)
	Collection<Ticket> ticket;
	@ManyToOne//(cascade = CascadeType.PERSIST)
	Pilot pilot;
	@ManyToOne//(cascade = CascadeType.PERSIST)
	Plane plane;
	@ManyToOne//(cascade = CascadeType.PERSIST)
	Destination to;
	@ManyToOne//(cascade = CascadeType.PERSIST)
	Destination from;
	
	/**
	 * Prida let do tabulky flights.
	 * @param date
	 * @param fromDest
	 * @param toDest
	 * @param planeId
	 * @param pilotId
	 * @param takeOff
	 * @param landing
	 * @throws DBExceptions
	 */
	void addFlight(Date date, String fromDest, String toDest, int planeId, int pilotId, Timestamp takeOff, Timestamp landing) throws DBExceptions {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		List<Destination> from = Destination.getDestinationByName(fromDest);
		List<Destination> to = Destination.getDestinationByName(toDest);
		if (!date.after(Date.valueOf(LocalDate.now()))) {
			throw new DBExceptions("Invalid date.");
		}
		if(from.isEmpty() || to.isEmpty()) {
			throw new DBExceptions("Missing Destination.");
		}
		List<Flight> f = Flight.getFlightByPlaneAndDate(date, planeId);
		if (!f.isEmpty()) {
			throw new DBExceptions("Plane not available.");
		}
		f = Flight.getFlightByPilotAndDate(date, pilotId);
		if (!f.isEmpty()) {
			throw new DBExceptions("Pilot not available.");
		}
		if (landing != null && !takeOff.before(landing)) {
			throw new DBExceptions("Invalid times.");
		}
		
		Flight input = new Flight();
		input.date = date;
		input.from = from.get(0);
		input.to = to.get(0);
		input.pilot = em.find(Pilot.class, pilotId);
		input.plane = em.find(Plane.class, planeId);
		input.take_off_time = takeOff;
		input.landing_time = landing;
		em.persist(input);
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Vrati let s danym plane_id a datumom date
	 * @param date
	 * @param plane_id
	 * @return List<Flight>
	 */
	public static List<Flight> getFlightByPlaneAndDate(Date date, int plane_id) {
		EntityManager em = DBFactory.getManager();
		TypedQuery<Flight> query = em.createQuery("SELECT f FROM Flight f WHERE f.plane.id = :planeId AND f.date = :date", Flight.class);
		query.setParameter("planeId", plane_id);
		query.setParameter("date", date);
		return query.getResultList();
	}
	
	/**
	 * Vrati let s danym politom pilot_id a datumom date
	 * @param date
	 * @param pilot_id
	 * @return List<Flight>
	 */
	public static List<Flight> getFlightByPilotAndDate(Date date, int pilot_id) {
		EntityManager em = DBFactory.getManager();
		TypedQuery<Flight> query = em.createQuery("SELECT f FROM Flight f WHERE f.pilot.id = :pilotId AND f.date = :date", Flight.class);
		query.setParameter("pilotId", pilot_id);
		query.setParameter("date", date);
		return query.getResultList();
	}
	
	/**
	 * Zmaze let z tabulky flights, kde flight_id == this.flight_id
	 * @param flight_id
	 */
	public static void deleteFlight(int flight_id) {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		List<Ticket> tickets = Ticket.findByFlightId(flight_id);
		for (Ticket t : tickets) {
			Ticket ticket = em.merge(t);
			em.remove(ticket);
		}
		Flight f = em.find(Flight.class, flight_id);
		em.remove(f);
		em.getTransaction().commit();
	}
	
	/**
	 * Vrati vsetky lety, take ze param == flights.plane_id
	 * @param plane_id
	 * @return List<Flight>
	 */
	public static List<Flight> getFlightByPlaneId(int plane_id) {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		TypedQuery<Flight> query = em.createQuery("SELECT f FROM Flight f WHERE f.plane.id = :plane_id", Flight.class);
		query.setParameter("plane_id", plane_id);
		return query.getResultList();
	}
	
	/**
	 * * Vrati vsetky lety
	 * @return List<Flight>
	 */
	public static List<Flight> getAll() {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		TypedQuery<Flight> query = em.createQuery("SELECT f FROM Flight f", Flight.class);
		return query.getResultList();
	}
	
	
	/**
	 * Zmeni letadlo pre dany let.
	 * @param flight_id
	 * @throws SQLException
	 * @throws DBExceptions
	 */
	public static void changePlane(int flight_id) throws SQLException, DBExceptions {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		
		List<Plane> planes = Plane.getAll();
		Flight f = em.find(Flight.class, flight_id);
		List<Plane> planes_on_date = Flight.getPlaneByDate(f.date);
		ArrayList<Integer> plane_ids_on_date = new ArrayList<>();
		for (int i = 0; i < planes_on_date.size(); i++) {
			plane_ids_on_date.add(planes_on_date.get(i).id);
		}
		Plane res = planes.get(0);
		boolean flag = false;
		for (int i = 1; i < planes.size(); i++) {
			if (plane_ids_on_date.contains(res.id)) {
				res = planes.get(i);
			}
			else {
				flag = true;
				break;
			}
		}
		if (!flag) {
			throw new DBExceptions("No plane available.");
		}
		f.plane = em.find(Plane.class, res.id);
		em.persist(f);
		
		em.getTransaction().commit();
	}
	
	/**
	 * Vrati vsetky lietadla letiace v dany den date
	 * @param date
	 * @return List<Plane>
	 * @throws SQLException
	 */
	static List<Plane> getPlaneByDate(Date date) throws SQLException {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		TypedQuery<Plane> query = em.createQuery("SELECT f.plane FROM Flight f WHERE :date = f.date", Plane.class);
		query.setParameter("date", date);
		return query.getResultList();
	}
}

/**
 * Statistiky
 *
 */
class Stats {
	
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
	 */
	public static LinkedHashMap<String, Integer> mostFrequentDest() {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		List<Flight> flights = Flight.getAll();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		java.util.Date lastMonth = cal.getTime();
		TreeMap<Integer, Integer> destCount = new TreeMap<Integer, Integer>();
		for (Flight f : flights) {
			if (f.date.after(lastMonth)) { // date
				int id = f.to.id; // to_id
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
			result.put(em.find(Destination.class, e.getKey()).name, e.getValue());
		}
		em.close();
		return result;
	}
	
	/**
	 * Vrati pocet letov za mesiac utriedene zostupne
	 * @return LinkedHashMap<String, Integer>
	 * @throws SQLException
	 */
	public static LinkedHashMap<String, Integer> flightDemandPerMonth() throws SQLException {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		List<Flight> flights = Flight.getAll();
		TreeMap<String, Integer> months = new TreeMap<String, Integer>();
		
		for (Flight f : flights) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(f.date.getTime());
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
		em.close();
		return result;
	}
	
	/**
	 * Vrati najziskovejsie destinacie(prvych 10) utriedenych zostupne
	 * @param month
	 * @return LinkedHashMap<String, BigDecimal>
	 * @throws SQLException
	 */
	public static LinkedHashMap<String, BigDecimal> mostProfitableDest(String month) throws SQLException {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		Vector<Object[]> data = (Vector<Object[]>) getPricesDuringMonth(month);
		TreeMap<Integer, BigDecimal> destinations = new TreeMap<Integer, BigDecimal>();
		for (Object[] o : data) {
			Date date = (Date) o[0];
			int to_id = (int) o[1];
			BigDecimal price = (BigDecimal) o[2];
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
			result.put(em.find(Destination.class, e.getKey()).name, e.getValue());
		}
		return result;
	}
	
	/**
	 * Vrati ceny vsetkych listov za mesiac "param"
	 * @param month - format = <"01" - "12">
	 * @return ResultSet
	 * @throws SQLException
	 */
	static List getPricesDuringMonth(String month) throws SQLException {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		Query query = em.createQuery
				("SELECT f.date, f.to.id, t.price FROM Flight f JOIN Ticket t ON f.id = t.flight.id WHERE FUNCTION('date_part', 'month', f.date) = CAST(:month AS integer)");
		query.setParameter("month", month);
		return query.getResultList();
	}
}




