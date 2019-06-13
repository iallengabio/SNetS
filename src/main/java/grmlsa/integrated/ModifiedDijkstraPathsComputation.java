package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Node;
import util.IntersectionFreeSpectrum;

/**
 * Implementation based on the Modified Dijkstra Path Computation (MD-PC) algorithm presented in:
 * - A Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment Scheme for Future Elastic Optical Networks (2013)
 * 
 * @author Alexandre
 */
public class ModifiedDijkstraPathsComputation implements IntegratedRMLSAAlgorithmInterface {
	
	private ModulationSelectionAlgorithmInterface modulationSelection;
	private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
	
	@Override
	public boolean rsa(Circuit circuit,ControlPlane cp) {
		if(modulationSelection == null){
			modulationSelection = cp.getModulationSelection(); // Uses the modulation selection algorithm defined in the simulation file
		}
		if(spectrumAssignment == null){
			spectrumAssignment = cp.getSpectrumAssignment(); // Uses the spectrum assignment algorithm defined in the simulation file
		}
		
		Vector<Node> nodeList = cp.getMesh().getNodeList();
		Vector<Link> linkList = cp.getMesh().getLinkList();
		Node s = getNode(nodeList, circuit.getSource().getName());
		Node d = getNode(nodeList, circuit.getDestination().getName());
		
		Route route = PathComputation(circuit, nodeList, linkList, s, d, cp);
		circuit.setRoute(route);
		if(route != null){
			
			Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
			circuit.setModulation(mod);
			if(mod != null){
				
				int requeridSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
				List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
				int band[] = spectrumAssignment.policy(requeridSlots, merge, circuit, cp);
				
				circuit.setSpectrumAssigned(band);
				if(band != null){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Modified version of Dijkstra's shortest path algorithm
	 * 
	 * @param circuit Circuit
	 * @param nodeList Vector<Node>
	 * @param linkList Vector<Link>
	 * @param s Node
	 * @param d Node
	 * @param cp ControlPlane
	 * @return Route
	 */
	private Route PathComputation(Circuit circuit, Vector<Node> nodeList, Vector<Link> linkList, Node s, Node d, ControlPlane cp){
		HashMap<Node, Double> dist = new HashMap<Node, Double>(); //distance from the source node to node i
		HashMap<Node, Node> previous = new HashMap<Node, Node>(); //node prior to node i
		
		Node lastNode = null; //for error correction
		
		List<Node> q = new ArrayList<Node>(); //list of unvisited nodes
		
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
				
				Node v = getNode(nodeList, link.getDestination().getName());
				if((v != null) && (q.contains(v))){
					Vector<Node> listOfNodesTemp = new Vector<Node>();
					listOfNodesTemp.add(v);
					listOfNodesTemp.add(u);
					Node aux = u;
					while(previous.get(aux) != null){
						aux = previous.get(aux);
						listOfNodesTemp.add(aux);
					}
					
					Vector<Node> listOfNodesAuxTemp = new Vector<Node>();
					for(int i = listOfNodesTemp.size() - 1; i >= 0; i--){
						listOfNodesAuxTemp.add(listOfNodesTemp.get(i));
					}
					Route routeTemp = new Route(listOfNodesAuxTemp);
					
					circuit.setRoute(routeTemp);
					Modulation mod = modulationSelection.selectModulation(circuit, routeTemp, spectrumAssignment, cp);
					circuit.setModulation(mod);
					
					if(mod != null){
						int requeridSlots = mod.requiredSlots(circuit.getRequiredBandwidth());
						
						List<int[]> merge = IntersectionFreeSpectrum.merge(routeTemp, circuit.getGuardBand());
						int faixa[] = spectrumAssignment.policy(requeridSlots, merge, circuit, cp);
						circuit.setSpectrumAssigned(faixa);
						
						if(faixa != null){
							boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, routeTemp, mod, faixa, null, false);
							
							if(QoT){
								Double cust = dist.get(u) + link.getDistance();
								if(cust < dist.get(v)){
									dist.put(v, cust);
									previous.put(v, u);
									
									lastNode = u;
								}
							}
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
		
		if(route == null){
			//complete the part of the missing route to the destination
			route = completeRoute(nodeList, linkList, s, d, lastNode, previous);
		}
		
		return route;
	}
	
	/**
	 * Method that completes a route to the destination
	 * 
	 * @param nodeList Vector<Node>
	 * @param linkList Vector<Link>
	 * @param s Node
	 * @param d Node
	 * @param lastNode Node
	 * @param previous HashMap<Node, Node>
	 * @return Route
	 */
	private Route completeRoute(Vector<Node> nodeList, Vector<Link> linkList, Node s, Node d, Node lastNode, HashMap<Node, Node> previous){
		Vector<Node> nodeListP1 = new Vector<Node>();
		Node aux = lastNode;
		if(previous.get(aux) != null){
			nodeListP1.add(lastNode);
		}
		
		while(previous.get(aux) != null){
			aux = previous.get(aux);
			nodeListP1.add(aux);
			
			if(aux == s){
				break;
			}
		}
		
		if(nodeListP1.size() > 0){
			Vector<Link> linkListRem = new Vector<Link>();
			for(int i = 0; i < nodeListP1.size() - 1; i++){
				Link link = nodeListP1.get(i).getOxc().linkTo(nodeListP1.get(i + 1).getOxc());
				linkListRem.add(link);
			}
			
			Vector<Link> newLinkList = new Vector<Link>();
			for(int i = 0; i < linkList.size(); i++){
				Link link = linkList.get(i);
				if(!linkListRem.contains(link)){
					newLinkList.add(link);
				}
			}
			
			Route routeP2 = Dijkstra(nodeList, newLinkList, lastNode, d, 1);
			Vector<Node> nodeListP2 = routeP2.getNodeList();
			
			Vector<Node> listOfNodesAux = new Vector<Node>();
			for(int i = nodeListP1.size() - 1; i >= 0 ; i--){
				listOfNodesAux.add(nodeListP1.get(i));
			}
			
			for(int i = 1; i < nodeListP2.size(); i++){
				listOfNodesAux.add(nodeListP2.get(i));
			}
			
			boolean flagRepeticao = false;
			for(int i = 0; i < nodeListP1.size(); i++){
				Node nodeI = nodeListP1.get(i);
				int cont = 0;
				for(int j = 0; j < listOfNodesAux.size(); j++){
					Node nodeJ = listOfNodesAux.get(j);
					if(nodeI == nodeJ){
						cont++;
					}
				}
				if(cont > 1){
					flagRepeticao = true;
				}
			}
			
			for(int i = 1; i < nodeListP2.size(); i++){
				Node nodeI = nodeListP2.get(i);
				int cont = 0;
				for(int j = 0; j < listOfNodesAux.size(); j++){
					Node nodeJ = listOfNodesAux.get(j);
					if(nodeI == nodeJ){
						cont++;
					}
				}
				if(cont > 1){
					flagRepeticao = true;
				}
			}
			
			Route routeTemp = null;
			if(flagRepeticao){
				routeTemp = Dijkstra(nodeList, linkList, lastNode, d, 1);
				
			}else{
				routeTemp = new Route(listOfNodesAux);
			}
			
			return routeTemp;
		}
		
		return null;
	}
	
	/**
	 * Method that implements Dijkstra's shortest path algorithm
	 * 
	 * @param nodeList Vector<Node>
	 * @param linkList Vector<Link>
	 * @param s Node
	 * @param d Node
	 * @param typeCost int
	 * @return Route
	 */
	private Route Dijkstra(Vector<Node> nodeList, Vector<Link> linkList, Node s, Node d, int typeCost){
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
	 * Method that returns a node of the network by the name entered
	 * 
	 * @param nodeList Vector<Node>
	 * @param name String
	 * @return Node
	 */
	private Node getNode(Vector<Node> nodeList, String name) {
		for (int i = 0; i < nodeList.size(); i++) {
			Node tmp = nodeList.get(i);
			if (tmp.getName().equals(name)) {
				return tmp;
			}
		}
		return null;
	}
	
	/**
	 * Method that rotates the node with the lowest cost in a list of costs
	 * 
	 * @param q List<Node>
	 * @param dist HashMap<Node, Double>
	 * @return Node
	 */
	private Node getNodeMinDist(List<Node> q, HashMap<Node, Double> dist){
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
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return null;
    }
}
