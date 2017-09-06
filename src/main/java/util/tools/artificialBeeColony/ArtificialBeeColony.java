package util.tools.artificialBeeColony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This class represents the Artificial Bee Colony (ABC) algorithm.
 * 
 * Code inspired by the java code for abc algorithm at artificial bee colony's website 
 * found at http://mf.erciyes.edu.tr/abc/ and https://github.com/jimsquirt/JAVA-ABC.
 *
 * @author Alexandre
 */
public class ArtificialBeeColony {

	// ABC PARAMETERS
	private int maxLength; 		// The number of parameters of the problem to be optimized
	private int NP; 			// The number of total bees/colony size. employed + onlookers
    private int foodNumber; 	// The number of food sources equals the half of the colony size
    private int trialLimit;  	// A food source which could not be improved through "limit" trials is abandoned by its employed bee
    private int maxEpoch; 		// The number of cycles for foraging {a stopping criteria}
    
    private double minX;        // Minimum value for a parameter
    private double maxX;        // Maximum value for a parameter
    
    private Random rand;
    private ArrayList<FoodSource> foodSources;
    private FoodSource gBest; // Global best
    private int epoch;

    /**
     * Instantiates the artificial bee colony algorithm along with its parameters.
     * 
     * @param n int - numger of parameters (dimension) of the problem
     * @param minX double - Minimum value for a dimension
     * @param maxX double - Maximum value for a dimension
     */
    public ArtificialBeeColony(int n, double minX, double maxX) {
    	this.maxLength = n;
    	this.NP = 40;
    	this.foodNumber = NP/2;
    	this.trialLimit = 200;
    	this.maxEpoch = 1000;
    	this.gBest = null;
    	this.epoch = 0;
        
        this.minX = minX;
        this.maxX = maxX;
    }

    /**
     * This method executes the ABC algorithm
     * 
     * @return boolean
     */
    public boolean execute() {
    	foodSources = new ArrayList<FoodSource>();
        rand = new Random();
        epoch = 0;
        boolean done = false;

        initialize();
        memorizeBestFoodSource();

        while(!done) {
            if(epoch < maxEpoch) {
                if(gBest.isSatisfiedCondition()) {
                    done = true;
                }
                sendEmployedBees();
                calculateProbabilities();
                sendOnlookerBees();
                sendScoutBees();
                memorizeBestFoodSource();
                
                epoch++;
                
                // This is here simply to show the runtime status.
                System.out.println("Epoch: " + epoch);
            } else {
                done = true;
            }
        }
        
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
        //The parameter (dimension) to be changed is determined randomly
        int parameterToChange = getRandomNumber(0, maxLength - 1);
        
        //Produce a new candidate solution
        double currentFitness = currentBee.getFitness();
        double currentValue = currentBee.getNectar(parameterToChange);
        double neighborValue = neighborBee.getNectar(parameterToChange);
        double rid = (rand.nextDouble() - 0.5) * 2.0; //It is a random real number within the ranger [-1, 1]
        double newValue = currentValue + rid * (currentValue - neighborValue);
        
        //Trap the value within upper bound and lower bound limits
        if(newValue < minX){
        	newValue = minX;
        }
        if(newValue > maxX){
        	newValue = maxX;
        }
        
        //Evaluate the new candidate solution
        currentBee.setNectar(parameterToChange, newValue);
        currentBee.toEvaluate();
        double newFitness = currentBee.getFitness();
        
        //Greedy selection
        if(currentFitness <= newFitness) { //No improvement
            currentBee.setNectar(parameterToChange, currentValue);
            currentBee.setTrials(currentBee.getTrials() + 1);
            currentBee.setFitness(currentFitness);
            
        } else { //Improved solution
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
                currentBee.setTrials(0);
            }
        }
    }
    
    /**
     * Sets the selection probability of each solution.
     * The higher the fitness the greater the probability.
     */
	public void calculateProbabilities() {
		HashMap<FoodSource, Double> fit = new HashMap<>();
		FoodSource thisFood = null;
        double sum = 0.0;
        
        for(int i = 0; i < foodNumber; i++) {
            thisFood = foodSources.get(i);
            double fiti = 0.0;
            
            if(thisFood.getFitness() >= 0.0) {
            	fiti = 1.0 / (1.0 + thisFood.getFitness());
                
            } else {
            	fiti = 1.0 + Math.abs(thisFood.getFitness());
            }
            
            sum += fiti;
        	fit.put(thisFood, fiti);
        }
        
        for(int i = 0; i < foodNumber; i++) {
            thisFood = foodSources.get(i);
            thisFood.setSelectionProbability(fit.get(thisFood) / sum);
        }
    }

	/**
	 * Initializes food locations
	 */
    public void initialize() {
        for(int i = 0; i < foodNumber; i++) {
        	FoodSource newFoodSource = new FoodSource(maxLength, rand, minX, maxX, 1);
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
     * Memorizes the best solution
     */
    public void memorizeBestFoodSource() {
    	if((gBest == null) || ((gBest != null) && (foodSources.get(0).getFitness() < gBest.getFitness()))){
    		gBest = foodSources.get(0);
    	}
    	
    	for(int i = 1; i < foodNumber; i++) {
    		if(gBest.getFitness() > foodSources.get(i).getFitness()){
    			gBest = foodSources.get(i);
    		}
    	}
    	
    	//Save the best solution
    	MainABC.saveBestSolution(gBest.getNectar());
    }

	/**
	 * Returns the best solution
	 * 
	 * @return the gBest
	 */
	public FoodSource getgBest() {
		return gBest;
	}

    
}