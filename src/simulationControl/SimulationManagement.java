package simulationControl;

import java.io.*;
import java.rmi.*;
import java.util.*;

import measurement.*;
import network.*;
import request.*;
import simulationControl.resultManagers.BandwidthBlockingProbResultManager;
import simulationControl.resultManagers.BlockingProbResultManager;
import simulationControl.resultManagers.ExternalFragmentationManager;
import simulationControl.resultManagers.RelativeFragmentationManager;
import simulationControl.resultManagers.SpectrumSizeStatisticsResultManager;
import simulationControl.resultManagers.SpectrumUtilizationResultManager;
import simulationControl.resultManagers.TransmitersReceiversUtilizationResultManager;
import simulator.Simulation;
import simulator.Simulator;

public class SimulationManagement {

    private Simulator simulator;
    private List<List<Simulation>> simulations;
    private String pathResultFiles;

    
    /**
     * armazena os resultados para todos os pontos com todas replicacoes
     */
    private List<List<Measurements>> mainMeasuremens;
           
    public SimulationManagement(List<List<Simulation>> simulations, String pathResultFiles) {
        this.simulations = simulations;
        this.pathResultFiles = pathResultFiles;
    }

    /**
     * executa as simulações de forma centralizada
     */
    public void startSimulations(){
    	mainMeasuremens = new ArrayList<>();
    	Util.pairs.addAll(simulations.get(0).get(0).getMesh().getPairList());
    	
    	
    	double quantLP = simulations.size();
    	double quantLPE = 0;
    	for (List<Simulation> loadPoint : simulations) {
			ArrayList<Measurements> measurementsLoadPoint = new ArrayList<>();
			this.mainMeasuremens.add(measurementsLoadPoint);
			
			for (Simulation replication : loadPoint) {
				
				
				simulator = new Simulator(replication);
				measurementsLoadPoint.add(simulator.start()); 
				
				
			}
			quantLPE++;
			System.out.println(quantLPE/quantLP*100.0 + "%");
		}
    	
    }
  
    public void saveResults(){
    	//pegar nome da pasta
    	String aux[] = pathResultFiles.split("/");
    	String nome = aux[aux.length-1];
    	
    	//probabilidade de bloqueio
    	List<List<ProbabilidadeDeBloqueio>> pbs = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<ProbabilidadeDeBloqueio> lpb = new ArrayList<>();
			pbs.add(lpb);
			for (Measurements measurements : listMeas) {
				lpb.add(measurements.getProbabilidadeDeBloqueioMeasurement());
			}
		}
    	
    	//probabilidade de bloqueio de banda
    	List<List<ProbabilidadeDeBloqueioDeBanda>> pbbs = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<ProbabilidadeDeBloqueioDeBanda> lpbb = new ArrayList<>();
			pbbs.add(lpbb);
			for (Measurements measurements : listMeas) {
				lpbb.add(measurements.getProbabilidadeDeBloqueioDeBandaMeasurement());
			}
		}
    	
    	//fragmentação externa
    	List<List<FragmentacaoExterna>> llfe = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<FragmentacaoExterna> lfe = new ArrayList<>();
			llfe.add(lfe);
			for (Measurements measurements : listMeas) {
				lfe.add(measurements.getFragmentacaoExterna());
			}
		}
    	
    	//fragmentação externa
    	List<List<FragmentacaoRelativa>> llfr = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<FragmentacaoRelativa> lfr = new ArrayList<>();
			llfr.add(lfr);
			for (Measurements measurements : listMeas) {
				lfr.add(measurements.getFragmentacaoRelativa());
			}
		}
    	
    	//utilização de espectro
    	List<List<UtilizacaoSpectro>> llus = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<UtilizacaoSpectro> lus = new ArrayList<>();
			llus.add(lus);
			for (Measurements measurements : listMeas) {
				lus.add(measurements.getUtilizacaoSpectro());
			}
		}
    	
    	//utilização de espectro
    	List<List<SpectrumSizeStatistics>> llsss = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<SpectrumSizeStatistics> lsss = new ArrayList<>();
			llsss.add(lsss);
			for (Measurements measurements : listMeas) {
				lsss.add(measurements.getSpectrumSizeStatistics());
			}
		}
    	
    	//utilização de transmissores e receptores
    	List<List<TransmitersReceiversUtilization>> lltru = new ArrayList<>();
    	for (List<Measurements> listMeas : this.mainMeasuremens) {
			List<TransmitersReceiversUtilization> ltru = new ArrayList<>();
			lltru.add(ltru);
			for (Measurements measurements : listMeas) {
				ltru.add(measurements.getTransmitersReceiversUtilization());
			}
		}
    	
    	try {
    		//List<Pair> pairs = new ArrayList(this.simulations.get(0).get(0).getMesh().getPairList());
    		//probabilidade de bloqueio de circuitos
    		BlockingProbResultManager bprm = new BlockingProbResultManager(pbs);
			FileWriter fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"BlockingProb.csv"));
			fw.write(bprm.result());
			fw.close();
			
			//probabilidade de bloqueio de banda
			BandwidthBlockingProbResultManager bbprm = new BandwidthBlockingProbResultManager(pbbs);
			fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"BandwidthBlockingProb.csv"));
			fw.write(bbprm.result());
			fw.close();
			
			//fragmentação externa
			ExternalFragmentationManager efm = new ExternalFragmentationManager(llfe);
			fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"ExternalFragmentation.csv"));
			fw.write(efm.result());
			fw.close();
			
			//fragmentação externa
			RelativeFragmentationManager rfm = new RelativeFragmentationManager(llfr);
			fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"RelativeFragmentation.csv"));
			fw.write(rfm.result());
			fw.close();
			
			//Utilização
			SpectrumUtilizationResultManager surm = new SpectrumUtilizationResultManager(llus);
			fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"SpectrumUtilization.csv"));
			fw.write(surm.result());
			fw.close();
			
			//estatísticas de espectro
			SpectrumSizeStatisticsResultManager sssrm = new SpectrumSizeStatisticsResultManager(llsss);
			fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"SpectrumSizeStatistics.csv"));
			fw.write(sssrm.result());
			fw.close();
			
			//estatísticas de tx e rx
			TransmitersReceiversUtilizationResultManager trurm = new TransmitersReceiversUtilizationResultManager(lltru);
			fw = new FileWriter(new File(pathResultFiles+"/"+ nome +"TransmitersReceiversUtilization.csv"));
			fw.write(trurm.result());
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
