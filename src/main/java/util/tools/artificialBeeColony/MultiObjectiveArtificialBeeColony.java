package util.tools.artificialBeeColony;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class implements the MOABC (Multi-Objective Artificial Bee Colony) algorithm based on the article:
 *  - An artificial bee colony algorithm for multi-objective optimisation (2017)
 * 
 * @author Alexandre
 */
public class MultiObjectiveArtificialBeeColony implements ABCInterface {

	// ABC PARAMETERS
	private int maxLength; 		// The number of parameters of the problem to be optimized
	private int NP; 			// The number of total bees/colony size. employed + onlookers
    private int foodNumber; 	// The number of food sources equals the half of the colony size
    private int trialLimit;  	// A food source which could not be improved through "limit" trials is abandoned by its employed bee
    private int maxEpoch; 		// The number of cycles for foraging {a stopping criteria}
    
    private double minX[];        // Minimum value for a parameter
    private double maxX[];        // Maximum value for a parameter
    
    private Random rand;
    private ArrayList<FoodSource> foodSources;
    private FoodSource gBest; // Global best
    private FoodSource lBest; // Local best
    private int epoch;
    
    private double Pm;
    private double F;
    
    private int objectivesNumber;
    
    private ArrayList<FoodSource> archive;
    private int arcSize;
    
    private double paretoFront[][];
    
    private List<Double> IGDepoch;
    
    /**
     * Instantiates the adaptive artificial bee colony algorithm along with its parameters.
     * 
     * @param n int - numger of parameters (dimension) of the problem
     * @param minX double - Minimum value for a dimension
     * @param maxX double - Maximum value for a dimension
     */
    public MultiObjectiveArtificialBeeColony(int n, double minX[], double maxX[], int objectivesNumber, String pathPF) {
    	this.maxLength = n;
    	this.NP = 100;
    	this.foodNumber = NP / 2;
    	this.trialLimit = 200;
    	this.maxEpoch = 5000;
    	this.epoch = 0;
        
        this.minX = minX;
        this.maxX = maxX;
        
        this.Pm = 0.5;
		this.F = 0.5;
		
		this.objectivesNumber = objectivesNumber;
		
		this.arcSize = NP;
		this.archive = new ArrayList<>();
		
		this.IGDepoch = new ArrayList<>();
		
		readParetoFront(pathPF);
    }

    /**
     * This method executes the ABC algorithm
     * 
     * @return boolean
     */
    public boolean execute() {
    	foodSources = new ArrayList<>();
        rand = new Random();
        epoch = 0;
        boolean done = false;

        initialize();
        //memorizeBestFoodSource();

        while(!done) {
            if(epoch < maxEpoch) {
                //if(gBest.isSatisfiedCondition()) {
                //    done = true;
                //}
                sendEmployedBees();
                calculateProbabilities();
                sendOnlookerBees();
                sendScoutBees();
                
                //memorizeBestFoodSource();
                maintainingArchive();
                
                invertedGenerationalDistance(foodSources);
                epoch++;
                
                // This is here simply to show the runtime status.
                System.out.println("Epoch: " + epoch);
            } else {
                done = true;
            }
        }
        
        invertedGenerationalDistance(archive);
        
        System.out.println("done.");
        System.out.println("Completed " + epoch + " epochs.");
        
        return done;
    }

    /**
     * Sends the employed bees to optimize the solution
     */
    public void sendEmployedBees() {
        int neighborBeeIndex = 0;
        FoodSource currentBee = null;
        FoodSource neighborBee = null;
        
        for(int i = 0; i < foodNumber; i++) {
        	
        	//randomly chosen food source different from the i-th
        	neighborBeeIndex = getExclusiveRandomNumber(foodNumber, i);
            currentBee = foodSources.get(i);
            neighborBee = foodSources.get(neighborBeeIndex);
        	
            sendToWork(currentBee, neighborBee);
        }
    }

    /**
     * Sends the onlooker bees to optimize the solution.
     * Onlooker bees work on the best solutions from the employed bees.
     * Best solutions have high selection probability.
     */
    public void sendOnlookerBees() {
    	int i = 0;
        int t = 0;
        int neighborBeeIndex = 0;
        FoodSource currentBee = null;
        FoodSource neighborBee = null;

        while(t < foodNumber) {
            currentBee = foodSources.get(i);
            
            if(rand.nextDouble() < currentBee.getSelectionProbability()) {
                t++;
                
                //randomly chosen food source different from the i-th
                neighborBeeIndex = getExclusiveRandomNumber(foodNumber, i);
	            neighborBee = foodSources.get(neighborBeeIndex);
	            
	            sendToWork(currentBee, neighborBee);
            }
            
            i++;
            if(i == foodNumber) {
                i = 0;
            }
        }
    }

