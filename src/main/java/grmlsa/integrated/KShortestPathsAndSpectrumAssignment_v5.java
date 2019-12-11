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

public class KShortestPathsAndSpectrumAssignment_v5 implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; // Number of alternative routes
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
        
        // to avoid metrics error
  		Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
  		Double checkPower = 0.0;
  		
  		boolean QoTO = false;
  		
        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
        	// Begins with the most spectrally efficient modulation format
    		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
    			Modulation mod = avaliableModulations.get(m);
    			circuit.setModulation(mod);
            	
            	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
	            
	            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
	            circuit.setSpectrumAssigned(band);
	            
	    		if (band != null) {
	    			
	    			// enter the power assignment block
	    			
	    			//CPSD
	    			double lauchPower = Double.POSITIVE_INFINITY;
	    			
	    			if(algo.equals("EPA")) { // EPA
		    		    lauchPower = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, mod, band);
	            		
	    			}else if(algo.equals("EnPA")) { // EnPA with linear interpolation
	            		lauchPower = cp.getMesh().getPhysicalLayer().computePowerByLinearInterpolation(circuit, route, mod, band);
	            		
	    			}else if(algo.equals("CPA")) { // CPA
	    				lauchPower = cp.getMesh().getPhysicalLayer().getPowerLinear();
	            		
	    			}else if(algo.equals("APA")) { // APA
	    				lauchPower = AdaptivePowerAssignment(circuit, route, mod, band, cp);
	            		
	    			}else if(algo.equals("APA2")) { // APA2
	            		lauchPower = AdaptivePowerAssignment2(circuit, route, mod, band, cp);
	            		
	    			}else if(algo.equals("APSD")) { // APSD
	            		lauchPower = AdaptivePowerSpectralDensity(circuit, route, mod, band, cp);
	            		
	    			}else if(algo.equals("APSDb")) { // APSD binary search
	            		lauchPower = cp.getMesh().getPhysicalLayer().computePowerSpectralDensityByBinarySearch(circuit, route, mod, band, factorMult);
	            		
	    			}else if(algo.equals("APAb")) { // APA binary search
	            		lauchPower = cp.getMesh().getPhysicalLayer().computePowerByBinarySearch(circuit, route, mod, band, factorMult);
	            		
	    			}else if(algo.equals("APAb2")) { // APA binary search 2
		            	lauchPower = cp.getMesh().getPhysicalLayer().computePowerByBinarySearch2(circuit, route, mod, band, factorMult);
	    			}
            		
            		checkBand = band;
	    			checkRoute = route;
            		checkMod = mod;
            		checkPower = lauchPower;
            		
            		circuit.setLaunchPowerLinear(lauchPower);
            		
	    			if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band, null, false)){ //modulation has acceptable QoT
	    				chosenBand = band;
	    				chosenRoute = route;
		                chosenMod = mod;
		                chosenPower = lauchPower;
		                
		                QoTO = cp.computeQoTForOther(circuit);
		                if(QoTO) {
			                break; // Stop when reaches admissible QoTO
		                }
	            	}
	            }
	    		
	    		if(QoTO){ // to exit the search for modulations
	            	break;
	            }
            }
    		
    		if(QoTO){ // to exit search for route
            	break;
            }
        }
        
        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);
            circuit.setLaunchPowerLinear(chosenPower);
            
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
    
    private int computeNumUsedSlotsOfRoute(Route route){
    	int numSlots = 0;
    	
    	for(Link link : route.getLinkList()){
    		numSlots += link.getUsedSlots();
    	}
    	
    	return numSlots;
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
    	
    	double Pmax = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, mod, sa);
		circuit.setLaunchPowerLinear(Pmax);
		cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, sa, null, false);
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
			boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, sa, null, false);
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
    		Pcurrent = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, mod, sa);
    	}
		
    	return Pcurrent;
    }
    
    public double AdaptivePowerSpectralDensity(Circuit circuit, Route route, Modulation mod, int sa[], ControlPlane cp){
    	
    	if(powerDatabase == null) {
    		powerDatabase = new HashMap<Route, HashMap<Double, HashMap<Modulation, Double>>>();
    	}
    	
    	int NewCall = 0;
    	int CountSub = 0;
    	int CountAdd = 0;
    	
    	double PSDcurrent = 0.0; //power spectral density current
    	double PSDmax = 0.0;
    	double PSDmin = 0.0;
    	
    	double Pcurrent = 0.0;
    	double Pmin = 1.0E-11; //W, -80 dBm
    	double SNRth = mod.getSNRthreshold();
    	
    	double slotBandwidth = route.getLinkList().firstElement().getSlotSpectrumBand();
		double numOfSlots = sa[1] - sa[0] + 1.0;
		double Bsi = (numOfSlots - mod.getGuardBand()) * slotBandwidth; // Circuit bandwidth, less the guard band
		
    	double Pmax = cp.getMesh().getPhysicalLayer().computeMaximumPower2(circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, mod, sa);
		circuit.setLaunchPowerLinear(Pmax);
		cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, sa, null, false);
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
    				
    				PSDcurrent = Pcurrent / Bsi;
    			}
    		}
    	}
    	
    	PSDmax = Pmax / Bsi;
		PSDmin = Pmin / Bsi;
    	
    	boolean search = true;
    	if(!isPowerDB) { // if this does not exist in the database
    		NewCall++;
    		
    		if(SNRmax >= SNRth) {
    			//Pcurrent = Pmin + (factorMult * (Pmax - Pmin));
    			PSDcurrent = PSDmin + (factorMult * (PSDmax - PSDmin));
    			
    		}else { // SNRmax < SNRth
    			//Pcurrent = Pmax;
    			PSDcurrent = PSDmax;
    			search = false;
    		}
    		
    		Pcurrent = PSDcurrent * Bsi;
    	}
    	
		while (search) {
			
			Pcurrent = PSDcurrent * Bsi;
			//PSDcurrent = Pcurrent / Bsi;
			
			circuit.setLaunchPowerLinear(Pcurrent);
			boolean QoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, sa, null, false);
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
						//Pcurrent = Pcurrent - 0.1 * Pcurrent;
						PSDcurrent = PSDcurrent - 0.1 * PSDcurrent;
					}
				}
				
			} else { // SNRcurrent < SNRth
				//if(Pcurrent + 0.1 * Pcurrent > Pmax) {
				if(PSDcurrent + 0.1 * PSDcurrent > PSDmax) {
					break;
					
				} else { // Pcurrent + 0.1 * Pcurrent <= Pmax
					CountAdd++;
					
					if(CountAdd >= 6) {
						break;
						
					} else { // CountAdd < 6
						//Pcurrent = Pcurrent + 0.1 * Pcurrent;
						PSDcurrent = PSDcurrent + 0.1 * PSDcurrent;
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
	        				double SNRcurrent = cp.getMesh().getPhysicalLayer().computeSNRSegment(circuitTest, circuitTest.getRoute(), 0, circuitTest.getRoute().getNodeList().size() - 1, circuitTest.getModulation(), circuitTest.getSpectrumAssigned(), null, false);
	        				
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
	    				double SNRcurrent = cp.getMesh().getPhysicalLayer().computeSNRSegment(circuitTest, circuitTest.getRoute(), 0, circuitTest.getRoute().getNodeList().size() - 1, circuitTest.getModulation(), circuitTest.getSpectrumAssigned(), null, false);
	    				
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
