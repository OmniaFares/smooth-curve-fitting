package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;


public class files {
    int numberOfTestcases = 0;
    int numberOfPoints = 0;
    Vector<pair<Double, Double>> pairs = new Vector<>();
    SmoothCurveFitting testcase;

    public void read() throws IOException {
        File inputFile = new File("input-2.txt");
        Scanner reader = new Scanner(inputFile);
        numberOfTestcases = reader.nextInt();
        for (int i = 0; i < numberOfTestcases; i++) {
            numberOfPoints = reader.nextInt();
            int degree = reader.nextInt();
            for (int j = 0; j < numberOfPoints; j++) {
                pairs.add(new pair(reader.nextDouble(), reader.nextDouble()));
            }

            testcase = new SmoothCurveFitting(numberOfPoints, degree, pairs);
            testcase.performGA();
            pairs.clear();
        }
        reader.close();
    }
}
