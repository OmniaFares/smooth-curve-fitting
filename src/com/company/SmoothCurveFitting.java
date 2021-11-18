package com.company;

import java.util.*;

public class SmoothCurveFitting {
    int numberOfPoints;
    int degree;
    int turn = 0;
    int totalTurn = 500;
    final double Pc = 0.7;
    final double Pm = 0.01;
    int b = 1;
    Random rand;
    Vector<pair<Double, Double>> points;
    Vector<ArrayList<Double>> population;
    Vector<pair<ArrayList<Double>, Double>> fitnessValues;
    Vector<ArrayList<Double>> selectedChromosomes;
    Vector<pair<ArrayList<Double>, Double>> offSprings;

    public SmoothCurveFitting(int numberOfPoints, int degree, Vector<pair<Double, Double>> points) {
        this.numberOfPoints = numberOfPoints;
        this.degree = degree;
        this.points = points;
        rand = new Random();
        population = new Vector<>();
        fitnessValues = new Vector<>();
        selectedChromosomes = new Vector<>();
        offSprings = new Vector<>();
    }

    public int binarySearch(Vector<Integer> cumulative, int l, int r, int x) {
        if (r >= l) {
            int mid = l + (r - l) / 2;

            if (cumulative.get(mid + 1) > x && cumulative.get(mid) <= x)
                return mid;

            if (cumulative.get(mid) > x)
                return binarySearch(cumulative, l, mid - 1, x);

            return binarySearch(cumulative, mid + 1, r, x);
        }

        return -1;
    }

    public long factorialOf(long n) {
        if (n <= 2) {
            return n;
        }
        return n * factorialOf(n - 1);
    }

    public ArrayList<Double> generateChromosome() {
        ArrayList<Double> chromosome = new ArrayList<Double>();
        for (int i = 0; i < degree+1; i++) {
            chromosome.add(-10 + rand.nextDouble() * 20);
        }
        return chromosome;
    }

    public int getPopulationSize() {
        int N = numberOfPoints*4;
        int result = (int) (factorialOf(numberOfPoints) * 0.000002);
        if (result > N) N = (result % 2 == 0 ? result : result + 1);
        return N;
    }

    public Double getFitness(ArrayList<Double> chromosome) {
        /*Error at (1,5) = ((1.33 + 0.12 * 1 + 4.09 * 1^2) – 5)^2 = 0.2916
        Error at (2,8) = ((1.33 + 0.12 * 2 + 4.09 * 2^2) – 8)^2 = 98.6049
        Error at (3,13) = ((1.33 + 0.12 * 3 + 4.09 * 3^2) – 13)^2 = 650.25
        Error at (4,20) = ((1.33 + 0.12 * 4 + 4.09 * 4^2) – 20)^2 = 2232.5625
        Total error = (9.67 + 5.15 + 20.88 + 303.1) / 4 = 84.7*/
        Double totalValue = 0.0;
        for(int i = 0; i < numberOfPoints; i++){
            Double sum = 0.0;
            for (int j = 0; j < chromosome.size(); j++) {
                sum += (chromosome.get(j) * Math.pow(points.get(i).key, j));
                //System.out.println((chromosome.get(j) * Math.pow(points.get(i).key, j)));
            }
            //System.out.println(Math.pow((sum - points.get(i).value),2));
            totalValue += Math.pow((sum - points.get(i).value),2);

        }

        return totalValue / numberOfPoints;
    }

    public void DoSelection() {
        TreeSet<Integer> temp = new TreeSet<Integer>();
        Vector<pair<ArrayList<Double>, Double>> indexs = new Vector<>();
        for(int i = 0; i < population.size(); i++){
            while (temp.size() != 3){
                temp.add((int) (Math.random() * (population.size()-1)));
            }
            while (temp.size() != 0){
                int location = temp.pollFirst();
                indexs.add(new pair<>(fitnessValues.get(location).key, fitnessValues.get(location).value));
            }
            selectedChromosomes.add(getMax(indexs).key);
        }
    }

