/**@author Savvas Giortsis (sagi2536)*/
public class Main {

	public static void main(String[] args) {
		Graph graph = new Graph(true);
		
		long startTime = System.currentTimeMillis();
		System.out.println(graph.findRoute("Skärholmen T-bana", "Blåsut T-bana", 1000));
		System.out.println("Time taken to find path: " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
}
