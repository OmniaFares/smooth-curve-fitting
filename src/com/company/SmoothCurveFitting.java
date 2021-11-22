package com.company;

import java.util.*;

public class SmoothCurveFitting {
    int numberOfPoints;
    int degree;
    int turn = 0;
    int totalTurn = 1000;
    final double Er = 0.05;
    final double Pc = 0.001;
    final double Pm = 0.9;
    int numberOfBest;
    double b = 5;
    Random rand;
    Vector<pair<Double, Double>> points;

    public SmoothCurveFitting(int numberOfPoints, int degree, Vector<pair<Double, Double>> points) {
        this.numberOfPoints = numberOfPoints;
        this.degree = degree;
        this.points = points;
        rand = new Random();
    }

    public ArrayList<Double> generateChromosome() {
        ArrayList<Double> chromosome = new ArrayList<Double>();
        for (int i = 0; i < degree + 1; i++) {
            chromosome.add(-10 + rand.nextDouble() * 20);
        }
        return chromosome;
    }

    public int getPopulationSize() {
        return numberOfPoints * 10;
    }

    public Double getFitness(ArrayList<Double> chromosome) {
        Double totalValue = 0.0;
        for (int i = 0; i < numberOfPoints; i++) {
            Double sum = 0.0;
            for (int j = 0; j < chromosome.size(); j++) {
                sum += (chromosome.get(j) * Math.pow(points.get(i).key, j));
            }
            totalValue += Math.pow((sum - points.get(i).value), 2);
        }
        return totalValue / numberOfPoints;
    }

    public pair<ArrayList<Double>, Double> getMin(Vector<pair<ArrayList<Double>, Double>> array) {
        ArrayList<Double> bestChromosome = array.get(0).key;
        Double fitnessValue = array.get(0).value;

        for (int i = 1; i < array.size(); i++) {
            if (fitnessValue > array.get(i).value) {
                fitnessValue = array.get(i).value;
                bestChromosome = array.get(i).key;
            }
        }

        return new pair<>(bestChromosome, fitnessValue);
    }

    public Vector<pair<ArrayList<Double>, Double>> elitism(Vector<pair<ArrayList<Double>, Double>> array) {
        Vector<pair<ArrayList<Double>, Double>> bestChromosomes = new Vector<>();
        array.sort(new Comparator<pair<ArrayList<Double>, Double>>() {
            @Override
            public int compare(pair<ArrayList<Double>, Double> o1, pair<ArrayList<Double>, Double> o2) {
                if (o1.value < o2.value) {
                    return -1;
                } else if (o1.value > o2.value) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (int i = 0; i < numberOfBest; i++) {
            bestChromosomes.add(array.get(0));
            array.remove(0);
        }
        return bestChromosomes;
    }

    public void print(pair<ArrayList<Double>, Double> bestChromosome) {
        for (Double coefficient : bestChromosome.key) {
            System.out.print(coefficient + " , ");
        }
        System.out.println("Error = " + bestChromosome.value);
    }

    public Vector<ArrayList<Double>> DoSelection(Vector<pair<ArrayList<Double>, Double>> array) {
        Vector<pair<ArrayList<Double>, Double>> indexes = new Vector<>();
        Vector<ArrayList<Double>> selectedChromosomes = new Vector<>();

        for (int i = 0; i < array.size(); i++) {
            indexes.clear();
            while (indexes.size() < 10) {
                int location = (int) (Math.random() * (array.size() - 1));
                indexes.add(new pair<>(array.get(location).key, array.get(location).value));
            }
            selectedChromosomes.add(getMin(indexes).key);
        }
        return selectedChromosomes;
    }

    public pair<ArrayList<Double>, ArrayList<Double>> DoCrossover(ArrayList<Double> chromosome1, ArrayList<Double> chromosome2) {
        int chromosomeLength = chromosome1.size();
        ArrayList<Double> offspring1, offspring2;
        offspring1 = chromosome1;
        offspring2 = chromosome2;

        double r1 = rand.nextDouble();
        if (r1 <= Pc) {
            offspring1 = new ArrayList<>();
            offspring2 = new ArrayList<>();
            int r2 = -1, r3 = -1;
            while (r2 == r3) {
                r2 = (int) Math.floor(Math.random() * ((chromosomeLength - 1)) + 1);
                r3 = (int) Math.floor(Math.random() * ((chromosomeLength - 1)) + 1);
                if (r3 < r2) {
                    int temp = r2;
                    r2 = r3;
                    r3 = temp;
                }
            }
            int i = 0;
            for (; i < r2; i++) {
                offspring1.add(chromosome2.get(i));
                offspring2.add(chromosome1.get(i));
            }
            for (; i < r3; i++) {
                offspring1.add(chromosome1.get(i));
                offspring2.add(chromosome2.get(i));
            }
            for (; i < chromosomeLength; i++) {
                offspring1.add(chromosome2.get(i));
                offspring2.add(chromosome1.get(i));
            }
        }

        return new pair<>(offspring1, offspring2);
    }

    public Vector<pair<ArrayList<Double>, Double>> DoMutation(Vector<ArrayList<Double>> array) {
        Vector<pair<ArrayList<Double>, Double>> newChromosomes = new Vector<>();
        double change, xOld, lower, upper, r1, r2, y;
        ArrayList<Double> newOne;

        for (int i = 0; i < array.size(); i++) {
            newOne = new ArrayList<>();
            for (Double gene : array.get(i)) {
                double r = rand.nextDouble();
                if (r <= Pm) {
                    change = -1.0;
                    xOld = gene;
                    lower = xOld + 10;
                    upper = 10 - xOld;
                    r1 = rand.nextDouble();
                    y = lower;
                    if (r1 > 0.5) {
                        y = upper;
                        change = 1.0;
                    }

                    r2 = rand.nextDouble();

                    change *= y * (1 - Math.pow(r2, (Math.pow((1 - turn / (double) totalTurn), b))));

                    gene = (xOld + change);
                }
                newOne.add(gene);
            }
            newChromosomes.add(new pair<>(newOne, getFitness(newOne)));
        }
        return newChromosomes;
    }

    public void performGA() {
        Vector<pair<ArrayList<Double>, Double>> generation = new Vector<>();
        int popSize = getPopulationSize();
        numberOfBest = (int) (popSize * Er);
        numberOfBest = (numberOfBest % 2 == 0 ? numberOfBest : numberOfBest + 1);
        HashSet<ArrayList<Double>> population = new HashSet<>();
        while (population.size() < popSize) {
            ArrayList<Double> chromosome = generateChromosome();
            if (!population.contains(chromosome)) {
                population.add(chromosome);
                generation.add(new pair<>(chromosome, getFitness(chromosome)));
            }
        }


        while (turn < totalTurn) {
            Vector<ArrayList<Double>> offSprings = new Vector<>();
            Vector<pair<ArrayList<Double>, Double>> newGeneration = elitism(generation);
            Collections.shuffle(generation);
            Vector<ArrayList<Double>> selectedChromosomes = DoSelection(generation);
            Collections.shuffle(selectedChromosomes);
            for (int i = 0; i < selectedChromosomes.size(); i += 2) {
                pair<ArrayList<Double>, ArrayList<Double>> twoChromosome = DoCrossover(selectedChromosomes.get(i), selectedChromosomes.get(i + 1));
                offSprings.add(twoChromosome.key);
                offSprings.add(twoChromosome.value);
            }
            newGeneration.addAll(DoMutation(offSprings));
            generation = newGeneration; // replacement
            turn++;
        }
        print(getMin(generation));
    }
}
