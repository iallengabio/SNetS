package grmlsa.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import grmlsa.Route;
import grmlsa.YenKShortestPath;
import network.Link;
import network.Mesh;
import network.Node;
import network.SurvivalCircuit;
import request.RequestForConnection;

public class FixedDoubleRouteBacktracking {

	private static final String DIV = "-";
	private HashMap<String, List<Route>> routesForAllPairs;
	
	public List<Route> findRoute(RequestForConnection rfc, Mesh mesh){
		if(routesForAllPairs == null){
			computeAllRoutes(mesh);
			//salveRoutesByPar();
		}
		
		Node source = rfc.getPair().getSource();
		Node destination = rfc.getPair().getDestination();
		
		List<Route> listRoutes = routesForAllPairs.get(source.getName() + DIV + destination.getName());
		
		return listRoutes;
	}
	
	public boolean findRoute(SurvivalCircuit circuit, Mesh mesh){
		if(routesForAllPairs == null){
			computeAllRoutes(mesh);
			//salveRoutesByPar();
		}
		
		Node source = circuit.getSource();
		Node destination = circuit.getDestination();
		
		List<Route> listRoutes = routesForAllPairs.get(source.getName() + DIV + destination.getName());
		
		Route workRoute = listRoutes.get(0);
		Route backupRoute = listRoutes.get(1);
		
		List<Route> backupRoutes = new ArrayList<>();
		circuit.setRoute(workRoute);
		circuit.setBackupRoutes(backupRoutes);
		
		if(workRoute != null && backupRoute != null){
			backupRoutes.add(backupRoute);
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Computa duas rotas disjuntas para todos os pares
	 * @param mesh
	 */
	public void computeAllRoutes(Mesh mesh){
		routesForAllPairs = new HashMap<String, List<Route>>();
		
		for (Node n1 : mesh.getNodeList()) {
			for (Node n2 : mesh.getNodeList()) {
				if (n1 == n2)
					continue;
				List<Route> listRoutes = computeBacktracking(n1, n2, mesh);
				routesForAllPairs.put(n1.getName() + DIV + n2.getName(), listRoutes);
			}
		}
	}
	
	/**
	 * Utiliza a tecnica backtracking para computar as rotas disjuntas
	 * @param s
	 * @param d
	 * @param mesh
	 * @return
	 */
	private static List<Route> computeBacktracking(Node s, Node d, Mesh mesh) {
		Route routePrimariaAux = shortestPathsCheckingFails(s, d, mesh);
		Route routeBackupAux = disjointShortestPath(routePrimariaAux, mesh);
		
		if (routeBackupAux == null) {
			Vector<Route> rotasPrimarias = new Vector<Route>();
			Vector<Link> linkListWork = routePrimariaAux.getLinkList();
			for (int i = 0; i < linkListWork.size(); i++) {
				rotasPrimarias.add(disjointShortestPathOfLink(routePrimariaAux.getSource(), routePrimariaAux.getDestination(), linkListWork.get(i), mesh));
			}
			
			Route newRouteA = null;
			Route newRouteB = null;
			while ((newRouteB == null) && (rotasPrimarias.size() > 0)) {
				newRouteA = lessCostRoute(rotasPrimarias);
				if (newRouteA != null) {
					newRouteB = disjointShortestPath(newRouteA, mesh);
				}
				if (newRouteB != null) {
					routePrimariaAux = new Route(newRouteA.getNodeList());
				} else {
					rotasPrimarias.remove(newRouteA);
				}
			}
			
			if (newRouteB == null) {
				System.out.println("erro no backtracking");
			} else {
				routeBackupAux = new Route(newRouteB.getNodeList());
			}
		}
		
		List<Route> rotas = new ArrayList<Route>(2);
		rotas.add(routePrimariaAux);
		rotas.add(routeBackupAux);
		
		return rotas;
	}
	
	/**
	 * Busca um rota de menor caminho para um dado par origem e destino.
	 * Evitar enlaces marcados como falhos.
	 * @param source
	 * @param destination
	 * @param mesh
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Route shortestPathsCheckingFails(Node source, Node destination, Mesh mesh){
		HashMap<Node, Double> undefined = new HashMap<>(); //distancias atuais dos nos ate a origem
		HashMap<Node, Vector<Node>> routes = new HashMap<>(); //rotas atuais da origem ate cada no
		
		for (Node n : mesh.getNodeList()) { //sinalizando distancia infinita para todos os nos da rede
			undefined.put(n, Double.MAX_VALUE);
		}
		
		undefined.put(source, 0.0); //distancia 0 do no de origem para ele mesmo
		Node nAux1;
		Vector<Node> rAux;
		
		nAux1 = source;		
		rAux = new Vector<Node>();
		rAux.add(nAux1);
		routes.put(nAux1, rAux);
		
		Route route = null;
		
		while(!undefined.isEmpty()){
			nAux1 = minDistAt(undefined);
			
			//abrir um vertice
			for (Node n : mesh.getAdjacents(nAux1)) {
				if(!undefined.containsKey(n))
					continue;
				
				if(routes.get(nAux1) != null){
					rAux = (Vector<Node>) routes.get(nAux1).clone();
					rAux.add(n);
					
					Link enlace = mesh.getLink(nAux1.getName(), n.getName());
					if(!enlace.isFailed()){ //verifica se o enlace nao esta falho
						//verificar se eh necessario atualizar a rota
						if(undefined.get(n) == null || (undefined.get(n) > undefined.get(nAux1) + enlace.getDistance())){
							undefined.put(n, undefined.get(nAux1)+ mesh.getLink(nAux1.getName(), n.getName()).getDistance());
							routes.put(n, rAux);
						}
					}
				}
			}
			undefined.remove(nAux1); //fechando o vertice		
			
			if(nAux1.getName().equals(destination.getName()) && routes.get(nAux1) != null){
				route = new Route(routes.get(nAux1));
				break;
			}
		}
		
		return route;
	}
	
	/**
	 * seleciona o no com a menor distancia atual
	 * @param undefined
	 * @return
	 */
	private static Node minDistAt(HashMap<Node, Double> undefined){
		Iterator<Node> it = undefined.keySet().iterator();
		
		Node res = it.next();
		Node aux;
		while(it.hasNext()){
			aux = it.next();
			if(undefined.get(res) > undefined.get(aux))
				res = aux;
		}
		
		return res;
	}
	
	/**
	 * retorna a menor rota disjunta da rota informada
	 * @param route Route
	 * @return Route
	 */
	public static Route disjointShortestPath(Route route, Mesh mesh) {
		Route routeDisjoint = null;
		
		// setando os links que compoem a rota primaria (ida e volta) como falhos
		if (route != null) {
			for (int i = 0; i < route.getLinkList().size(); i++) {
				// configurando ida
				Link linkIda = route.getLinkList().get(i);
				linkIda.setFailed();
				// configurando vota
				Link linkVolta = linkIda.getDestination().linkTo(linkIda.getSource());
				linkVolta.setFailed();
			}
			
			// invoca shortestPath() com os links da rota primaria alterados
			routeDisjoint = shortestPathsCheckingFails(route.getSource(), route.getDestination(), mesh);
			
			// setando os links da rota primaria (ida e volta) como normais
			for (int i = 0; i < route.getLinkList().size(); i++) {
				// configurando ida
				Link linkIda = route.getLinkList().get(i);
				linkIda.fixLink();
				// configurando volta
				Link linkVolta = linkIda.getDestination().linkTo(linkIda.getSource());
				linkVolta.fixLink();
			}
		}
		
		return routeDisjoint;
	}
	
	/**
	 * retorna k menores rotas disjuntas da rota informada
	 * @param route Route
	 * @return Route
	 */
	public static List<Route> disjointKShortestPath(Route route, Mesh mesh, int k, int typeCost) {
		List<Route> kDisjointRoutes = null;
		
		// setando os links que compoem a rota primaria (ida e volta) como falhos
		if (route != null) {
			
			Vector<Node> nodeList = mesh.getNodeList(); //lista de todos os nodes da rede
			Vector<Link> linkList = new Vector<Link>(); //lista de enlaces sem os enlaces da rota informada
			
			for (int i = 0; i < mesh.getLinkList().size(); i++) {
				Link link = mesh.getLinkList().get(i);
				
				if(!route.containThisLink(link)){ //se a rota informada nao pussir o enlace
					linkList.add(link);
				}
			}
			
			// tenta encontrar k menores caminhos que sao disjuntos da rota informada
			kDisjointRoutes = YenKShortestPath.computeKRoutes(route.getSource(), route.getDestination(), nodeList, linkList, k, typeCost);
		}
		
		return kDisjointRoutes;
	}
	
	/**
	 * retorna o menor caminho para o par P disjunto do Link
	 * @param p Pair
	 * @param disjLink Link
	 * @return Route
	 */
	private static Route disjointShortestPathOfLink(Node source, Node destination, Link disjLink, Mesh mesh) {
		// setando o enlace como falho
		disjLink.setFailed();

		Route routeDisjoint = shortestPathsCheckingFails(source, destination, mesh);

		// setando o enlace como normal
		disjLink.fixLink();

		return routeDisjoint;
	}
	
	/**
	 * Retorna a menor rota do Vector de rotas
	 * @param routes Vector
	 * @return Route
	 */
	protected static Route lessCostRoute(Vector<Route> routes) {
		Route route = routes.firstElement();
		for (int i = 0; i < routes.size(); i++) { //comeca com a primeira rota porque o vetor de rotas pode estar vazio
			if (routes.get(i) == null) { //para de procurar na primeira rota se o vetor de rotas estiver vazio
				break;
			}
			if (routes.get(i).size() < route.size()) {
				route = routes.get(i);
			}
		}
		return route;
	}
	
	public HashMap<String, List<Route>> getRoutesForAllPairs(){
		return routesForAllPairs;
	}
}
