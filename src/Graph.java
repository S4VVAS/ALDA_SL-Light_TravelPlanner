import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class Graph {

	private Hashtable<Long, Station> stations; // StationId, station
	private Hashtable<Long, Trip> trips; // TripId, trip
	private final boolean aStar;
	private Station goal;

	public Graph(boolean aStar) {
		this.aStar = aStar;
		SLFileParser slP = new SLFileParser();
		stations = slP.parsedStations;
		trips = slP.parsedTrips;
	}

	public String findRoute(String start, String goal, int currentTime) {
		if (start.isEmpty() || goal.isEmpty() || getStation(start) == null || getStation(goal) == null)
			throw new IllegalArgumentException();
		this.goal = getStation(goal);
		return backtrack(makeMinSpanTree(getStation(start), currentTime));
	}

	private Station getStation(String name) {
		for (Iterator<Entry<Long, Station>> it = stations.entrySet().iterator(); it.hasNext();) {
			Entry<Long, Station> ent = it.next();
			if (ent.getValue().name.equals(name))
				return ent.getValue();
		}
		return null;
	}

	private PathVisit makeMinSpanTree(Station start, int currentTime) {
		PriorityQueue<PathVisit> queue = new PriorityQueue<PathVisit>();
		Hashtable<Station, PathVisit> visited = new Hashtable<Station, PathVisit>(); //Would use a stack, but faster with no A* using this

		queue.add(new PathVisit(start, null, null, currentTime, aStar ? distanceToGoal(start) : 0)); //Adds the start node to queue

		for (PathVisit currNode; queue.size() != 0 && (aStar ? !queue.peek().station.equals(goal) : true);) { //Loop while queue is not empty
			currNode = queue.poll(); //Poll from queue
			Hashtable<Long, Trip> availTrips = currNode.station.getTrips(); // Fetch all avail trips from curr station.
			
			for (Iterator<Entry<Long, Trip>> it = availTrips.entrySet().iterator(); it.hasNext();) { //Loop through trips
				Entry<Long, Trip> currTrip = it.next();
				Station nextStation = currTrip.getValue().getNextStation(currNode.station); //Gets next station for trip from current station

				if (nextStation != null) { // If not null, this station is not last in the trip (slutstation)
					int changingPenalty = currTrip.getValue().equals(currNode.trip) ? 0 : 5;
					
					int nextDeparture = currTrip.getValue().getDepartureTime(currNode.station);
				//	DEPARTURE TIME RATHER THAN NEXT STATION TIME
					
					int nextStationTime = currTrip.getValue().getNextStationTime(nextDeparture + changingPenalty); //Gets the time of arrival at next station
					double distanceLeft = aStar ? distanceToGoal(nextStation) : 0; //If a* is used, calculates distance to end
					PathVisit newTrip = new PathVisit(nextStation, currNode, currTrip.getValue() , nextStationTime, distanceLeft); //Creates path visit
					if (!visited.containsKey(newTrip.station)) //If station not visited, add to queue
						queue.add(newTrip);
					else if (visited.get(newTrip.station).weight > newTrip.weight) //Else it is visited, if previous weight is bigger, relax
						visited.replace(newTrip.station, newTrip);
					// if newTrip existed in visited, but was of bigger weight, we don't do anything to it.
				}
			}
			visited.put(currNode.station, currNode); //Place current node in visited
		}
		if(aStar) //If aStar it will skip adding the goal node to visited in loop due to condition
			visited.put(queue.peek().station, queue.poll()); //So we do that here
		
		return visited.get(goal);
	}

	private String backtrack(PathVisit last) {
		StringBuilder output = new StringBuilder("");
		PathVisit current = last;
		PathVisit prev = last;
		
		
		for(; current.pathVia != null; current = current.pathVia) {
			output.append(current.station.name + 
					" at time " + current.time +
					" on line " + (prev.trip.equals(current.trip) ? "" : "Line changed") + "\n\n");
			prev = current;
		}
		output.append(current.station.name + " at time " + current.time + "\n\n");
				
		return output.toString();
	}
	
	private double distanceToGoal(Station curr) {
		return Math.sqrt(Math.pow((curr.cordX - goal.cordX), 2) + Math.pow((curr.cordY - goal.cordY), 2)) * 1000 ;
	}

	class PathVisit implements Comparable<PathVisit> {
		public final Station station;
		public final PathVisit pathVia;
		public final Trip trip;
		public final double weight; // Time
		public final int time;
		public final double distanceLeft;

		public PathVisit(Station currentNode, PathVisit pathVia, Trip trip, int time, double distanceLeft) {
			// CALC TIME
			station = currentNode;
			this.pathVia = pathVia;
			this.trip = trip;
			this.time = time;
			weight = (pathVia == null ? 0 : Math.abs(time - pathVia.time)); // If trip changed
			this.distanceLeft = distanceLeft;

		}

		@Override
		public int compareTo(PathVisit other) {
			if ((distanceLeft + weight) == (other.distanceLeft + other.weight))
				return 0;
			else if ((distanceLeft + weight) > (other.distanceLeft + other.weight))
				return 1;
			else
				return -1;
		}

		@Override
		public String toString() {
			return "n:" + station.name + "w:" + (int) (weight + distanceLeft);
		}
	}
}
