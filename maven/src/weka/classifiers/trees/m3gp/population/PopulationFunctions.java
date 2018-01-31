package weka.classifiers.trees.m3gp.population;

import weka.classifiers.trees.m3gp.tree.Tree;
import weka.classifiers.trees.m3gp.tree.TreeCrossoverHandler;
import weka.classifiers.trees.m3gp.tree.TreeMutationHandler;
import weka.classifiers.trees.m3gp.tree.TreePruningHandler;
import weka.classifiers.trees.m3gp.util.Mat;

public class PopulationFunctions {
	/*
	 * -1 : Accuracy
	 * 1 : Mean distance to centroid
	 * -2 : accuracy - mean distance to centroid as a very small value
	 * -3 : accuracy - sigmoid(#dimensions)/trainset_size
	 * -4 : sigmoid(mean dist between clusters) - sigmoin(mean distance of points to the centroids)
	 * -5 : accuracy - sigmoid(#nodes)/trainset_size
	 */
	static int fitnessType = -5;
	public static double fitnessTrain(Tree t, double [][] data, String [] target, double trainFraction) {
		double d = 0,acc,dist_ce,d_size, dist_cl;
		switch (fitnessType){
		case -1:
			d = t.getTrainAccuracy(data, target, trainFraction); 
			break;
		case 1:
			d = t.getTrainMeanDistanceToCentroid(data, target, trainFraction); 
			break;
		case -2:
			acc = t.getTrainAccuracy(data, target, trainFraction); 
			dist_ce = t.getTrainMeanDistanceToCentroid(data, target, trainFraction); 
			dist_ce = Mat.sigmod(dist_ce);
			d = acc - dist_ce/(data.length*trainFraction);			
			break;
		case -3:
			acc = t.getTrainAccuracy(data, target, trainFraction); 
			d_size = t.getDimensions().size();
			d_size = Mat.sigmod(d_size);
			d = acc - d_size/(data.length*trainFraction);			
			break;
		case -4:
			dist_cl = Mat.sigmod(t.getMeanDistanceBetweenCentroids(data, target, trainFraction)/t.getDimensions().size());
			dist_ce = Mat.sigmod(t.getTrainMeanDistanceToCentroid(data, target, trainFraction)/t.getDimensions().size()); 
			d = dist_cl-dist_ce;
		case -5:
			acc = t.getTrainAccuracy(data, target, trainFraction); 
			d_size = t.getSize();
			d_size = Mat.sigmod(d_size/100.0);
			d = acc - d_size/(data.length*trainFraction);
			break;
		}
		return d;		
	}

	static double fitnessTest(Tree t, double [][] data, String [] target, double trainFraction) {
		double d = 0,acc,dist_ce,d_size, dist_cl;
		switch (fitnessType){
		case -1:
			d = t.getTestAccuracy(data, target, trainFraction); 
			break;
		case 1:
			d = t.getTestMeanDistanceToCentroid(data, target, trainFraction); 
			break;
		case -2:
			acc = t.getTestAccuracy(data, target, trainFraction); 
			dist_ce = t.getTestMeanDistanceToCentroid(data, target, trainFraction); 
			dist_ce = Mat.sigmod(dist_ce);
			d = acc + dist_ce/(data.length - data.length*trainFraction);			
			break;
		case -3:
			acc = t.getTestAccuracy(data, target, trainFraction); 
			d_size = t.getDimensions().size();
			d_size = Mat.sigmod(d_size);
			d = acc - d_size/(data.length*trainFraction);			
			break;
		case -4:
			dist_cl = Mat.sigmod(t.getMeanDistanceBetweenCentroids(data, target, trainFraction));
			dist_ce = Mat.sigmod(t.getTestMeanDistanceToCentroid(data, target, trainFraction)); 
			d = dist_cl-dist_ce;
		case -5:
			acc = t.getTestAccuracy(data, target, trainFraction); 
			d_size = t.getSize();
			d_size = Mat.sigmod(d_size);
			d = acc - d_size/(data.length*trainFraction);			
			break;
		}
		return d;
	}

	/**
	 * Picks as many trees from population as the size of the tournament
	 * and return the one with the lower fitness, assuming the population
	 * is already sorted
	 * @param population Tree population
	 * @return The winner tree
	 */
	static boolean smallerIsBetter = fitnessType > 0;
	static Tree tournament(Tree [] population, int tournamentSize) {
		int pick = Mat.random(tournamentSize);
		for(int i = 1; i < tournamentSize; i ++){
			if(smallerIsBetter)
				pick = Math.min(pick, Mat.random(population.length));
			else
				pick = Math.max(pick, Mat.random(population.length));
		}
		return population[pick];
	}


	/**
	 * This method creates as many trees as the size of the tournament
	 * which are descendents of p1 and p2 through crossover and returns
	 * the smallest one
	 * @param p1 Parent 1
	 * @param p2 Parent 2
	 * @return Descendent of p1 and p2 through crossover
	 */
	static Tree[] crossover(Tree[] pop, int tournamentSize, double [][] data, String [] target, double trainFraction) {
		Tree p1 = tournament(pop, tournamentSize);
		Tree p2 = tournament(pop, tournamentSize);
		return TreeCrossoverHandler.crossover(p1, p2,data, target, trainFraction);
	}

	/**
	 * This method creates as many trees as the size of the tournament
	 * which are descendents from p through mutation and returns the 
	 * smallest one
	 * @param p Original Tree
	 * @return Descendent of p by mutation
	 */
	static Tree mutation(Tree[] population, int tournSize, String [] operations, String[] terminals,
			double terminalRateForGrow, int maxDepth, double [][] data, 
			String [] target, double trainFraction) {
		Tree p = tournament(population, tournSize);
		return TreeMutationHandler.mutation(p, operations, terminals, terminalRateForGrow, maxDepth, data, target, trainFraction);
	}


	public static Tree prun(Tree tree, double[][] data, String[] target, double trainFraction) {
		return TreePruningHandler.prun(tree, data, target, trainFraction);
	}

	public static boolean betterTrain(Tree t1, Tree t2, double[][] data, String[] target, double trainFract) {
		double t1_fit = fitnessTrain(t1,data,target,trainFract);
		double t2_fit = fitnessTrain(t2,data,target,trainFract);
		return smallerIsBetter? t1_fit < t2_fit : t1_fit > t2_fit; 
	}
}