    public pair<ArrayList<Double>,Double> getMax(Vector<pair<ArrayList<Double>, Double>> array) {
        ArrayList<Double> bestChromosome = array.get(0).key;
        Double fitnessValue = array.get(0).value;

        for (int i = 1; i < array.size(); i++){
            if(fitnessValue > array.get(i).value ){
                fitnessValue = array.get(i).value;
                bestChromosome = array.get(i).key;
            }
        }

        return new pair<>(bestChromosome,fitnessValue);
    }

    public void DoCrossover(ArrayList<Double> chromosome1, ArrayList<Double> chromosome2) {
        int chromosomeLength = chromosome1.size();
        ArrayList<Double> offspring1, offspring2;
        offspring1 = chromosome1;
        offspring2 = chromosome2;

        double r1 = rand.nextDouble();
        if (r1 <= Pc) {
            offspring1 = new ArrayList<>();
            offspring2 = new ArrayList<>();
            int r2 = -1,r3 = -1;
            while ( r2 == r3){
                r2 = (int) Math.floor(Math.random() * ((chromosomeLength - 1)) + 1);
                r3 = (int) Math.floor(Math.random() * ((chromosomeLength - 1)) + 1);
                if(r3 < r2){
                   int temp = r2;
                   r2 = r3;
                   r3 = temp;
                }
            }
            int i = 0;
            for(; i < r2; i++){
                offspring1.add(chromosome1.get(i));
                offspring2.add(chromosome2.get(i));
            }
            for(; i < r3; i++){
                offspring1.add(chromosome2.get(i));
                offspring2.add(chromosome1.get(i));
            }
            for(; i < chromosomeLength; i++){
                offspring1.add(chromosome1.get(i));
                offspring2.add(chromosome2.get(i));
            }
        }
        offSprings.add(new pair<>(offspring1, 1.0));
        offSprings.add(new pair<>(offspring2, 1.0));
    }

    public void DoMutation() {
        for (pair<ArrayList<Double>, Double> offSpring : offSprings) {
            for (int j = 0; j < offSpring.key.size(); j++) {
                double r = rand.nextDouble();
                if (r <= Pm) {
                    Double change = -1.0;
                    Double xOld = offSpring.key.get(j);
                    Double lower = xOld + 10;
                    Double upper = 10 - xOld;
                    double r1 = rand.nextDouble();
                    Double y = lower;
                    if(r1 > 0.5){
                        y = upper;
                        change = 1.0;
                    }

                    double r2 = rand.nextDouble();

                    change *= y * (1 - Math.pow(r2 , (Math.pow ((1 - turn/(double)totalTurn) , b))));

                    offSpring.key.set(j ,xOld + change);
                }
            }
            offSpring.value = getFitness(offSpring.key);
        }
    }

    public void DoReplacement() {
        population.clear();
        fitnessValues.clear();
        for (pair<ArrayList<Double>, Double> offSpring : offSprings) {
            population.add(offSpring.key);
            fitnessValues.add(new pair<>(offSpring.key, offSpring.value));
        }
        offSprings.clear();
        selectedChromosomes.clear();
    }

    public void performGA() {
        int popSize = getPopulationSize();
        while (population.size() < popSize) {
            ArrayList<Double> chromosome = generateChromosome();
            population.add(chromosome);
            Double fitness = getFitness(chromosome);
            fitnessValues.add(new pair<>(chromosome, fitness));
        }
        pair<ArrayList<Double>,Double> bestChromosome = new pair<>(new ArrayList<>(),1000000000000000.0);
        while (turn < totalTurn) {
            DoSelection();
            for (int i = 0; i < selectedChromosomes.size(); i += 2) {
                DoCrossover(selectedChromosomes.get(i), selectedChromosomes.get(i + 1));
            }
            DoMutation();
            DoReplacement();
            pair<ArrayList<Double>,Double> temp = getMax(fitnessValues);
            bestChromosome = (bestChromosome.value < temp.value ? bestChromosome : temp);
            turn++;
        }
        print(bestChromosome);
    }

    public void print(pair<ArrayList<Double>,Double> bestChromosome) {
        for(Double coefficient : bestChromosome.key){
            System.out.print(coefficient + " , ");
        }
        System.out.println("Error = " + bestChromosome.value);
    }

}
