package grmlsa.integrated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Pair;
import request.RequestForConnection;
import simulationControl.Util;
import util.IntersectionFreeSpectrum;

public class KShortestPathsAndSpectrumAssignment_v3 implements IntegratedRMLSAAlgorithmInterface {
	
	private int k = 2; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
    private Double factorMult;
    private String algo;
    
    private HashMap<Route, HashMap<Double, HashMap<Modulation, Double>>> powerDatabase; // route, transmission rate e modulation
    
    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k);
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
            spectrumAssignment = cp.getSpectrumAssignment();
        }
        
        if(factorMult == null){
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			factorMult = Double.parseDouble((String)uv.get("factorMult"));
			algo = (String)uv.get("algo");
		}
        
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = null;
        Double chosenPower = null;
        
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        int kFF = 3; // for the k-FirstFit spectrum allocation algorithm
        
        // to avoid metrics error
  		Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		Double checkPower = null;
  		
  		boolean QoTO = false;
  		
        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
        	// Begins with the most spectrally efficient modulation format
    		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
    			Modulation mod = avaliableModulations.get(m);
    			circuit.setModulation(mod);
            	
            	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
	            
	            // for the k-FirstFit spectrum allocation algorithm
	            ArrayList<int[]> bandList = new ArrayList<int[]>();
	    		for (int[] bandTemp : merge) { // checks and guard the free bands that can establish the requisition
	    			if(bandTemp[1] - bandTemp[0] + 1 >= slotsNumber){
	    				
	    				int faixaTemp[] = bandTemp.clone();
	    				bandList.add(faixaTemp);
	    				
	    				if(bandList.size() == kFF) { // stop when you reach the k value of free bands
	    					break;
	    				}
	    			}
	    		}
	            
	    		// traverses the free spectrum bands
	    		for (int[] bandTemp : bandList) {
	    			int band[] = bandTemp.clone();
	    			band[1] = band[0] + slotsNumber - 1;
	    			
	    			circuit.setSpectrumAssigned(band);
	    			
	    			// enter the power assignment block
	    			
	    			//CPA
	    			double lauchPower = Double.POSITIVE_INFINITY;
	    			
	    			if(algo.equals("EPA")) { // EPA
		    		    lauchPower = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit, route, 0, route.getNodeList().size() - 1, mod, band);
	            		
	    			}else if(algo.equals("APAb")) { // APA binary search
	            		lauchPower = cp.getMesh().getPhysicalLayer().computePowerByBinarySearch(circuit, route, mod, band, factorMult);
	            		
	    			}else if(algo.equals("EnPA")) { // EnPA with linear interpolation
	            		lauchPower = cp.getMesh().getPhysicalLayer().computePowerByLinearInterpolation(circuit, route, mod, band);
	            		
	    			}else if(algo.equals("CPA")) { // CPA
	    				lauchPower = cp.getMesh().getPhysicalLayer().getPowerLinear();
	            		
	    			}else if(algo.equals("APA")) { // APA
	    				lauchPower = AdaptivePowerAssignment(circuit, route, mod, band, cp);
	            		
	    			}else if(algo.equals("APA2")) { // APA2
	            		lauchPower = AdaptivePowerAssignment2(circuit, route, mod, band, cp);
	    			}
            		
            		checkBand = band;
	    			checkRoute = route;
            		checkMod = mod;
            		checkPower = lauchPower;
            		
            		circuit.setLaunchPowerLinear(lauchPower);
            		
	    			if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null)){ //modulation has acceptable QoT
	    				chosenBand = band;
	    				chosenRoute = route;
		                chosenMod = mod;
		                chosenPower = lauchPower;
		                
		                QoTO = cp.computeQoTForOther(circuit);
		                if(QoTO) {
		                	break; // Stop when a modulation reaches admissible QoTO
		                }
		                
		                //break; // Stop when a modulation reaches admissible QoT
	            	}
	            }
	    		
	    		if(QoTO){
	            	break;
	            }
            }
    		
    		if(QoTO){
            	break;
            }
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);
            circuit.setLaunchPowerLinear(chosenPower);
            
            // imprimir banco de dados do APA
