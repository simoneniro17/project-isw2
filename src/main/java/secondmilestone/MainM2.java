package secondmilestone;

import utils.CSV2Arff;
import utils.Printer;
import utils.Properties;

import java.util.Scanner;

public class MainM2 {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        Printer.printCLI("\nFrom which project do you want to obtain the evaluation?\n");
        Printer.printCLI("1. " + Properties.COMMON_PROJECT + "\n");
        Printer.printCLI("2. " + Properties.CUSTOM_PROJECT + "\n");
        Printer.printCLI("Insert the number corresponding to the project: ");
        
        int choice = scanner.nextInt();
        String sourcePathname;
        String destinationPathname;
        MLAnalyzer mlAnalyzer;
        
        switch (choice) {
            case 1:
                sourcePathname = Properties.OUTPUT_DIRECTORY + Properties.COMMON_PROJECT + Properties.DATASET_FILE;
                destinationPathname = Properties.OUTPUT_DIRECTORY + Properties.COMMON_PROJECT + Properties.DATASET_FILE_ARFF;
                mlAnalyzer = new MLAnalyzer(Properties.COMMON_PROJECT);
                break;
            case 2:
                sourcePathname = Properties.OUTPUT_DIRECTORY + Properties.CUSTOM_PROJECT + Properties.DATASET_FILE;
                destinationPathname = Properties.OUTPUT_DIRECTORY +  Properties.CUSTOM_PROJECT + Properties.DATASET_FILE_ARFF;
                mlAnalyzer = new MLAnalyzer(Properties.CUSTOM_PROJECT);
                break;
            default:
                Printer.printCLI("Invalid choice. Exit...");
                return;
        }
        
        // dataset from csv to arff in order to use weka
        Printer.printMessage("Converting from CSV to ARFF...\n");
        CSV2Arff.csvToArffConverter(sourcePathname, destinationPathname);
        Printer.printMessage("Conversion completed!\n");
        
        // model evaluation on the dataset generated before
        Printer.printMessage("Evaluating...\n");
        mlAnalyzer.loadDataset();
        Printer.printMessage("Evaluation completed!\n");
    }
}
