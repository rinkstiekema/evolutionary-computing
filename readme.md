# Evolutionary Algorithms

A project for the course Evolutionary Computing course of master track Big Data Engineering at the VU Amsterdam.

# What can be find in this repository?
The src folder contains various java files with implementations of different evolutionary algorithms that try to optimize the following 3 functions:
- Schaffers
- Bent Cigar
- Katsuura

The following 4 algorithms can be found:
- Differential Evolution
- Particle Swarm Optimization
- Simulated Annealing
- A custom algorithm based on tournament selection

In the scripts folder there are several `.bat` files that were used to run a random search to optimize the parameters for the algorithms. Further more there is a folder containing raw data and a folder with the data processed into plots.

Our paper written on this subject can be found as a `.pdf` in the root folder.

# How do I run this code?
You can run the code by using the following commands:
`javac -cp contest.jar xxx.java`
`java -jar testrun.jar -submission=xxx -evaluation=BentCigarFunction -seed=1`
The `-evaluation` parameter can take three values: BentCigarFunction, SchaffersEvaluation and KatsuuraEvaluation. 
