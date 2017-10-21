import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;

public class player02DifferentialIsland implements ContestSubmission
{
	Random rand;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;

	public player02DifferentialIsland()
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
                    System.out.println(isMultimodal, hasStructure, isSeparable);
        }else{
            // Do sth else
        }
    }

    private int[] chooseRandomAgents(int pSize, int agentExclude, int pertubationSize){
		
		int[] randomAgents = new int[2*pertubationSize + 1];
		Set<Integer> set = new HashSet<Integer>();

		set.add(agentExclude);
		while (set.size() < (2*pertubationSize + 1)){
			set.add(rand.nextInt(pSize));
		}
		set.remove(agentExclude);

		int i = 0;
		for (Integer val : set){
			randomAgents[i++] = val;
			
		}
		return randomAgents;
	}

	private int returnBestAgent(double[] myArray){

		// return argmax

		int bestAgent = rand.nextInt(10);
		for(int k = 0; k < myArray.length; k++){
			if(myArray[k] > myArray[bestAgent]){
				bestAgent = k;
			}
		}

		return bestAgent;
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

	public void run()
	{
		// parameters
		double crossoverRate = 0.7;
		double differentialRate = 0.1;
		int populationSize = 30;
		int pertubationSize = 2; // in {1,2,..., populationSize/2 - 1}
		Boolean bestBase = false; // otherwise best

		// Island parameters
		int exchangeFreq = 200;
		int exchangeSize = 1;
		int communitySize = 5;

  		// Random randParameters = new Random();
		// double crossoverRate = randParameters.nextDouble(); // min 0 max 1
		// double differentialRate = 2 * randParameters.nextDouble(); // min 0, max 2
		// int populationSize = 4 + randParameters.nextInt(47); // min 4, max?
		// int pertubationSize = 1 + randParameters.nextInt(Math.min(3, (populationSize-1)/2));
		// Boolean bestBase = false; // of rand.nextBoolean(), maar werkt slecht.

		// System.out.println("populationSize: " + populationSize);
		// System.out.println("crossoverRate: " + differentialRate);
		// System.out.println("differentialRate: " + crossoverRate);
		// System.out.println("pertubationSize: " + pertubationSize);
		// System.out.println("bestBase: " + bestBase);
		
		// Run your algorithm here
        int evals = 0;

        // init population
		double[][] communityFitness = new double[communitySize][populationSize];
		double[][][] community = new double[communitySize][populationSize][10];

		for(int p = 0; p < communitySize; p++){
			for(int i = 0; i < populationSize; i++){
				for(int j = 0; j < 10; j++){
					community[p][i][j] = -5 + 10 * rand.nextDouble();
				}
			}	
		}
	
		int generations = 0;

        // calculate fitness
        while(evals + populationSize <= evaluations_limit_){

        	generations++;
        	// System.out.println(generations);
			

			for(int p = 0; p < communitySize; p++){


				if(evals + populationSize <= evaluations_limit_){				

					double[][] population = community[p];
					double[] fitnessArray = communityFitness[p];

					double[][] newPopulation = new double[populationSize][10];
					
					//Loop through all agents
					for(int i = 0; i < populationSize; i++){

						//Create array to choose random agents from the population
						int[] randomAgents = chooseRandomAgents(populationSize, i, pertubationSize);
						int randomIndex = rand.nextInt(10);

						// Compute new position of agent
						for(int j = 0; j < 10; j++){
							double uniDistrNumber = 0 + (1 - 0) * rand.nextDouble();
							if(uniDistrNumber < crossoverRate || j == randomIndex){
								// base vector
								if(bestBase){
									int bestAgent = returnBestAgent(fitnessArray);
									newPopulation[i][j] = population[bestAgent][j];
								} else {
									newPopulation[i][j] = population[randomAgents[0]][j];
								}
								// pertubation vectors
								for(int k = 0; k < pertubationSize; k++){
									newPopulation[i][j] += differentialRate * (population[randomAgents[2*k + 1]][j] - population[randomAgents[2*k + 2]][j]);
								}	
							} else {
								newPopulation[i][j] = population[i][j];
							}
							
							newPopulation[i][j] = Math.min(5, Math.max(-5, newPopulation[i][j]));
						
						}
					}

					//Eval new population
					for(int i = 0; i < populationSize; i++){
						Double fitnessNew = (double) evaluation_.evaluate(newPopulation[i]);
						evals++;

						if(fitnessNew < fitnessArray[i]){
							//Keep old agent
							newPopulation[i] = population[i];
						} else {
							fitnessArray[i] = fitnessNew;
						}
					}

					population = newPopulation;

					community[p] = population;
					communityFitness[p] = fitnessArray;

			        // Exchange between populations
		            if(generations % exchangeFreq == 0 && generations > 0){
		                int[][] departing = new int[communitySize * exchangeSize][2];
		                int departingIndex = 0;
		                // draw departing individuals from each population
		                for(int r = 0; r < communitySize; r++){
		                    int[] departingSample = getSample(populationSize, exchangeSize);
		                    for(int q = 0; q < exchangeSize; q++){
		                        departing[departingIndex][0] = r;
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
	}
}
