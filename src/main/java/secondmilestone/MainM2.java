package secondmilestone;

import utils.CSV2Arff;
import utils.Printer;
import utils.Properties;

import java.io.IOException;
import java.util.Scanner;

public class MainM2 {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        Printer.printCLI("\nFrom which project do you want to obtain the evaluation?\n");
        Printer.printCLI("1. BOOKKEEPER\n");
        Printer.printCLI("2. STORM\n");
        Printer.printCLI("Insert the number corresponding to the project: ");
        
        int scelta = scanner.nextInt();
        String pathname;
        
        switch (scelta) {
            case 1:
                pathname = "C:/Users/simon/IdeaProjects/project-isw2/BOOKKEEPERdataset.csv";
                break;
            case 2:
                pathname = "C:/Users/simon/IdeaProjects/project-isw2/STORMdataset.csv";
                break;
            default:
                Printer.printCLI("Invalid choice. Exit...");
                return;
        }
        
        CSV2Arff.csvToArffConverter(pathname);
    }
}
