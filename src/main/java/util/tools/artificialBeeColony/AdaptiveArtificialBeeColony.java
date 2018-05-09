package util.tools.artificialBeeColony;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This class represents the Adaptive Artificial Bee Colony (ABC) algorithm based on the article:
 * - An adaptive artificial bee colony algorithm for global optimization (2015)
 * 
 * @author Alexandre
 */
public class AdaptiveArtificialBeeColony {

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
    private FoodSource lBest; // Local best
    private int epoch;
    private int epochGBest;
    
    // For search rules
    private int rulesNumber;
    private int successfulAttempts[];
    private int totalAttempts[];
    private double successRate[];
    private double probabilitySelecting[];
    
    // Limits to psi
    private double minPsi;
    private double maxPsi;
    
    // Limits to F1
    private double minF1;
    private double maxF1;
    
    // Limits to F2
    private double minF2;
    private double maxF2;
    
    // Memory length
    private int ML;
    
    private String pathFileOfSimulation; //way to save the state of an epoch
    
    /**
     * Instantiates the adaptive artificial bee colony algorithm along with its parameters.
     * 
     * @param n int - numger of parameters (dimension) of the problem
     * @param minX double - Minimum value for a dimension
     * @param maxX double - Maximum value for a dimension
     */
    public AdaptiveArtificialBeeColony(int n, double minX, double maxX) {
    	this.maxLength = n;
    	this.NP = 40;
    	this.foodNumber = NP / 2;
    	this.trialLimit = 200;
    	this.maxEpoch = 1000;
    	this.gBest = null;
    	this.epoch = 0;
        
        this.minX = minX;
        this.maxX = maxX;
        
        this.rulesNumber = 6;
        this.successfulAttempts = new int[rulesNumber];
        this.totalAttempts = new int[rulesNumber];
        this.successRate = new double[rulesNumber];
        this.probabilitySelecting = new double[rulesNumber];
        
        for(int i = 0; i < rulesNumber; i++){
        	successfulAttempts[i] = 0;
        	totalAttempts[i] = 0;
        	successRate[i] = 0.0;
        	probabilitySelecting[i] = 0.0;
        }
        
        this.minPsi = 0.0;
        this.maxPsi = 1.5;
        this.minF1 = 0.0;
        this.maxF1 = 1.6;
        this.minF2 = 0.0;
        this.maxF2 = 0.4;
        
        this.ML = 5000;
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
                
                long beg = System.nanoTime();
                
                sendEmployedBees();
                calculateProbabilities();
                sendOnlookerBees();
                sendScoutBees();
                memorizeBestFoodSource();
                
                epoch++;
                
                // This is here simply to show the runtime status.
                System.out.println("Epoch: " + epoch);
                long end = System.nanoTime();
        		long time = end - beg;
        		System.out.println("Total time of epoch: " + time/1000000000.0 + "s");
        		System.out.println("-------------------\n");
        		
                stateEpochSave();
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
    	System.out.println("\n---- sendEmployedBees ----");
    	
        int neighborBeeIndex = 0;
        FoodSource currentBee = null;
        FoodSource neighborBee = null;
        
        for(int i = 0; i < foodNumber; i++) {
        	
        	//randomly chosen food source different from the i-th
        	neighborBeeIndex = getExclusiveRandomNumber(foodNumber, i);
            currentBee = foodSources.get(i);
            neighborBee = foodSources.get(neighborBeeIndex);
        	
            sendToWork(currentBee, neighborBee, false);
        }
    }
    