    /**
     * The optimization part of the algorithm.
     * Improves the current bee by choosing a random neighbor bee. 
     * The changes is a randomly generated number of times to try and improve the current solution.
     * 
     * @param currentBee FoodSource
     * @param neighborBee FoodSource
     */
    public void sendToWork(FoodSource currentBee, FoodSource neighborBee) {
    	double newValue = 0;
    	
    	//The parameter (dimension) to be changed is determined randomly
        int parameterToChange = getRandomNumber(0, maxLength - 1);
        double currentFitnessByObjective[] = currentBee.getFitnessByObjective();
        double currentFitness = computesFiness(currentBee, foodSources);
        
        double currentValue = currentBee.getNectar(parameterToChange);
        double neighborValue = neighborBee.getNectar(parameterToChange);
        
        //Produce a new candidate solution
        if(rand.nextDouble() < Pm) {
        	// Vij = Xr2j + F * (Xr1j - Xr3j)
        	FoodSource neighborBeeR2 = getExclusiveFoodSource(currentBee, neighborBee, null);
        	double neighborValue2 = neighborBeeR2.getNectar(parameterToChange);
        	
        	FoodSource neighborBeeR3 = getExclusiveFoodSource(currentBee, neighborBee, neighborBeeR2);
        	double neighborValue3 = neighborBeeR3.getNectar(parameterToChange);
        	
        	newValue = neighborValue2 + F * (neighborValue - neighborValue3);
        	
        } else {
        	// Vij = Xij + fi * (Xij - Xr1j)
	        double fi = (rand.nextDouble() - 0.5) * 2.0; //It is a random real number within the ranger [-1, 1]
	        newValue = currentValue + fi * (currentValue - neighborValue);
        }
        
        //Trap the value within upper bound and lower bound limits
        if(newValue < minX[parameterToChange]){
        	newValue = minX[parameterToChange];
        }
        if(newValue > maxX[parameterToChange]){
        	newValue = maxX[parameterToChange];
        }
        
        //Evaluate the new candidate solution
        currentBee.setNectar(parameterToChange, newValue);
        currentBee.toEvaluate();
        double newFitness = computesFiness(currentBee, foodSources);
        currentBee.setFitness(newFitness);
        
        //Greedy selection
        if(newFitness <= currentFitness) { //No improvement
            currentBee.setNectar(parameterToChange, currentValue);
            currentBee.setFitnessByObjective(currentFitnessByObjective);
            currentBee.setFitness(currentFitness);
            currentBee.setTrials(currentBee.getTrials() + 1);
            
        } else { //Improved solution, newFitness > currentFitness
            //addToArchive(currentBee);
            currentBee.setTrials(0);
        }
    }

    /**
     * Finds food sources which have been abandoned/reached the limit.
     * Scout bees will generate a totally random solution from the existing and it will also reset its trials back to zero.
     */
    public void sendScoutBees() {
    	FoodSource currentBee = null;
        
        for(int i = 0; i < foodNumber; i++) {
            currentBee = foodSources.get(i);
            
            if(currentBee.getTrials() > trialLimit) {
            	currentBee.initializeNectar(rand, minX, maxX);
            	double newFitness = computesFiness(currentBee, foodSources);
                currentBee.setFitness(newFitness);
                currentBee.setTrials(0);
            }
        }
    }
    
    public double computesFiness(FoodSource currentSource, ArrayList<FoodSource> foodSources){
    	List<Double> fie = new ArrayList<>();
    	FoodSource thisFood = null;
    	double s = 0.01;
    	double c = Double.NEGATIVE_INFINITY;
    	double sum = 0.0;
    	
    	for(int i = 0; i < foodSources.size(); i++) {
    		thisFood = foodSources.get(i);
            
            if(!thisFood.equals(currentSource)){
            	double epsilon = Double.NEGATIVE_INFINITY;
            	
            	for(int m = 0; m < objectivesNumber; m++){
            		double a = currentSource.getFitnessByObjective(m);
            		double b = thisFood.getFitnessByObjective(m);
            		
            		double e =  b - a;
            		if(e > epsilon){
            			epsilon = e;
            		}
            	}
            	
	            if(epsilon > c){
	            	c = epsilon;
	            }
	            
	            fie.add(epsilon);
    		}
    	}
    	
    	for(int i = 0; i < fie.size(); i++) {
            double ie = fie.get(i);
            double exp = -1.0 * (ie / (c * s));
            sum += -1.0 * Math.pow(Math.E, exp);
    	}
    	
    	return sum;
    }
    
