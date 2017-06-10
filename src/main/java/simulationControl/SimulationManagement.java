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
            for(Simulation replication : loadPoint){
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
     */
    public void saveResults(String pathResultFiles) {
    	// Pick folder name
    	String separator = System.getProperty("file.separator");
        String aux[] = pathResultFiles.split(Pattern.quote(separator));
        String nome = aux[aux.length - 1];
        try {
            //List<Pair> pairs = new ArrayList(this.simulations.get(0).get(0).getMesh().getPairList());
            // Circuit blocking probability
            FileWriter fw = new FileWriter(new File(pathResultFiles + separator + nome + "BlockingProb.csv"));
            fw.write(getBlockingProbabilityCsv());
            fw.close();

            // Bandwidth blocking probability
            fw = new FileWriter(new File(pathResultFiles + separator + nome + "BandwidthBlockingProb.csv"));
            fw.write(getBandwidthBlockingProbabilityCsv());
            fw.close();

            // External fragmentation
            fw = new FileWriter(new File(pathResultFiles + separator + nome + "ExternalFragmentation.csv"));
            fw.write(getExternalFragmentationCsv());
            fw.close();

            // Relative fragmentation
            fw = new FileWriter(new File(pathResultFiles + separator + nome + "RelativeFragmentation.csv"));
            fw.write(getRelativeFragmentationCsv());
            fw.close();

            // Spectrum utilization
            fw = new FileWriter(new File(pathResultFiles + separator + nome + "SpectrumUtilization.csv"));
            fw.write(getSpectrumUtilizationCsv());
            fw.close();

            // Spectrum statistics
            fw = new FileWriter(new File(pathResultFiles + separator + nome + "SpectrumSizeStatistics.csv"));
            fw.write(getSpectrumStatisticsCsv());
            fw.close();

            // Statistics of tx and rx
            fw = new FileWriter(new File(pathResultFiles + separator + nome + "TransmitersReceiversUtilization.csv"));
            fw.write(getTransceiversUtilizationCsv());
            fw.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getBlockingProbabilityCsv(){
        // Circuit blocking probability
        List<List<BlockingProbability>> pbs = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<BlockingProbability> lpb = new ArrayList<>();
            pbs.add(lpb);
            for (Measurements measurements : listMeas) {
                lpb.add(measurements.getProbabilidadeDeBloqueioMeasurement());
            }
        }

        BlockingProbResultManager bprm = new BlockingProbResultManager(pbs);

        return bprm.result();
    }

    public String getBandwidthBlockingProbabilityCsv(){
        // Bandwidth blocking probability
        List<List<BandwidthBlockingProbability>> pbbs = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<BandwidthBlockingProbability> lpbb = new ArrayList<>();
            pbbs.add(lpbb);
            for (Measurements measurements : listMeas) {
                lpbb.add(measurements.getProbabilidadeDeBloqueioDeBandaMeasurement());
            }
        }

        BandwidthBlockingProbResultManager bbprm = new BandwidthBlockingProbResultManager(pbbs);
        return bbprm.result();
    }

    public String getExternalFragmentationCsv(){
        // External fragmentation
        List<List<ExternalFragmentation>> llfe = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<ExternalFragmentation> lfe = new ArrayList<>();
            llfe.add(lfe);
            for (Measurements measurements : listMeas) {
                lfe.add(measurements.getFragmentacaoExterna());
            }
        }
        ExternalFragmentationManager efm = new ExternalFragmentationManager(llfe);
        return efm.result();
    }

    public String getRelativeFragmentationCsv(){
    	// Relative fragmentation
        List<List<RelativeFragmentation>> llfr = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<RelativeFragmentation> lfr = new ArrayList<>();
            llfr.add(lfr);
            for (Measurements measurements : listMeas) {
                lfr.add(measurements.getFragmentacaoRelativa());
            }
        }

        RelativeFragmentationManager rfm = new RelativeFragmentationManager(llfr);
        return rfm.result();
    }

    public String getSpectrumUtilizationCsv(){
        // Spectrum utilization
        List<List<SpectrumUtilization>> llus = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<SpectrumUtilization> lus = new ArrayList<>();
            llus.add(lus);
            for (Measurements measurements : listMeas) {
                lus.add(measurements.getUtilizacaoSpectro());
            }
        }

        SpectrumUtilizationResultManager surm = new SpectrumUtilizationResultManager(llus);
        return surm.result();
    }

    public String getSpectrumStatisticsCsv(){
        // Spectrum size statistics
        List<List<SpectrumSizeStatistics>> llsss = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<SpectrumSizeStatistics> lsss = new ArrayList<>();
            llsss.add(lsss);
            for (Measurements measurements : listMeas) {
                lsss.add(measurements.getSpectrumSizeStatistics());
            }
        }
        SpectrumSizeStatisticsResultManager sssrm = new SpectrumSizeStatisticsResultManager(llsss);
        return sssrm.result();
    }

    public String getTransceiversUtilizationCsv(){
        // Transmitters and receivers utilization
        List<List<TransmittersReceiversUtilization>> lltru = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<TransmittersReceiversUtilization> ltru = new ArrayList<>();
            lltru.add(ltru);
            for (Measurements measurements : listMeas) {
                ltru.add(measurements.getTransmitersReceiversUtilization());
            }
        }
        TransmittersReceiversUtilizationResultManager trurm = new TransmittersReceiversUtilizationResultManager(lltru);
        return trurm.result();
    }


    public static interface SimulationProgressListener {

        public void onSimulationProgressUpdate(double progress);

        public void onSimulationFinished();

    }

}
