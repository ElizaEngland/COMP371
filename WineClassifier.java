package part1;

import org.jcp.xml.dsig.internal.SignerOutputStream;
import sun.security.jca.GetInstance;

import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class WineClassifier {
     int k;
     int numAtts;
     int noOfInstances;
     int correctClasses;
     int incorrectClasses;
     List<String> attNames;
     List<WineInstance> trainingInstances;
     List<WineInstance> testingInstances;
     List<Double> ranges;

    public static void main(String[] args) {
        WineClassifier wine = new WineClassifier();
        if(args.length > 0) { // arguments
            File training = new File("src/part1/"+ args[0]); // training file argument
            File testing = new File("src/part1/" + args[1]); // testing file argument
            String k = args[2]; // k argument
            wine.classify(training,testing,k);
        }
    }

    /**
     * Load in data from files
     * @param file file of data

     */
    public void loadData(File file, boolean train){
        attNames = new ArrayList();
        try {
            Scanner sc = new Scanner(file); // scanner for training file

            attNames = new ArrayList<String>();
            Scanner firstLineScanner = new Scanner(sc.nextLine()); //scans the first line to get attributes
            while (firstLineScanner.hasNext()) {
                attNames.add(firstLineScanner.next());
            }
            numAtts = attNames.size();
            firstLineScanner.close();

            while(sc.hasNextLine()){ // scans rest of file
                ArrayList<String> attributes = new ArrayList<String>();
                Scanner instanceLineScanner = new Scanner(sc.nextLine());

                while (instanceLineScanner.hasNext()) { // scans through each line
                    attributes.add(instanceLineScanner.next());
                }
                WineInstance instance = new WineInstance(attributes); // new instance created

                if(train){ trainingInstances.add(instance);} //instance is added to list of either training or testing instances
                else{testingInstances.add(instance);}
                noOfInstances++;
                instanceLineScanner.close();
            }

            if(train){findRanges();} // finds the range of each attribute using training instances

            sc.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Data File caused IO exception");
        }

    }

    /**
     * Classifies the tdata
     * @param training training data file
     * @param testing testing file data
     * @param k k
     */
    public void classify(File training, File testing, String k){
        this.k = Integer.parseInt(k);
        trainingInstances = new ArrayList();
        testingInstances = new ArrayList<>();

        //load data
        loadData(training,true);
        loadData(testing,false);

        //classify each testing instance
        for(int test = 0; test<testingInstances.size(); test++){
            LinkedHashMap<WineInstance, Double> closestToFurthest = new LinkedHashMap<>(); //hash map to store the instances from closest to furthest
            WineInstance a = testingInstances.get(test);

            for(int train =0 ;train<trainingInstances.size();train++){ //loops through the training set
                WineInstance b = trainingInstances.get(train);
                double distance = findDistanceBetween(a,b); // finds distance between testing instance and training instance
                closestToFurthest.put(b,distance); // adds to hashmap
            }

            LinkedHashMap<WineInstance,Double> hm = sortByValue(closestToFurthest); //order map into ascending order

            int classOne =0, classTwo =0, classThree =0;

            for (int i =0; i < this.k ; i++) { //find k amount of shortest distances in hashmap, to then calculate average of k instances
                WineInstance wine =  (WineInstance) (hm.keySet().toArray()[i]);
                int wineClass = wine.getTrueClassOfInstance();
                if(1==wineClass){classOne++;}
                else if(2==wineClass){classTwo++;}
                else if(3==wineClass){classThree++;}
            }

            int check =0;
            if(classOne > classTwo && classOne > classThree){ check = 1; } // class 1 has majority
            else if(classTwo>classOne && classTwo >classThree){ check =2; } // class 2 has majority
            else if(classThree>classOne && classThree> classTwo){ check=3; } // class 3 has majority
            else{ //tie breaker necessary
                int tiebreaker = (int) ( Math.random() * 2 + 1); // generates a random number
                if(classOne == classTwo){ // tie between 1 and 2
                    if(tiebreaker==1){check=1;}
                    if(tiebreaker==2){check=2;}
                }
                else if(classTwo == classThree){ // tie between 2 and 3
                    if(tiebreaker==1){check=2;}
                    if(tiebreaker==2){check=3;}
                }
                else if(classThree == classOne){ // tie between 1 and 3
                    if(tiebreaker==1){check=1;}
                    if(tiebreaker==2){check=3;}
                }
                else if(classThree == classOne && classThree == classTwo){ // 3-way tie
                    tiebreaker = (int) ( Math.random() * 3 + 1);
                    if(tiebreaker==1){check=1;}
                    if(tiebreaker==2){check=2;}
                    if(tiebreaker==3){check=3;}
                }
            }

            //check to see if class is equal to the true class of the instance
            if(check==a.getTrueClassOfInstance()){correctClasses++;}
            else{incorrectClasses++;
                System.out.println("ERROR BELOW");
            }

            System.out.println("Instance "+test + ": "+ check);

        }

        System.out.println("Correct = " + correctClasses);
        System.out.println("Incorrect = " + incorrectClasses);

    }

    /**
     * Sort the hashmap into closest to furthest distance
     * @param hm hashmap to organise
     * @return hashmap of newly organized instances
     */
    public static LinkedHashMap<WineInstance, Double> sortByValue(LinkedHashMap<WineInstance, Double> hm) {
        // Create a list from elements of hashmap
        List<Map.Entry<WineInstance, Double> > list = new LinkedList<Map.Entry<WineInstance, Double> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<WineInstance, Double> >() {
            public int compare(Map.Entry<WineInstance, Double> no1, Map.Entry<WineInstance, Double> no2) {
                return (no1.getValue()).compareTo(no2.getValue());
            }
        }
        );

        // put data from sorted list to hashmap
        LinkedHashMap<WineInstance, Double> temp = new LinkedHashMap<WineInstance, Double>();
        for (Map.Entry<WineInstance, Double> instance : list){
            temp.put(instance.getKey(), instance.getValue());
        }
        return temp;
    }

    /**
     * Find the range of each attribute
     */
    public void findRanges(){
        ranges = new ArrayList<Double>();
        for(int attribute = 0; attribute < numAtts-1; attribute++) { // for every attribute find the range
                double max =0;
                double min= 10000000;
            for (int i = 0; i < trainingInstances.size(); i++) {
                WineInstance instance = trainingInstances.get(i);
                List<String> instanceAtts = instance.getDetails();
                double number = Double.parseDouble(instanceAtts.get(attribute));
                if(number > max){max = number;}
                if(number < min){min = number;}
            }
            double range = max-min;
            ranges.add(range);
        }

    }

    /**
     * Find the distance between two WineInstances
     * @param test WineInstance to find the distance between
     * @param comparingAgainst WineInstance we are comparing test against
     * @return distance between test and comparingAgainst
     */
    public double findDistanceBetween(WineInstance test, WineInstance comparingAgainst){
        double distance=0;
        List<String> attributesA = test.getDetails();
        List<String> attributesB = comparingAgainst.getDetails();
        for(int attNo =0; attNo < attributesA.size()-1; attNo++){
            // Euclidean distance
            double a = Double.parseDouble(attributesA.get(attNo));
            double b = Double.parseDouble(attributesB.get(attNo));
            double range = ranges.get(attNo);
            double topRow = ((a-b)*(a-b)); //(a-b)^2
            double bottomRow = (range*range); // r^2
            double equals = (topRow/bottomRow);
            distance = distance + equals;
        }
        distance =Math.sqrt(distance); // final distance

        return distance;
    }


}



