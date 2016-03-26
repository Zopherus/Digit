
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 1012746
 */
public class Computation {
    
    public static ArrayList<Double> find_range_of_motion(ArrayList<ArrayList<Double>> data){ //calculate the range for x and y (seperate)
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
    
    public static ArrayList<Double> find_mode(ArrayList<ArrayList<Double>> data){ //calculate average mode of data
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
    
    public static Double find_muscle_smoothness(ArrayList<ArrayList<Double>> data){ //average acceleration between points
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
    
     public static int find_tremors(ArrayList<ArrayList<Double>> data){ //count number of tremors (small amplitude patterns over ~6-16 data points (8-12 Hz))
       ArrayList<Double> velocity = new ArrayList<>();
        for (int i = 0; i < data.size() - 1; i++){ //calculate velocity (difference between data points) and add to velocity
            ArrayList<Double> temp_velocity = new ArrayList<>();
            temp_velocity.add(data.get(i + 1).get(0) - data.get(i).get(0));
            temp_velocity.add(data.get(i + 1).get(1) - data.get(i).get(1));
            velocity.add(d2_distance_formula(temp_velocity));
        }
            
        ArrayList<Double> acceleration = new ArrayList<>();
        for (int i = 0; i < velocity.size() - 1; i++){ //calculate acceleraion (difference between velocity data points) and add to acceleration
            acceleration.add(Math.abs(velocity.get(i+1) - velocity.get(i)));
        }
        
        int tremorCount = 0;
        for (int counter = 0; counter < acceleration.size() - 5; counter++)
        {
            if (acceleration.get(counter) > 0 && acceleration.get(counter + 1) < 0 
                    && acceleration.get(counter + 2) > 0)
                tremorCount++;
        }
        return tremorCount;
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
    private static Double mode(ArrayList<Double> list){ //returns mode (or if multiple, modal average)
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
    
    private static Double d2_distance_formula(ArrayList<Double> point){ //distance formula for 2 dimensional
        return Math.sqrt(Math.pow(point.get(0), 2) + Math.pow(point.get(1), 2));
    }
    
}
