package utils;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;
import java.io.IOException;

public class CSV2Arff {
    
    public static void csvToArffConverter(String pathname) throws IOException {
        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(pathname));
        Instances data = loader.getDataSet();//get instances object
        
        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("C:/Users/simon/IdeaProjects/project-isw2/BOOKKEEPERdataset.arff"));
        saver.writeBatch();
    }
}
