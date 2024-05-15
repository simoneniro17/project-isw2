package secondmilestone;

import model.MLModelEval;
import model.MLProfile;
import utils.Printer;
import utils.Properties;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class MLAnalyzer {
    private final String datasetArff;
    
    public MLAnalyzer(String projectName) {
        datasetArff = Properties.OUTPUT_DIRECTORY + projectName + Properties.DATASET_FILE_ARFF;
    }
    
    public void loadDataset() throws Exception {
        DataSource source = new DataSource(datasetArff);
        Instances dataset = source.getDataSet();
        dataset.deleteStringAttributes();
        
        DataFilter dataFilter = new DataFilter();
        int numberOfAttributes = dataset.numAttributes();
        int numberOfVersions = dataset.attribute(0).numValues();
        List<MLModelEval> modelEvaluations = new ArrayList<>();
        Evaluation evaluation;
        
        // feature selection iteration
        for (MLProfile.FEATURE_SELECTION featureSelection : MLProfile.FEATURE_SELECTION.values()) {
            
            // sampling iteration
            for (MLProfile.BALANCING balancing : MLProfile.BALANCING.values()) {
                
                // walk forward
                for (int i = 1; i < numberOfVersions; i++) {
                    Instances training = dataFilter.getTrainingSet(dataset, i, numberOfVersions);
                    Instances testing = dataFilter.getTestingSet(dataset, i);
                    
                    training.deleteAttributeAt(0);
                    testing.deleteAttributeAt(0);
                    training.setClassIndex(numberOfAttributes - 2);
                    testing.setClassIndex(numberOfAttributes - 2);
                    
                    dataFilter.trainingData = training;
                    dataFilter.testingData = testing;
                    
                    // apply feature selection and sampling
                    dataFilter.applyFeatureSelection(featureSelection);
                    dataFilter.applySampling(balancing);
                    
                    training = dataFilter.trainingData;
                    testing = dataFilter.testingData;
                    
                    // using classifiers
                    for (MLProfile.CLASSIFIER classifier : MLProfile.CLASSIFIER.values()) {
                        evaluation = executeAnalysis(training, testing, classifier);
                        
                        Printer.printCLI("(" + i + ") Classificatore: " + classifier + "\t\tFeature Selection: " + featureSelection + "\t\tBalancing: " + balancing);
                        Printer.printCLI("\nAccuratezza: " + evaluation.pctCorrect() + "%");
                        Printer.printCLI("\nPrecision: " + evaluation.precision(1));
                        Printer.printCLI("\nRecall: " + evaluation.recall(1));
                        Printer.printCLI("\nAUC: " + evaluation.areaUnderROC(1));
                        Printer.printCLI("\nKappa: " + evaluation.kappa() + "\n\n");
                        
                        modelEvaluations.add(new MLModelEval(classifier, featureSelection, balancing, evaluation));
                    }
                }
            }
        }
    }
    
    private static Evaluation executeAnalysis(Instances trainingSet, Instances testingSet, MLProfile.CLASSIFIER classifier) throws Exception {
        Classifier cls;
        
        if (classifier.equals(MLProfile.CLASSIFIER.RANDOM_FOREST)) {
            cls = new RandomForest();
        }
        else if (classifier.equals(MLProfile.CLASSIFIER.NAIVE_BAYES)) {
            cls = new NaiveBayes();
        } else {
            cls = new IBk();
        }
        
        // classifier training
        cls.buildClassifier(trainingSet);
        
        // model evaluation
        Evaluation evaluation = new Evaluation(testingSet);
        evaluation.evaluateModel(cls, testingSet);
        
        return evaluation;
    }
    
    //private static void writeResultsToCSV

}
