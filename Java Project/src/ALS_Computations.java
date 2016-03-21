/*                              NOTES
//  Tremors code is commented out due to upcoming changes
//
//  function to find mode was very quickly implemented and should be improved
//  if time allows - investigate for accuracy
*/
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Collections;
import java.util.Objects;

public class ALS_Computations {

    static ArrayList<ArrayList<Double>> data = new ArrayList<>(); //data (list of x/y coordinate)
    static Double LENIENCY = 0.5; //leniency for tremors calculations

    public static void import_data(String file_name) throws FileNotFoundException, IOException{ //import data from .txt file to variable
        BufferedReader reader = new BufferedReader(new FileReader(file_name));
        while(true)
        {
            String line = reader.readLine();
            if(line==null)
            {
                break;
            }
            String[] fields = line.split(" ");
            ArrayList<Double> temp_point = new ArrayList<>();
            temp_point.add(Double.parseDouble(fields[0]));
            temp_point.add(Double.parseDouble(fields[1]));
            data.add(temp_point);
        }
        reader.close();
    }
    
    public static void calculate_metrics(String name_of_file_read, String name_of_file_write) throws IOException{
        import_data(name_of_file_read); //import data
        System.out.println("importing data complete");
        ArrayList<Double> range_of_motion = find_range_of_motion(); //calculate various metrics (and give helpful messages)
        System.out.println("range of motion calculation complete");
        System.out.println("\tx-range: " + range_of_motion.get(0).toString());
        System.out.println("\ty-range: " + range_of_motion.get(1).toString());
        ArrayList<Double> data_mode = find_mode();
        System.out.println("mode calculation complete");
        System.out.println("\tx-mode: " + data_mode.get(0).toString());
        System.out.println("\ty-mode: " + data_mode.get(1).toString());
        Double muscle_smoothness = find_muscle_smoothness();
        System.out.println("muscle smoothness calculation complete");
        System.out.println("\tmuscle smoothness: " + muscle_smoothness.toString());
        //int num_tremors = find_tremors();
        //System.out.println("number of tremors calculation complete");
        //System.out.println("\tnumber of tremors: " + num_tremors);

        int success = write_to_file(name_of_file_write,   //write metrics to file (and give helpful message)  
                                    range_of_motion.get(0), range_of_motion.get(1), data_mode.get(0), 
                                    data_mode.get(1), muscle_smoothness/*, num_tremors*/);
        if(success == 0){
            System.out.println("writing to file complete: succeeded");
        }
        else{
            System.out.println("writing to file complete: failed");
        }
    }
    
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter import data file location: ");
        String file_read = scanner.nextLine();
        System.out.print("Enter output data file location: ");
        String file_write = scanner.nextLine();
        calculate_metrics(file_read, file_write);
    }
    
    public static ArrayList<Double> find_range_of_motion(){ //calculate the range for x and y (seperate)
        ArrayList<Double> data_x = new ArrayList<>();
        ArrayList<Double> data_y = new ArrayList<>();
    
        for (ArrayList<Double> point : data){ //split into x and y data lists
            data_x.add(point.get(0));
            data_y.add(point.get(1));
        }

        Double x_range = Collections.max(data_x) - Collections.min(data_x); //find the range (max - min)
        Double y_range = Collections.max(data_y) - Collections.min(data_y);

        ArrayList<Double> range_list = new ArrayList<>();
        range_list.add(x_range);
        range_list.add(y_range);
        
        return range_list; //return range as simple coordinate
    }
    
    //no yet implemented in java (much of the code below is still python): unknown way
    public static ArrayList<Double> find_mode(){ //calculate average mode of data
        ArrayList<Double> data_x = new ArrayList<>();
        ArrayList<Double> data_y = new ArrayList<>();

        for (ArrayList<Double> point : data){ //split into x and y data lists
            data_x.add(point.get(0));
            data_y.add(point.get(1));
        }

        ArrayList<Double> modes = new ArrayList<>();
        modes.add(mode(data_x));
        modes.add(mode(data_y));
        return modes; //return mode (the average there are multiple modes)
    }
    
    static Double find_muscle_smoothness(){ //average acceleration between points
        ArrayList<Double> velocity = new ArrayList<>();
        for (int i = 0; i < data.size() - 1; i++){ //calculate velocity (difference between data points) and add to velocity
            ArrayList<Double> temp_velocity = new ArrayList<>();
            temp_velocity.add(data.get(i + 1).get(0) - data.get(i).get(0));
            temp_velocity.add(data.get(i + 1).get(1) - data.get(i).get(1));
            velocity.add(d2_distance_formula(temp_velocity));
        }
            
        ArrayList<Double> acceleration_list = new ArrayList<>();
        for (int i = 0; i < velocity.size() - 1; i++){ //calculate acceleraion (difference between velocity data points) and add to acceleration (omit any points with 0 velocity)
            if((velocity.get(i) != 0.0) & (velocity.get(i+1) != 0.0)){
                acceleration_list.add(Math.abs(velocity.get(i+1) - velocity.get(i)));
            }
        }
                
        Double acceleration = mean(acceleration_list); //find average acceleration
        return acceleration;
    }
    
    static Double d2_distance_formula(ArrayList<Double> point){ //distance formula for 2 dimensional
        return Math.sqrt(Math.pow(point.get(0), 2) + Math.pow(point.get(1), 2));
    }

