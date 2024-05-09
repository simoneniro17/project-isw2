package secondmilestone;

import weka.core.Instances;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.converters.ConverterUtils.DataSource;

public class TestWekaEasy{
    public static void main(String args[]) throws Exception{
        //load datasets
        DataSource source1 = new DataSource("C://Users//simon//IdeaProjects//project-isw2//BOOKKEEPERdataset.arff");
        Instances training = source1.getDataSet();
        DataSource source2 = new DataSource("C://Users//simon//IdeaProjects//project-isw2//BOOKKEEPERdataset.arff");
        Instances testing = source2.getDataSet();
        
        int numAttr = training.numAttributes();
        training.setClassIndex(numAttr - 1);
        testing.setClassIndex(numAttr - 1);
        
        NaiveBayes classifier = new NaiveBayes();
        
        classifier.buildClassifier(training);
        
        Evaluation eval = new Evaluation(testing);
        
        eval.evaluateModel(classifier, testing);
        
        System.out.println("AUC = "+eval.areaUnderROC(1));
        System.out.println("kappa = "+eval.kappa());
        
        
    }
}