//            if(powerDatabase != null) {
//	        	for(Route r : powerDatabase.keySet()) {
//	        		HashMap<Double, HashMap<Modulation, Double>> trsModsPower = powerDatabase.get(r);
//	        		
//	        		System.out.println("roure = " + r.getRouteInString());
//	        		
//	        		for(Double tr : trsModsPower.keySet()) {
//	        			HashMap<Modulation, Double> modsPower = trsModsPower.get(tr);
//	        			
//	        			System.out.println("------tr = " + tr);
//	        			
//	        			for(Modulation mod : modsPower.keySet()) {
//	        				System.out.println("-------------mod = " + mod.getName() + ", power = " + modsPower.get(mod));
//	        			}
//	        		}
//	        	}
//            }
//        	System.out.println("===============================================================");
            
            return true;

        } else {
        	if(checkRoute == null){
				checkRoute = candidateRoutes.get(0);
				checkMod = avaliableModulations.get(0);
			}
            circuit.setRoute(checkRoute);
            circuit.setModulation(checkMod);
            circuit.setSpectrumAssigned(checkBand);
            circuit.setLaunchPowerLinear(checkPower);
            
            return false;
        }
    }
    
    /**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return kShortestsPaths;
    }
    
    public double AdaptivePowerAssignment(Circuit circuit, Route route, Modulation mod, int sa[], ControlPlane cp){
    	
    	if(powerDatabase == null) {
    		powerDatabase = new HashMap<Route, HashMap<Double, HashMap<Modulation, Double>>>();
    	}
    	
    	int NewCall = 0;
    	int CountSub = 0;
    	int CountAdd = 0;
    	
    	double Pcurrent = 0.0;
    	double Pmin = 1.0E-11; //W, -80 dBm
    	double SNRth = mod.getSNRthreshold();
    	
    	double Pmax = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit, route, 0, route.getNodeList().size() - 1, mod, sa);
		circuit.setLaunchPowerLinear(Pmax);
		cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, sa, null);
		double SNRmax = circuit.getSNR();
		
		boolean isPowerDB = false;
		HashMap<Double, HashMap<Modulation, Double>> trsModsPower = powerDatabase.get(route);
    	if(trsModsPower != null) {
    		HashMap<Modulation, Double> modsPower = trsModsPower.get(circuit.getRequiredBandwidth());
    		if(modsPower != null) {
    			Double power = modsPower.get(mod);
    			if(power != null) {
    				Pcurrent = power;
    				isPowerDB = true;
    			}
    		}
    	}
    	
    	boolean search = true;
    	if(!isPowerDB) { // if this does not exist in the database
    		NewCall++;
    		
    		if(SNRmax >= SNRth) {
    			Pcurrent = Pmin + (factorMult * (Pmax - Pmin));
    			
    		}else { // SNRmax < SNRth
    			Pcurrent = Pmax;
    			search = false;
    		}
    	}
		
		while (search) {
			circuit.setLaunchPowerLinear(Pcurrent);
			boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, sa, null);
			double SNRcurrent = circuit.getSNR();
			
			if(SNRcurrent >= SNRth) { // line 3
				
				boolean QoTO = cp.computeQoTForOther(circuit);
				
				if(QoT && QoTO) {
					if(NewCall > 0) {
						NewCall++;
					}
					break;
					
				} else {
					CountSub++;
					
					if(CountSub >= 6) {
						break;
						
					} else { // ContSub < 6
						Pcurrent = Pcurrent - 0.1 * Pcurrent;
					}
				}
				
			} else { // SNRcurrent < SNRth
				if(Pcurrent + 0.1 * Pcurrent > Pmax) {
					break;
					
				} else { // Pcurrent + 0.1 * Pcurrent <= Pmax
					CountAdd++;
					
					if(CountAdd >= 6) {
						break;
						
					} else { // CountAdd < 6
						Pcurrent = Pcurrent + 0.1 * Pcurrent;
					}
				}
			}
		}
    	
		if (NewCall > 1) { // line 5
			// saves the power in the database
			
			trsModsPower = powerDatabase.get(route);
	    	if(trsModsPower == null) {
	    		trsModsPower = new HashMap<Double, HashMap<Modulation,Double>>();
	    		powerDatabase.put(route, trsModsPower);
	    	}
	    	
	    	HashMap<Modulation, Double> modsPower = trsModsPower.get(circuit.getRequiredBandwidth());
    		if(modsPower == null) {
    			modsPower = new HashMap<Modulation, Double>();
    			trsModsPower.put(circuit.getRequiredBandwidth(), modsPower);
    		}
	    	
    		modsPower.put(mod, Pcurrent);
		}
		
    	return Pcurrent;
    }
    
    public double AdaptivePowerAssignment2(Circuit circuit, Route route, Modulation mod, int sa[], ControlPlane cp){
    	
    	if(powerDatabase == null) {
    		calculatePowerDatabase(cp);
    	}
    	
    	double Pcurrent = 0.0;
    	
		boolean isPowerDB = false;
		HashMap<Double, HashMap<Modulation, Double>> trsModsPower = powerDatabase.get(route);
    	if(trsModsPower != null) {
    		HashMap<Modulation, Double> modsPower = trsModsPower.get(circuit.getRequiredBandwidth());
    		if(modsPower != null) {
    			Double power = modsPower.get(mod);
    			if(power != null) {
    				Pcurrent = power;
    				isPowerDB = true;
    			}
    		}
    	}
    	
    	if(!isPowerDB) {
    		Pcurrent = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit, route, 0, route.getNodeList().size() - 1, mod, sa);
    	}
		
    	return Pcurrent;
    }
    
    public void calculatePowerDatabase(ControlPlane cp) {
    	powerDatabase = new HashMap<Route, HashMap<Double, HashMap<Modulation, Double>>>();
    	
    	int totalSlots = cp.getMesh().getLinkList().firstElement().getNumOfSlots();
    	
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        Set<Double> transmissionRateList = Util.bandwidths;
        Vector<Route> routesForAllPairs = new Vector<Route>();
		
        HashMap<String, List<Route>> routes = cp.getIntegrated().getRoutingAlgorithm().getRoutesForAllPairs();
		for(String pair : routes.keySet()){
			for(Route route : routes.get(pair)){
				routesForAllPairs.add(route);
			}
		}
        
		for(int i = 0; i < routesForAllPairs.size(); i++){
			Route routeTemp = routesForAllPairs.get(i);
			
    		for(Double transRateTemp : transmissionRateList) {
    			for(Modulation modTemp : avaliableModulations) {
    				
    				int slotNumber = modTemp.requiredSlots(transRateTemp);
    				int quantCircuits = (int)(totalSlots / slotNumber); // number of circuits
    				
    				ArrayList<int[]> circuitsSa = new ArrayList<int[]>(quantCircuits);
    				
    				for(int c = 0; c < quantCircuits; c++){
    					int saTemp[] = new int[2];
    					saTemp[0] = 1 + (c * slotNumber);
    					saTemp[1] = saTemp[0] + slotNumber - 1;
    					circuitsSa.add(saTemp);
    				}
    				
    				Route routeClone = routeTemp.clone();
    				
    				Pair pair = new Pair(routeClone.getNodeList().firstElement(), routeClone.getNodeList().lastElement());
    				
    				for(int c = 0; c < quantCircuits; c++){
    					RequestForConnection requestTemp = new RequestForConnection();
    					requestTemp.setPair(pair);
    					requestTemp.setRequiredBandwidth(transRateTemp);
    					
    					Circuit circuitTemp = new Circuit();
    					circuitTemp.setPair(pair);
    					circuitTemp.setRoute(routeClone);
    					circuitTemp.setModulation(modTemp);
    					circuitTemp.setSpectrumAssigned(circuitsSa.get(c));
    					circuitTemp.addRequest(requestTemp);
    					
    					for(int l = 0; l < routeClone.getLinkList().size(); l++) {
    						Link linkTemp = routeClone.getLink(l);
    						linkTemp.addCircuit(circuitTemp);
    					}
    				}
    				
    				Circuit circuitTest = routeClone.getLink(0).getCircuitList().first();
    				
    				double SNRth = modTemp.getSNRthresholdLinear();
    				SNRth *= factorMult;
    				
    				//double Pcurrent = 1.0E-11; //W, -80 dBm
    				double Pcurrent = 1.0E-4; //W, -10 dBm
    				
    				boolean exhaustiveSearch = true;
    				if(exhaustiveSearch) {
    				
	    				// seeking the power with the exhaustive search
	    				
	    				double Pmax = 1.0E-3; //W, 0 dBm
	    				//double Pinc = 1.0E-4; //W
	    				double Pinc = 0.0012589254117941673; //W, 1 dBm
	    				
	    				while(Pcurrent < Pmax) {
	    					
	    					circuitTest.setLaunchPowerLinear(Pcurrent);
	        				double SNRcurrent = cp.getMesh().getPhysicalLayer().computeSNRSegment(circuitTest, circuitTest.getRoute(), 0, circuitTest.getRoute().getNodeList().size() - 1, circuitTest.getModulation(), circuitTest.getSpectrumAssigned(), null);
	        				
	        				if(SNRcurrent >= SNRth) {
		        				//boolean QoTO = cp.computeQoTForOther(circuitTest);
		        				boolean QoTO = true;
		        				
		        				if(QoTO) {
		        					
		        					// saves the power found in the database
		        					
		        					HashMap<Double, HashMap<Modulation, Double>> trsModsPower = powerDatabase.get(routeTemp);
		        			    	if(trsModsPower == null) {
		        			    		trsModsPower = new HashMap<Double, HashMap<Modulation,Double>>();
		        			    		powerDatabase.put(routeTemp, trsModsPower);
		        			    	}
		        			    	
		        			    	HashMap<Modulation, Double> modsPower = trsModsPower.get(transRateTemp);
		        		    		if(modsPower == null) {
		        		    			modsPower = new HashMap<Modulation, Double>();
		        		    			trsModsPower.put(transRateTemp, modsPower);
		        		    		}
		        			    	
		        		    		modsPower.put(modTemp, Pcurrent);
		        					
		        					break;
		        				}
	        				}
	        				
	    					Pcurrent += Pinc;
	    				}
    				
    				}else {
    					
	    				// seeking power with binary search
	    				Pcurrent = cp.getMesh().getPhysicalLayer().computePowerByBinarySearch(circuitTest, circuitTest.getRoute(), circuitTest.getModulation(), circuitTest.getSpectrumAssigned(), factorMult);
	    				
	    				circuitTest.setLaunchPowerLinear(Pcurrent);
	    				double SNRcurrent = cp.getMesh().getPhysicalLayer().computeSNRSegment(circuitTest, circuitTest.getRoute(), 0, circuitTest.getRoute().getNodeList().size() - 1, circuitTest.getModulation(), circuitTest.getSpectrumAssigned(), null);
	    				
	    				if(SNRcurrent >= SNRth) {
	        				//boolean QoTO = cp.computeQoTForOther(circuitTest);
	        				boolean QoTO = true;
	        				
	        				if(QoTO) {
	        					
	        					// saves the power found in the database
	        					
	        					HashMap<Double, HashMap<Modulation, Double>> trsModsPower = powerDatabase.get(routeTemp);
	        			    	if(trsModsPower == null) {
	        			    		trsModsPower = new HashMap<Double, HashMap<Modulation,Double>>();
	        			    		powerDatabase.put(routeTemp, trsModsPower);
	        			    	}
	        			    	
	        			    	HashMap<Modulation, Double> modsPower = trsModsPower.get(transRateTemp);
	        		    		if(modsPower == null) {
	        		    			modsPower = new HashMap<Modulation, Double>();
	        		    			trsModsPower.put(transRateTemp, modsPower);
	        		    		}
	        			    	
	        		    		modsPower.put(modTemp, Pcurrent);
	        				}
	    				}
    				}
    				
    			}
    		}
    	}
    }
}
