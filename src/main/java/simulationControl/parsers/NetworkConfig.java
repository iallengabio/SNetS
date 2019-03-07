package simulationControl.parsers;

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents the Network configuration file, its representation in entity form is 
 * important for the storage and transmission of this type of configuration in JSON format.
 * 
 * Created by Iallen on 04/05/2017.
 */
public class NetworkConfig {
	
    private List<NodeConfig> nodes = new ArrayList<>();
    private List<LinkConfig> links = new ArrayList<>();
    private List<ModulationConfig> modulations = new ArrayList<>();
    private int guardBand = 1;
    private int bvtSpectralAmplitude = 1000;

    /**
     * Returns the list of modulations
     * 
     * @return List<ModulationConfig>
     */
    public List<ModulationConfig> getModulations() {
        return modulations;
    }

    /**
     * Sets the list of modulations
     * 
     * @param modulations List<ModulationConfig>
     */
    public void setModulations(List<ModulationConfig> modulations) {
        this.modulations = modulations;
    }

    /**
     * Returns the list of nodes
     * 
     * @return List<NodeConfig>
     */
    public List<NodeConfig> getNodes() {
        return nodes;
    }

    /**
     * Sets the list of nodes
     * 
     * @param nodes List<NodeConfig>
     */
    public void setNodes(List<NodeConfig> nodes) {
        this.nodes = nodes;
    }

    /**
     * Returns the list of links
     * 
     * @return List<LinkConfig>
     */
    public List<LinkConfig> getLinks() {
        return links;
    }

    /**
     * Sets the list of links
     * 
     * @param links List<LinkConfig>
     */
    public void setLinks(List<LinkConfig> links) {
        this.links = links;
    }

    /**
     * Returns the guard band
     * 
     * @return int
     */
    public int getGuardBand() {
        return guardBand;
    }

    /**
     * Sets the guard band
     * 
     * @param guardBand int
     */
    public void setGuardBand(int guardBand) {
        this.guardBand = guardBand;
    }

    public int getBvtSpectralAmplitude() {
        return bvtSpectralAmplitude;
    }

    public void setBvtSpectralAmplitude(int bvtSpectralAmplitude) {
        this.bvtSpectralAmplitude = bvtSpectralAmplitude;
    }

    /**
     * This class represents a noda of the network
     * 
     * @author Iallen
     */
    public static class NodeConfig{

        private String name;
        private int transmitters;
        private int receivers;
        private int regenerators;

        /**
         * Creates a new instance of NodeConfig
         * 
         * @param name String
         * @param transeivers int
         * @param receivers int
         */
        public NodeConfig(String name, int transmiters, int receivers, int regenerators) {
            this.name = name;
            this.transmitters = transmiters;
            this.receivers = receivers;
            this.regenerators = regenerators;
        }

        /**
         * Returns the node name
         * 
         * @return String
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the node name
         * 
         * @param name String
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the number of transmitters
         * 
         * @return int
         */
        public int getTransmitters() {
            return transmitters;
        }

        /**
         * Sets the number of transmitters
         * 
         * @param transmitters int
         */
        public void setTransmitters(int transmitters) {
            this.transmitters = transmitters;
        }

        /**
         * Returns the number of receivers
         * 
         * @return int
         */
        public int getReceivers() {
            return receivers;
        }

        /**
         * Sets the number of receivers
         * 
         * @param receivers int
         */
        public void setReceivers(int receivers) {
            this.receivers = receivers;
        }
        
        /**
         * Returns the number of regenerators
         * 
         * @return int
         */
        public int getRegenerators() {
            return regenerators;
        }

        /**
         * Sets the number of regenerators
         * 
         * @param regenerators int
         */
        public void setRegenerators(int regenerators) {
            this.regenerators = regenerators;
        }
    }

    /**
     * This class represents a link of the network
     * 
     * @author Iallen
     */
    public static class LinkConfig{
    	
        private String source;
        private String destination;
        private int slots;
        private double spectrum;
        private double size;

        /**
         * Creates a new instance of LinkConfig
         * 
         * @param source
         * @param destination
         * @param slots
         * @param sectrum
         * @param size
         */
        public LinkConfig(String source, String destination, int slots, double spectrum, double size) {
            this.source = source;
            this.destination = destination;
            this.slots = slots;
            this.spectrum = spectrum;
            this.size = size;
        }

        /**
         * Returns the source name
         * 
         * @return String
         */
        public String getSource() {
            return source;
        }

        /**
         * Sets the source name
         * 
         * @param source String
         */
        public void setSource(String source) {
            this.source = source;
        }

        /**
         * Returns the destination name
         * 
         * @return String
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Sets the destination name
         * 
         * @param destination String
         */
        public void setDestination(String destination) {
            this.destination = destination;
        }

        /**
         * Returns the number of slots
         * 
         * @return int
         */
        public int getSlots() {
            return slots;
        }

        /**
         * Sets the number of sltos
         * 
         * @param slots int
         */
        public void setSlots(int slots) {
            this.slots = slots;
        }

        /**
         * Returns the spectrum bandwidth
         * 
         * @return double
         */
        public double getSpectrum() {
            return spectrum;
        }

        /**
         * Sets the spectrum bandwidth
         * 
         * @param spectrum double
         */
        public void setSpectrum(double spectrum) {
            this.spectrum = spectrum;
        }

        /**
         * Returns the size of the link
         * 
         * @return double
         */
        public double getSize() {
            return size;
        }

        /**
         * Sets the size of the link
         * 
         * @param size double
         */
        public void setSize(double size) {
            this.size = size;
        }
    }

    /**
     * This class represents a modulation of the network
     * 
     * @author Iallen
     */
    public static class ModulationConfig {
    	
        private String name;
        private double maxRange; // km
        private double M; // Number of modulation format symbols
        private double SNR; // SNR threshold (dB)
        
        /**
         * Creates a new instance of ModulationConfig
         * 
         * @param name
         * @param bitsPerSymbol
         * @param maxRange
         */
        public ModulationConfig(String name, double M, double maxRange, double SNR) {
            this.name = name;
            this.M = M;
            this.maxRange = maxRange;
        }

        /**
         * Returns the modulation name
         * 
         * @return String
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the modulation name
         * 
         * @param name String
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the bits per symbol of the modulation
         * 
         * @return double
         */
        public double getM() {
            return M;
        }

        /**
         * Sets the bits per symbol of the modulation
         * 
         * @param bitsPerSymbol
         */
        public void setM(double M) {
            this.M = M;
        }
        
        /**
         * Returns the maximum range of the modulation
         * 
         * @return double
         */
        public double getMaxRange() {
            return maxRange;
        }

        /**
         * Sets the maximum range of the modulation
         * 
         * @param maxRange double
         */
        public void setMaxRange(double maxRange) {
            this.maxRange = maxRange;
        }
        
        /**
         * Returns the SNR threshold
         * 
         * @return double
         */
		public double getSNR() {
			return SNR;
		}
		
		/**
		 * Sets the SNR threshold
		 * 
		 * @param SNR double
		 */
		public void setSNR(double sNR) {
			SNR = sNR;
		}
	}

}
