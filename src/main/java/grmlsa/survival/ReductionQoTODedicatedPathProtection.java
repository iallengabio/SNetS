package grmlsa.survival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.Route;
import grmlsa.YenKShortestPath;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.routing.FixedDoubleRouteBacktracking;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.EnergyConsumption;
import network.SurvivalCircuit;
import network.SurvivalControlPlane;
import request.RequestForConnection;
import util.IntersectionFreeSpectrum;

public class ReductionQoTODedicatedPathProtection implements SurvivalStrategyInterface {
	
	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    
    private HashMap<Route, Route> backupRoutes;
    
    private double sigma;

    @Override
	public boolean applyStrategy(RequestForConnection rfc, SurvivalControlPlane cp) throws Exception {
    	return false;
    }
    
    @Override
    public boolean applyStrategy(Circuit circuit, SurvivalControlPlane cp) {
        if (kShortestsPaths == null){
            kShortestsPaths = new YenKShortestPath(cp.getMesh().getNodeList(), cp.getMesh().getLinkList(), k, 1);
        }
        if (modulationSelection == null){
            modulationSelection = cp.getModulationSelection();
            
            //read the sigma value
			Map<String, String> uv = cp.getMesh().getOthersConfig().getVariables();
			sigma = Double.parseDouble((String)uv.get("sigma"));
        }
        if(spectrumAssignment == null){
            spectrumAssignment = cp.getSpectrumAssignment();
        }
        
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        computeBackupRoutes(candidateRoutes, cp);
        
  		InfoRoutes infosPrimaryAndSecondary = null;
  		double lowestMetricPCTotal = 0.0;
  		
  		InfoRoutes infosPrimaryAndSecondaryAux = null;
  		double lowestMetricPCTotalAux = 0.0;
  		
  		List<InfoRoutes> infosPrimaryAndSecondaryList = new ArrayList<>();
  		
        for (Route primaryRoute : candidateRoutes) {
            
            InfoRoute infoPrimaryRoute = checkRoute(circuit, primaryRoute, cp);
            if(infoPrimaryRoute.getCheckRoute() != null){
            	
            	Route secondaryRoute = backupRoutes.get(primaryRoute);
            	if(secondaryRoute != null){
            		
            		InfoRoute infoSecondaryRoute = checkRoute(circuit, secondaryRoute, cp);
					if(infoSecondaryRoute.getCheckRoute() != null){
						
						if((infoPrimaryRoute.getRoute() != null) && (infoSecondaryRoute.getRoute() != null)){ //rotas com QoT aceitavel
							
							double totalMetricPC = infoPrimaryRoute.getPowerConsumption() + infoSecondaryRoute.getPowerConsumption();
							boolean QoTforOther = isAdmissibleQoTOther(circuit, infoPrimaryRoute, infoSecondaryRoute, cp);
							
							if((lowestMetricPCTotalAux == 0.0) || (totalMetricPC < lowestMetricPCTotalAux)){ //menor consumo de energia
								lowestMetricPCTotalAux = totalMetricPC;
								
								infosPrimaryAndSecondaryAux = new InfoRoutes(infoPrimaryRoute, infoSecondaryRoute, QoTforOther);
							}
							
							if(QoTforOther){ //rotas de trabalho e backup com QoTO aceitavel
								if((lowestMetricPCTotal == 0.0) || (totalMetricPC < lowestMetricPCTotal)){ //menor consumo de energia
									lowestMetricPCTotal = totalMetricPC;
									
									infosPrimaryAndSecondary = new InfoRoutes(infoPrimaryRoute, infoSecondaryRoute, QoTforOther);
								}
							}
							
						} else {
							//guarda as informacoes das rotas de trabalho e backup que conseguiram alocar espectro
							InfoRoutes infosPrimaryAndSecondaryTemp = new InfoRoutes(infoPrimaryRoute, infoSecondaryRoute, true);
							infosPrimaryAndSecondaryList.add(infosPrimaryAndSecondaryTemp);
						}
					}
            	}
            }
        }
        
        if(infosPrimaryAndSecondary == null){
        	infosPrimaryAndSecondary = infosPrimaryAndSecondaryAux;
        }
        
        if(infosPrimaryAndSecondary != null){
        	
        	circuit.setQoTForOther(infosPrimaryAndSecondary.isQoTOther());
        	
        	//rota de trabalho
        	circuit.setRoute(infosPrimaryAndSecondary.getPrimaryRoute().getRoute());
        	circuit.setModulation(infosPrimaryAndSecondary.getPrimaryRoute().getMod());
        	circuit.setSpectrumAssigned(infosPrimaryAndSecondary.getPrimaryRoute().getBand());
        	
        	//rota de backup
        	((SurvivalCircuit)circuit).setBackupRoute(infosPrimaryAndSecondary.getSecondaryRoute().getRoute());
        	((SurvivalCircuit)circuit).setModulationByBackupRoute(infosPrimaryAndSecondary.getSecondaryRoute().getMod());
 			((SurvivalCircuit)circuit).setSpectrumAssignedByBackupRoute(infosPrimaryAndSecondary.getSecondaryRoute().getBand());
        	
        	return true;
        }
        
        checkInfos(circuit, infosPrimaryAndSecondaryList, candidateRoutes,  cp);
        
        return false;
    }
    

