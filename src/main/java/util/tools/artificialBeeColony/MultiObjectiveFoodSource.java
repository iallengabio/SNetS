package util.tools.artificialBeeColony;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

/**
 * This class represents the multi objective food source.
 * 
 * Base code at http://mf.erciyes.edu.tr/abc/ and https://github.com/jimsquirt/JAVA-ABC
 * 
 * @author Alexandre
 */
@SuppressWarnings("serial")
public class MultiObjectiveFoodSource implements Comparable<MultiObjectiveFoodSource>, Serializable {

	protected int maxLength;
	protected double nectar[];
	protected int trials;
	protected double fitness;
	protected double fitnessByObjective[];
	protected double selectionProbability;
    
    
    public MultiObjectiveFoodSource(int size, Random rand, double minX[], double maxX[], int objectivesNumber) {
        this.maxLength = size;
        this.nectar = new double[maxLength];
        this.trials = 0;
        this.selectionProbability = 0.0;
        this.fitness = 0.0;
        
        this.fitnessByObjective = new double[objectivesNumber];
        for(int m = 0; m < objectivesNumber; m++){
        	this.fitnessByObjective[m] = 0.0;
        }
        
        initializeNectar(rand, minX, maxX);
    }
    
    public void initializeNectar(Random rand, double minX[], double maxX[]) {
        for(int i = 0; i < maxLength; i++) {
            nectar[i] = minX[i] + rand.nextDouble() * (maxX[i] - minX[i]);
        }
        toEvaluate();
    }
    
    public void toEvaluate(){
    	//System.out.println("*** Evaluating the food ***");
    	//System.out.println("    Simulation start...");
    	
    	this.fitnessByObjective = MainMOABC.runSimulation(nectar, maxLength, fitnessByObjective.length);
    	
    	//System.out.println("    End of simulation.\n");
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
	
	public double[] getFitnessByObjective(){
		return fitnessByObjective;
	}
	
	public double getFitnessByObjective(int m){
		return fitnessByObjective[m];
	}
	
	public void setFitnessByObjective(double fitnessByObjective[]){
		this.fitnessByObjective = fitnessByObjective;
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
	
	/**
	 * Gets the data on a specified index.
	 * 
	 * @param index of data
	 * @return double - value of the data
	 */
	public double getNectar(int index){
		return nectar[index];
	}
	
	/**
	 * Sets the data on a specified index.
	 * 
	 * @param index of data
	 * @param value of the data - double
	 */
	public void setNectar(int index, double value){
		this.nectar[index] = value;
	}
	
	@Override
	public int compareTo(MultiObjectiveFoodSource other) {
		int cont = 0;
		int cont2 = 0;
		
		for(int i = 0; i < maxLength; i++){
			if(nectar[i] == other.getNectar(i)){
				cont++;
				
			}else if(nectar[i] < other.getNectar(i)){
				cont2++;
			}
		}
		
		if(cont == maxLength){
			return 0;
		}
		
		if(cont2 > maxLength / 2){
			return 1;
		}
		
		return -1;
	}
	
	@Override
	public boolean equals(Object o) {
		if((o != null) && (o instanceof MultiObjectiveFoodSource)){
			if(((MultiObjectiveFoodSource)o).getNectar().length == maxLength){
				if(this.compareTo((MultiObjectiveFoodSource)o) == 0){
					return true;
				}
			}
		}
		return false;
	}
	
	public MultiObjectiveFoodSource clone(){
	    Serializable obj = this;
	    ObjectOutputStream out = null;
	    ObjectInputStream in = null;
	    
	    try {
		    ByteArrayOutputStream bout = new ByteArrayOutputStream();
		    out = new ObjectOutputStream(bout);
		    out.writeObject(obj);
		    out.close();
		    
		    ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		    in = new ObjectInputStream(bin);			
		    Object copy = in.readObject();
		    in.close();
		    
		    return (MultiObjectiveFoodSource)copy;
		    
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    	
	    } finally {
			try {
				if(out != null) {
					out.close();
				}
				if(in != null) {
					in.close();
				}
			} catch (IOException ignore) {}
	    }
	    
	    return null;
	}
}
