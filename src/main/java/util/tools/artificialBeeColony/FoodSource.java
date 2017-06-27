package util.tools.artificialBeeColony;

import java.util.Random;

/**
 * This class represents the food source.
 * 
 * Base code at http://mf.erciyes.edu.tr/abc/ and https://github.com/jimsquirt/JAVA-ABC
 * 
 * @author Alexandre
 */
public class FoodSource {

	private int maxLength;
	private double nectar[];
    private int trials;
    private double fitness;
    private double selectionProbability;
    	
    
    public FoodSource(int size, Random rand, double minX, double maxX) {
        this.maxLength = size;
        this.nectar = new double[maxLength];
        this.trials = 0;
        this.fitness = 0.0;
        this.selectionProbability = 0.0;
        
        initializeNectar(rand, minX, maxX);
    }
    
    public void initializeNectar(Random rand, double minX, double maxX) {
        for(int i = 0; i < maxLength; i++) {
            nectar[i] = minX + rand.nextDouble() * (maxX - minX);
        }
    }
    
    public void toEvaluate(){
    	System.out.println("*** Evaluating the food ***");
    	System.out.println("    Simulation start...");
    	
    	this.fitness = MainABC.runSimulation(nectar);
    	
    	System.out.println("    End of simulation.\n");
    }

    public boolean isSatisfiedCondition(){
    	return false;
    }

	/**
	 * @return the maxLength
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * @return the nectar
	 */
	public double[] getNectar() {
		return nectar;
	}

	/**
	 * @param nectar the nectar to set
	 */
	public void setNectar(double[] nectar) {
		this.nectar = nectar;
	}

	/**
	 * @return the trials
	 */
	public int getTrials() {
		return trials;
	}

	/**
	 * @param trials the trials to set
	 */
	public void setTrials(int trials) {
		this.trials = trials;
	}

	/**
	 * @return the fitness
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * @param fitness the fitness to set
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * @return the selectionProbability
	 */
	public double getSelectionProbability() {
		return selectionProbability;
	}

	/**
	 * @param selectionProbability the selectionProbability to set
	 */
	public void setSelectionProbability(double selectionProbability) {
		this.selectionProbability = selectionProbability;
	}
	
}
