package grmlsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import network.Link;
import network.Node;

public class YenKShortestPath implements KRoutingAlgorithmInterface {
	
	private static final String DIV = "-";
	private int k; //quantidade de menores caminhos
	private int typeCost; //tipo de custo: 0, se for um custo informado para o link, ou 1, se for a distancia do link
	private HashMap<String, List<Route>> routesForAllPairs; //k menores caminhos para todos os pares de nodes da rede
	
	public YenKShortestPath(Vector<Node> nodeList, Vector<Link> linkList, int k, int typeCost) {
		this.k = k;
		this.typeCost = typeCost;
		this.computeAllRoutes(nodeList, linkList);
		
		//salveRoutesByPar(nodeList);
	}
	
	/**
	 * Metodo para computar todas as rotas para todos os pares de nodes da rede
	 * @param nodeList
	 * @param linkList
	 */
	private void computeAllRoutes(Vector<Node> nodeList, Vector<Link> linkList) {
		routesForAllPairs = new HashMap<String, List<Route>>();
		for (Node n1 : nodeList) {
			for (Node n2 : nodeList) {
				if (n1 == n2)
					continue;

				routesForAllPairs.put(n1.getName() + DIV + n2.getName(), computeKRoutes(n1, n2, nodeList, linkList, k, typeCost));
			}
		}
	}
	
	/**
	 * Metodos que utiliza a estrategia proposta por Yen para computar k menores rotas para um dado par de nodes da rede
	 * @param n1 - Node
	 * @param n2 - Node
	 * @param nodeList - Vector<Node>
	 * @param linkList - Vector<Link>
	 * @return List<Route>
	 */
	public static List<Route> computeKRoutes(Node n1, Node n2, Vector<Node> nodeList, Vector<Link> linkList, int k, int typeCost) {
		Route routeAux = Dijkstra(nodeList, linkList, n1, n2, typeCost);
		
		List<Route> listRoutesA = new ArrayList<Route>(k);
		listRoutesA.add(routeAux);
		
		List<Route> listRoutesB = new ArrayList<Route>();
		
		for(int r = 1; r < k; r++){
			
			Route routeTemp = listRoutesA.get(r - 1);
			int sizeNodesRoute = routeTemp.getNodeList().size();
			
			for(int i = 0; i < sizeNodesRoute - 1; i++){
				
				Node spurNode = routeTemp.getNode(i);
				
				Vector<Node> rootPath = new Vector<Node>();
				for(int n = 0; n <= i; n++){
					rootPath.add(routeTemp.getNode(n));
				}
				
				Vector<Node> listOfNodes = new Vector<Node>();
				for(int n = 0; n < nodeList.size(); n++){
					listOfNodes.add(nodeList.get(n));
				}
				
				Vector<Link> listOfLinks = new Vector<Link>();
				for(int l = 0; l < linkList.size(); l++){
					listOfLinks.add(linkList.get(l));
				}
				
				for(int p = 0; p < listRoutesA.size(); p++){
					Route path = listRoutesA.get(p);
					
					boolean flag = false;
					if(path.getNodeList().size() >= rootPath.size()){
						int cont = 0;
						for(int n = 0; n < rootPath.size(); n++){
							if(rootPath.get(n) == path.getNode(n)){
								cont++;
							}
						}
						if(cont == rootPath.size()){
							flag = true;
						}
					}
					if(flag){
						if(path.getNodeList().size() > i + 1){
							Link link = path.getNode(i).getOxc().linkTo(path.getNode(i + 1).getOxc());
							listOfLinks.remove(link);
						}
					}
				}
				
				for(int n = 0; n < rootPath.size(); n++){
					Node node = rootPath.get(n);
					if(node != spurNode){
						listOfNodes.remove(node);
					}
				}
				
				Route spurPath = Dijkstra(listOfNodes, listOfLinks, spurNode, n2, typeCost);
				if((spurPath != null) && (spurPath.getNodeList().size() != 0)){
					
					Vector<Node> listOfNodeRoute = new Vector<Node>();
					for(int n = 0; n < rootPath.size(); n++){
						listOfNodeRoute.add(rootPath.get(n));
					}
					for(int n = 0; n < spurPath.getNodeList().size(); n++){
						Node node = spurPath.getNodeList().get(n);
						if(!listOfNodeRoute.contains(node)){
							listOfNodeRoute.add(node);
						}
					}
					
					Route totalPath = new Route(listOfNodeRoute);
					listRoutesB.add(totalPath);
				}
			}
			
			if(listRoutesB.isEmpty()){
				break;
			}
			
			ordenarRotasPorDistancia(listRoutesB);
			if(!listRoutesA.contains(listRoutesB.get(0))){
				listRoutesA.add(listRoutesB.get(0));
			}else{
				r--;
			}
			listRoutesB.remove(0);
		}
		
		return listRoutesA;
	}
	
