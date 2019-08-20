package simulationControl.resultManagers;

import measurement.Measurement;
import measurement.Measurements;
import simulationControl.parsers.SimulationRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * This class compile the results of a simulation.
 */
public class ResultManager {
    private List<List<Measurements>> mainMeasuremens;

    public ResultManager(List<List<Measurements>> mainMeasuremens) {
        this.mainMeasuremens = mainMeasuremens;
    }

    public SimulationRequest.Result getResults(){
        SimulationRequest.Result r = new SimulationRequest.Result();
        int numMetrics = mainMeasuremens.get(0).get(0).getMetrics().size();

        Measurement metric;
        List<List<Measurement>> llms;

        for(int m = 0; m < numMetrics; m++){
            metric = mainMeasuremens.get(0).get(0).getMetrics().get(m);

            llms = new ArrayList<List<Measurement>>();
            for (List<Measurements> listMeasurements : mainMeasuremens) {
                List<Measurement> lms = new ArrayList<Measurement>();
                llms.add(lms);
                for (Measurements measurements : listMeasurements) {
                    lms.add(measurements.getMetrics().get(m));
                }
            }

            switch (metric.getFileName()){
                case SimulationRequest.Result.FILE_BANDWIDTH_BLOCKING_PROBABILITY:
                    r.bandwidthBlockingProbability = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_BLOCKING_PROBABILITY:
                    r.blockingProbability = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_CONSUMEDEN_ERGY:
                    r.consumedEnergy = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_EXTERNAL_FRAGMENTATION:
                    r.externalFragmentation = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_GROOMING_STATISTICS:
                    r.groomingStatistics = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_MODULATION_UTILIZATION:
                    r.modulationUtilization = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_RELATIVE_FRAGMENTATION:
                    r.relativeFragmentation = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_SPECTRUM_STATISTICS:
                    r.spectrumStatistics = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_SPECTRUM_UTILIZATION:
                    r.spectrumUtilization = metric.result(llms);
                    break;
                case SimulationRequest.Result.FILE_TRANSMITTERS_RECEIVERS_REGENERATORS_UTILIZATION:
                    r.transmittersReceiversRegeneratorsUtilization = metric.result(llms);
            }
        }

        return r;
    }

}
