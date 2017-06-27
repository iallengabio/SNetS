package util.tools.artificialBeeColony;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import grmlsa.integrated.KShortestPathsReductionQoTO;
import simulationControl.Main;

public class MainABC {
	
	private static String pathFileOfSimulation;
	private static String pathFileOfSigma;
	private static String separador;
	private static int numberOfNodes;

	public static void main(String[] args) {
		
		separador = System.getProperty("file.separator");
		pathFileOfSimulation = "C:" + separador + "Users" + separador + "Alexandre" + separador + "src" + separador + "workspace" + separador + "SNetS" + separador + "simulations" + separador + "A6NET";
		pathFileOfSigma = pathFileOfSimulation + separador + "sigmaForAllPairs.txt";
		
		numberOfNodes = 6;
		int n = numberOfNodes * (numberOfNodes - 1); //quantidade de pares
		double minX = 0.0; //valor minimo para o sigma
		double maxX = 1.0; //valor maximo para o sigma
		
		ArtificialBeeColony abc = new ArtificialBeeColony(n, minX, maxX);
		
		abc.execute();
		
	}
	
	public static double runSimulation(double sigmaAllPairs[]) {
		Double result = 0.0;
		
		KShortestPathsReductionQoTO.createFileSigmaAllPairs(numberOfNodes, pathFileOfSigma, sigmaAllPairs);
		
		String[] args = new String[1];
		args[0] = pathFileOfSimulation;
		
		try {
			
			Main.main(args);
			result = computesBP();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static double computesBP() {
		String nomeDaPasta = "A6NET";
		String nomeDoArquivoLeitura = nomeDaPasta + "_BlockingProbability.csv";
		String caminhoArqOrigem = pathFileOfSimulation + separador + nomeDoArquivoLeitura;
		String metrica = "blocking probability";
		
		int quantReplicacoes = 3;
		double bp[] = new double[quantReplicacoes];
		
		try{
			FileReader fr = new FileReader(caminhoArqOrigem);
			BufferedReader in = new BufferedReader(fr);
		    
			while(in.ready()){
				String[] linha = in.readLine().split(",");
				
				if((linha.length >= 5) && linha[0].equals(metrica)){
					
					for(int r = 0; r < quantReplicacoes; r++){
						bp[r] = Double.parseDouble(linha[6 + r]);
					}
					
					break;
				}
		    }
			
		    in.close();
		    fr.close();
		} catch (IOException e) {
		    e.printStackTrace();
	    }
		
		double sumBP = 0.0;
		for(int r = 0; r < quantReplicacoes; r++){
			sumBP += bp[r];
		}
		double averageBP = sumBP / quantReplicacoes;
		
		return averageBP;
	}
}