    private void checkInfos(Circuit circuit, List<InfoRoutes> infosPrimaryAndSecondaryList, List<Route> candidateRoutes, ControlPlane cp){
    	
    	//esses passos sao para ajudar a identificar o tipo de bloqueio
  		Route primaryRoute = null;
  		Modulation primaryMod = null;
  		int primarySa[] = null;
  		
  		Route secondaryRoute = null;
  		Modulation secondaryMod = null;
  		int secondarySa[] = null;
  		
  		boolean QoTforOther = true;
  		
  		if(infosPrimaryAndSecondaryList.size() > 0){
  			
  			for(int i = 0; i < infosPrimaryAndSecondaryList.size(); i++){
  				InfoRoutes infos = infosPrimaryAndSecondaryList.get(i);
  				
  				QoTforOther = infos.isQoTOther();
  				
  				if((infos.getPrimaryRoute().getRoute() != null) && (infos.getSecondaryRoute().getCheckRoute() != null)){
  					primaryRoute = infos.getPrimaryRoute().getRoute();
  					primaryMod = infos.getPrimaryRoute().getMod();
  					primarySa = infos.getPrimaryRoute().getBand();
  					
  					secondaryRoute = infos.getSecondaryRoute().getCheckRoute();
  					secondaryMod = infos.getSecondaryRoute().getCheckMod();
  					secondarySa = infos.getSecondaryRoute().getCheckBand();
  					
  					break;
  					
  				}else if((infos.getPrimaryRoute().getCheckRoute() != null) && (infos.getSecondaryRoute().getRoute() != null)){
  					primaryRoute = infos.getPrimaryRoute().getCheckRoute();
  					primaryMod = infos.getPrimaryRoute().getCheckMod();
  					primarySa = infos.getPrimaryRoute().getCheckBand();
  					
  					secondaryRoute = infos.getSecondaryRoute().getRoute();
  					secondaryMod = infos.getSecondaryRoute().getMod();
  					secondarySa = infos.getSecondaryRoute().getBand();
  					
  					break;
  					
  				}else if((infos.getPrimaryRoute().getCheckRoute() != null) && (infos.getSecondaryRoute().getCheckRoute() != null)){
  					primaryRoute = infos.getPrimaryRoute().getCheckRoute();
  					primaryMod = infos.getPrimaryRoute().getCheckMod();
  					primarySa = infos.getPrimaryRoute().getCheckBand();
  					
  					secondaryRoute = infos.getSecondaryRoute().getCheckRoute();
  					secondaryMod = infos.getSecondaryRoute().getCheckMod();
  					secondarySa = infos.getSecondaryRoute().getCheckBand();
  					
  					break;
  				}
  			}
  		}
  		
  		if(primaryRoute == null){
  			primaryRoute = candidateRoutes.get(0);
  			primaryMod = modulationSelection.getAvaliableModulations().get(0);
  			
  			secondaryRoute = FixedDoubleRouteBacktracking.disjointShortestPath(primaryRoute, cp.getMesh());
  			secondaryMod = modulationSelection.getAvaliableModulations().get(0);
  		}
  		
  		circuit.setQoTForOther(QoTforOther);
  		
  		// rota de trabalho
  		circuit.setRoute(primaryRoute);
  		circuit.setModulation(primaryMod);
  		circuit.setSpectrumAssigned(primarySa);
  		
  		//rota de backup
  		((SurvivalCircuit)circuit).setBackupRoute(secondaryRoute);
    	((SurvivalCircuit)circuit).setModulationByBackupRoute(secondaryMod);
		((SurvivalCircuit)circuit).setSpectrumAssignedByBackupRoute(secondarySa);
    }

