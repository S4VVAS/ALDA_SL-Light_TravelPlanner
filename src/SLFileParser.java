/**@author Savvas Giortsis (sagi2536)*/
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;

public class SLFileParser {
	// <tripId, servideId>
	private Hashtable<Long, Long> trips = new Hashtable<Long, Long>();
	// <tripId, <stopid, depTime>>
	private Hashtable<Long, Hashtable<Long, Integer>> stopTimes = new Hashtable<Long, Hashtable<Long, Integer>>(); 
	 // <stopid,<name, int[x,y]>>
	private Hashtable<Long, Hashtable<String, double[]>> stations = new Hashtable<Long, Hashtable<String, double[]>>();

	public Hashtable<Long, Station> parsedStations = new Hashtable<Long, Station>(); //StationId, station
	public Hashtable<Long, Trip> parsedTrips = new Hashtable<Long, Trip>(); //TripId, trip

	public SLFileParser() {
		try {
			parseTrips();
			parseTimes();
			parseStations();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		makeStations();
		makeTrips();
	}

	private void parseTrips() throws FileNotFoundException {
		Scanner file = new Scanner(new File("sl_trips.txt"));
		file.nextLine();
		
		while(file.hasNext()) {
			String[] line = file.nextLine().split(",");
			trips.put(Long.parseLong(line[2]), Long.parseLong((String) line[0].subSequence(6, 7)));
		}
		file.close();
	}

	private void parseTimes() throws FileNotFoundException {
		Scanner file = new Scanner(new File("sl_stop_times.txt"));
		file.nextLine();
		
		while(file.hasNext()) {
			String[] line = file.nextLine().split(",");
			Long tid = Long.parseLong(line[0]);
			if(!stopTimes.containsKey(tid)) 
				stopTimes.put(tid, new Hashtable<Long, Integer>());
			stopTimes.get(tid).put(Long.parseLong(line[3]), decTime(line[2]));
		}
		file.close();
	}

	private Integer decTime(String time) {
		String[] newTime = time.split(":");
		int h = (Integer.parseInt(newTime[0])) * 60;
		int m = Integer.parseInt(newTime[1]);
		return (h + m);
	}

	private void parseStations() throws FileNotFoundException {
		Scanner file = new Scanner(new File("stations.txt"));
		file.nextLine();
		
		while(file.hasNext()) {
			String[] line = file.nextLine().split(",");
			if(!stations.containsKey(Long.parseLong(line[0])))
				stations.put(Long.parseLong(line[0]), new Hashtable<String, double[]>());
			stations.get(Long.parseLong(line[0])).put(line[1], new double[] {Double.parseDouble(line[2]), Double.parseDouble(line[3])});
		}
		file.close();
	}

	private void makeStations() {
		stations.forEach((k, v) -> {
			Entry<String, double[]> ent = v.entrySet().iterator().next();
			parsedStations.put(k, new Station(ent.getKey(), k, ent.getValue()[0], ent.getValue()[1]));
		});
	}

	private Hashtable<Integer, Station> makeTripTable(long tripId){ //Deptime, station
		Hashtable<Long, Integer> tripTimes = stopTimes.get(tripId); //stopid, depTime
		Hashtable<Integer, Station> stationTimes = new Hashtable<Integer, Station>(); //Station, depTime
		tripTimes.forEach((k,v) -> {
			stationTimes.put(v, parsedStations.get(k));
		});
		return stationTimes;
	}

	private void makeTrips() {
		trips.forEach((k,v) -> {
			Trip t = new Trip(k, v, makeTripTable(k));
			parsedTrips.put(k, t);
		});
	}
	
}
