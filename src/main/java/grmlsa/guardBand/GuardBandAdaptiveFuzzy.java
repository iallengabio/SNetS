package grmlsa.guardBand;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
import util.IntersectionFreeSpectrum;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Rule;

/**
 * This class represents the implementation of an adaptive guard band algorithm
 * using Fuzzy Logic.
 * 
 * Under construction.
 * 
 * @author Neclyeux, Alexandre
 */
public class GuardBandAdaptiveFuzzy implements IntegratedRMLSAAlgorithmInterface {

	private int k = 3; //This algorithm uses 3 alternative paths
    private KRoutingAlgorithmInterface kShortestsPaths;
    private ModulationSelectionAlgorithmInterface modulationSelection;
    private SpectrumAssignmentAlgorithmInterface spectrumAssignment;
    public static double maxValFuzzy = 0;
    public static double menorValFuzzy = 999;
    public static int menorM = 999;
    public static int maiorM = 0;
    public static double maxUtilizacaoRede = 0;
    public static double maxUtilizacaoRota = 0;
    public static long tempoInicio = System.currentTimeMillis();
    private static final String DIV = "-";
    static String filename = "simulations/SPAN80/FUZZY/NSFNet_v3_FUZZY/tipper.fcl";
    private TreeSet<Circuit> connectionList = new TreeSet<>();
    private Iterator<Circuit> iterator = null;
    private Circuit circuito = null;
    
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
        List<Modulation> avaliableModulations = cp.getMesh().getAvaliableModulations();
        Route checkRoute = null;
  		Modulation checkMod = null;
  		int checkBand[] = null;
        
        FIS fis = FIS.load(filename, true);

        if(fis == null){
            System.err.println("Can't load file '" + filename + "'");
            System.exit(1);
        }

        double GuardBand = 0;

        FunctionBlock fb = fis.getFunctionBlock(null);
        
        //connectionList = cp.getConnections();
        //iterator = connectionList.iterator();

        for (Route route : candidateRoutes) {
            circuit.setRoute(route);
            
            /*select guard band before select the modulation*/
            
            for (int m = avaliableModulations.size()-1; m >= 0; m--) {
                Modulation mod = avaliableModulations.get(m);
                
                fb.setVariable("utilizacaoRota", utilizacaoRota(route));
                fb.setVariable("eficienciaEspectral", mod.getM());
                
                fb.evaluate();
                fb.getVariable("bandaGuarda").defuzzify();
                GuardBand = fb.getVariable("bandaGuarda").getValue();
                
                /*if(utilizacaoRota(route) > 0.51) {
                	System.out.println("#################################");
                	System.out.println("Route Utilization: " + utilizacaoRota(route));
                	System.out.println("Modulation: " + mod.getM());
                	System.out.println("Guard Band chosen: " + (int)GuardBand);
                }*/
                
                Modulation modClone = null;
                
                try {
                	modClone = (Modulation) mod.clone();
                	modClone.setGuardBand((int)GuardBand);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                circuit.setModulation(modClone);

                int slotsNumber = modClone.requiredSlots(circuit.getRequiredBandwidth());
                List<int[]> merge = IntersectionFreeSpectrum.merge(route, modClone.getGuardBand());
                
                int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
                circuit.setSpectrumAssigned(band);
    
                if (band != null && band[0] < chosenBand[0]) {
                    checkRoute = route;
                    checkMod = modClone;
                    checkBand = band;
                    
                    if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modClone, band, null, false)){ //modulation has acceptable QoT
                        chosenRoute = route;
                        chosenBand = band;
                        chosenMod = modClone;
                    }
                }
            }
            
            /*if(utilizacaoRota(route) > maxUtilizacaoRota) {
            	maxUtilizacaoRota = utilizacaoRota(route);
            	System.out.println("Utilizacao Maxima Rota: " + maxUtilizacaoRota);
            }*/

        }
        
        /*if(UtilizacaoGeral(cp.getMesh()) > maxUtilizacaoRede) {
        	maxUtilizacaoRede = UtilizacaoGeral(cp.getMesh());
        	System.out.println("Utilizacao Maxima Rede: " + maxUtilizacaoRede);
        }*/

        if (chosenRoute != null) { //If there is no route chosen is why no available resource was found on any of the candidate routes
            circuit.setRoute(chosenRoute);
            circuit.setModulation(chosenMod);
            circuit.setSpectrumAssigned(chosenBand);
            
            return true;

        } else {
        	if(checkRoute == null) {
        		checkRoute = candidateRoutes.get(0);
        		checkMod = avaliableModulations.get(0);
        	}
        	
            circuit.setRoute(checkRoute);
            circuit.setModulation(checkMod);
            circuit.setSpectrumAssigned(checkBand);
            
            /*System.out.println("Lista de Conexões ativas: ");
            while(iterator.hasNext()) {
            	circuito = null;
            	circuito = iterator.next();
            	System.out.print("Rota: ");
            	circuito.getRoute().printRoute();
            	System.out.print(" - Qot: " + circuito.isQoT());
            	System.out.println(" - QoTN: " + circuito.isQoTForOther());
            }
            
            System.out.println("Causa do bloqueio: " + circuit.getBlockCause());*/
            
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
    
    /**
     * Returns the total usage of the route
     * 
     * @param rota
     * @return
     */
    private double utilizacaoRota (Route rota) {
    	double utGeral = 0.0;
    	for(Link link: rota.getLinkList()) {
    		utGeral += link.getUtilization();
    	}
    	utGeral = utGeral / rota.getLinkList().size();
    	return utGeral;
    }

}


