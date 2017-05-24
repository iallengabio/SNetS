package simulationControl.parsers.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta classe representa o arquivo de configuração Traffic, sua representação na forma de entidade é importante para a armazenação e transmissão deste tipo de configução no formato JSON.
 * Created by Iallen on 04/05/2017.
 */
public class TrafficConfig {

    private List<RequestGeneratorConfig> requestGenerators = new ArrayList<>();

    public List<RequestGeneratorConfig> getRequestGenerators() {
        return requestGenerators;
    }

    public void setRequestGenerators(List<RequestGeneratorConfig> requestGenerators) {
        this.requestGenerators = requestGenerators;
    }

    public static class RequestGeneratorConfig{

        private String source;
        private String destination;
        private double bandwidth;
        private double arrivalRate;
        private double holdRate;
        private double arrivalRateIncrease;

        public RequestGeneratorConfig(String source, String destination, double bandwidth, double arrivalRate, double holdRate, double arrivalRateIncrease) {
            this.source = source;
            this.destination = destination;
            this.bandwidth = bandwidth;
            this.arrivalRate = arrivalRate;
            this.holdRate = holdRate;
            this.arrivalRateIncrease = arrivalRateIncrease;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public double getBandwidth() {
            return bandwidth;
        }

        public void setBandwidth(double bandwidth) {
            this.bandwidth = bandwidth;
        }

        public double getArrivalRate() {
            return arrivalRate;
        }

        public void setArrivalRate(double arrivalRate) {
            this.arrivalRate = arrivalRate;
        }

        public double getHoldRate() {
            return holdRate;
        }

        public void setHoldRate(double holdRate) {
            this.holdRate = holdRate;
        }

        public double getArrivalRateIncrease() {
            return arrivalRateIncrease;
        }

        public void setArrivalRateIncrease(double arrivalRateIncrease) {
            this.arrivalRateIncrease = arrivalRateIncrease;
        }
    }

}
