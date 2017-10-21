import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

public class player02Annealing implements ContestSubmission {
	Random rand;
	ContestEvaluation evaluation_;
    private int evaluations_limit_;

	public player02Annealing()
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

    public double[] getNeighbour(double[] agent){

    	int randomDimension = rand.nextInt(10);
    	agent[randomDimension] =  -5 + (5 - -5) * rand.nextDouble();

		return agent;
	}

	public double calculateProbability(double fitnessNeighbour, double fitness, double temperature){
        if(fitnessNeighbour < fitness){
            return 1.0;
        } else {
        	return Math.exp(-(fitnessNeighbour - fitness) / temperature);
        }
    }

	public void run(){

		// parameters
    Random randParameters = new Random();
		int populationSize = randParameters.nextInt(200);
		double endTemp = randParameters.nextInt(10);
		// double cooling = Math.pow(initTemp, (-populationSize/ evaluations_limit_));
		double cooling =  randParameters.nextDouble(5);
		double temperature = Math.pow(cooling, (-evaluations_limit_ / populationSize));

		System.out.println("populationSize: " + populationSize);

		// Run your algorithm here
    int evals = 0;
    // init population
		double[][] population = new double[populationSize][10];
		double[] fitnessArray = new double[populationSize];
		for(int i = 0; i < populationSize; i++){
			for(int j = 0; j < 10; j++){
				population[i][j] = -5 + (5 - -5) * rand.nextDouble();
			}
			fitnessArray[i] = 0.0;
		}

		double[][] newPopulation = new double[populationSize][10];

	    // calculate fitness
	    while(evals + populationSize <= evaluations_limit_){

			// double temperature = evaluations_limit_ - evals;
			temperature *= cooling;

			// Do annealing for every agent
			for(int i = 0; i < populationSize; i++){
		    double[] neighbour = getNeighbour(population[i]);
		    double fitnessNeighbour = (double) evaluation_.evaluate(neighbour);
		    evals++;
		    double probability = calculateProbability(fitnessNeighbour, fitnessArray[i], temperature);

		    if( probability > Math.random()){
		        population[i] = neighbour;
		        fitnessArray[i] = fitnessNeighbour;
		    }
			}
    }
	}
}
