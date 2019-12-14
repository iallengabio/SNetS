package grmlsa.guardBand;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import grmlsa.Route;
import grmlsa.modulation.Modulation;
import grmlsa.modulation.ModulationSelectionAlgorithmInterface;
import grmlsa.spectrumAssignment.SpectrumAssignmentAlgorithmInterface;
import network.Circuit;
import network.ControlPlane;
import network.Link;
import network.Mesh;
import util.IntersectionFreeSpectrum;


public class ModulationAndGuardBandSelectionByDeepLearning implements ModulationSelectionAlgorithmInterface {

	private List<Modulation> avaliableModulations;
	

	@Override
	public Modulation selectModulation(Circuit circuit, Route route,
			SpectrumAssignmentAlgorithmInterface spectrumAssignment, ControlPlane cp) {
		Route checkRoute = null;
		Route chosenRoute = null;

		Modulation checkMod = null;
		Modulation chosenMod = null;

		int checkBand[] = null;
		int chosenBand[] = { 999999, 999999 }; // Value never reached
		
		Link link = null;

		if (avaliableModulations == null) {
			avaliableModulations = cp.getMesh().getAvaliableModulations();
		}

		int guardBand = 0;

		circuit.setRoute(route);
		
		
        // Test basic inference on the model.
        //INDArray input = Nd4j.create(256, 100);
        //INDArray output = model.output(input);

		//int m = avaliableModulations.size() - 1; m >= 0; m-- -> decrescente
		//int m = 0; m < avaliableModulations.size(); m++ --> crescente
		for (int m = 0; m < avaliableModulations.size(); m++) {
			
			Modulation mod = avaliableModulations.get(m);
			

			// PythonInterpreter interpreter = new PythonInterpreter();
			// interpreter.execfile("/Users/Neclyeux/Documents/SNetS/simulations/Cost239Python/teste.py");
			// PyObject str = interpreter.eval("repr(executar(" +
			// maxUtilizationRouteLink(route) + "," + mod.getM() + "))");

			// guardBand = Double.valueOf(str.toString());

			/*String line = "";
			try {
				//String comando = "cmd.exe /c python C:\\Users\\Neclyeux\\Documents\\SNetS\\simulations\\Cost239Python\\teste2.py "
						//+ maxUtilizationRouteLink(route) + " " + mod.getM();
				String comando = "cmd.exe /c python C:\\\\Users\\\\Neclyeux\\\\Desktop\\\\Socket\\client.py "
					+ maxUtilizationRouteLink(route) + " " + mod.getM();
				//System.out.println(comando);
				Process p = Runtime.getRuntime().exec(comando);

				// pega o retorno do processo
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
				// printa o retorno
				//System.out.println(stdInput.readLine());
				while ((line = stdInput.readLine()) != null) {
					guardBand = Integer.parseInt(line);
					//System.out.println(guardBand);
				}
				stdInput.close();
				
			} catch (IOException ex) {
				System.out.println(ex);
			}*/
			
			String mensagem = "";
			link = linkMostUsed(route);
			/*para MLP*/
			mensagem = link.getUsedSlots() + "/" + link.getUtilization() + "/" + link.getCircuitList().size() + "/" + route.getHops() + "/" + numberOfFreeSlots(route) + "/" + UtilizacaoGeral(cp.getMesh()) + "/" + mod.getM() + "/" + circuit.getSNR();
			/*para CONV*/
			//mensagem = link.getUsedSlots() + "/" + link.getCircuitList().size() + "/" + route.getHops() + "/" + mod.getM();
			//System.out.println(mensagem);
			//mensagem = link.getUtilization() +"/"+ mod.getM();
			// Conectando com o servidor
			Socket cliente = null;
			try {
				cliente = new Socket("127.0.0.1", 7000);
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Enviando dados para o servidor
			PrintStream saida = null;
			try {
				saida = new PrintStream(cliente.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			saida.println(mensagem);

			//Pegando retorno do servidor
			Scanner s = null;
			try {
				s = new Scanner(cliente.getInputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			guardBand = Integer.parseInt(s.nextLine());
			//System.out.println(mensagem);
			//System.out.println(guardBand);
			
			// Fechando a conexão
			s.close();
			saida.close();
			try {
				cliente.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
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

			int slotsNumber = modClone.requiredSlots(circuit.getRequiredBandwidth());
			List<int[]> merge = IntersectionFreeSpectrum.merge(route, modClone.getGuardBand());

			int band[] = spectrumAssignment.policy(slotsNumber, merge, circuit, cp);
			circuit.setSpectrumAssigned(band);

			if (band != null && band[0] < chosenBand[0]) {
				checkRoute = route;
				checkMod = modClone;
				checkBand = band;

				if (cp.getMesh().getPhysicalLayer().isAdmissibleModultion(circuit, route, modClone, band, null, false)) { // modulation has acceptable QoT
					chosenRoute = route;
					chosenBand = band;
					chosenMod = modClone;
				}
			}
		}

		if (chosenMod == null) { // QoT is not enough for all modulations
			chosenMod = avaliableModulations.get(0); // To avoid metric error
			chosenBand = null;
		}

		// Configures the circuit information. They can be used by the method that requested the modulation selection
		circuit.setModulation(chosenMod);
		circuit.setSpectrumAssigned(chosenBand);
		// System.out.println("GB:" + chosenMod.getGuardBand());
		return chosenMod;

	}

	/**
	 * Returns the maximum utilization found on the links of a route.
	 * 
	 * @param rota
	 * @return maxUtilizationRouteLink
	 */
	private double maxUtilizationRouteLink(Route route) {

		double maxUtilizationRouteLink = 0.0;

		for (Link link : route.getLinkList()) {
			if (link.getUtilization() > maxUtilizationRouteLink) {
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
