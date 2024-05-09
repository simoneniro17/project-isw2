package secondmilestone;

import utils.Printer;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;

public class TestWekaEasy{
    public static void main(String[] args) throws Exception{
        //load datasets
        DataSource source1 = new DataSource("C://Users//simon//IdeaProjects//project-isw2//src//main//java//outputs//BOOKKEEPERdataset.arff");
        Instances training = source1.getDataSet();
        DataSource source2 = new DataSource("C://Users//simon//IdeaProjects//project-isw2//src//main//java//outputs//BOOKKEEPERdataset.arff");
        Instances testing = source2.getDataSet();
        
        int numAttr = training.numAttributes();
        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);
        
        NaiveBayes classifier = new NaiveBayes();
        
        classifier.buildClassifier(training);
        
        Evaluation eval = new Evaluation(testing);
        
        eval.evaluateModel(classifier, testing);
        
        Printer.printCLI("AUC = " + eval.areaUnderROC(1) + "\n");
        Printer.printCLI("kappa = "+eval.kappa() + "\n");
    }
}
