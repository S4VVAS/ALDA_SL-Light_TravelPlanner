
public class Main {

	public static void main(String[] args) {
		SLFileParser sp = new SLFileParser();
		
		sp.parsedStations.forEach((k,v) -> {//StationId, station
			System.out.println("AT: " + v.name);
			v.getTrips().forEach((k2,v2) -> {
				System.out.println(k2 + " to station " + (v2.getNextStation(v) != null ? v2.getNextStation(v).name : "none"));
			});
			
			System.out.println("\n\n\n");
		});
		
		System.out.println("AMT OF TRIPS: " + sp.parsedTrips.size());
	}
	
}
