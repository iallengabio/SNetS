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

public class SimulationManagement {

    private static final int NUMBER_OF_ACTIVE_THREADS = 5;

    private List<List<Simulation>> simulations;
    private int done;
    private int numOfSimulations;

    /**
     * armazena os resultados para todos os pontos com todas replicacoes
     */
    private List<List<Measurements>> mainMeasuremens;

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
     * executa as simulações de forma centralizada
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
        while(done<numOfSimulations){//wait untill all simulations have done
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

    public void saveResults(String pathResultFiles) {
        //pegar nome da pasta
        String aux[] = pathResultFiles.split("/");
        String nome = aux[aux.length - 1];
        try {
            //List<Pair> pairs = new ArrayList(this.simulations.get(0).get(0).getMesh().getPairList());
            //probabilidade de bloqueio de circuitos
            FileWriter fw = new FileWriter(new File(pathResultFiles + "/" + nome + "BlockingProb.csv"));
            fw.write(getBlockingProbabilityCsv());
            fw.close();

            //probabilidade de bloqueio de banda
            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "BandwidthBlockingProb.csv"));
            fw.write(getBandwidthBlockingProbabilityCsv());
            fw.close();

            //fragmentação externa
            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "ExternalFragmentation.csv"));
            fw.write(getExternalFragmentationCsv());
            fw.close();

            //fragmentação relativa
            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "RelativeFragmentation.csv"));
            fw.write(getRelativeFragmentationCsv());
            fw.close();

            //Utilização

            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "SpectrumUtilization.csv"));
            fw.write(getSpectrumUtilizationCsv());
            fw.close();

            //estatísticas de espectro

            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "SpectrumSizeStatistics.csv"));
            fw.write(getSpectrumStatisticsCsv());
            fw.close();

            //estatísticas de tx e rx
            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "TransmitersReceiversUtilization.csv"));
            fw.write(getTransceiversUtilizationCsv());
            fw.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getBlockingProbabilityCsv(){
        //probabilidade de bloqueio
        List<List<ProbabilidadeDeBloqueio>> pbs = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<ProbabilidadeDeBloqueio> lpb = new ArrayList<>();
            pbs.add(lpb);
            for (Measurements measurements : listMeas) {
                lpb.add(measurements.getProbabilidadeDeBloqueioMeasurement());
            }
        }

        BlockingProbResultManager bprm = new BlockingProbResultManager(pbs);

        return bprm.result();
    }

    public String getBandwidthBlockingProbabilityCsv(){
        //probabilidade de bloqueio de banda
        List<List<ProbabilidadeDeBloqueioDeBanda>> pbbs = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<ProbabilidadeDeBloqueioDeBanda> lpbb = new ArrayList<>();
            pbbs.add(lpbb);
            for (Measurements measurements : listMeas) {
                lpbb.add(measurements.getProbabilidadeDeBloqueioDeBandaMeasurement());
            }
        }

        BandwidthBlockingProbResultManager bbprm = new BandwidthBlockingProbResultManager(pbbs);
        return bbprm.result();
    }

    public String getExternalFragmentationCsv(){
        //fragmentação externa
        List<List<FragmentacaoExterna>> llfe = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<FragmentacaoExterna> lfe = new ArrayList<>();
            llfe.add(lfe);
            for (Measurements measurements : listMeas) {
                lfe.add(measurements.getFragmentacaoExterna());
            }
        }
        ExternalFragmentationManager efm = new ExternalFragmentationManager(llfe);
        return efm.result();
    }

    public String getRelativeFragmentationCsv(){
        List<List<FragmentacaoRelativa>> llfr = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<FragmentacaoRelativa> lfr = new ArrayList<>();
            llfr.add(lfr);
            for (Measurements measurements : listMeas) {
                lfr.add(measurements.getFragmentacaoRelativa());
            }
        }

        RelativeFragmentationManager rfm = new RelativeFragmentationManager(llfr);
        return rfm.result();
    }

    public String getSpectrumUtilizationCsv(){
        //utilização de espectro
        List<List<UtilizacaoSpectro>> llus = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<UtilizacaoSpectro> lus = new ArrayList<>();
            llus.add(lus);
            for (Measurements measurements : listMeas) {
                lus.add(measurements.getUtilizacaoSpectro());
            }
        }

        SpectrumUtilizationResultManager surm = new SpectrumUtilizationResultManager(llus);
        return surm.result();
    }

    public String getSpectrumStatisticsCsv(){
        //utilização de espectro
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
        //utilização de transmissores e receptores
        List<List<TransmitersReceiversUtilization>> lltru = new ArrayList<>();
        for (List<Measurements> listMeas : this.mainMeasuremens) {
            List<TransmitersReceiversUtilization> ltru = new ArrayList<>();
            lltru.add(ltru);
            for (Measurements measurements : listMeas) {
                ltru.add(measurements.getTransmitersReceiversUtilization());
            }
        }
        TransmitersReceiversUtilizationResultManager trurm = new TransmitersReceiversUtilizationResultManager(lltru);
        return trurm.result();
    }


    public static interface SimulationProgressListener{

        public void onSimulationProgressUpdate(double progress);

        public void onSimulationFinished();

    }

}
