package com.company;

import java.util.*;

public class SmoothCurveFitting {
    int numberOfPoints;
    int degree;
    int turn = 0;
    int totalTurn = 200;
    final double Er = 0.2;
    final double Pc = 0.7;
    final double Pm = 0.1;
    int numberOfBest;
    int b = 5;
    Random rand;
    Vector<pair<Double, Double>> points;
    Vector<pair<ArrayList<Double>, Double>> fitnessValues;
    Vector<ArrayList<Double>> selectedChromosomes;
    Vector<pair<ArrayList<Double>, Double>> offSprings;

    public SmoothCurveFitting(int numberOfPoints, int degree, Vector<pair<Double, Double>> points) {
        this.numberOfPoints = numberOfPoints;
        this.degree = degree;
        this.points = points;
        rand = new Random();
        //population = new Vector<>();
        fitnessValues = new Vector<>();
        selectedChromosomes = new Vector<>();
        offSprings = new Vector<>();
    }

    public ArrayList<Double> generateChromosome() {
        ArrayList<Double> chromosome = new ArrayList<Double>();
        for (int i = 0; i < degree+1; i++) {
            chromosome.add(-10 + rand.nextDouble() * 20);
        }
        return chromosome;
    }

    public int getPopulationSize() {
        int N = numberOfPoints*7;
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
        Vector<pair<ArrayList<Double>, Double>> indexs = new Vector<>();
        for(int i = 0; i < fitnessValues.size(); i++){
            while (temp.size() != 3){
                temp.add((int) (Math.random() * (fitnessValues.size()-1)));
            }
            while (temp.size() != 0){
                int location = temp.pollFirst();
                indexs.add(new pair<>(fitnessValues.get(location).key, fitnessValues.get(location).value));
            }
            selectedChromosomes.add(getMin(indexs).key);
        }
    }

    public pair<ArrayList<Double>,Double> getMin(Vector<pair<ArrayList<Double>, Double>> array) {
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
       // System.out.println("here");
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
           // System.out.println("r2 " + r2 + "r3 " + r3);
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
        for (int i = numberOfBest; i < offSprings.size(); i++) {
            for (int j = 0; j < offSprings.get(i).key.size(); j++) {
                double r = rand.nextDouble();
                if (r <= Pm) {
                    
                    Double change = -1.0;
                    Double xOld = offSprings.get(i).key.get(j);
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

                    offSprings.get(i).key.set(j ,xOld + change);
                }
            }
            offSprings.get(i).value = getFitness(offSprings.get(i).key);
        }
    }

    public void DoReplacement() {
        fitnessValues.clear();
        for (pair<ArrayList<Double>, Double> offSpring : offSprings) {
            fitnessValues.add(new pair<>(offSpring.key, offSpring.value));
        }
        offSprings.clear();
        selectedChromosomes.clear();
    }

    public void performGA() {
        int popSize = getPopulationSize();
        numberOfBest = (int)(popSize * Er);
        numberOfBest = (numberOfBest % 2 == 0 ? numberOfBest : numberOfBest + 1);
        while (fitnessValues.size() < popSize) {
            ArrayList<Double> chromosome = generateChromosome();
            Double fitness = getFitness(chromosome);
            fitnessValues.add(new pair<>(chromosome, fitness));
        }


        while (turn < totalTurn) {
            elitism();
            DoSelection();
            for (int i = 0; i < selectedChromosomes.size(); i += 2) {
                DoCrossover(selectedChromosomes.get(i), selectedChromosomes.get(i + 1));
            }
            DoMutation();
            DoReplacement();
            turn++;
        }
        print(getMin(fitnessValues));
    }

    public void elitism(){
        Collections.sort(fitnessValues, new Comparator<pair<ArrayList<Double>, Double>>() {
            @Override
            public int compare(pair<ArrayList<Double>, Double> o1, pair<ArrayList<Double>, Double> o2) {
                if(o1.value < o2.value){
                    return -1;
                }else if(o1.value > o2.value){
                    return 1;
                }else{
                    return 0;
                }
            }
        });

        for (int i = 0; i < numberOfBest; i++){
            offSprings.add(fitnessValues.get(0));
            fitnessValues.remove(0);
        }
    }

    public void print(pair<ArrayList<Double>,Double> bestChromosome) {
        for(Double coefficient : bestChromosome.key){
            System.out.print(coefficient + " , ");
        }
        System.out.println("Error = " + bestChromosome.value);
    }

}
