package weka.classifiers.trees.m3gp.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import weka.classifiers.trees.m3gp.population.Population;
import weka.classifiers.trees.m3gp.util.Arrays;
import weka.classifiers.trees.m3gp.util.Data;
import weka.classifiers.trees.m3gp.util.Mat;

/**
 * 
 * @author Jo�o Batista, jbatista@di.fc.ul.pt
 *
 */
public class ClientWekaSim {

	private static int file = 1; // ST, GS

	private static String filename = "brazil.csv heart.csv waveform.csv vowel.csv".split(" ")[file];
	private static String datasetFilename = "datasets\\" + filename;
	private static String treeType = "Ramped";

	private static String [] operations = "+ - * /".split(" ");
	private static String [] terminals = null;

	private static double trainFraction = 0.70;
	private static double tournamentFraction = 0.01;
	private static double elitismFraction = 0.002 ;

	private static int numberOfGenerations = 20;
	private static int numberOfRuns = 10;
	private static int populationSize = 500;
	private static int maxDepth = 6;

	private static boolean shuffleDataset = true;

	private static double [][] data = null;
	private static String [] target = null;
	
	private static String resultOutputFilename = "results("+filename.split(".csv")[0]+"_r"+numberOfRuns+"_ps"+populationSize+"_gen"+numberOfGenerations+").csv";
	private static String dimensionsOutputFilename = "dimensions("+filename.split(".csv")[0]+"_r"+numberOfRuns+"_ps"+populationSize+"_gen"+numberOfGenerations+").csv";
	private static String sizeOutputFilename = "size("+filename.split(".csv")[0]+"_r"+numberOfRuns+"_ps"+populationSize+"_gen"+numberOfGenerations+").csv";
	private static String fitnessOutputFilename = "fitness("+filename.split(".csv")[0]+"_r"+numberOfRuns+"_ps"+populationSize+"_gen"+numberOfGenerations+").csv";


	// Variables
	@SuppressWarnings("unchecked")
	public static ArrayList<Double>[][] results = new ArrayList[numberOfGenerations][4];// treino, teste, dimensoes, tamanho
	public static ArrayList<Double>[] al_dim = new ArrayList[numberOfGenerations];// dimensoes
	public static ArrayList<Double>[] al_size = new ArrayList[numberOfGenerations];// tamanho
	public static ArrayList<Double>[] al_fit_tr = new ArrayList[numberOfGenerations];// fitness treino
	public static ArrayList<Double>[] al_fit_te = new ArrayList[numberOfGenerations];// fitness teste
	private static Population f = null;

	/**
	 * main
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		for ( int y  = 0; y < results.length; y++) {
			for ( int x = 0; x < results[0].length; x++) {
				results [y][x] = new ArrayList<Double>();
			}
			al_dim[y] = new ArrayList<Double>();
			al_size[y] = new ArrayList<Double>();
			al_fit_tr[y] = new ArrayList<Double>();
			al_fit_te[y] = new ArrayList<Double>();
		}
		
		treatArgs(args);
		init();

		long time = System.currentTimeMillis();
		for(int run = 0 ; run < numberOfRuns; run++){
			run(run);
		}
		System.out.println((System.currentTimeMillis() - time) + "ms");


		BufferedWriter out = new BufferedWriter(new FileWriter(resultOutputFilename));
		out.write("Treino;Teste;n_dimensoes;n_size\n");
		double treino,teste,n_dim, n_nodes;
		for(int i = 0; i < results.length; i++){
			treino = Mat.median(results[i][0]);
			teste = Mat.median(results[i][1]);
			n_dim = Mat.median(results[i][2]);
			n_nodes = Mat.median(results[i][3]);
			out.write(treino + ";" + teste +";" + n_dim +";" + n_nodes + "\n");
		}
		out.close();
		
		out = new BufferedWriter(new FileWriter(fitnessOutputFilename));
		for( int i = 0; i < numberOfRuns; i++) {
			out.write("Run " + i + " : treino ;Run " + i + " : teste;");
		}
		out.write("\n");
		for (int y = 0; y < al_fit_tr.length; y++) {
			for(int x = 0; x < al_fit_tr[y].size(); x++) {
				out.write(al_fit_tr[y].get(x)+";"+al_fit_te[y].get(x)+";");
			}
			out.write("\n");
		}
		out.close();
		
		out = new BufferedWriter(new FileWriter(dimensionsOutputFilename));
		for( int i = 0; i < numberOfRuns; i++) {
			out.write("Run " + i + ";");
		}
		out.write("\n");
		for (int y = 0; y < al_dim.length; y++) {
			for(int x = 0; x < al_dim[y].size(); x++) {
				out.write(al_dim[y].get(x)+";");
			}
			out.write("\n");
		}
		out.close();
		
		
		out = new BufferedWriter(new FileWriter(sizeOutputFilename));
		for( int i = 0; i < numberOfRuns; i++) {
			out.write("Run " + i + ";");
		}
		out.write("\n");
		for (int y = 0; y < al_size.length; y++) {
			for(int x = 0; x < al_size[y].size(); x++) {
				out.write(al_size[y].get(x)+";");
			}
			out.write("\n");
		}
		out.close();
	}

	/**
	 * Prepara o cliente para a sua execucao
	 * @throws IOException
	 */
	private static void init() throws IOException{
		Object [] datatarget = Data.readDataTarget(datasetFilename);
		data = (double[][]) datatarget [0];
		target = (String[]) datatarget [1];
	}

	/**
	 * Executa uma simulacao
	 * @param run
	 * @throws IOException
	 */
	private static void run(int run) throws IOException{
		System.out.println("Run " + run + ":");

		if(shuffleDataset)Arrays.shuffle(data, target);

		setTerm(data);

		double [][] train = new double [(int) (data.length*trainFraction)][data[0].length];
		double [][] test = new double [data.length - train.length][data[0].length];

		for(int i = 0; i < data.length; i++){
			if( i < train.length)
				train[i] = data[i];
			else
				test[i - train.length] = data[i];
		}

		setPopulation();
		
		f.train();
		
		System.out.println(f);
	}

	/**
	 * Trata dos argumentos fornecidos
	 * @param args
	 */
	private static void treatArgs(String [] args){
		for(int i = 0; i < args.length; i++){
			String [] split = args[i].split(":");
			switch(split[0]){
			case "depth":
				maxDepth = Integer.parseInt(split[1]);
				break;
			case "maxgen":
				numberOfGenerations = Integer.parseInt(split[1]);
				break;
			case "popsize":
				populationSize = Integer.parseInt(split[1]);
				break;
			}
		}
	}

	/**
	 * Define o valor dos terminais
	 * @param data
	 */
	private static void setTerm(double [][] data){
		terminals = new String [data[0].length+1];
		for(int i = 0; i < terminals.length; i++)
			terminals[i] = "x"+i;
		terminals[terminals.length-1] = "r";
	}

	/**
	 * Cria uma nova floresta
	 * @throws IOException
	 */
	private static void setPopulation() throws IOException{
		f = new Population("", operations, 
				terminals, maxDepth, data, target, 
				populationSize,trainFraction, treeType,numberOfGenerations,
				tournamentFraction, elitismFraction);
	}
}