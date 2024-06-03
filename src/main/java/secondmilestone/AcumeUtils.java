package secondmilestone;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import model.Acume;
import utils.Printer;
import utils.Properties;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// utility class for integrating ACUME tool and computing NPofB20 metric
public class AcumeUtils {
    
    private AcumeUtils(){}
    
    /**
     * Computes the NPofB20 metric for the given test instances and classifier.
     *
     * @param testInstances the instances to test
     * @param classifier the classifier used for predictions
     * @return the NPofB20 metric value
     */
    public static String getNpofb(Instances testInstances, Classifier classifier) {
        
        Printer.printMessage("Computing NPofB20...\n");
        
        List<Acume> acumeList = new ArrayList<>();
        Acume acumeObj;
        int i;
        
        try {
            // prediction for each instance in the test set
            for(i = 0; i < testInstances.numInstances(); i++) {
                Instance instance = testInstances.instance(i);
                
                boolean actual = false;
                int actualClass = (int) instance.classValue();
                if (actualClass == 1)
                    actual = true;
                
                
                double[] distribution = classifier.distributionForInstance(instance);
                double prediction = distribution[1];
                
                // truncate the prediction probability
                BigDecimal bigDecimal = BigDecimal.valueOf(prediction).setScale(3, RoundingMode.DOWN);
                prediction = bigDecimal.doubleValue();
                
                double size = instance.value(instance.attribute(1));
                acumeObj = new Acume(i, size, prediction, actual);
                acumeList.add(acumeObj);
            }
        } catch (Exception e) {
            Printer.printError("Error during acume.csv writing: " + e.getMessage());
        }
        
        createAcumeCSV(acumeList);
        String npofb20 = evaluateNPofB20();
        
        deleteGeneratedFiles();
        
        return npofb20;
    }
    
    /**
     * Evaluates the NPofB20 metric by running the ACUME tool and reading its output.
     *
     * @return the NPofB20 metric value
     */
    private static String evaluateNPofB20() {
        String npofb20 = null;
        
        try {
            // set working directory and command for ACUME
            File workDir = new File(Properties.ACUME_DIRECTORY);
            String[] cmd = {"python3", "main.py", "NPofB"};
            
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            processBuilder.directory(workDir);
            
            Process process = processBuilder.start();
            
            // wait for the ACUME command to finish
            process.waitFor();
            
            npofb20 = readNpofbB20FromCsv();
        } catch (IOException e) {
            Printer.printError("IO error during NPofB20 evaluation: " + e.getMessage());
        } catch (InterruptedException e) {
            // restore the interrupted status
            Thread.currentThread().interrupt();
            Printer.printError("InterruptedException during NPofB20 evaluation: " + e.getMessage());
        }
        return npofb20;
    }
    
    /**
     * Reads the NPofB20 value from the ACUME output CSV file.
     *
     * @return the NPofB20 value
     */
    private static String readNpofbB20FromCsv() {
        try (CSVReader reader = new CSVReader(new FileReader(Properties.ACUME_DIRECTORY + "EAM_NEAM_output.csv"))) {
            // ignore the first two lines
            reader.readNext();
            reader.readNext();
            
            String[] nextLine = reader.readNext();
            // the fourth column is the NPofB20
            return nextLine[3];
        } catch (IOException | CsvValidationException e) {
            Printer.printError("Error while evaluating NPofB20: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Creates the acume.csv file required by the ACUME tool.
     *
     * @param acumeList the list of Acume objects to write to the CSV file
     */
    private static void createAcumeCSV(List<Acume> acumeList) {
        Printer.printMessage("Creating acume.csv file...\n");
        
        try (FileWriter fileWriter = new FileWriter(Properties.ACUME_DIRECTORY + "acume.csv")) {
            fileWriter.append("ID,Size,Predicted,Actual\n");
            
            for (Acume acumeEntry : acumeList) {
                String line = String.format("%s,%s,%s,%s%n",
                        acumeEntry.getId(), (int) acumeEntry.getSize(), acumeEntry.getPredicted(), acumeEntry.getActualStringValue());
                fileWriter.append(line);
            }
        } catch (Exception e) {
            Printer.printError(e.getMessage());
        }
        
        Printer.printMessage("acume.csv file created\n");
    }
    
    /**
     * Deletes the generated CSV files after the NPofB20 evaluation.
     */
    private static void deleteGeneratedFiles() {
        File file1 = new File(Properties.ACUME_DIRECTORY + "acume.csv");
        File file2 = new File(Properties.ACUME_DIRECTORY + "EAM_NEAM_output.csv");
        File file3 = new File(Properties.ACUME_DIRECTORY + "norm_EAM_NEAM_output.csv");
        
        if(file1.delete() && file2.delete() && file3.delete())
            Printer.printMessage("Deleted generated files!\n");
        else
            Printer.printError("Error while deleting generated files!\n");
        
    }
}
