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
        
        int choice = scanner.nextInt();
        String sourcePathname;
        String destinationPathname;
        
        switch (choice) {
            case 1:
                sourcePathname = Properties.PROJECT_PATH + "/BOOKKEEPERdataset.csv";
                destinationPathname = Properties.PROJECT_PATH + "/BOOKKEEPERdataset.arff";
                break;
            case 2:
                sourcePathname = Properties.PROJECT_PATH +  "/STORMdataset.csv";
                destinationPathname = Properties.PROJECT_PATH +  "/STORMdataset.arff";
                break;
            default:
                Printer.printCLI("Invalid choice. Exit...");
                return;
        }
        
        CSV2Arff.csvToArffConverter(sourcePathname, destinationPathname);
    }
}
