import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Collections;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

public class player02TournamentIsland implements ContestSubmission {
	Random rand;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;

	public player02TournamentIsland()
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

        // Choose optimal parameters
        int populationSize = 20;
        int tournamentSize = 10; 
        int tournamentWinners = 2;
        double selectionRate = 4;
        double crossOverProb = 1;
        Boolean crossOverRand = true;

        // Island parameters
        int exchangeFreq = 10; 
        int exchangeSize = 2;
        int communitySize = 3;

		// Compute other variables
		int parentSizeMin = (int) selectionRate*populationSize;
		int parentSize = 0;
		while(parentSize < parentSizeMin){
			parentSize += tournamentWinners; // lambda
		}
		int mu = populationSize; 
		int childrenSize = parentSize; // for crossover

        System.out.println(populationSize + "," + tournamentSize + "," + tournamentWinners + "," + selectionRate + "," + parentSize + "," + crossOverProb + "," + crossOverRand);
        System.out.println(exchangeFreq + "," + exchangeSize + " " + communitySize);

		// Run your algorithm here
        int evals = 0;

        // init population/community
        double[][][] community = new double [communitySize][populationSize][10];
    	double[][] parents = new double[parentSize][10];
        double[][] children = new double[childrenSize][10];
		double[][] survivors = new double[mu][10];

        double[][] communityFitness = new double[communitySize][populationSize];
        double[] childrenFitness = new double[parentSize];
		double[] survivorsFitness = new double[mu];     

        // random initiation
        for(int p = 0; p < communitySize; p++){
            for(int i = 0; i < populationSize; i++){
                for(int j = 0; j < 10; j++){
                    community[p][i][j] = -5 + 10*rand.nextDouble();
                }
                communityFitness[p][i] = (double) evaluation_.evaluate(community[p][i]);  
                evals++;
            }
        }

        int communityIndex = 0;
        int generations = 0;

        // calculate fitness
        while(evals + childrenSize <= evaluations_limit_){
        	
            System.out.println(generations + " " + evals);

            // Choose population from community
            double[][] population = community[communityIndex];
            double[] populationFitness = communityFitness[communityIndex];

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
			
            // update community
            community[communityIndex] = population;
            communityFitness[communityIndex] = populationFitness;
            if(communityIndex < communitySize - 1){
                communityIndex++;
            } else {
                communityIndex = 0;
                generations++;
            }

            // Exchange between populations
            if(generations % exchangeFreq == 0 && generations > 0){
                int[][] departing = new int[communitySize * exchangeSize][2];
                int departingIndex = 0;
                // draw departing individuals from each population
                for(int p = 0; p < communitySize; p++){
                    int[] departingSample = getSample(populationSize, exchangeSize);
                    for(int q = 0; q < exchangeSize; q++){
                        departing[departingIndex][0] = p;
                        departing[departingIndex][1] = departingSample[q];
                        departingIndex++;
                    }
                }
                
                int[][] arriving = departing;

                // shuffle departing individuals
                for(int i = 0; i < arriving.length; i++){ 
                    int randomPosition = rand.nextInt(arriving.length);

                    // make sure to shuffle to other populations:
                    while( arriving[randomPosition][0] == departing[i][0] ){
                        randomPosition = rand.nextInt(arriving.length);                    
                    }

                    int[] temp = arriving[i];
                    arriving[i] = arriving[randomPosition];
                    arriving[randomPosition] = temp;
                }

                // replace departing individuals
                for(int i = 0; i < departing.length; i++){
                    double[] temp = community[arriving[i][0]][arriving[i][1]];
                    community[arriving[i][0]][arriving[i][1]] = community[departing[i][0]][departing[i][1]];
                    community[departing[i][0]][departing[i][1]] = temp;
                }
            }
        }
  	}
}