    /**
     * Sends the onlooker bees to optimize the solution.
     * Onlooker bees work on the best solutions from the employed bees.
     * Best solutions have high selection probability.
     */
    public void sendOnlookerBees() {
    	System.out.println("\n---- sendOnlookerBees ----");
    	
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
	            
	            sendToWork(currentBee, neighborBee, true);
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
    public void sendToWork(FoodSource currentBee, FoodSource neighborBee, boolean isOnlookerBeePhase) {
    	double newValue = 0;
    	
    	//The parameter (dimension) to be changed is determined randomly
        int parameterToChange = getRandomNumber(0, maxLength - 1);
        
        double currentFitness = currentBee.getFitness();
        double currentValue = currentBee.getNectar(parameterToChange);
        double neighborValue = neighborBee.getNectar(parameterToChange);
        
        //Randomly select the search rule
        int s = getRandomNumber(0, rulesNumber - 1);
        
        //For onlooker bee phase
        if(isOnlookerBeePhase){
        	//Choose the search strategy according to the adaptive search strategy
        	double sumSuccessRate = 0.0;
        	for(int i = 0; i < rulesNumber; i++){
        		successRate[i] = (double)successfulAttempts[i] / (double)totalAttempts[i];
        		sumSuccessRate += successRate[i];
        	}
        	
        	double sumProbabilitySelecting = 0.0;
        	for(int i = 0; i < rulesNumber; i++){
        		probabilitySelecting[i] = successRate[i] / sumSuccessRate;
        		sumProbabilitySelecting += probabilitySelecting[i];
        	}
        	
        	//Determine s by roulette wheel
        	double selection = rand.nextDouble() * sumProbabilitySelecting;
        	for(int i = 0; i < rulesNumber; i++){
        		selection -= probabilitySelecting[i];
        		if(selection < 0.0){
        			s = i;
        			break;
        		}
        	}
        }
        
        //Produce a new candidate solution using search rule s
        if(s == 0){
        	// Vij = Xij + fi * (Xij - Xr1j)
	        double fi = (rand.nextDouble() - 0.5) * 2.0; //It is a random real number within the ranger [-1, 1]
	        newValue = currentValue + fi * (currentValue - neighborValue);
	        
        }else if(s == 1){
        	// Vij = Xij + fi * (Xij - Xr1j) + psi * (Xgbestj - Xij)
        	double fi = (rand.nextDouble() - 0.5) * 2.0; //It is a random real number within the ranger [-1, 1]
        	double psi = minPsi + rand.nextDouble() * (maxPsi - minPsi); //It is a random real number within the specific ranger
        	double gbestValue = gBest.getNectar(parameterToChange);
	        newValue = currentValue + fi * (currentValue - neighborValue) + psi * (gbestValue - currentValue);
        	
        }else if(s == 2){
        	// Vij = Xlbestj + fi * (Xr1j - Xr2j)
        	double fi = (rand.nextDouble() - 0.5) * 2.0; //It is a random real number within the ranger [-1, 1]
        	double lbestValue = lBest.getNectar(parameterToChange);
        	FoodSource neighborBeeR2 = getExclusiveFoodSource(currentBee, neighborBee);
        	double neighborValue2 = neighborBeeR2.getNectar(parameterToChange);
        	newValue = lbestValue + fi * (neighborValue - neighborValue2);
        	
        }else if(s == 3){
        	// Vij = Xlbestj + fi * (Xij - Xr1j)
        	double fi = (rand.nextDouble() - 0.5) * 2.0; //It is a random real number within the ranger [-1, 1]
        	double lbestValue = lBest.getNectar(parameterToChange);
        	newValue = lbestValue + fi * (currentValue - neighborValue);
        	
        }else if(s == 4){
        	// Vij = Xr1j + fi2 * (Xlbestj - Xr1j)
        	double fi2 = rand.nextDouble(); //It is a random real number within the ranger [0, 1]
        	double lbestValue = lBest.getNectar(parameterToChange);
        	newValue = neighborValue + fi2 * (lbestValue - neighborValue);
        	
        }else{ // s == 5
        	// Vij = Xij + F1 * (Xlbestj - Xij) + F2 * (Xr1j - Xr2j)
        	double F1 = minF1 + rand.nextDouble() * (maxF1- minF1);; //It is a random real number within the specific ranger
        	double F2 = minF2 + rand.nextDouble() * (maxF2 - minF2);; //It is a random real number within the specific ranger
        	double lbestValue = lBest.getNectar(parameterToChange);
        	FoodSource neighborBeeR2 = getExclusiveFoodSource(currentBee, neighborBee);
        	double neighborValue2 = neighborBeeR2.getNectar(parameterToChange);
        	newValue = currentValue + F1 * (lbestValue - currentValue) + F2 * (neighborValue - neighborValue2);
        }
        
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
            
            //Update the number of success for s
            successfulAttempts[s] = successfulAttempts[s] + 1;
        }
        
        //Update the number of total attempts for s
        totalAttempts[s] = totalAttempts[s] + 1;
        
        //For onlooker bee phase
        if(isOnlookerBeePhase){
        	int sumTA = 0;
        	for(int i = 0; i < rulesNumber; i++){
        		sumTA += totalAttempts[i];
        	}
        	
        	if(sumTA > ML){
        		for(int i = 0; i < rulesNumber; i++){
                	successfulAttempts[i] = 0;
                	totalAttempts[i] = 0;
                }
        	}
        }
    }

