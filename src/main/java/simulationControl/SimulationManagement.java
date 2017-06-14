package simulationControl;

import measurement.*;
import simulationControl.resultManagers.*;
import simulator.Simulation;
import simulator.Simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * This class is responsible for managing the executions of the simulations
 * 
 * @author Iallen
 */
public class SimulationManagement {

    private static final int NUMBER_OF_ACTIVE_THREADS = 5;

    private List<List<Simulation>> simulations;
    private int done;
    private int numOfSimulations;
    /**
     * Stores the results for all points with all replicas
     */
    private List<List<Measurements>> mainMeasuremens;

    /**
     * Creates a new instance of SimulationManagement
     * 
     * @param simulations List<List<Simulation>>
     */
    public SimulationManagement(List<List<Simulation>> simulations) {
        this.simulations = simulations;
        done = 0;
        numOfSimulations = 0;
        mainMeasuremens = new ArrayList<>();
        
        for(List<Simulation> loadPoint : simulations){
            List<Measurements> aux = new ArrayList<>();
            mainMeasuremens.add(aux);
            for(int replication = 0; replication < loadPoint.size(); replication++){
                aux.add(null);
                numOfSimulations++;
            }
        }
    }

    /**
     * Runs the simulations centrally
     */
    public void startSimulations(SimulationProgressListener simulationProgressListener) {

        Util.pairs.addAll(simulations.get(0).get(0).getMesh().getPairList());

        ExecutorService executor = Executors.newScheduledThreadPool(NUMBER_OF_ACTIVE_THREADS);
        done = 0;
        for(List<Simulation> loadPoint : simulations){
            for(Simulation replication : loadPoint){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Simulator simulator = new Simulator(replication);
                        mainMeasuremens.get(replication.getLoadPoint()).set(replication.getReplication(), simulator.start());
                        done++;
                        simulationProgressListener.onSimulationProgressUpdate((double)done/numOfSimulations);
                    }
                });
            }
        }
        while(done<numOfSimulations){ //wait untill all simulations have done
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        simulationProgressListener.onSimulationFinished();
    }

    public void startSimulations(){
        startSimulations(new SimulationProgressListener() {
            @Override
            public void onSimulationProgressUpdate(double progress) {

            }

            @Override
            public void onSimulationFinished() {

            }
        });
    }

    /**
     * This method is responsible for calling the method of the class responsible for saving 
     * the results associated with each metric
     * 
     * @param pathResultFiles String
     */
    public void saveResults(String pathResultFiles){
    	// Pick folder name
    	String separador = System.getProperty("file.separator");
    	String aux[] = pathResultFiles.split(Pattern.quote(separador));
    	String nomePasta = aux[aux.length-1];
    	
    	try {
    		int numMetrics = this.mainMeasuremens.get(0).get(0).getMetrics().size();
    		for(int m = 0; m < numMetrics; m++){
    			Measurement metric = this.mainMeasuremens.get(0).get(0).getMetrics().get(m);
    			
				List<List<Measurement>> llms = new ArrayList<List<Measurement>>();
				for (List<Measurements> listMeasurements : this.mainMeasuremens) {
					List<Measurement> lms = new ArrayList<Measurement>();
					llms.add(lms);
					for (Measurements measurements : listMeasurements) {
						lms.add(measurements.getMetrics().get(m));
					}
				}
				
				String path = pathResultFiles + separador + nomePasta + metric.getFileName();
				
				FileWriter fw = new FileWriter(new File(path));
				fw.write(metric.result(llms));
	            fw.close();
    		}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public String getBlockingProbabilityCsv(){
        // Circuit blocking probability
        List<List<Measurement>> pbs = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lpb = new ArrayList<>();
            pbs.add(lpb);
            for (Measurements measurements : listMeas) {
                lpb.add(measurements.getProbabilidadeDeBloqueioMeasurement());
            }
        }

        BlockingProbResultManager bprm = new BlockingProbResultManager();
        return bprm.result(pbs);
    }

    public String getBandwidthBlockingProbabilityCsv(){
        // Bandwidth blocking probability
        List<List<Measurement>> pbbs = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lpbb = new ArrayList<>();
            pbbs.add(lpbb);
            for (Measurements measurements : listMeas) {
                lpbb.add(measurements.getProbabilidadeDeBloqueioDeBandaMeasurement());
            }
        }

        BandwidthBlockingProbResultManager bbprm = new BandwidthBlockingProbResultManager();
        return bbprm.result(pbbs);
    }

    public String getExternalFragmentationCsv(){
        // External fragmentation
        List<List<Measurement>> llfe = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lfe = new ArrayList<>();
            llfe.add(lfe);
            for (Measurements measurements : listMeas) {
                lfe.add(measurements.getFragmentacaoExterna());
            }
        }
        ExternalFragmentationResultManager efm = new ExternalFragmentationResultManager();
        return efm.result(llfe);
    }

    public String getRelativeFragmentationCsv(){
    	// Relative fragmentation
        List<List<Measurement>> llfr = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lfr = new ArrayList<>();
            llfr.add(lfr);
            for (Measurements measurements : listMeas) {
                lfr.add(measurements.getFragmentacaoRelativa());
            }
        }

        RelativeFragmentationResultManager rfm = new RelativeFragmentationResultManager();
        return rfm.result(llfr);
    }

    public String getSpectrumUtilizationCsv(){
        // Spectrum utilization
        List<List<Measurement>> llus = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lus = new ArrayList<>();
            llus.add(lus);
            for (Measurements measurements : listMeas) {
                lus.add(measurements.getUtilizacaoSpectro());
            }
        }

        SpectrumUtilizationResultManager surm = new SpectrumUtilizationResultManager();
        return surm.result(llus);
    }

    public String getSpectrumStatisticsCsv(){
        // Spectrum size statistics
        List<List<Measurement>> llsss = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lsss = new ArrayList<>();
            llsss.add(lsss);
            for (Measurements measurements : listMeas) {
                lsss.add(measurements.getSpectrumSizeStatistics());
            }
        }
        SpectrumSizeStatisticsResultManager sssrm = new SpectrumSizeStatisticsResultManager();
        return sssrm.result(llsss);
    }

    public String getTransceiversUtilizationCsv(){
        // Transmitters and receivers utilization
        List<List<Measurement>> lltru = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> ltru = new ArrayList<>();
            lltru.add(ltru);
            for (Measurements measurements : listMeas) {
                ltru.add(measurements.getTransmitersReceiversUtilization());
            }
        }
        TransmittersReceiversRegeneratorsUtilizationResultManager trurm = new TransmittersReceiversRegeneratorsUtilizationResultManager();
        return trurm.result(lltru);
    }
    
    public String getEnergyConsumptionCsv(){
        // Energy consumption
        List<List<Measurement>> llec = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lec = new ArrayList<>();
            llec.add(lec);
            for (Measurements measurements : listMeas) {
                lec.add(measurements.getMetricsOfEnergyConsumption());
            }
        }
        MetricsOfEnergyConsumptionResultManager mecm = new MetricsOfEnergyConsumptionResultManager();
        return mecm.result(llec);
    }
    
    public String getModulationUtilizationCsv(){
        // Modulation utilization
        List<List<Measurement>> llmu = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lmu = new ArrayList<>();
            llmu.add(lmu);
            for (Measurements measurements : listMeas) {
                lmu.add(measurements.getModulationUtilization());
            }
        }
        ModulationUtilizationResultManager murm = new ModulationUtilizationResultManager();
        return murm.result(llmu);
    }


    public static interface SimulationProgressListener {

        public void onSimulationProgressUpdate(double progress);

        public void onSimulationFinished();
    }

}
