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

public class SimulationManagement {

    private Simulator simulator;
    private List<List<Simulation>> simulations;


    /**
     * armazena os resultados para todos os pontos com todas replicacoes
     */
    private List<List<Measurements>> mainMeasuremens;

    public SimulationManagement(List<List<Simulation>> simulations) {
        this.simulations = simulations;
    }

    /**
     * executa as simulações de forma centralizada
     */
    public void startSimulations() {
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
            System.out.println(quantLPE / quantLP * 100.0 + "%");
        }

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
            fw.write(getSpectrumUtilization());
            fw.close();

            //estatísticas de espectro

            fw = new FileWriter(new File(pathResultFiles + "/" + nome + "SpectrumSizeStatistics.csv"));
            fw.write(getSpectrumStatistics());
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

    public String getSpectrumUtilization(){
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

    public String getSpectrumStatistics(){
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

}
