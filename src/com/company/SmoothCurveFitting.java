package com.company;

import java.util.*;

public class SmoothCurveFitting {
    int numberOfPoints;
    int degree;
    int turn = 0;
    int totalTurn = 200;
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

    public int factorialOf(int n) {
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
        int N = numberOfPoints;
        int result = (int) (factorialOf(numberOfPoints) * 0.000002);
        if (result > N) N = (result % 2 == 0 ? result : result + 1);

        return N;
    }

    public Double getFitness(ArrayList<Double> chromosome) {
        Double totalValue = 0.0;
        for(int i = 0; i < numberOfPoints; i++){
            Double sum = 0.0;
            for (int j = 0; j < chromosome.size(); j++) {
                sum += (chromosome.get(j) * Math.pow(points.get(i).key, j));
            }
            totalValue += Math.pow((sum - points.get(i).value),2);
        }

        return totalValue / numberOfPoints;
    }

    public void DoSelection() {
        TreeSet<Integer> temp = new TreeSet<Integer>();
        for(int i = 0; i < population.size(); i++){
            while (temp.size() != 3){
                temp.add((int) (Math.random() * (population.size()-1)));
            }
            selectedChromosomes.add(getMax(temp));
        }
    }

    public ArrayList<Double> getMax(TreeSet<Integer> temp) {
        ArrayList<Double> bestChromosome = new ArrayList<>();
        int index = temp.pollFirst();
        bestChromosome = fitnessValues.get(index).key;

        Iterator value = temp.iterator();
        while (value.hasNext()){
            int x = (int)value.next();
            if(fitnessValues.get(index).value > fitnessValues.get(x).value){
                index = x;
                bestChromosome = fitnessValues.get(index).key;
            }
        }

        return bestChromosome;
    }

    public void DoCrossover(ArrayList<Double> chromosome1, ArrayList<Double> chromosome2) {
        int chromosomeLength = chromosome1.size();
        ArrayList<Double> offspring1, offspring2;
        offspring1 = chromosome1;
        offspring2 = chromosome2;

        double r1 = rand.nextDouble();
        if (r1 <= Pc) {
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
        offSprings.add(new pair<>(offspring1, getFitness(offspring1)));
        offSprings.add(new pair<>(offspring2, getFitness(offspring2)));
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
        }
    }

    public void DoReplacement() {
        population.clear();
        fitnessValues.clear();
        for (pair<ArrayList<Double>, Double> offSpring : offSprings) {
            population.add(offSpring.key);
            fitnessValues.add(new pair<>(offSpring.key, getFitness(offSpring.key)));
        }
        offSprings.clear();
        selectedChromosomes.clear();
    }

    public void performGA(int caseNumber) {
        int popSize = getPopulationSize();
        while (population.size() < popSize) {
            ArrayList<Double> chromosome = generateChromosome();
            population.add(chromosome);
            Double fitness = getFitness(chromosome);
            fitnessValues.add(new pair<>(chromosome, fitness));
        }
        ArrayList<Double> bestChromosome = new ArrayList<>();
        while (turn < totalTurn) {
            DoSelection();
            for (int i = 0; i < selectedChromosomes.size(); i += 2) {
                DoCrossover(selectedChromosomes.get(i), selectedChromosomes.get(i + 1));
            }
            DoMutation();
            DoReplacement();
            //bestChromosome = (getFitness(bestChromosome) > getFitness(getMax()) ? bestChromosome : getMax());
            turn++;
        }
       // print(bestChromosome, caseNumber);
    }

   /* public void print(ArrayList<Double> bestChromosome, int caseNumber) {
        StringBuilder output = new StringBuilder();
        int numberOfItems = 0;
        for (int i = 0; i < bestChromosome.length(); i++) {
            if (bestChromosome.charAt(i) == '1') {
                numberOfItems++;
                output.append(points.get(i).key).append(" ").append(points.get(i).value).append('\n');
            }
        }
        //System.out.println("Weight = " + getWeight(bestChromosome));
        System.out.println("Case " + caseNumber + ": " + getFitness(bestChromosome));
        System.out.println(numberOfItems);
        System.out.println(output);
    }*/

}
