package simulationControl.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Traffic configuration file, its representation in entity form is 
 * important for the storage and transmission of this type of configuration in JSON format.
 * 
 * Created by Iallen on 04/05/2017.
 */
public class TrafficConfig {

    private List<RequestGeneratorConfig> requestGenerators = new ArrayList<>();

    /**
     * Returns the list of request generators
     * 
     * @return List<RequestGeneratorConfig>
     */
    public List<RequestGeneratorConfig> getRequestGenerators() {
        return requestGenerators;
    }

    /**
     * Sets the list of request generators
     * 
     * @param requestGenerators List<RequestGeneratorConfig>
     */
    public void setRequestGenerators(List<RequestGeneratorConfig> requestGenerators) {
        this.requestGenerators = requestGenerators;
    }

    /**
     * This class represents a request generator
     * 
     * @author Iallen
     */
    public static class RequestGeneratorConfig{

        private String source;
        private String destination;
        private double bandwidth;
        private double arrivalRate;
        private double holdRate;
        private double arrivalRateIncrease;

        /**
         * Creates a new instance of RequestGeneratorConfig
         * 
         * @param source String
         * @param destination String
         * @param bandwidth double
         * @param arrivalRate double
         * @param holdRate double
         * @param arrivalRateIncrease double
         */
        public RequestGeneratorConfig(String source, String destination, double bandwidth, double arrivalRate, double holdRate, double arrivalRateIncrease) {
            this.source = source;
            this.destination = destination;
            this.bandwidth = bandwidth;
            this.arrivalRate = arrivalRate;
            this.holdRate = holdRate;
            this.arrivalRateIncrease = arrivalRateIncrease;
        }

        /**
         * Returns the source node
         * 
         * @return String
         */
        public String getSource() {
            return source;
        }

        /**
         * Sets the source node
         * 
         * @param source String
         */
        public void setSource(String source) {
            this.source = source;
        }

        /**
         * Returns the destination node
         * 
         * @return String
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Sets the destination node
         * 
         * @param destination String
         */
        public void setDestination(String destination) {
            this.destination = destination;
        }

        /**
         * Returns the requested bandwidth
         * 
         * @return double
         */
        public double getBandwidth() {
            return bandwidth;
        }

        /**
         * Sets the requested bandwidth
         * 
         * @param bandwidth double
         */
        public void setBandwidth(double bandwidth) {
            this.bandwidth = bandwidth;
        }

        /**
         * Returns the arrival rate
         * 
         * @return double
         */
        public double getArrivalRate() {
            return arrivalRate;
        }

        /**
         * Sets the arrival rate
         * 
         * @param arrivalRate
         */
        public void setArrivalRate(double arrivalRate) {
            this.arrivalRate = arrivalRate;
        }

        /**
         * Returns the hold rate
         * 
         * @return double
         */
        public double getHoldRate() {
            return holdRate;
        }

        /**
         * Sets the hold rate
         * 
         * @param holdRate double
         */
        public void setHoldRate(double holdRate) {
            this.holdRate = holdRate;
        }

        /**
         * Returns the arrival rate increase
         * 
         * @return double
         */
        public double getArrivalRateIncrease() {
            return arrivalRateIncrease;
        }

        /**
         * Sets the arrival rate increase
         * 
         * @param arrivalRateIncrease double
         */
        public void setArrivalRateIncrease(double arrivalRateIncrease) {
            this.arrivalRateIncrease = arrivalRateIncrease;
        }
    }

}