    /**
     * Sets the selection probability of each solution.
     * The higher the fitness the greater the probability.
     */
	public void calculateProbabilities() {
		FoodSource thisFood;
		List<FoodSource> rank = new ArrayList<>(foodNumber);
		double a = 0.5;
		double sum = 0.0;
        
		// Initializes rank
        for(int i = 0; i < foodNumber; i++) {
        	thisFood = foodSources.get(i);
            double fie = computesFiness(thisFood, foodSources);
            thisFood.setFitness(fie);
            rank.add(thisFood);
            
            sum += Math.pow((i + 1), (-1.0 * a));
        }
        
        // Sort descending rank
        for(int i = 1; i < foodNumber; i++) {
        	thisFood = rank.get(i);
            double key = thisFood.getFitness();
            int j = i;
            
            while((j > 0) && (rank.get(j - 1)).getFitness() < key){
            	rank.set(j, rank.get(j - 1));
            	j -= 1;
            }
            
            rank.set(j, thisFood);
        }
        
        // Calculating the probability
        for(int i = 0; i < foodNumber; i++) {
        	thisFood = rank.get(i);
        	double prob = Math.pow((i + 1), (-1.0 * a)) / sum;
        	thisFood.setSelectionProbability(prob);
        }
    }
	
	public void addToArchive(FoodSource foodSource){
		for(int i = 0; i < archive.size(); i++){
			if(archive.get(i).equals(foodSource)){ // Is already on archive
				return;
			}
		}
		archive.add(foodSource.clone()); // Add to archive
	}
	
	/**
	 * Verifies whether food2 dominates food1
	 * 
	 * @param food1 FoodSource
	 * @param food2 FoodSource
	 * @return boolean
	 */
	public boolean checksForDominance(FoodSource food1, FoodSource food2){
		int cont = 0;
		boolean flag = false;
		boolean dominated = false;
		
    	for(int m = 0; m < objectivesNumber; m++){
    		double a = food1.getFitnessByObjective(m);
    		double b = food2.getFitnessByObjective(m);
    		
    		// Verifying whether food2 dominates food1
    		if(b <= a){
    			cont++;
    			if(b < a){
    				flag = true;
    			}
    		}
    	}
    	
    	if((cont == objectivesNumber) && flag){
    		dominated = true;
    	}
    	
    	return dominated;
	}
	
	public void maintainingArchive(){
		List<FoodSource> nonDominated = new ArrayList<>();
		FoodSource food1, food2;
		
		// Select all the non-dominated solutions in the population as P1
		for(int i = 0; i < foodNumber; i++) {
        	food1 = foodSources.get(i);
        	boolean dominated = false;
        	
        	for(int j = 0; j < foodNumber; j++) {
        		food2 = foodSources.get(j);
        		
        		if(!food1.equals(food2)){
		        	if(checksForDominance(food1, food2)){
		        		dominated = true;
		        		break;
		        	}
        		}
        	}
        	
        	if(!dominated){ //non-dominated
        		nonDominated.add(food1);
        	}
		}
		
		// Merge the archive A with P1 into A1
		for(int i = 0; i < nonDominated.size(); i++) {
        	food1 = nonDominated.get(i);
        	addToArchive(food1);
		}
		
		// Removes excess solutions
		while(archive.size() > arcSize){
			
			// Calculate the fitness
	        for(int i = 0; i < archive.size(); i++) {
	            double fie = computesFiness(archive.get(i), archive);
	            archive.get(i).setFitness(fie);
	        }
	        
	        // Sort descending archive
	        for(int i = 1; i < archive.size(); i++) {
	        	FoodSource food = archive.get(i);
	            double key = food.getFitness();
	            int j = i;
	            
	            while((j > 0) && (archive.get(j - 1)).getFitness() < key){
	            	archive.set(j, archive.get(j - 1));
	            	j -= 1;
	            }
	            
	            archive.set(j, food);
	        }
	        
	        // Removes the solution with the least fitness
	        archive.remove(archive.size() - 1);
		}
	}

	/**
	 * Initializes food locations
	 */
    public void initialize() {
        for(int i = 0; i < foodNumber; i++) {
        	FoodSource newFoodSource = new FoodSource(maxLength, rand, minX, maxX, objectivesNumber);
        	foodSources.add(newFoodSource);
        }
    }

