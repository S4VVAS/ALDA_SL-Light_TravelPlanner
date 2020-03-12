/**@author Savvas Giortsis (sagi2536)*/
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class Graph {

	/**Stores stopId as key and {@link Station} as value.
	 * @contains all the stations that have been parsed*/
	private Hashtable<Long, Station> stations;
	private final boolean aStar;
	private Station goal;

	public Graph(boolean aStar) {
		this.aStar = aStar;
		SLFileParser slP = new SLFileParser();
		stations = slP.parsedStations;
	}
	
	/**
	 * Generates a route in the form of a {@link String}, from station <code>start</code> to <code>goal</code>.
	 *
	 * @throws IllegalArgumentException if no {@link Station} objects with same
	 * station name based on <code>start</code> or <code>goal</code> exist in 
	 * {@link #stations}.
 	 * @throws IllegalArgumentException if <code>start</code> or <code>goal</code> are null. 
 	 * @throws IllegalArgumentException if <code>currentTime</code> is bellow 0.
	 * can't be bellow 0.
	 * @param start is the starting station from where the path is calculated (case sensitive).
	 * @param goal is the destination station to where the path is calculated (case sensitive).
	 * @param currentTime is the time of departure from <code>start</code>. Minutes in a day, 00:00 is 0 or 1440, 16:40 is 1000 etc.
	 * @return Returns a {@link String} with the path that has been generated from {@link #backtrack(PathVisit) backtrack}
	 * using {@link #makeMinSpanTree(Station, int) makeMinSpanTree}.
	 * @since V1.0
	 * @author Savvas Giortsis (sagi2536)
	 * */
	public String findRoute(String start, String goal, int currentTime) {
		if (getStation(start) == null || getStation(goal) == null || currentTime < 0)
			throw new IllegalArgumentException();
		this.goal = getStation(goal);
		PathVisit lastNode = makeMinSpanTree(getStation(start), currentTime);
		if(lastNode == null)
			return "No path could be found at this time";
		return backtrack(lastNode);
	}

	private Station getStation(String name) {
		for (Iterator<Entry<Long, Station>> it = stations.entrySet().iterator(); it.hasNext();) {
			Entry<Long, Station> ent = it.next();
			if (ent.getValue().name.equalsIgnoreCase(name))
				return ent.getValue();
		}
		return null;
	}
	
	/**
	 * Creates a minimum spanning tree, staring from <code>start</code>. This method creates different spanning
	 * trees depending on {@link #aStar}. The aStar boolean dictates when the creation of the spanning tree is to 
	 * be stopped. If <code>true</code> the creation of the tree will stop when the <code>goal</code> {@link Station}
	 * is reached, stored in {@link Graph}. if {@link #aStar} is <code>false</code> the creation may continue 
	 * until all the {@link Station}s that can be visited, are visited. That occurs when the queue, 
	 * <code>queue</code> is empty.
	 * @return A PathVisit object which contains the goal station. That path contains links to other paths
	 * so that backtracking can be done all the way to the origin, the starting node.
	 * @param start is the starting station, from where the path is calculated.
	 * @param currentTime is the time of departure from the starting station.
	 * @since V1.0
	 * @author Savvas Giortsis (sagi2536)
	 * */
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
		if(aStar && queue.size() > 0) //If aStar it will skip adding the goal node to visited in loop due to condition
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
			if(!prev.trip.equals(current.trip)) 
				output.insert(0,"\nChange lines at " + current.station.name + " from line " + prev.trip.serviceId + " to line " + current.trip.serviceId + "\n" +
						 "Line " + current.trip.serviceId + " departs at " + convertTimeToTime(current.time) + " from " + current.station.name + "\n\n");
			else 
				output.insert(0, "\t Travel past " + current.station.name + " at " + convertTimeToTime(current.time) + "\n");
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
		public final int time; //Time at currentNode
		public final double distanceLeft;

		public PathVisit(Station currentNode, PathVisit pathVia, Trip trip, int time) {
			station = currentNode;
			this.pathVia = pathVia;
			this.trip = trip;
			this.time = time;
			weight = (pathVia == null ? 0 : Math.abs(time - pathVia.time));
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
