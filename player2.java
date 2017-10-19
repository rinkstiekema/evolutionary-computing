import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;

public class player2 implements ContestSubmission
{
	Random rand;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;
    public String evaluationMethod = "";
	public player2()
	{
		rand = new Random();
		rand.nextInt();
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
        if(evaluations_limit_ == 10000){
            evaluationMethod = "BentCigarFunction";
        } else if(evaluations_limit_ == 100000){
            evaluationMethod = "SchaffersEvaluation";
        } else if(evaluations_limit_ == 1000000) {
            evaluationMethod = "KatsuuraEvaluation";
        }
		// Do sth with property values, e.g. specify relevant settings of your algorithm
        if(isMultimodal){
            // Do sth
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

	public void SchaffersAlg() {
        //parameters
        double crossoverRate = 0.7;
        double differentialRate = 0.5;
        int populationSize = 30;
        int pertubationSize = 2; // in {1,2,..., populationSize/2 - 1}
        Boolean bestBase = false; // otherwise best

        // Run your algorithm here
        int evals = 0;

        // init population
        double[] fitnessArray = new double[populationSize];
        double[][] population = new double[populationSize][10];
        for(int i = 0; i < populationSize; i++){
            for(int j = 0; j < 10; j++){
                population[i][j] = -5 + 10 * rand.nextDouble();
            }
        }

        // calculate fitness
        while(evals + populationSize <evaluations_limit_){

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
                        for(int k = 0; k < pertubationSize; k++) {
                            /*if (fitnessArray[randomAgents[0]] > 9.8){
                                newPopulation[i][j] += 0.6 * (population[randomAgents[2 * k + 1]][j] - population[randomAgents[2 * k + 2]][j]);
                            } if (fitnessArray[randomAgents[0]] > 9.9){
                                newPopulation[i][j] += 0.7 * (population[randomAgents[2 * k + 1]][j] - population[randomAgents[2 * k + 2]][j]);
                            } else {*/
                            newPopulation[i][j] += differentialRate * (population[randomAgents[2 * k + 1]][j] - population[randomAgents[2 * k + 2]][j]);
                            //}
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

    public void BentCigarAlg(){
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

    public void KatsuuraAlg(){

    }

	public void run()
	{
        if(evaluationMethod.equals("SchaffersEvaluation")) {
            SchaffersAlg();
        } else if(evaluationMethod.equals("BentCigarFunction")) {
            BentCigarAlg();
        } else {
            KatsuuraAlg();
        }
	}
}
