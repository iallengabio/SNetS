package simulationControl.parsers.parsers;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta classe representa o arquivo de configuração Network, sua representação na forma de entidade é importante para a armazenação e transmissão deste tipo de configução no formato JSON.
 * Created by Iallen on 04/05/2017.
 */
public class NetworkConfig {

    private List<NodeConfig> nodes = new ArrayList<>();
    private List<LinkConfig> links = new ArrayList<>();
    private List<ModulationConfig> modulations = new ArrayList<>();
    private int guardBand=1;



    public List<ModulationConfig> getModulations() {
        return modulations;
    }

    public void setModulations(List<ModulationConfig> modulations) {
        this.modulations = modulations;
    }

    public List<NodeConfig> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeConfig> nodes) {
        this.nodes = nodes;
    }

    public List<LinkConfig> getLinks() {
        return links;
    }

    public void setLinks(List<LinkConfig> links) {
        this.links = links;
    }

    public int getGuardBand() {
        return guardBand;
    }

    public void setGuardBand(int guardBand) {
        this.guardBand = guardBand;
    }

    public static class NodeConfig{

        private String name;
        private int transmiters;
        private int receivers;

        public NodeConfig(String name, int transmiters, int receivers) {
            this.name = name;
            this.transmiters = transmiters;
            this.receivers = receivers;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTransmiters() {
            return transmiters;
        }

        public void setTransmiters(int transmiters) {
            this.transmiters = transmiters;
        }

        public int getReceivers() {
            return receivers;
        }

        public void setReceivers(int receivers) {
            this.receivers = receivers;
        }
    }

    public static class LinkConfig{
        private String source;
        private String destination;
        private int slots;
        private double spectrum;
        private double size;

        public LinkConfig(String source, String destination, int slots, double spectrum, double size) {
            this.source = source;
            this.destination = destination;
            this.slots = slots;
            this.spectrum = spectrum;
            this.size = size;
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

        public int getSlots() {
            return slots;
        }

        public void setSlots(int slots) {
            this.slots = slots;
        }

        public double getSpectrum() {
            return spectrum;
        }

        public void setSpectrum(double spectrum) {
            this.spectrum = spectrum;
        }

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
        }
    }

    public static class ModulationConfig{
        private String name;
        private double bitsPerSimbol;
        private double maxRange;


        public ModulationConfig(String name, double bitsPerSimbol, double maxRange) {
            this.name = name;
            this.bitsPerSimbol = bitsPerSimbol;
            this.maxRange = maxRange;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getBitsPerSimbol() {
            return bitsPerSimbol;
        }

        public void setBitsPerSimbol(double bitsPerSimbol) {
            this.bitsPerSimbol = bitsPerSimbol;
        }

        public double getMaxRange() {
            return maxRange;
        }

        public void setMaxRange(double maxRange) {
            this.maxRange = maxRange;
        }
    }

}
