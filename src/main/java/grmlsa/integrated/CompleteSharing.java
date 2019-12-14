package grmlsa.integrated;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;

import grmlsa.KRoutingAlgorithmInterface;
import grmlsa.NewKShortestPaths;
import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.FirstFit;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import util.IntersectionFreeSpectrum;

/**
 * This class represents the implementation of the Complete Sharing algorithm presented in the article:
 *  - Spectrum management in heterogeneous bandwidth optical networks (2014)
 *  
 * In the Complete Sharing the route and the frequency slots are selected in order to allocate a range of 
 * spectrum closer to the beginning of the optical spectrum.
 * 
 * @author Iallen
 */
public class CompleteSharing implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    public static double maxUtilizacaoEnlace = 0;
    public static double maxUtilizacaoTopologia = 0;
    public static double maxSaltosTopologia = 0;
    public static double minTamanhoRota = 999;
    public static double maxTamanhoRota = 0;
    public static double requisicoes_total = 0;
    //public static Link link = null;
    
    //public static String[] cabecalho = {"slotsUsados", "slotsTotal", "quantidadeSaltos", "modulacao", "tipoBloqueio", "bandaDeGuarda"};
    //public static List<String[]> linhas = new ArrayList<>();

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
        
        requisicoes_total++;
        System.out.println("Requisicoes total: " + requisicoes_total);

        List<Route> candidateRoutes = kShortestsPaths.getRoutes(circuit.getSource(), circuit.getDestination());
        Route chosenRoute = null;
        Modulation chosenMod = null;
        int chosenBand[] = {999999, 999999}; // Value never reached

        /*if(UtilizacaoGeral(cp.getMesh()) > maxUtilizacaoTopologia) {
        	maxUtilizacaoTopologia = UtilizacaoGeral(cp.getMesh());
        	System.out.println("Utilizacao maxima topologia: " + maxUtilizacaoTopologia);
        }*/
        
        for (Route route : candidateRoutes) {
        	
        	/*
        	if(maxUtilizationRouteLink(route) > maxUtilizacaoEnlace) {
        		maxUtilizacaoEnlace = maxUtilizationRouteLink(route);
        	}
        	
        	
        	if(route.getHops() > maxSaltosTopologia) {
        		maxSaltosTopologia = route.getHops();
        	}
        	
        	if(route.getDistanceAllLinks() > maxTamanhoRota) {
        		maxTamanhoRota = route.getDistanceAllLinks();
        	}
        	
        	if(route.getDistanceAllLinks() < minTamanhoRota) {
        		minTamanhoRota = route.getDistanceAllLinks();
        	}*/
        	
            circuit.setRoute(route);
            
            Modulation mod = modulationSelection.selectModulation(circuit, route, spectrumAssignment, cp);
            circuit.setModulation(mod);
            
            if(mod != null){
	            List<int[]> merge = IntersectionFreeSpectrum.merge(route, circuit.getGuardBand());
	            
	            // Calculate how many slots are needed for this route
	            int ff[] = spectrumAssignment.policy(mod.requiredSlots(circuit.getRequiredBandwidth()), merge, circuit, cp);
	
	            if (ff != null && ff[0] < chosenBand[0]) {
	                chosenBand = ff;
	                chosenRoute = route;
	                chosenMod = mod;
	            }
            }
        }

        
        //System.out.println("#######################################################");
        //System.out.println("Utilizacao maxima Topologia: " + maxUtilizacaoTopologia);
        //System.out.println("Maxima Utilizacao Enlace: " + maxUtilizacaoEnlace);
        //System.out.println("Tamanho de rota minimo: " + minTamanhoRota);
        //System.out.println("Tamanho de rota maximo: " + maxTamanhoRota);
        
        //PythonInterpreter interpreter = new PythonInterpreter();
        //interpreter.execfile("/Users/Neclyeux/Documents/SNetS/simulations/Cost239Python/soma.py");
        //PyObject str = interpreter.eval("repr(soma(5,2))");
        //System.out.println(str.toString());
       
        
        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);
            
            //link = linkMostUsed(chosenRoute);
            
            //linhas.add(new String[]{Integer.toString(link.getUsedSlots()), Integer.toString(link.getNumOfSlots()), Integer.toString(chosenRoute.getHops()), Double.toString(circuit.getModulation().getM()),Integer.toString(circuit.getBlockCause()),Integer.toString(circuit.getModulation().getGuardBand())});
                        
            return true;

        } else {
            circuit.setRoute(candidateRoutes.get(0));
            circuit.setModulation(cp.getMesh().getAvaliableModulations().get(0));
            circuit.setSpectrumAssigned(null);
            
            //linhas.add(new String[]{Integer.toString(link.getUsedSlots()), Integer.toString(link.getNumOfSlots()), Double.toString(circuit.getModulation().getM()),Integer.toString(circuit.getBlockCause()),Integer.toString(circuit.getModulation().getGuardBand())});
                        
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
     * Returns the maximum utilization found on the links of a route.
     * 
     * @param rota
     * @return maxUtilizationRouteLink
     */
    private double maxUtilizationRouteLink (Route route) {
    	
    	double maxUtilizationRouteLink = 0.0;
    	
    	for(Link link: route.getLinkList()) {
    		if(link.getUtilization() > maxUtilizationRouteLink) {
    			maxUtilizationRouteLink = link.getUtilization();
    		}
    	}
    	
    	return maxUtilizationRouteLink;
    }
    
    /**
     * Returns the most used link of a route.
     * 
     * @param rota
     * @return maxUtilizationRouteLink
     */
    private Link linkMostUsed (Route route) {
    	
    	Link maxLink = null;
    	double maxUtilizationRouteLink = 0.0;
    	
    	for(Link link: route.getLinkList()) {
    		if(link.getUtilization() >= maxUtilizationRouteLink) {
    			maxUtilizationRouteLink = link.getUtilization();
    			maxLink = link;
    		}
    	}
    	
    	return maxLink;
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
    
    
    /*public static void SalvarCSV() {
    	
    	Writer writer = null;
        try {
        	writer = Files.newBufferedWriter(Paths.get("baseNSFNetComBloqueio.csv"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        CSVWriter csvWriter = new CSVWriter(writer);            

        csvWriter.writeNext(cabecalho);
        csvWriter.writeAll(linhas);

        try {
			csvWriter.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }*/
   
    
}
