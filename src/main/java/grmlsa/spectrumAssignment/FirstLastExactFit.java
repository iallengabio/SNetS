package grmlsa.spectrumAssignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import grmlsa.Route;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the spectrum allocation technique called FirstLastExactFit.
 * Algorithm based on: A spectrum allocation scheme based on first-last-exact fit policy
 *                     for elastic optical networks (2016)
 * 
 * @author Alexandre
 */
public class FirstLastExactFit implements SpectrumAssignmentAlgorithmInterface {

	List<Route> disjointConnectionGroup;

    @Override
    public boolean assignSpectrum(int numberOfSlots, Circuit circuit, ControlPlane cp) {
        List<int[]> composition = IntersectionFreeSpectrum.merge(circuit.getRoute());

        int chosen[] = policy(numberOfSlots, composition, circuit, cp);
        circuit.setSpectrumAssigned(chosen);
        
        if (chosen == null)
        	return false;

        return true;
    }
    
    /**
     * Creates the disjoint connection group
     * 
     * @param cp ControlPlane
     */
    public void createGraphCheckDisjoint(ControlPlane cp){
		if(disjointConnectionGroup != null){
			return;
		}
		
		HashMap<Route, List<Route>> pathGraph = new HashMap<>();
		Vector<Route> routesForAllPairs = new Vector<Route>();
		
		if(cp.getIntegrated() != null && cp.getIntegrated().getRoutingAlgorithm().getRoutesForAllPairs() != null){
			HashMap<String, List<Route>> routes = cp.getIntegrated().getRoutingAlgorithm().getRoutesForAllPairs();
			
			for(String pair : routes.keySet()){
				for(Route route : routes.get(pair)){
					routesForAllPairs.add(route);
				}
			}
			
		}else if(cp.getRouting().getRoutesForAllPairs() != null){
			HashMap<String, Route> routes = cp.getRouting().getRoutesForAllPairs();
			routesForAllPairs = new Vector<Route>(routes.values());
		}
		
		for(int i = 0; i < routesForAllPairs.size(); i++){
			Route routeI = routesForAllPairs.get(i);
			List<Route> listRoute = new ArrayList<>();
			
			for(int j = 0; j < routesForAllPairs.size(); j++){
				Route routeJ = routesForAllPairs.get(j);
				
				if(!routeI.equals(routeJ)){
					for(int l = 0; l < routeJ.getLinkList().size(); l++){
						Link linkJ = routeJ.getLinkList().get(l);
						
						if(routeI.containThisLink(linkJ)){
							listRoute.add(routeJ);
							break;
						}
					}
				}
			}
			
			pathGraph.put(routeI, listRoute);
		}
		
		disjointConnectionGroup = new ArrayList<>();
		for(int i = 0; i < routesForAllPairs.size(); i++){
			
			Route routeI = routesForAllPairs.get(i);
			List<Route> listRoute = pathGraph.get(routeI);
			
			boolean flagPertence = false;
			for(int j = 0; j < listRoute.size(); j++){
				Route routeJ = listRoute.get(j);
				
				if(disjointConnectionGroup.contains(routeJ)){
					flagPertence = true;
					break;
				}
			}
			
			if(!flagPertence){
				disjointConnectionGroup.add(routeI);
			}
		}
	}

    /**
	 * Apply first exact fit policy
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @return int[]
	 */
	public static int[] firstExactFit(int numberOfSlots, List<int[]> freeSpectrumBands){
		int chosen[] = null;
		for (int[] band : freeSpectrumBands) {
			int tamFaixa = band[1] - band[0] + 1;
			if(tamFaixa == numberOfSlots){
				chosen = band.clone();
				chosen[1] = chosen[0] + numberOfSlots - 1;//nao eh necessario alocar a faixa inteira, apenas a quantidade de slots necessaria
				break;
			}
		}
		
		if(chosen == null){//nao encontrou nenhuma faixa contigua e continua disponivel
		//agora basta buscar a faixa livre com tamanho mais distante da quantidade de slots requisitados
		
			int maiorDif = -1;
			for (int[] band : freeSpectrumBands) {
				int tamFaixa = band[1] - band[0] + 1;
				if(tamFaixa >= numberOfSlots){
					if(tamFaixa - numberOfSlots > maiorDif){ //encontrou uma faixa com quantidade de slots mais "diferente"
						chosen = band.clone();
						chosen[1] = chosen[0] + numberOfSlots - 1;//nao eh necessario alocar a faixa inteira, apenas a quantidade de slots necessaria
						maiorDif = tamFaixa - numberOfSlots;
					}
				}
			}
		}
		
		return chosen;
	}
	
	/**
	 * Apply first exact fit policy
	 * 
	 * @param numberOfSlots
	 * @param livres
	 * @return
	 */
	public static int[] lastExactFit(int numberOfSlots, List<int[]> livres){
		int chosen[] = null;
		for (int i = livres.size()-1; i >= 0; i--) {
			int band[] = livres.get(i);
			
			int tamFaixa = band[1] - band[0] + 1;
			if(tamFaixa == numberOfSlots){
				chosen = band.clone();
				chosen[0] = chosen[1] - numberOfSlots + 1;//nao eh necessario alocar a faixa inteira, apenas a quantidade de slots necessaria
				break;
			}
		}		
		
		if(chosen == null){//nao encontrou nenhuma faixa contigua e continua disponivel
		//agora basta buscar a faixa livre com tamanho mais distante da quantidade de slots requisitados
		
			int maiorDif = -1;
			for (int i = livres.size()-1; i >= 0; i--) {
				int band[] = livres.get(i);
				
				int tamFaixa = band[1] - band[0] + 1;
				if(tamFaixa >= numberOfSlots){
					if(tamFaixa - numberOfSlots > maiorDif){ //encontrou uma faixa com quantidade de slots mais "diferente"
						chosen = band.clone();
						chosen[0] = chosen[1] - numberOfSlots + 1;//nao eh necessario alocar a faixa inteira, apenas a quantidade de slots necessaria
						maiorDif = tamFaixa - numberOfSlots;
					}
				}
			}
		}
		
		return chosen;
	}
	
	/**
	 * Uses the disjoint connection group to choose between FirstExactFit or LastExactFit
	 * 
	 * @param numberOfSlots int
	 * @param freeSpectrumBands List<int[]>
	 * @param circuit Circuit
	 * @return int[]
	 */
	public int[] policy(int numberOfSlots, List<int[]> freeSpectrumBands, Circuit circuit, ControlPlane cp){
		createGraphCheckDisjoint(cp);
		
		if (disjointConnectionGroup.contains(circuit.getRoute())) {
			return firstExactFit(numberOfSlots, freeSpectrumBands);
			
		}else {
			return lastExactFit(numberOfSlots, freeSpectrumBands);
		}
	}
}
