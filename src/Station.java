/**@author Savvas Giortsis (sagi2536)*/
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class Station {
	
	public final Long stopId;
	public final double cordX, cordY;
	public final String name;
	private LinkedList<Trip> trips = new LinkedList<Trip>(); // depTime, trip

	public Station(String name, Long stopId, double d, double e) {
		this.stopId = stopId;
		this.name = name;
		this.cordX = d;
		this.cordY = e;
	}

	public void addTrip(Trip trip) {
		trips.add(trip);
	}

	public LinkedList<Trip> getTrips() {
		return trips;
	}

	public LinkedList<Trip> getLowestWightTrips(int time, Trip lastTrip) {
		LinkedList<Trip> erlTrips = new LinkedList<Trip>(); // depTime, trip
		Hashtable<Station, Trip> seenStations = new Hashtable<Station, Trip>();

		for (Iterator<Trip> it = trips.iterator(); it.hasNext();) {
			Trip ent = it.next();
			Station next = ent.getNextStation(this);
			int changingPenalty = ent.equals(lastTrip) ? 0 : 2;
	
			if (ent.getDepartureTime(this) >= (time + changingPenalty) && next != null) {
				if (!seenStations.containsKey(next))
					seenStations.put(next, ent);
				else if (seenStations.get(next).getDepartureTime(this) > ent.getDepartureTime(this))
					seenStations.replace(next, ent);
			}
		}
		seenStations.forEach((k, v) -> erlTrips.add(v));
		return erlTrips;
	}

}