	private boolean isAdmissibleQoTOther(Circuit circuit, InfoRoute infoWorkRoute, InfoRoute infoBackupRoute, ControlPlane cp){
		boolean QoTForOther = true;
		
		//rota de trabalho
    	circuit.setRoute(infoWorkRoute.getRoute());
    	circuit.setModulation(infoWorkRoute.getMod());
    	circuit.setSpectrumAssigned(infoWorkRoute.getBand());
    	
    	//rota de backup
    	((SurvivalCircuit)circuit).setBackupRoute(infoBackupRoute.getRoute());
    	((SurvivalCircuit)circuit).setModulationByBackupRoute(infoBackupRoute.getMod());
		((SurvivalCircuit)circuit).setSpectrumAssignedByBackupRoute(infoBackupRoute.getBand());    	
        
		try {
			cp.allocateCircuit(circuit);
			
			QoTForOther = cp.computeQoTForOther(circuit);
			
			cp.releaseCircuit(circuit);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return QoTForOther;
	}
    
    /**
	 * Returns the routing algorithm
	 * 
	 * @return KRoutingAlgorithmInterface
	 */
    public KRoutingAlgorithmInterface getRoutingAlgorithm(){
    	return kShortestsPaths;
    }
    
    private InfoRoute checkRoute(Circuit circuit, Route route, ControlPlane cp){
    	
    	InfoRoute infoRoute = null;
    	
    	//para guarda as informacoes relacionadas com a modulacao escolhida
		Modulation chosenMod = null;
		int chosenBand[] = null;
		boolean chosenQoTMod = false;
		double lowestMetricPC = 0.0;
		double snr = 0.0;
		
		//segunda opcao
		Modulation chosenModAux = null;
		int chosenBandAux[] = null;
		boolean chosenQoTModAux = false;
		double lowestMetricPCAux = 0.0;
		double snrAux = 0.0;
		double highestLevel = 0.0;
		
		//para evitar erro nas metricas
		Route checkRoute = null;
		Modulation checkMod = null;
		int checkBand[] = null;
		
		//seta a rota da requisicao
		circuit.setRoute(route);
		
		List<Modulation> avaliableModulations = modulationSelection.getAvaliableModulations();
    	
    	for(Modulation mod : avaliableModulations){
        	circuit.setModulation(mod);
        	
        	int slotsNumber = mod.requiredSlots(circuit.getRequiredBandwidth());
            List<int[]> merge = IntersectionFreeSpectrum.merge(route);
            
            int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
            circuit.setSpectrumAssigned(band);

            if (band != null) {
            	if(checkRoute == null){
            		checkRoute = route;
            		checkMod = mod;
            		checkBand = band;
            	}
            	
            	boolean flagQoT = cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, mod, band);
            	if(flagQoT){ //modulation has acceptable QoT
            		
            		double deltaSNR = circuit.getSNR() - mod.getSNRthreshold();
            		
            		double metricPC = EnergyConsumption.computePowerConsumptionBySegment(cp, circuit, circuit.getRequiredBandwidth(), route, 0, route.getNodeList().size() - 1, mod, band);
            		
            		if(deltaSNR >= sigma){
		                chosenMod = mod;
		                chosenBand = band;
		                chosenQoTMod = flagQoT;
		                snr = circuit.getSNR();
		                lowestMetricPC = metricPC;
            		}
            		
            		if(mod.getBitsPerSymbol() > highestLevel){
            			chosenModAux = mod;
		                chosenBandAux = band;
		                chosenQoTModAux = flagQoT;
		                snrAux = circuit.getSNR();
		                lowestMetricPCAux = metricPC;
		                
		                highestLevel = mod.getBitsPerSymbol();
            		}
            	}
            }
        }
    	
    	if(chosenMod == null){
    		chosenMod = chosenModAux;
            chosenBand = chosenBandAux;
            chosenQoTMod = chosenQoTModAux;
            snr = snrAux;
            lowestMetricPC = lowestMetricPCAux;
    	}
    	
    	if(chosenMod != null){
			infoRoute = new InfoRoute(route, chosenMod, chosenBand, lowestMetricPC, chosenQoTMod, snr);
		}
		
		if(infoRoute == null){
			infoRoute = new InfoRoute();
		}
		infoRoute.setCheckRoute(checkRoute);
		infoRoute.setCheckMod(checkMod);
		infoRoute.setCheckBand(checkBand);
    	
    	return infoRoute;
    }
    
    protected class InfoRoute {
    	
    	private Route route;
		private Modulation mod;
		private int band[];
		private double powerConsumption;
		private boolean QoT;
		private double snr;
		
		private Route checkRoute;
		private Modulation checkMod;
		private int checkBand[];
		
		public InfoRoute(){
			this.route = null;
		}
		
		public InfoRoute(Route route, Modulation mod, int band[], double powerConsumption, boolean QoT, double snr){
			this.route = route;
			this.mod = mod;
			this.band = band;
			this.powerConsumption = powerConsumption;
			this.QoT = QoT;
			this.snr = snr;
		}