//                find_tremors code is being revised, so for now it is commented out
    
/*    public static int find_tremors(){ //count number of tremors (small amplitude patterns over ~6-16 data points (8-12 Hz))
        ArrayList<Integer> n = new ArrayList<>();   //intervals to test (every n[0]th, every n[1]th, every n[2]th, etc...)
        n.add(6);   
        n.add(7);  
        n.add(7); 
        n.add(8); 
        n.add(9);  
        n.add(10);
        n.add(11); 
        n.add(13);  
        n.add(15);
        
        ArrayList<Integer> all_tremors = new ArrayList<>(); //keeps track of all positions for tremors (
        for item in n:
            all_tremors = all_tremors + find_tremors_with_interval(item);
        all_tremors.sort(); //sort list

        if(len(all_tremors) == 0): return 0; //no tremors found
        if(len(all_tremors) == 1): return 1; //only 1 tremor found, and it occurs over only 1 interval

        int num_tremors = 1; //we know there must be at least 1 tremor (see above)
        for i in range(1, len(all_tremors)):
            if(all_tremors[i] - all_tremors[i-1] != 1 & all_tremors[i] - all_tremors[i-1] != 0): //if there is a break in continuity (differs by more than 1 between 2 elements, not the same element),
                num_tremors = num_tremors + 1;          //then there is in fact a new distinct tremor (increase num_tremors)

        return num_tremors;
    }

    public static ArrayList<int> find_tremors_with_interval(int n){
        isolated_data = []; //isolated data always contains first
        for i in range(0, floor(len(data) / n) + 1): //isolate every nth data point in the data list (converts using distance formula first)
            temp_formatted_data = [Decimal(data[i*(n-1)][0]), Decimal(data[i*(n-1)][1])]; //format data as decimal
            isolated_data.append(Decimal(d2_distance_formula(temp_formatted_data)));

        ArrayList<int> tremor_locations = new ArrayList<int>();
        for i in range(0, len(isolated_data) - 1):
            if(abs(isolated_data[i] - isolated_data[i+1]) <= LENIENCY): //checks if values are equal at beginning and end of tremor intervals
                is_false_positive = True;
                for j in range(i*n, (i*n)+n):
                    //format data as decimal temporarily (for comparison to isolated_data)
                    temp_data = Decimal(d2_distance_formula([Decimal(data[j][0]), Decimal(data[j][1])]));
                    if(abs(isolated_data[i] - temp_data) >= LENIENCY): is_false_positive = False; //if any number in the tremor interval differs from the endpoint by more than LENIENCY
                                                                                                  //then it is not a false positive (looks at amplitude): otherwise, disregard (not real tremor)
                if(is_false_positive == False): //if it is not a false positive, add it to the list
                    for x in range((i*n), (i+1)*n): //add all data points between start and end (the entire tremor)
                        tremor_locations.append(x);

        return tremor_locations;
    }*/

    public static int write_to_file(String file_name, Double x_range, Double y_range, Double x_mode, 
                                    Double y_mode, Double muscle_smoothness/*, int num_tremors*/){ //write to file (returns 0 if success, -1 otherwise)
        try{
        BufferedWriter writer = new BufferedWriter(new FileWriter(file_name)); //writes to file (append, so it does NOT overwirte current data) in the following order:
            writer.append(x_range.toString()); writer.newLine();            //x_range
            writer.append(y_range.toString()); writer.newLine();            //y_range
            writer.append(x_mode.toString());  writer.newLine();                       //x_mode
            writer.append(y_mode.toString());  writer.newLine();                       //y_mode
            writer.append(muscle_smoothness.toString());  writer.newLine();            //muscle_smoothness
//            file.write(str(num_tremors) + "\n");               //num_tremors
            writer.close();
        return 0;
        }
        catch (IOException e) {
            System.out.println("Error writing to file");
            return -1;
        }
    }
        
    public static Double mean(ArrayList<Double> list){ //calcualte mean
        Double mean = 0.0;
        for(int i = 0; i < list.size(); i++){
            mean = mean + list.get(i);
        }
        mean = mean / list.size();
        
        return mean;
    }
    
    //very quick implementation of finding mode (improve if time allows)
    public static Double mode(ArrayList<Double> list){ //returns mode (or if multiple, modal average)
        ArrayList<Double> modes = new ArrayList<>();
        ArrayList<Integer> frequency = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){ //add elements to modes/frequency
            boolean in_list = false;
            for(int j = 0; j < modes.size(); j++)
            {
                if(Objects.equals(modes.get(j), list.get(i))) //it is in the list (increase frequency)
                {
                    frequency.set(j, frequency.get(j) + 1);
                    in_list = true;
                }
            }
            if(!in_list) //it is not in the list (add new element)
            {
                modes.add(list.get(i));
                frequency.add(i);
            }
        }
        
        Double average_mode = 0.0;
        int num_modes = 0;
        
        Double largest_mode = 0.0;
        for(int k = 0; k < modes.size(); k++)
        {
            if(modes.get(k) > largest_mode) //new largest found (replace previous)
            {
                largest_mode = modes.get(k);
                average_mode = largest_mode;
                num_modes = 1;
            }
            if(Objects.equals(modes.get(k), largest_mode))
            {
                average_mode = modes.get(k);
                num_modes = 1;
            }
        }
        
        return average_mode / num_modes;
    }
}
