import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class StationHandler {

	private final Hashtable<Long, Station> stations;
	
	public StationHandler(Hashtable<Long, Station> stations) {
		this.stations = stations;
	}
	
	public Station getStation(long stationId) {
		if(stations.containsKey(stationId))
			return stations.get(stationId);
		return null;
	}
	
	public Iterator<Entry<Long, Station>> stationIterator() {
		return stations.entrySet().iterator();
	}
	
}