		/**
		 * @return the route
		 */
		public Route getRoute() {
			return route;
		}

		/**
		 * @param route the route to set
		 */
		public void setRoute(Route route) {
			this.route = route;
		}

		/**
		 * @return the mod
		 */
		public Modulation getMod() {
			return mod;
		}

		/**
		 * @param mod the mod to set
		 */
		public void setMod(Modulation mod) {
			this.mod = mod;
		}

		/**
		 * @return the band
		 */
		public int[] getBand() {
			return band;
		}

		/**
		 * @param band the band to set
		 */
		public void setBand(int[] band) {
			this.band = band;
		}

		/**
		 * @return the powerConsumption
		 */
		public double getPowerConsumption() {
			return powerConsumption;
		}

		/**
		 * @param powerConsumption the powerConsumption to set
		 */
		public void setPowerConsumption(double powerConsumption) {
			this.powerConsumption = powerConsumption;
		}

		/**
		 * @return the qoT
		 */
		public boolean isQoT() {
			return QoT;
		}

		/**
		 * @param qoT the qoT to set
		 */
		public void setQoT(boolean qoT) {
			QoT = qoT;
		}

		/**
		 * @return the snr
		 */
		public double getSnr() {
			return snr;
		}

		/**
		 * @param snr the snr to set
		 */
		public void setSnr(double snr) {
			this.snr = snr;
		}

		/**
		 * @return the checkRoute
		 */
		public Route getCheckRoute() {
			return checkRoute;
		}

		/**
		 * @param checkRoute the checkRoute to set
		 */
		public void setCheckRoute(Route checkRoute) {
			this.checkRoute = checkRoute;
		}

		/**
		 * @return the checkMod
		 */
		public Modulation getCheckMod() {
			return checkMod;
		}

		/**
		 * @param checkMod the checkMod to set
		 */
		public void setCheckMod(Modulation checkMod) {
			this.checkMod = checkMod;
		}

		/**
		 * @return the checkBand
		 */
		public int[] getCheckBand() {
			return checkBand;
		}

		/**
		 * @param checkBand the checkBand to set
		 */
		public void setCheckBand(int[] checkBand) {
			this.checkBand = checkBand;
		}
    	
    }
    
    protected class InfoRoutes {
    	
    	private InfoRoute primaryRoute;
		private InfoRoute secondaryRoute;
		private boolean QoTOther;
		
		public InfoRoutes(InfoRoute primaryRoute, InfoRoute secondaryRoute, boolean QoTOther){
			this.primaryRoute = primaryRoute;
			this.secondaryRoute = secondaryRoute;
			this.QoTOther = QoTOther;
		}
		
		public InfoRoute getPrimaryRoute(){
			return primaryRoute;
		}
		
		public InfoRoute getSecondaryRoute(){
			return secondaryRoute;
		}
		
		public boolean isQoTOther(){
			return QoTOther;
		}
    }
    
    @Override
	public boolean thereAreFreeTransponders(Circuit circuit){
    	
        // Need two transmitters and two receivers because it is dedicated protection
        ((SurvivalCircuit)circuit).setRequiredNumberOfTxs(2);
        ((SurvivalCircuit)circuit).setRequiredNumberOfRxs(2);
        
    	
		// precisa de dois transmissores e dois receptores
    	int txs = ((SurvivalCircuit)circuit).getRequiredNumberOfTxs();
		int rxs = ((SurvivalCircuit)circuit).getRequiredNumberOfRxs();
    	
		if(circuit.getSource().getTxs().hasEnoughFreeTransmitters(txs) && circuit.getDestination().getRxs().hasEnoughFreeTransmitters(rxs)){
			return true;
		}
		
    	return false;
    }
	
	@Override
	public boolean survive(Circuit circuit) throws Exception {
		
		
		return true;
	}
	
	private void computeBackupRoutes(List<Route> candidateRoutes, ControlPlane cp){
		if(backupRoutes == null){
			backupRoutes = new HashMap<>();
		}
		
		for (Route primaryRoute : candidateRoutes) {
			
			if(!backupRoutes.containsKey(primaryRoute)){
				Route secondaryRoute = FixedDoubleRouteBacktracking.disjointShortestPath(primaryRoute, cp.getMesh());
				
				if(secondaryRoute == null){
					secondaryRoute = FixedDoubleRouteBacktracking.computeBacktrackingByRoute(primaryRoute, cp.getMesh());
				}
				
				backupRoutes.put(primaryRoute, secondaryRoute);
			}
		}
	}
}
