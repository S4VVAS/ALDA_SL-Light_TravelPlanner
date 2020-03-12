import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
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
		queue.add(new PathVisit(start, null, null, currentTime)); //Adds the start node to queue

		for (PathVisit currNode; queue.size() != 0 && (aStar ? !queue.peek().station.equals(goal) : true);) { //Loop while queue is not empty
			currNode = queue.poll(); //Poll from queue
			LinkedList<Trip> availTrips = currNode.station.getLowestWightTrips(currNode.time, currNode.trip); // Fetch all earliest trips
			
			for (Iterator<Trip> it = availTrips.iterator(); it.hasNext();) { //Loop through trips
				Trip currTrip = it.next(); //Gets next trip
				Station nextStation = currTrip.getNextStation(currNode.station); //Gets next station for trip from current station
				int nextStationTime = currTrip.getDepartureTime(nextStation); //Gets the time at next station
				PathVisit newTrip = new PathVisit(nextStation, currNode, currTrip , nextStationTime); //Creates path visit
					
				if (!visited.containsKey(nextStation)) //If station not visited, add to queue
					queue.add(newTrip);
				else if (visited.get(nextStation).weight > newTrip.weight) //Else it is visited, if previous weight is bigger, relax
					visited.replace(nextStation, newTrip);
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
		int arrTime = last.time;
		
		output.append("\nYou arrive at your destination, " + last.station.name + " at " + convertTimeToTime(last.time) + "\n");
		
		for(; current.pathVia != null; current = current.pathVia) {
			if(!prev.trip.equals(current.trip)) {
				output.insert(0,"\nChange lines at " + current.station.name + " from line " + prev.trip.serviceId + " to line " + current.trip.serviceId + "\n" +
						 "Line " + current.trip.serviceId + " departs at " + convertTimeToTime(current.time) + " from " + current.station.name + "\n\n");
			}
			else {
				output.insert(0, "\t Travel past " + current.station.name + " at " + convertTimeToTime(current.time) + "\n");
			}
			prev = current;
		}
		output.insert(0,"Your journey begins at " + current.station.name + " at " + convertTimeToTime(current.time) + "\n\n");

		output.append("\nYour total trip time is: " + (arrTime - current.time) + " minutes\n");
		return output.toString();
	}
	
	private String convertTimeToTime(int time) {
		return (time - time%60) / 60 + ":" + (time%60 < 10 ? "0" + time%60 : time%60);
	}
	
	private double dirTimeToGoal(Station curr) {
		double notAccurateButSomewhatOkDistanceInMeters = Math.sqrt(Math.pow((curr.cordX - goal.cordX), 2) + Math.pow((curr.cordY - goal.cordY), 2)) * 100000;
		double estimatedTravelTime =  (notAccurateButSomewhatOkDistanceInMeters/ 30); //30 m/s (108-ish km/h)
		return estimatedTravelTime / 60; //divide to get time in minutes
	}

	class PathVisit implements Comparable<PathVisit> {
		public final Station station;
		public final PathVisit pathVia;
		public final Trip trip;
		public final double weight; // Based on time
		public final int time; //Time at station
		public final double distanceLeft;

		public PathVisit(Station currentNode, PathVisit pathVia, Trip trip, int time) {
			// CALC TIME
			station = currentNode;
			this.pathVia = pathVia;
			this.trip = trip;
			this.time = time;
			weight = (pathVia == null ? 0 : Math.abs(time - pathVia.time)); // If trip changed
			this.distanceLeft = aStar ? dirTimeToGoal(currentNode) : 0;

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
