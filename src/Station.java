import java.util.Hashtable;

public class Station {
	
	public final Long stopId; 
	public final double cordX, cordY; 
	public final String name;
	private Hashtable<Long, Trip> trips = new Hashtable<Long, Trip>(); //Tripid, trip
	
	
	public Station(String name, Long stopId, double d, double e) {
		this.stopId = stopId;
		this.name = name;
		this.cordX = d;
		this.cordY = e;
	}
	
	public void addTrip(Trip trip) {
		trips.put(trip.tripId ,trip);
	}
	
	public Trip getTrip(long id) {
		if(trips.containsKey(id))
			return trips.get(id);
		return null;
	}
	
	public boolean hasLine(long id) {
		return trips.containsKey(id);
	}
	
	public Hashtable<Long, Trip> getTrips(){
		return trips;
	}
	

}
