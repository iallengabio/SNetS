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

    private int numberOfThreads = 1;

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
    public SimulationManagement(List<List<Simulation>> simulations, int threads) {
        numberOfThreads = threads;
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
     * This constructor is used to reuse the methods that converts results into csv files.
     * @param mainMeasuremens
     */
    public SimulationManagement(List<List<Simulation>> simulations, List<List<Measurements>> mainMeasuremens) {
        this.mainMeasuremens = mainMeasuremens;
    }

    /**
     * Runs the simulations centrally
     */
    public void startSimulations(SimulationProgressListener simulationProgressListener) {

        Util.pairs.addAll(simulations.get(0).get(0).getMesh().getPairList());

        ExecutorService executor = Executors.newScheduledThreadPool(numberOfThreads);
        done = 0;
        for(List<Simulation> loadPoint : simulations){
            for(Simulation replication : loadPoint){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Simulator simulator = new Simulator(replication);
                            mainMeasuremens.get(replication.getLoadPoint()).set(replication.getReplication(), simulator.start());
                            done++;
                            simulationProgressListener.onSimulationProgressUpdate((double) done / numOfSimulations);
                        }catch (Exception ex){
                            ex.printStackTrace();
                            executor.shutdown();
                        }
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

        executor.shutdown();

        simulationProgressListener.onSimulationFinished();
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
    	String nomePasta = aux[aux.length - 1];
    	
    	try {
    		int numMetrics = this.mainMeasuremens.get(0).get(0).getMetrics().size();

    		Measurement metric = this.mainMeasuremens.get(0).get(0).getConsumedEnergyMetric();
            List<List<Measurement>> llms = new ArrayList<List<Measurement>>();
            for (List<Measurements> listMeasurements : this.mainMeasuremens) {
                List<Measurement> lms = new ArrayList<Measurement>();
                llms.add(lms);
                for (Measurements measurements : listMeasurements) {
                    lms.add(measurements.getConsumedEnergyMetric());
                }
            }
            String path = pathResultFiles + separador + nomePasta + metric.getFileName();
            FileWriter fw = new FileWriter(new File(path));
            fw.write(metric.result(llms));
            fw.close();

    		for(int m = 0; m < numMetrics; m++){
    			metric = this.mainMeasuremens.get(0).get(0).getMetrics().get(m);
    			
				llms = new ArrayList<List<Measurement>>();
				for (List<Measurements> listMeasurements : this.mainMeasuremens) {
					List<Measurement> lms = new ArrayList<Measurement>();
					llms.add(lms);
					for (Measurements measurements : listMeasurements) {
						lms.add(measurements.getMetrics().get(m));
					}
				}
				
				path = pathResultFiles + separador + nomePasta + metric.getFileName();
				
				fw = new FileWriter(new File(path));
				fw.write(metric.result(llms));
	            fw.close();
    		}

			
		} catch (IOException e) {
			e.printStackTrace();
		}

    }

    /**
     * Returns a string with the results file of the metric of circuit blocking probability
     * 
     * @return String
     */
    public String getBlockingProbabilityCsv(){
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

    /**
     * Returns a string with the results file of the metric of bandwidth blocking probability
     * 
     * @return String
     */
    public String getBandwidthBlockingProbabilityCsv(){
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

    /**
     * Returns a string with the results file of the metric of external fragmentation
     * 
     * @return String
     */
    public String getExternalFragmentationCsv(){
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

    /**
     * Returns a string with the results file of the metric of relative fragmentation
     * 
     * @return String
     */
    public String getRelativeFragmentationCsv(){
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

    /**
     * Returns a string with the results file of the metric of spectrum utilization
     * 
     * @return String
     */
    public String getSpectrumUtilizationCsv(){
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

    /**
     * Returns a string with the results file of the metric of spectrum size statistics
     * 
     * @return String
     */
    public String getSpectrumStatisticsCsv(){
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

    /**
     * Returns a string with the results file of the metric of transmitters, receivers, and regenerators utilization
     * 
     * @return String
     */
    public String getTransceiversUtilizationCsv(){
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
    
    /**
     * Returns a string with the results file of the metric of energy consumption
     * 
     * @return String
     */
    public String getEnergyConsumptionCsv(){
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
    
    /**
     * Returns a string with the results file of the metric of modulation utilization
     * 
     * @return String
     */
    public String getModulationUtilizationCsv(){
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

    public String getConsumedEnergyCsv(){
        List<List<Measurement>> llmu = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<Measurement> lmu = new ArrayList<>();
            llmu.add(lmu);
            for (Measurements measurements : listMeas) {
                lmu.add(measurements.getConsumedEnergyMetric());
            }
        }
        ConsumedEnergyResultManager murm = new ConsumedEnergyResultManager();
        return murm.result(llmu);
    }


    public static interface SimulationProgressListener {

        public void onSimulationProgressUpdate(double progress);

        public void onSimulationFinished();
    }

}