    /**
     * Finds food sources which have been abandoned/reached the limit.
     * Scout bees will generate a totally random solution from the existing and it will also reset its trials back to zero.
     */
    public void sendScoutBees() {
    	System.out.println("\n---- sendScoutBees ----");
    	
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
        
        for(int j = 0; j < foodNumber; j++) {
            thisFood = foodSources.get(j);
            thisFood.setSelectionProbability(fit.get(thisFood) / sum);
        }
    }

	/**
	 * Initializes food locations
	 */
    public void initialize() {
    	System.out.println("---- Initialize ----");
    	
    	if(!stateEpochReader()){ //there is no file with configuration of an epoch
    		for(int i = 0; i < foodNumber; i++) {
            	FoodSource newFoodSource = new FoodSource(maxLength, rand, minX, maxX);
            	foodSources.add(newFoodSource);
            }	
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
    public FoodSource getExclusiveFoodSource(FoodSource foodSource1, FoodSource foodSource2){
    	boolean done = false;
    	int getRand = 0;
    	FoodSource food = null;
    	
    	while(!done){
	    	getRand = rand.nextInt(foodNumber);
	    	food = foodSources.get(getRand);
	    	if((food != null) && (!food.equals(foodSource1)) && (!food.equals(foodSource2))){
	    		done = true;
	    	}
    	}
    	
    	return food;
    }
    
    
    /**
     * Memorizes the best solution
     */
    public void memorizeBestFoodSource() {
    	lBest = foodSources.get(0);
    	for(int i = 1; i < foodNumber; i++) {
    		if(lBest.getFitness() > foodSources.get(i).getFitness()){
    			lBest = foodSources.get(i);
    		}
    	}
    	
    	boolean changed = false;
    	if((gBest == null) || ((gBest != null) && (lBest.getFitness() < gBest.getFitness()))){
    		gBest = lBest;
    		changed = true;
    	}
    	
    	//Save the best solution
    	if(changed){
    		epochGBest = epoch;
    		MainABC.saveBestSolution(gBest.getNectar(), epochGBest); //Save the best solution
    	}
    }

	/**
	 * Returns the best solution
	 * 
	 * @return the gBest
	 */
	public FoodSource getgBest() {
		return gBest;
	}
	
	/**
	 * Returns the epoch of gBest
	 * 
	 * @return the epochGBest
	 */
	public int getEpochGBest(){
		return epochGBest;
	}
	
	public void setPathFileOfSimulation(String pathFileOfSimulation){
		this.pathFileOfSimulation = pathFileOfSimulation;
	}
	
	public void stateEpochSave(){
		String separator = System.getProperty("file.separator");
		String path = pathFileOfSimulation + separator + "stateEpoch.txt";
		
		try {
			FileWriter fw = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fw);
			
			StringBuilder sb1 = new StringBuilder();
			sb1.append("epoch:" + epoch + ";");
			sb1.append("successfulAttempts:");
			for(int i = 0; i < successfulAttempts.length; i++){
            	sb1.append(successfulAttempts[i]);
            	if(i < successfulAttempts.length - 1){
					sb1.append("&");
				}
            }
			sb1.append(";totalAttempts:");
			for(int i = 0; i < totalAttempts.length; i++){
				sb1.append(totalAttempts[i]);
            	if(i < totalAttempts.length - 1){
					sb1.append("&");
				}
            }
			sb1.append("\n");
			
			sb1.append("gBestFoodSource:" + epochGBest + ";");
            sb1.append("trials:" + gBest.getTrials() + ";");
            sb1.append("fitness:" + gBest.getFitness() + ";");
            sb1.append("selectionProbability:" + gBest.getSelectionProbability() + ";");
            
            sb1.append("nectar:");
            double nectarGBest[] = gBest.getNectar();
            for(int n = 0; n < nectarGBest.length; n++) {
            	sb1.append(String.valueOf(nectarGBest[n]));
            	if(n < nectarGBest.length - 1){
					sb1.append("&");
				}
            }
            sb1.append("\n");
            
            out.append(sb1.toString());
			
			for(int i = 0; i < foodNumber; i++) {
				StringBuilder sb = new StringBuilder();
	            FoodSource currentBee = foodSources.get(i);
	            
	            sb.append("foodSource:" + (i + 1) + ";");
	            sb.append("trials:" + currentBee.getTrials() + ";");
	            sb.append("fitness:" + currentBee.getFitness() + ";");
	            sb.append("selectionProbability:" + currentBee.getSelectionProbability() + ";");
	            
	            sb.append("nectar:");
	            double nectar[] = currentBee.getNectar();
	            for(int n = 0; n < nectar.length; n++) {
	            	sb.append(String.valueOf(nectar[n]));
	            	if(n < nectar.length - 1){
						sb.append("&");
					}
	            }
	            sb.append("\n");
	            
				out.append(sb.toString());
			}
			
			out.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean stateEpochReader(){
		String separator = System.getProperty("file.separator");
		String path = pathFileOfSimulation + separator + "stateEpoch.txt";
		
    	if(Paths.get(path).toFile().exists()){ //there is file with configuration of an epoch
	    	try {
				FileReader fr = new FileReader(path);
				BufferedReader in = new BufferedReader(fr);
				
				while(in.ready()){
					String linha[] = in.readLine().split(";");
					String info[] = linha[0].split(":");
					
					if(info[0].equals("epoch")){
						this.epoch = Integer.valueOf(info[1]);
						
						for(int i = 1; i < linha.length; i++){
							String attribute[] = linha[i].split(":");
							
							if(attribute[0].equals("successfulAttempts")){
								String values[] = attribute[1].split("&");
								for(int v = 0; v < values.length; v++){
									this.successfulAttempts[v] = Integer.valueOf(values[v]);
								}
								
							}else if(attribute[0].equals("totalAttempts")){
								String values[] = attribute[1].split("&");
								for(int v = 0; v < values.length; v++){
									this.totalAttempts[v] = Integer.valueOf(values[v]);
								}
							}
						}
						
					}else if(info[0].equals("gBestFoodSource")){
						this.epochGBest = Integer.valueOf(info[1]);
						this.gBest = createFoodSource(linha);;
						
					}else if(info[0].equals("foodSource")){
						FoodSource newFoodSource = createFoodSource(linha);
						foodSources.add(newFoodSource);
					}
				}
				
				in.close();
			    fr.close();
			    
			    return true;
			    
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	}
    	
    	return false;
	}
	
	public FoodSource createFoodSource(String linha[]){
		double nectar[] = new double[maxLength];
		int trials = 0;
		double fitness = 0.0;
		double selectionProbability = 0.0;
		
		for(int i = 1; i < linha.length; i++){
			String attribute[] = linha[i].split(":");
			
			if(attribute[0].equals("trials")){
				trials = Integer.valueOf(attribute[1]);
				
			}else if(attribute[0].equals("fitness")){
				fitness = Double.valueOf(attribute[1]);
				
			}else if(attribute[0].equals("selectionProbability")){
				selectionProbability = Double.valueOf(attribute[1]);
				
			}else if(attribute[0].equals("nectar")){
				String values[] = attribute[1].split("&");
				for(int v = 0; v < values.length; v++){
					nectar[v] = Double.valueOf(values[v]);
				}
			}
		}
		
		FoodSource foodSource = new FoodSource(maxLength, trials, fitness, selectionProbability, nectar);
		return foodSource;
	}
}
