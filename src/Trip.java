import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class Trip {

	public final Long tripId;
	public final Long serviceId;
	public final Hashtable<Integer, Station> trip; // Time, Station

	public Trip(Long tripId, Long serviceId, Hashtable<Integer, Station> trip) {
		this.tripId = tripId;
		this.serviceId = serviceId;
		this.trip = trip;
		trip.forEach((k, v) -> v.addTrip(this));
	}

	public int getNextStationTime(int currTime) {
		currTime++; // So it doesnt return itself, also there cant be two departures in a trip, at the same time.
		while (currTime <= 2000) {
			if (trip.containsKey(currTime))
				return currTime;
			currTime++;
		}
		return -1;
	}

	public Station getNextStation(Station currStation) {
		for (Iterator<Entry<Integer, Station>> it = trip.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, Station> ent = it.next();
			if (ent.getValue().equals(currStation))
				return trip.get(getNextStationTime(ent.getKey()));
		}
		return null;
	}
	
	public int getDepartureTime(Station station) {
		for(Iterator<Entry<Integer, Station>> it = trip.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, Station> ent = it.next();
			if(station.equals(ent.getValue()))
				return ent.getKey();
		}
		return Integer.MAX_VALUE;
	}

}