	/**
	 * Metodo que ordena por insercao uma lista de rotas
	 * @param listRoutes
	 */
	private static void ordenarRotasPorDistancia(List<Route> listRoutes){
		for(int i = 1; i < listRoutes.size(); i++){
			Route rota = listRoutes.get(i);
			int j = i - 1;
			
			while((j >= 0) && (rota.getDistanceAllLinks() < listRoutes.get(j).getDistanceAllLinks())){
				listRoutes.set(j + 1, listRoutes.get(j));
				j--;
			}
			
			listRoutes.set(j + 1, rota);
		}
	}
	
	/**
	 * Metodo que utiliza o algoritmo de menor caminho de Dijkstra para computar uma rota para um dado par de nodes da rede
	 * @param nodeList - Vector<Node>
	 * @param linkList - Vector<List>
	 * @param s - Node
	 * @param d - Node
	 * @param typeCost - int - tipo de custo utilizado pelo algoritmo (1 = distancia do enlace)
	 * @return Route
	 */
	public static Route Dijkstra(Vector<Node> nodeList, Vector<Link> linkList, Node s, Node d, int typeCost){
		HashMap<Node, Double> dist = new HashMap<Node, Double>();
		HashMap<Node, Node> previous = new HashMap<Node, Node>();
		
		List<Node> q = new ArrayList<Node>();
		
		for(int n = 0; n < nodeList.size(); n++){
			Node node = nodeList.get(n);
			
			dist.put(node, Double.MAX_VALUE);
			previous.put(node, null);
			
			q.add(node);
		}
		
		dist.put(s, 0.0);
		
		while(!q.isEmpty()){
			Node u = getNodeMinDist(q, dist);
			q.remove(u);
			
			if(dist.get(u) == Double.MAX_VALUE){
				break;
			}
			
			Vector<Link> listOfLinks = u.getOxc().getLinksList();
			for(int l = 0; l < listOfLinks.size(); l++){
				Link link = listOfLinks.get(l);
				
				if(linkList.contains(link)){
					Node v = getNode(nodeList, link.getDestination().getName());
					
					if((v != null) && (q.contains(v))){
						Double linkCost = link.getCost();
						if(typeCost == 1){
							linkCost = link.getDistance();
						}
						Double cust = dist.get(u) + linkCost;
						if(cust < dist.get(v)){
							dist.put(v, cust);
							previous.put(v, u);
						}
					}
				}
			}
		}
		
		Vector<Node> listOfNodes = new Vector<Node>();
		Node aux = d;
		if(previous.get(aux) != null){
			listOfNodes.add(d);
		}
		
		while(previous.get(aux) != null){
			aux = previous.get(aux);
			listOfNodes.add(aux);
			
			if(aux == s){
				break;
			}
		}
		
		Vector<Node> listOfNodesAux = new Vector<Node>();
		for(int i = listOfNodes.size() - 1; i >= 0; i--){
			listOfNodesAux.add(listOfNodes.get(i));
		}
		
		Route route = null;
		if(listOfNodesAux.size() > 0){
			route = new Route(listOfNodesAux);
		}
		
		return route;
	}
	
	/**
	 * Metodo que retorna um node da rede pelo nome informado
	 * @param nodeList
	 * @param name
	 * @return Node
	 */
	protected static Node getNode(Vector<Node> nodeList, String name) {
		for (int i = 0; i < nodeList.size(); i++) {
			Node tmp = nodeList.get(i);
			if (tmp.getName().equals(name)) {
				return tmp;
			}
		}
		return null;
	}
	
	/**
	 * Metodo que rotar o node com o menor custo em uma lista de custos
	 * @param q
	 * @param dist
	 * @return Node
	 */
	private static Node getNodeMinDist(List<Node> q, HashMap<Node, Double> dist){
		Node minNode = q.get(0);
		Double minCust = dist.get(minNode);
		
		for(int i = 1; i < q.size(); i++){
			Node node = q.get(i);
			Double cust = dist.get(node);
			
			if(cust < minCust){
				minCust = cust;
				minNode = node;
			}
		}
		
		return minNode;
	}
	
	/**
	 * Metodo que retorna uma lista de rotas para um dado par de nodes da rede
	 * @param n1
	 * @param n2
	 * @return List<Route>
	 */
	public List<Route> getRoutes(Node n1, Node n2) {
		return this.routesForAllPairs.get(n1.getName() + DIV + n2.getName());
	}
	
	/**
	 * Metodo que retorna uma lista de rotas para um dado par de nodes da rede
	 * @param pair
	 * @return List<Route>
	 */
	public List<Route> getRoutes(String pair) {
		return this.routesForAllPairs.get(pair);
	}
	
	/**
	 * Returns the route list for all pairs
	 * 
	 * @return Vector<Route>
	 */
    public HashMap<String, List<Route>> getRoutesForAllPairs() {
		return routesForAllPairs;
	}

}
