package secondmilestone;

import model.MLProfile;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;


public class DataFilter {
    Instances trainingData;
    Instances testingData;
    
    public void applyFeatureSelection(MLProfile.FEATURE_SELECTION featureSelection) throws Exception {
        if (featureSelection.equals(MLProfile.FEATURE_SELECTION.BEST_FIRST)) {
            AttributeSelection attributeSelection = new AttributeSelection();
            attributeSelection.setEvaluator(new CfsSubsetEval());
            attributeSelection.setSearch(new BestFirst());
            attributeSelection.SelectAttributes(trainingData);
            
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray(attributeSelection.selectedAttributes());
            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(trainingData);
            
            trainingData = Filter.useFilter(trainingData, removeFilter);
            testingData = Filter.useFilter(testingData, removeFilter);
        }
    }
    
    public Instances getTrainingSet(Instances dataset, int trainingRelease, int releases) throws Exception {
        RemoveWithValues removeWithValues = new RemoveWithValues();
        int range = releases - trainingRelease;
        int[] arr = new int[range];
        
        for (int i = 1; i < range + 1; i++) {
            arr[range - i] = releases - i;
        }
        
        removeWithValues.setAttributeIndex("1");
        removeWithValues.setNominalIndicesArr(arr);
        removeWithValues.setInputFormat(dataset);
        
        return Filter.useFilter(dataset, removeWithValues);
    }
    
    public Instances getTestingSet(Instances dataset, int trainingRelease) throws Exception {
        String options = String.format("-C 1 -L %d -V", trainingRelease + 1);
        RemoveWithValues removeWithValues = new RemoveWithValues();
        
        removeWithValues.setOptions(Utils.splitOptions(options));
        removeWithValues.setInputFormat(dataset);
        
        return Filter.useFilter(dataset, removeWithValues);
    }
}
