package grmlsa.guardBand;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import util.IntersectionFreeSpectrum;

public class ModulationAndGuardBandSelectionByDeepLearning_v2 implements ModulationSelectionAlgorithmInterface {
	
	private List<Modulation> avaliableModulations;
	
	@Override
	public Modulation selectModulation(Circuit circuit, Route route, SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		
		if(avaliableModulations == null) {
			avaliableModulations = cp.getMesh().getAvaliableModulations();
		}
		
		boolean flagQoT = false; // Assuming that the circuit QoT starts as not acceptable
		
		// Modulation and spectrum selected
		Modulation chosenMod = null;
		int chosenBand[] = {999999, 999999}; // Value never reached
		
		// Modulation which at least allocates spectrum, used to avoid error in metrics
		Modulation alternativeMod = null;
		int alternativeBand[] = null;
		
		Link link = null;
		int guardBand = 0;
		
		// Begins with the most spectrally efficient modulation format
		for (int m = avaliableModulations.size()-1; m >= 0; m--) {
			Modulation mod = avaliableModulations.get(m);
			
			String mensagem = "";
			link = linkMostUsed(route);
			/*for MLP*/
			mensagem = link.getUsedSlots() + "/" + link.getUtilization() + "/" + link.getCircuitList().size() + "/" + route.getHops() + "/" + numberOfFreeSlots(route) + "/" + UtilizacaoGeral(cp.getMesh()) + "/" + mod.getM() + "/" + circuit.getSNR();
			/*for CONV*/
			//mensagem = link.getUsedSlots() + "/" + link.getCircuitList().size() + "/" + route.getHops() + "/" + mod.getM();
			//System.out.println(mensagem);
			//mensagem = link.getUtilization() +"/"+ mod.getM();
			
			// Connecting to the server
			Socket cliente = null;
			try {
				cliente = new Socket("127.0.0.1", 7000);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// Sending data to server
			PrintStream saida = null;
			try {
				saida = new PrintStream(cliente.getOutputStream());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			saida.println(mensagem);
			
			// Getting feedback from server
			Scanner s = null;
			try {
				s = new Scanner(cliente.getInputStream());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			guardBand = Integer.parseInt(s.nextLine());
			//System.out.println(mensagem);
			//System.out.println(guardBand);
			
			// Closing the connection
			s.close();
			saida.close();
			try {
				cliente.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
            Modulation modClone = null;
            try {
				modClone = (Modulation) mod.clone();
				modClone.setGuardBand(guardBand);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			circuit.setModulation(modClone);
			
			int numberOfSlots = modClone.requiredSlots(circuit.getRequiredBandwidth());
			List<int[]> merge = IntersectionFreeSpectrum.merge(route, modClone.getGuardBand());
			
			int band[] = spectrumAssignment.policy(numberOfSlots, merge, circuit, cp);
			circuit.setSpectrumAssigned(band);
			
			if (band != null && band[0] < chosenBand[0]) {
				alternativeMod = modClone; // The last modulation that was able to allocate spectrum
				alternativeBand = band;
				
				if(cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modClone, band, null, false)){
					chosenMod = modClone; // Save the modulation that has admissible QoT
					chosenBand = band;
					
					flagQoT = true;
					
					break; // Stop when a modulation reaches admissible QoT
				}
			}
		}
		
		if(chosenMod == null){ // QoT is not enough for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
			
			if(alternativeMod != null){ // Allocated spectrum using some modulation, but the QoT was inadmissible 
				chosenMod = alternativeMod;
				chosenBand = alternativeBand;
			}
		}
		
		// Configures the circuit information. They can be used by the method that requested the modulation selection
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		circuit.setQoT(flagQoT);
		
		return chosenMod;
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
    
    private int numberOfFreeSlots (Route route) {
    	
    	int numberOfFreeSlots = 0;
    	
    	for(Link link: route.getLinkList()) {
    		numberOfFreeSlots += link.getNumOfSlots() - link.getUsedSlots();
    	}
    	
    	return numberOfFreeSlots;
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
