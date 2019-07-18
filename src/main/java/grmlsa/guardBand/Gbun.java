package grmlsa.guardBand;

import java.util.List;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.integrated.IntegratedRMLSAAlgorithmInterface;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import simulationControl.parsers.NetworkConfig;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the implementation of the GBUN algorithm
 * presented in the article: - Novo algoritmo para Provisão de Banda
 * de Guarda Adaptativa em Redes Ópticas Elásticas (2019)
 * 
 * In the GBUN the size of guard band is selected based on the level
 * of the network utilization.
 * 
 * @authors Neclyeux, Alexandre, Iallen, Antonio costa, Divanilson Campelo, André Soares
 */
public class Gbun implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    public static int maxSaltos = 0;
    public static double maxUtilizacao = 0;
    private static final String DIV = "-";
    static String filename = "simulations/Cost239_v2_GBUN/tipper.fcl";
    
    @Override
    public boolean rsa(Circuit circuit, ControlPlane cp) {
        if (kShortestsPaths == null){
        	kShortestsPaths = new NewKShortestPaths(cp.getMesh(), k); //This algorithm uses 3 alternative paths
        }
        if (modulationSelection == null){
        	modulationSelection = cp.getModulationSelection();
        }
        if(spectrumAssignment == null){
			spectrumAssignment = new FirstFit();
		}
        
        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached
        
        for (Route route : candidateRoutes) {
            
            circuit.setRoute(route);
            
            List<Modulation> modulacoes = cp.getMesh().getAvaliableModulations();
            
            //GBUN - Cost239
            for (int m = 0; m < modulacoes.size(); m++) {
            	if(UtilizacaoGeral(cp.getMesh()) >= 0.266)
            		modulacoes.get(m).setGuardBand(1);
                if(UtilizacaoGeral(cp.getMesh()) < 0.266 && UtilizacaoGeral(cp.getMesh()) >= 0.228)
                	modulacoes.get(m).setGuardBand(2);
                if(UtilizacaoGeral(cp.getMesh()) < 0.228 && UtilizacaoGeral(cp.getMesh()) >= 0.19)
                	modulacoes.get(m).setGuardBand(3);
                if(UtilizacaoGeral(cp.getMesh()) < 0.19 && UtilizacaoGeral(cp.getMesh()) >= 0.152)
                	modulacoes.get(m).setGuardBand(4);
                if(UtilizacaoGeral(cp.getMesh()) < 0.152 && UtilizacaoGeral(cp.getMesh()) >= 0.114)
                	modulacoes.get(m).setGuardBand(5);
                if(UtilizacaoGeral(cp.getMesh()) < 0.114 && UtilizacaoGeral(cp.getMesh()) >= 0.076)
                	modulacoes.get(m).setGuardBand(6);
                if(UtilizacaoGeral(cp.getMesh()) < 0.076 && UtilizacaoGeral(cp.getMesh()) >= 0.038)
                	modulacoes.get(m).setGuardBand(7);
                if(UtilizacaoGeral(cp.getMesh()) < 0.038)
                	modulacoes.get(m).setGuardBand(8);
            }
            cp.getMesh().setAvaliableModulations(modulacoes);
            
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            Modulation mod1 = null;
            try {
				mod1 = (Modulation) mod.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            circuit.setModulation(mod1);
            
            if(mod1 != null){
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, mod1.getGuardBand());
	
	            // Calculate how many slots are needed for this route
	            int ff[] = spectrumAssignment.policy(mod1.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit, cp);
	            
		            if (ff != null && ff[0] < chosenBand[0]) {
		                chosenBand = ff;
		                chosenRoute = route;
		                chosenMod = mod1;
		        
		            }
            }
        }
        
        if(UtilizacaoGeral(cp.getMesh()) > maxUtilizacao) {
        	maxUtilizacao = UtilizacaoGeral(cp.getMesh());
        	System.out.println("Utilizacao Maxima: " + maxUtilizacao);
        }

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);
            
            return true;

        } else {
            circuit.setRoute(candidateRoutes.get(0));
            circuit.setModulation(cp.getMesh().getAvaliableModulations().get(0));
            circuit.setSpectrumAssigned(null);
            
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

    /**
     * Returns the number of busy slots in the current route
     * 
     * @param rotaAtual 
     */
    private int quantidadeSlotsOcupadosNaRota(Route rotaAtual, int guardBand){
        int totalLivre = 0, intervaloAtual = 0;
        List<int[]> composicao;
        int totalSlots = rotaAtual.getLink(0).getNumOfSlots();

        composicao = IntersectionFreeSpectrum.merge(rotaAtual, guardBand);

        for(int[] intervalo : composicao){
            intervaloAtual = intervalo[1] - intervalo[0] + 1;
            totalLivre += intervaloAtual;    
        }

        return totalSlots - totalLivre;

    }
    
    /**
     * Returns the total usage of the topology 
     * 
     * @param mesh
     */
    private double UtilizacaoGeral(Mesh mesh) {
        Double utGeral = 0.0;
        for (Link link : mesh.getLinkList()) {
            utGeral += link.getUtilization();
        }

        utGeral = utGeral / (double) mesh.getLinkList().size();

        return utGeral;
    }


}


