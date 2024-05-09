package utils;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class CSV2Arff {
    
    private CSV2Arff() {};
    
    /**
     * Convert a CSV file to ARFF format.
     *
     * @param sourcePathname the path of the CSV file to convert
     * @param destinationPathname the path of the ARFF file to save
     * @throws IOException if an I/O error occurs
     */
    public static void csvToArffConverter(String sourcePathname, String destinationPathname) throws IOException {
        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(sourcePathname));
        
        // isBuggy must be set as nominal attribute
        loader.setNominalAttributes("last");
        
        // get instances object
        Instances data = loader.getDataSet();
        
        // save ARFF
        ArffSaver saver = new ArffSaver();
        
        // set the dataset to convert and save it as ARFF
        saver.setInstances(data);
        saver.setFile(new File(destinationPathname));
        saver.writeBatch();
    }
}
