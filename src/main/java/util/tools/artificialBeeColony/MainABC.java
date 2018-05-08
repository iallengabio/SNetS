package util.tools.artificialBeeColony;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import grmlsa.integrated.KShortestPathsReductionQoTO_v1;
import simulationControl.Main;

public class MainABC {
	
	private static String pathFileOfSimulation;
	private static String pathFileOfSigma;
	private static String separator;
	private static String folderName;
	private static int numberOfNodes;
	private static int numberOfReplications;
	
	public static void main(String[] args) {
		
		separator = System.getProperty("file.separator");
		folderName = "A6NET_teste_AABC_carga_100";
		pathFileOfSimulation = "C:" + separator + "Users" + separator + "Alexandre" + separator + "src" + separator + "workspace" + separator + "SNetS" + separator + "simulations" + separator + "sbrc2018" + separator + folderName;
		pathFileOfSigma = pathFileOfSimulation + separator + "sigmaForAllPairs.txt";
		
		numberOfReplications = 2;
		
		numberOfNodes = 6;
		int n = numberOfNodes * (numberOfNodes - 1); //number of pairs
		double minX = 0.0; //minimum value for sigma
		double maxX = 1.0; //maximum value for sigma
		int k = 3; //Number of candidate routes
		
		//ArtificialBeeColony abc = new ArtificialBeeColony(n, minX, maxX);
		//abc.execute();
		//double sigmaAllPairs[] = abc.getgBest().getNectar();
		
		AdaptiveArtificialBeeColony aabc = new AdaptiveArtificialBeeColony(n * k, minX, maxX);
		aabc.setPathFileOfSimulation(pathFileOfSimulation);
		aabc.execute();
		double sigmaAllPairs[] = aabc.getgBest().getNectar();
		
		//Save the best solution
		saveBestSolution(sigmaAllPairs, aabc.getEpochGBest());
	}
	
	public static void saveBestSolution(double sigmaAllPairs[], int epoch){
		String pathFileBestSoluation = pathFileOfSimulation + separator + "bestSigmaForAllPairs_epoch_" + epoch + ".txt";;
		KShortestPathsReductionQoTO_v1.createFileSigmaAllPairs(numberOfNodes, pathFileBestSoluation, sigmaAllPairs);
	}
	
	public static double runSimulation(double sigmaAllPairs[]) {
		Double result = 0.0;
		
		KShortestPathsReductionQoTO_v1.createFileSigmaAllPairs(numberOfNodes, pathFileOfSigma, sigmaAllPairs);
		
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
		String readFileName = folderName + "_BlockingProbability.csv";
		String sourcePath = pathFileOfSimulation + separator + readFileName;
		String metric = "blocking probability";
		
		double bp[] = new double[numberOfReplications];
		
		try{
			FileReader fr = new FileReader(sourcePath);
			BufferedReader in = new BufferedReader(fr);
		    
			while(in.ready()){
				String[] line = in.readLine().split(",");
				
				if((line.length >= 5) && line[0].equals(metric)){
					
					for(int r = 0; r < numberOfReplications; r++){
						bp[r] = Double.parseDouble(line[6 + r]);
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
		for(int r = 0; r < numberOfReplications; r++){
			sumBP += bp[r];
		}
		double averageBP = sumBP / numberOfReplications;
		
		return averageBP;
	}
}
