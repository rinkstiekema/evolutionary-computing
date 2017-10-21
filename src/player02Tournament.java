import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Collections;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

public class player02Tournament implements ContestSubmission {
	Random rand;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;

	public player02Tournament()
	{
		rand = new Random();
	}

	public void setSeed(long seed)
	{
		// Set seed of algortihms random process
		rand.setSeed(seed);
	}

	public void setEvaluation(ContestEvaluation evaluation)
	{
		// Set evaluation problem used in the run
		evaluation_ = evaluation;

		// Get evaluation properties
		Properties props = evaluation.getProperties();
        // Get evaluation limit
        evaluations_limit_ = Integer.parseInt(props.getProperty("Evaluations"));
		// Property keys depend on specific evaluation
		// E.g. double param = Double.parseDouble(props.getProperty("property_name"));
        boolean isMultimodal = Boolean.parseBoolean(props.getProperty("Multimodal"));
        boolean hasStructure = Boolean.parseBoolean(props.getProperty("Regular"));
        boolean isSeparable = Boolean.parseBoolean(props.getProperty("Separable"));

		// Do sth with property values, e.g. specify relevant settings of your algorithm
        if(isMultimodal){
            // Do sth
        }else{
            // Do sth else
        }
    }

    private int[] getRanking(double[] array){

    	int[] ranking = new int[array.length];

    	for(int k = 0; k < array.length; k++){
        		ranking[k] = 0;
        		for(int l = 0; k < array.length; k++){
        			if(array[k] < array[l]){
        				ranking[k] += 1;	
    			}
    		}
    	}

    	return ranking;
    }

    private int[] getSample(int populationSize, int sampleSize){

		int[] sample = new int[sampleSize];
    	Set<Integer> set = new HashSet<Integer>();

		while (set.size() < sampleSize){
			set.add(rand.nextInt(populationSize));	
		}

		int i = 0;
		for (Integer val : set) sample[i++] = val;

		return sample;
    }

	public void run(){

		// parameters
		// int populationSize = 100; // mu
		// int tournamentSize = 50;
		// int tournamentWinners = 2;
		// int selectionRate = 7;

		// random parameters;
        Random randParameters = new Random();
		int populationSize = 1 + randParameters.nextInt(100);// min 1?, max ?
		int tournamentSize = 1 + randParameters.nextInt(populationSize); // min 1, max populationSize
		int tournamentWinners = 1 + randParameters.nextInt(tournamentSize); // min 1, max tournamentSize
		double selectionRate = 1 + 9 * randParameters.nextDouble(); // rate = mu/lambda, lecture notes: good around 3-7.
        double crossOverProb = randParameters.nextDouble();
        Boolean crossOverRand = randParameters.nextBoolean();

		// Compute other variables
		int parentSizeMin = (int) selectionRate*populationSize;
		int parentSize = 0;
		while(parentSize < parentSizeMin){
			parentSize += tournamentWinners; // lambda
		}
		int mu = populationSize; 
		int childrenSize = parentSize; // for crossover

		// System.out.println("populationSize: " + populationSize);
		// System.out.println("tournamentSize: " + tournamentSize);
		// System.out.println("tournamentWinners: " + tournamentWinners);
		// System.out.println("selectionRate: " + selectionRate);
		// System.out.println("parentSize: " + parentSize);
        // System.out.println("crossOverProb: " + crossOverProb);
        // System.out.println("crossOverProb: " + crossOverProb);   
        System.out.println(populationSize + "," + tournamentSize + "," + tournamentWinners + "," + "," + parentSize + "," + crossOverProb + "," + crossOverRand);

		// Run your algorithm here
        int evals = 0;

        // init population
        double[][] population = new double[populationSize][10];
		double[][] parents = new double[parentSize][10];
        double[][] children = new double[childrenSize][10];
		double[][] survivors = new double[mu][10];

        double[] populationFitness = new double[populationSize];
        double[] childrenFitness = new double[parentSize];
		double[] survivorsFitness = new double[mu];     

        // random initiation and first evaluation:
        for(int i = 0; i < populationSize; i++){
        	for(int j = 0; j < 10; j++){
        		population[i][j] = -5 + 10*rand.nextDouble();
        	}
			populationFitness[i] = 0; // (double) evaluation_.evaluate(population[i]);	
			// evals++;
		}

        // calculate fitness
        while(evals + childrenSize <= evaluations_limit_){
        	// Parent selection
		   	// Tournament parent selection
	   		int parentCount = 0;
	   		int rounds = 0;
    		while(rounds*tournamentWinners < parentSize){
        		int[] candidates = getSample(populationSize, tournamentSize);          		
        		int winnerCount = 0;
          		for(int can : candidates){
          			int score  = 0;
          			for(int opp : candidates){
           				if(populationFitness[can] < populationFitness[opp]){
           					score++;
          				}
          			}
      				if(score < tournamentWinners && winnerCount < tournamentWinners){
     					parents[parentCount] = population[can];

      					parentCount++;
      					winnerCount++;
          			}
        		}
        		rounds++;
        	}      		
			
        	// Recombination (random arithmetic crossover)
        	for(int i = 0; i < (parentSize / 2); i++){
        		for(int j = 0; j < 10; j++){
                    // Compute cross over parameter
                    double alpha = crossOverRand ? rand.nextDouble() : 0.5; // random or deterministic (1/2)
                    alpha = (rand.nextDouble() < crossOverProb) ? alpha : 0; // random cross over given probability.

           			children[2*i][j] = (1-alpha) * parents[2*i][j] + alpha*parents[2*i + 1][j];
        			children[2*i + 1][j] = alpha*parents[2*i][j] + (1-alpha)*parents[2*i + 1][j];
        		}
          	}
        		
        	// Mutation
       		for(int i = 0; i < childrenSize; i++){
      			int mutationIndex = rand.nextInt(10);
      			children[i][mutationIndex] = -5 + 10 * rand.nextDouble();	
      		}        	
        	
        	// Compute children fitness
        	for(int i = 0; i < childrenSize; i++){
           		childrenFitness[i] = (double) evaluation_.evaluate(children[i]);;
				evals++;
			}

        	// Survivor selection
    		// (mu, lambda) selection: single round tournament with all canditates
    		int winnerCount = 0;
      		for(int i = 0; i < childrenSize; i++){
      			int score  = 0;
      			for(int j = 0; j < childrenSize; j++){
       				if(childrenFitness[i] < childrenFitness[j]){
       					score++;
      				}
      			}
  				if(score < mu && winnerCount < mu){
  					survivors[winnerCount] = children[i];
  					survivorsFitness[winnerCount] = childrenFitness[i];
  					winnerCount++;
      			}
    		}

    		// update population
    		population = survivors;
    		populationFitness = survivorsFitness;
			
        }
  	}
}
