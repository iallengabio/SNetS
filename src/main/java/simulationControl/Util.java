package simulationControl;

import java.util.HashSet;
import java.util.Set;

import network.Pair;

/**
 * This class should record the pairs, bandwidths and other values necessary for 
 * recording the results of the simulation on file
 * 
 * @author Iallen
 */
public class Util {
	
	public Set<Double> bandwidths = new HashSet<>();
	
	public Set<Pair> pairs = new HashSet<>();
	
	public String projectPath = "";
	
	
	public void reset(){
		bandwidths = new HashSet<>();
		pairs = new HashSet<Pair>();
	}
}
