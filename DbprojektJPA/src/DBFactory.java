import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


/**
 * Trieda pre pripojenie do DB
 *
 */
public class DBFactory {

	public static void main(String[] args) {
		DBFactory df = new DBFactory();

		Generate g = new Generate();
		g.passengerJPA(100);
		g.loyaltyCardsJPA();
		g.destinationsJPA(100);
		g.planesJPA(50);
		g.pilotJPA(50);
		g.flightsJPA(150);
		g.ticketJPA(300);

	}

	
	public static EntityManagerFactory factory;
	
	static {
		factory = Persistence.createEntityManagerFactory("my-pu");
	}
	
	public static EntityManager getManager() {
		return factory.createEntityManager();
	}
}

/**
 * Trieda pre vygenerovanie testovacich dat.
 *
 */
class Generate {
	
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static SecureRandom rnd = new SecureRandom();
	
	/**
	 * Vytvori nahodni retazec pre mena a pod. v DB.
	 * @param len
	 * @return
	 */
	String randomString( int len ){
	   StringBuilder sb = new StringBuilder( len );
	   for( int i = 0; i < len; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}
	
	/**
	 * vygeneruje num cestujucich
	 * @param num
	 */
	public void passengerJPA(int num) {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		for (int i = 0; i < num; i++) {
			Passenger p = new Passenger();
			p.email = randomString(10) + "@gmail.com";
			p.first_name = randomString(10);
			p.last_name = randomString(10);
			em.persist(p);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * vygenruje num pilotov
	 * @param num
	 */
	public void pilotJPA(int num) {
		EntityManager em = DBFactory.getManager();
		em.getTransaction().begin();
		for (int i = 0; i < num; i++) {
			Pilot p = new Pilot();
			p.first_name = randomString(10);
			p.last_name = randomString(10);
			em.persist(p);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * vytvori nahodny Timestamp
	 * @return Timestamp
	 */
	public static Timestamp getRandomTimeStamp() {
		long offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
		long end = Timestamp.valueOf("2016-12-06 00:00:00").getTime();
		long diff = end - offset + 1;
		Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
		return rand;
	}
	
	/**
	 * vygeneruje num lietadiel
	 * @param num
	 */
	public void planesJPA(int num) {
		EntityManager em = DBFactory.getManager();
		Random rnd = new Random();
		em.getTransaction().begin();
		LocalDate ld = LocalDate.now();
		ld.minusYears(2);
		for (int i = 0; i < num; i++) {
			Plane p = new Plane();
			p.age = rnd.nextInt(8) + 1;
			p.code_name = randomString(10);
			p.condition = rnd.nextInt(15);
			p.distance_flown = rnd.nextInt(15000) + 1000;
			p.seat_num = rnd.nextInt(300) + 50;
			p.last_check_date = java.sql.Date.valueOf(ld.minusDays(rnd.nextInt(600)));
			em.persist(p);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Vygeneruje num destinacii
	 * @param num
	 */
	public void destinationsJPA(int num) {
		EntityManager em = DBFactory.getManager();
		Random rnd = new Random();
		em.getTransaction().begin();

		for (int i = 0; i < num; i++) {
			Destination dest = new Destination();
			dest.name = randomString(10);
			dest.latitude = new BigDecimal(90 * rnd.nextDouble());
			dest.longitude = new BigDecimal(90 * rnd.nextDouble());
			em.persist(dest);
		}
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * vygeneruje Passenger.size()/5 vernostnych kariet
	 */
	public void loyaltyCardsJPA() {
		EntityManager em = DBFactory.getManager();
		Random rnd = new Random();
		em.getTransaction().begin();
		List p = em.createQuery("SELECT p FROM Passenger p").getResultList();
		for (int i = 0; i < p.size(); i++) {
			if (i%5 == 0) {
				LoyaltyCards lc = new LoyaltyCards();
				lc.passenger = (Passenger) p.get(i);
				lc.discount = new BigDecimal(rnd.nextDouble());
				em.persist(lc);
			}
		}
		em.getTransaction().commit();
	}
	
	/**
	 * vygeneruje num letov
	 * @param num
	 */
	public void flightsJPA(int num) {
		EntityManager em = DBFactory.getManager();
		Random rnd = new Random();
		em.getTransaction().begin();
		List destination = em.createQuery("SELECT p FROM Destination p").getResultList();
		List plane = em.createQuery("SELECT p FROM Plane p").getResultList();
		List pilot = em.createQuery("SELECT p FROM Pilot p").getResultList();
		LocalDate ld = LocalDate.now();
		LocalDateTime cal = LocalDateTime.now();
		for (int i = 0; i < num; i++) {
			Flight f = new Flight();
			f.pilot = (Pilot) pilot.get(rnd.nextInt(pilot.size()));
			f.plane = (Plane) plane.get(rnd.nextInt(plane.size()));
			f.from = (Destination) destination.get(rnd.nextInt(destination.size()));
			f.to = (Destination) destination.get(rnd.nextInt(destination.size()));
			while (f.from == f.to) {
				f.to = (Destination) destination.get(rnd.nextInt(destination.size()));
			}
			f.date = java.sql.Date.valueOf(cal.toLocalDate());
			f.take_off_time = Timestamp.valueOf(cal);
			cal = cal.plusHours(rnd.nextInt(11) + 1);
			f.landing_time = Timestamp.valueOf(cal);
			cal = cal.plusDays(rnd.nextInt(2) + 1);
			em.persist(f);
		}
		em.getTransaction().commit();
	}
	
	/**
	 * vygeneruje num listkov
	 * @param num
	 */
	public void ticketJPA(int num) {
		EntityManager em = DBFactory.getManager();
		Random rnd = new Random();
		em.getTransaction().begin();
		List flight = em.createQuery("SELECT f FROM Flight f").getResultList();
		String[] classes = {"economy", "business", "first"};
		
		for (int i = 0; i < num; i++) {
			Ticket ticket = new Ticket();
			ticket.price = new BigDecimal(rnd.nextInt(250) + 50);
			ticket.travel_class = classes[rnd.nextInt(3)];
			ticket.flight = (Flight) flight.get(rnd.nextInt(flight.size()));
			em.persist(ticket);
		}
		em.getTransaction().commit();
	}
}