    /**
     * Gets a random number in the range of the parameters
     * 
     * @param low int -  the minimum random number
     * @param high int - the maximum random number
     * @return int
     */
    public int getRandomNumber(int low, int high) {
        return (int)Math.round((high - low) * rand.nextDouble() + low);
    }

    /**
     * Gets a random number with the exception of the parameter
     * 
     * @param high int - the maximum random number
     * @param except int - number to to be chosen
     * @return int
     */
    public int getExclusiveRandomNumber(int high, int except) {
        boolean done = false;
        int getRand = 0;

        while(!done) {
            getRand = rand.nextInt(high);
            if(getRand != except){
                done = true;
            }
        }

        return getRand;     
    }
    
    /**
     * Gets a random food source with the exception of the parameters
     * 
     * @param foodSource1 - Food that should not be selected
     * @param foodSource2 - Food that should not be selected
     * @return FoodSource
     */
    public FoodSource getExclusiveFoodSource(FoodSource foodSource1, FoodSource foodSource2, FoodSource foodSource3){
    	boolean done = false;
    	int getRand = 0;
    	FoodSource food = null;
    	
    	while(!done){
	    	getRand = rand.nextInt(foodNumber);
	    	food = foodSources.get(getRand);
	    	if((food != null) && (!food.equals(foodSource1)) && (!food.equals(foodSource2)) && (!food.equals(foodSource3))){
	    		done = true;
	    	}
    	}
    	
    	return food;
    }
    
    
    /**
     * Memorizes the best solution
     */
    public void memorizeBestFoodSource() {
    	double foodFitness = computesFiness(foodSources.get(0), foodSources);
    	foodSources.get(0).setFitness(foodFitness);
    	
    	lBest = foodSources.get(0).clone();
    	
    	for(int i = 1; i < foodNumber; i++) {
    		foodFitness = computesFiness(foodSources.get(i), foodSources);
    		foodSources.get(i).setFitness(foodFitness);
    		
    		if(lBest.getFitness() < foodFitness){
    			lBest = foodSources.get(i).clone();
    		}
    	}
    	
    	if((gBest == null) || ((gBest != null) && (lBest.getFitness() > gBest.getFitness()))){
    		gBest = lBest.clone();
    	}
    	
    	//Save the best solution
    	//MainABC.saveBestSolution(gBest.getNectar());
    	
    	
//    	for(int i = 0; i < foodNumber; i++) {
//    		FoodSource food = foodSources.get(i);
//    		System.out.println(food.getFitness() + "," + food.getFitnessByObjective(0) + "," + food.getFitnessByObjective(1));
//    	}
//    	System.out.println("para aqui");
    }

	/**
	 * Returns the best solution
	 * 
	 * @return the gBest
	 */
	public FoodSource getgBest() {
		return gBest;
	}
	
	public void invertedGenerationalDistance(ArrayList<FoodSource> foodSources){
		double sumDist = 0.0;
		for(int i = 0; i < paretoFront.length; i++){
			
			double minDist = Double.MAX_VALUE;
			for(int j = 0; j < foodSources.size(); j++){
				
				double dist = 0.0;
				for(int m = 0; m < objectivesNumber; m++){
					dist += Math.pow(paretoFront[i][m] - foodSources.get(j).getFitnessByObjective(m), 2);
				}
				dist = Math.sqrt(dist);
				
				if(dist < minDist){
					minDist = dist;
				}
			}
			
			sumDist += minDist;
		}
		
		double igd = Math.sqrt(sumDist) / (double)paretoFront.length;
		IGDepoch.add(igd);
	}

	public void readParetoFront(String path){
		paretoFront = new double[1000][2];
		
		try {
			FileReader fr = new FileReader(path);
			BufferedReader in = new BufferedReader(fr);
			
			int cont = 0;
			while(in.ready()){
				String linha[] = in.readLine().split("\t");
				
				Double f1 = Double.parseDouble(linha[0]);
				Double f2 = Double.parseDouble(linha[1]);
				
				paretoFront[cont][0] = f1;
				paretoFront[cont][1] = f2;
				cont++;
			}
			
			in.close();
		    fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the archive
	 */
	public ArrayList<FoodSource> getArchive() {
		return archive;
	}

	/**
	 * @return the iGDepoch
	 */
	public List<Double> getIGDepoch() {
		return IGDepoch;
	}

	
}
