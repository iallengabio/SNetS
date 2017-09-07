package util.tools.artificialBeeColony;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainMOABC {
	
	private static String functionName;
	private static String pathFileIGD;
	private static String pathFileArchive;
	private static String algorithmName;
	
	public static void main(String[] args) {
		
		int n = 30;
		int objectivesNumber = 2;
		double minX[] = new double[n];
		double maxX[] = new double[n];
		
		int replications = 30;
		List<Algo> moabcList = new ArrayList<>(replications);
		
		functionName = "UF6";
		algorithmName = "MOAABC";
		
		if(functionName.equals("UF1") || functionName.equals("UF5") || functionName.equals("UF6")){ // for UF1, UF5 and UF6
			minX[0] = 0.0;
			maxX[0] = 1.0;
			for(int i = 1; i < n; i++){
				minX[i] = -1.0;
				maxX[i] = 1.0;
			}
			
		}else if(functionName.equals("UF3")){ // for UF3
			for(int i = 0; i < n; i++){
				minX[i] = 0.0;
				maxX[i] = 1.0;
			}
			
		}else if(functionName.equals("UF4")){ // for UF4
			minX[0] = 0.0;
			maxX[0] = 1.0;
			for(int i = 1; i < n; i++){
				minX[i] = -2.0;
				maxX[i] = 2.0;
			}
		}
		
		String pathPF = "C:/Users/Alexandre/src/workspace/SNetS/simulations/multiObjective/" + functionName + ".dat";
		pathFileIGD = "C:/Users/Alexandre/src/workspace/SNetS/simulations/multiObjective/" + algorithmName + "_" + functionName + "_IGDepoch.csv";
		pathFileArchive = "C:/Users/Alexandre/src/workspace/SNetS/simulations/multiObjective/" + algorithmName + "_" + functionName + "_archive.csv";
		
		for(int i = 0; i < replications; i++){
			System.out.println("Replication " + (i + 1));
			
			if(algorithmName.equals("e-MOABC")){
				MultiObjectiveArtificialBeeColony moabc = new MultiObjectiveArtificialBeeColony(n, minX, maxX, objectivesNumber, pathPF);
				moabc.execute();
				//moabcList.add(moabc);
				
			}else if(algorithmName.equals("MOAABC")){
				MultiObjectiveAdaptiveArtificialBeeColony moaabc = new MultiObjectiveAdaptiveArtificialBeeColony(n, minX, maxX, objectivesNumber, pathPF);
				moaabc.execute();
				moabcList.add(moaabc);
			}
		}

		saveResults(moabcList);
	}
	
	public static double[] runSimulation(double x[], int n, int numObjectives) {
		double result[] = new double[numObjectives];
		
		if(functionName.equals("UF1")){
			result = UF1(x, n);
			
		}else if(functionName.equals("UF3")){
			result = UF3(x, n);
			
		}else if(functionName.equals("UF4")){
			result = UF4(x, n);
			
		}else if(functionName.equals("UF5")){
			result = UF5(x, n);
			
		}else if(functionName.equals("UF6")){
			result = UF6(x, n);
		}
		
		return result;
	}
	
	
	private static void saveResults(List<Algo> moabcList){
//		try {
//			FileWriter fw = new FileWriter(pathFileIGD);
//			BufferedWriter out = new BufferedWriter(fw);
//			
//			StringBuilder sb = new StringBuilder();
//			sb.append("Epoch, ");
//			for(int r = 0; r < moabcList.size(); r++){
//				sb.append(",Rep" + (r + 1));
//			}
//			sb.append("\n");
//			
//			int numEpoch = moabcList.get(0).getIGDepoch().size();
//			for(int e = 0; e < numEpoch; e++){
//				sb.append((e + 1) + ", ");
//				
//				for(int r = 0; r < moabcList.size(); r++){
//					double IGDepoch = moabcList.get(r).getIGDepoch().get(e);
//					sb.append("," + IGDepoch);
//				}
//				sb.append("\n");
//			}
//			
//			out.append(sb.toString());
//			
//			out.close();
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			FileWriter fw = new FileWriter(pathFileArchive);
//			BufferedWriter out = new BufferedWriter(fw);
//			
//			StringBuilder sb = new StringBuilder();
//			sb.append("FoodSource,Objective, ");
//			for(int r = 0; r < moabcList.size(); r++){
//				sb.append(",Rep" + (r + 1));
//			}
//			sb.append("\n");
//			
//			int numFood = moabcList.get(0).getArchive().size();
//			int numObjectives = moabcList.get(0).getArchive().get(0).getFitnessByObjective(numObjectives).length;
//			
//			for(int f = 0; f < numFood; f++){
//				String aux = (f + 1) + "";
//				
//				for(int o = 0; o < numObjectives; o++){
//					String aux2 = aux + "," + (o + 1) + ", ";
//					
//					for(int r = 0; r < moabcList.size(); r++){
//						int[] value = moabcList.get(r).getArchive().get(f).getFitnessByObjective(o);
//						aux2 += "," + value;
//					}
//					sb.append(aux2 + "\n");
//				}
//			}
//			
//			out.append(sb.toString());
//			
//			out.close();
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	
	
	public static double[] UF1(double x[], int n){
		int j, count1, count2;
		double sum1, sum2, yj;
		double f[] = new double[2];
		
		sum1 = 0.0;
		sum2 = 0.0;
		count1 = 0;
		count2 = 0;
		
		for(j = 2; j <= n; j++) {
			yj = x[j-1] - Math.sin(6.0 * Math.PI * x[0] + j * Math.PI / n);
			yj = yj * yj;
			if(j % 2 == 0) {
				sum2 += yj;
				count2++;
			} else {
				sum1 += yj;
				count1++;
			}
		}
		
		f[0] = x[0] + 2.0 * sum1 / (double)count1;
		f[1] = 1.0 - Math.sqrt(x[0]) + 2.0 * sum2 / (double)count2;
		
		return f;
	}
	
	public static double[] UF3(double x[], int n){
		int j, count1, count2;
		double sum1, sum2, yj, prod1, prod2, pj;
		double f[] = new double[2];
		
		sum1 = 0.0;
		sum2 = 0.0;
		count1 = 0;
		count2 = 0;
		prod1 = 1.0;
		prod2 = 1.0;
		
		for(j = 2; j <= n; j++){
			yj = x[j-1] - Math.pow(x[0], 0.5 * (1.0 + 3.0 * (j - 2.0) / (n - 2.0)));
			pj = Math.cos(20.0 * yj * Math.PI / Math.sqrt(j + 0.0));
			if (j % 2 == 0) {
				sum2  += yj * yj;
				prod2 *= pj;
				count2++;
			} else {
				sum1  += yj * yj;
				prod1 *= pj;
				count1++;
			}
		}
		
		f[0] = x[0] + 2.0 * (4.0 * sum1 - 2.0 * prod1 + 2.0) / (double)count1;
		f[1] = 1.0 - Math.sqrt(x[0]) + 2.0 * (4.0 * sum2 - 2.0 * prod2 + 2.0) / (double)count2;
		
		return f;
	}
	
	public static double[] UF4(double x[], int n){
		int j, count1, count2;
		double sum1, sum2, yj, hj;
		double f[] = new double[2];
		
		sum1 = 0.0;
		sum2 = 0.0;
		count1 = 0;
		count2 = 0;
		
		for(j = 2; j <= n; j++) {
			yj = x[j-1] - Math.sin(6.0 * Math.PI * x[0] + j * Math.PI / n);
			hj = fabs(yj) / (1.0 + Math.exp(2.0 * fabs(yj)));
			if (j % 2 == 0) {
				sum2  += hj;
				count2++;
			} else {
				sum1  += hj;
				count1++;
			}
		}
		
		f[0] = x[0] + 2.0 * sum1 / (double)count1;
		f[1] = 1.0 - x[0] * x[0] + 2.0 * sum2 / (double)count2;
		
		return f;
	}
	
	public static double[] UF5(double x[], int n){
		int j, count1, count2;
		double sum1, sum2, yj, hj, N2, E;
		double f[] = new double[2];
		
		sum1 = 0.0;
		sum2 = 0.0;
		count1 = 0;
		count2 = 0;
		N2 = 10.0;
		E = 0.1;
		
		for(j = 2; j <= n; j++) {
			yj = x[j-1] - Math.sin(6.0 * Math.PI * x[0] + j * Math.PI / n);
			hj = 2.0 * yj * yj - Math.cos(4.0 * Math.PI * yj) + 1.0;
			if (j % 2 == 0) {
				sum2  += hj;
				count2++;
			} else {
				sum1  += hj;
				count1++;
			}
		}
		
		hj = (0.5 / N2 + E) * fabs(Math.sin(2.0 * N2 * Math.PI * x[0]));
		f[0] = x[0] + hj + 2.0 * sum1 / (double)count1;
		f[1] = 1.0 - x[0] + hj + 2.0 * sum2 / (double)count2;
		
		return f;
	}
	
	public static double[] UF6(double x[], int n){
		int j, count1, count2;
		double sum1, sum2, prod1, prod2, yj, hj, pj, N2, E;
		double f[] = new double[2];
		
		sum1 = 0.0;
		sum2 = 0.0;
		count1 = 0;
		count2 = 0;
		prod1  = 1.0;
		prod2  = 1.0;
		N2 = 2.0;
		E = 0.1;
		
		for(j = 2; j <= n; j++) {
			yj = x[j-1] - Math.sin(6.0 * Math.PI * x[0] + j * Math.PI / n);
			pj = Math.cos(20.0 * yj * Math.PI / Math.sqrt(j + 0.0));
			if (j % 2 == 0) {
				sum2  += yj * yj;
				prod2 *= pj;
				count2++;
			} else {
				sum1  += yj * yj;
				prod1 *= pj;
				count1++;
			}
		}

		hj = 2.0 * (0.5 / N2 + E) * Math.sin(2.0 * N2 * Math.PI * x[0]);
		if(hj < 0.0)
			hj = 0.0;
		f[0] = x[0]	+ hj + 2.0 * (4.0 * sum1 - 2.0 * prod1 + 2.0) / (double)count1;
		f[1] = 1.0 - x[0] + hj + 2.0 *(4.0 * sum2 - 2.0 * prod2 + 2.0) / (double)count2;
		
		return f;
	}
	
	public static double fabs(double x){
		if(x < 0.0){
			return -1.0 * x;
		}
		return x;
	}
}
