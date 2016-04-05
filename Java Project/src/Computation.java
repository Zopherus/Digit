import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

//@author Eric Zhu and Simon Dushatel
public class Computation {
    
    public ArrayList<Double> find_range_of_motion(ArrayList<ArrayList<Double>> data){ //calculate the range for x and y (seperate)
        ArrayList<Double> data_x = new ArrayList<>();
        ArrayList<Double> data_y = new ArrayList<>();
    
        for (ArrayList<Double> point : data)
        { //split into x and y data lists
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
    
    public ArrayList<Double> find_mode(ArrayList<ArrayList<Double>> data){ //calculate average mode of data
        ArrayList<Double> data_x = new ArrayList<>();
        ArrayList<Double> data_y = new ArrayList<>();

        for (ArrayList<Double> point : data)
        { //split into x and y data lists
            data_x.add(point.get(0));
            data_y.add(point.get(1));
        }

        ArrayList<Double> modes = new ArrayList<>();
        modes.add(mode(data_x));
        modes.add(mode(data_y));
        return modes; //return mode (the average there are multiple modes)
    }
    
    public Double find_muscle_smoothness(ArrayList<ArrayList<Double>> data)
    {
        //average acceleration between points
        ArrayList<Double> velocity = new ArrayList<>();
        for (int i = 0; i < data.size() - 1; i++)
        { //calculate velocity (difference between data points) and add to velocity
            ArrayList<Double> temp_velocity = new ArrayList<>();
            temp_velocity.add(data.get(i + 1).get(0) - data.get(i).get(0));
            temp_velocity.add(data.get(i + 1).get(1) - data.get(i).get(1));
            velocity.add(d2_distance_formula(temp_velocity));
        }
            
        ArrayList<Double> acceleration_list = new ArrayList<>();
        for (int i = 0; i < velocity.size() - 1; i++)
        { //calculate acceleraion (difference between velocity data points) and add to acceleration (omit any points with 0 velocity)
            if((velocity.get(i) != 0.0) && (velocity.get(i+1) != 0.0)){
                acceleration_list.add(Math.abs(velocity.get(i+1) - velocity.get(i)));
            }
        }
                
        Double acceleration = mean(acceleration_list); //find average acceleration
        return acceleration;
    }
    
     public int find_tremors(ArrayList<ArrayList<Double>> data){ //count number of tremors (small amplitude patterns over ~6-16 data points (8-12 Hz))
       ArrayList<Double> velocity = new ArrayList<>();
        for (int i = 0; i < data.size() - 1; i++)
        { //calculate velocity (difference between data points) and add to velocity
            ArrayList<Double> temp_velocity = new ArrayList<>();
            temp_velocity.add(data.get(i + 1).get(0) - data.get(i).get(0));
            temp_velocity.add(data.get(i + 1).get(1) - data.get(i).get(1));
            velocity.add(d2_distance_formula(temp_velocity));
        }
            
        ArrayList<Double> acceleration = new ArrayList<>();
        for (int i = 0; i < velocity.size() - 1; i++)
        { //calculate acceleraion (difference between velocity data points) and add to acceleration
            acceleration.add(velocity.get(i+1) - velocity.get(i));
        }
        
        int tremorCount = 0;
        int startingPosition = 0;
        Boolean startingPositionIsPositive = false;     //negative is false, positive is true
        for (int counter = 0; counter < acceleration.size(); counter++)
        {
            // Cases where is possibly tremor
            if (acceleration.get(counter) < 0 && startingPositionIsPositive 
                    && counter - startingPosition >= 8 && counter - startingPosition <= 16)
            {
                int interval = counter - startingPosition;
                int numberOfIntervals = 1;
                while (counter + numberOfIntervals * interval < acceleration.size())
                {
                    if (acceleration.get(counter + numberOfIntervals * interval) < 0 && startingPositionIsPositive ||
                            acceleration.get(counter + numberOfIntervals * interval) >= 0 && !startingPositionIsPositive)
                    {
                        startingPositionIsPositive = !startingPositionIsPositive;
                        numberOfIntervals++;
                    }
                    else
                        break;
                }
                if (numberOfIntervals >= 5)
                    tremorCount++;
            }
            else if (acceleration.get(counter) >= 0 && !startingPositionIsPositive 
                    && counter - startingPosition >= 8 && counter - startingPosition <= 16)
            {
                int interval = counter - startingPosition;
                int numberOfIntervals = 1;
                while (counter + numberOfIntervals * interval < acceleration.size())
                {
                    if (acceleration.get(counter + numberOfIntervals * interval) < 0 && startingPositionIsPositive ||
                            acceleration.get(counter + numberOfIntervals * interval) >= 0 && !startingPositionIsPositive)
                    {
                        startingPositionIsPositive = !startingPositionIsPositive;
                        numberOfIntervals++;
                    }
                    else
                        break;
                }
                if (numberOfIntervals >= 5)
                    tremorCount++;
            }
            // Cases where not tremor, but acceleration sign has shifted
            else if (acceleration.get(counter) < 0 && startingPositionIsPositive)
            {
                startingPosition = counter;
                startingPositionIsPositive = true;
            }
            else if (acceleration.get(counter) >= 0 && !startingPositionIsPositive)
            {
                startingPosition = counter;
                startingPositionIsPositive = false;
            }
        }
        return tremorCount;
    }
     
    public Double mean(ArrayList<Double> list){ //calcualte mean
        Double mean = 0.0;
        for (Double list1 : list) 
        {
            mean = mean + list1;
        }
        mean = mean / (double)list.size();
        
        return mean;
    }
    
    //very quick implementation of finding mode (improve if time allows)
    private Double mode(ArrayList<Double> list)
    { //returns mode (or if multiple, modal average)
        ArrayList<Double> modes = new ArrayList<>();
        ArrayList<Integer> frequency = new ArrayList<>();
        for(int i = 0; i < list.size(); i++)
        { //add elements to modes/frequency
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
        for (Double mode : modes) 
        {
            if (mode > largest_mode) 
            {
                largest_mode = mode;
                average_mode = largest_mode;
                num_modes = 1;
            }
            if (Objects.equals(mode, largest_mode)) 
            {
                average_mode = mode;
                num_modes = 1;
            }
        }
        return average_mode / num_modes;
    }
    
    public String deteriorationIndex() throws FileNotFoundException, IOException
    {
        if (new File("DataMetrics").list().length < 8 && new File("DataFiles").list().length < 8)
            return "Deterioration index will only work after the first week.";
        
        double modeTotal = 0;
        double accelerationTotal = 0;
        double usageTotal = 0;
        for (int counter = 1; counter < 8; counter++)
        {
            String day = new File("DataMetrics").list()[counter];
            BufferedReader reader = new BufferedReader(new FileReader("DataMetrics\\" + day));
            String line = reader.readLine();
            while(line != null)
            {
                String[] metrics = line.split(","); //add each modeX and modeY to total mode, add acceleration to total acceleration
                modeTotal += Double.parseDouble(metrics[2]);
                modeTotal += Double.parseDouble(metrics[3]);
                accelerationTotal += Double.parseDouble(metrics[4]);
                line = reader.readLine();
            }
            
            usageTotal += calculateTotalDistance("DataFiles\\" + day, "DistanceCalculations\\" + day); //add distance to total distance (usage)
        }
        
        double modeWeekAverage = modeTotal / 14.0;
        double accelerationWeekAverage = accelerationTotal / 7.0;
        double usageWeekAverage = usageTotal / 7.0;
        
        String today = new File("DataMetrics").list()[0];
        BufferedReader reader = new BufferedReader(new FileReader("DataMetrics\\" + today));
        
        modeTotal = 0; //reset variables (for later use, since we now have everything stored in averages)
        accelerationTotal = 0;
        double tremorsTotal = 0;
        usageTotal = 0;
        
        String line = reader.readLine(); //get x mode, y mode, acceleration and tremors from file
        while (line != null)
        {
            String[] metrics = line.split(",");
            modeTotal += Double.parseDouble(metrics[2]); //same as above, but for today (add mode to total, etc)
            modeTotal += Double.parseDouble(metrics[3]);
            accelerationTotal += Double.parseDouble(metrics[4]);
            tremorsTotal += Double.parseDouble(metrics[5]);
            line = reader.readLine();
        }
        
        usageTotal = calculateTotalDistance("DataFiles\\" + today, "DistanceCalculations\\" + today); //same as above, but for today
                                                                                                       //(add distance to usage)
        
        double modeDayAverage = modeTotal / 2.0;
        double accelerationDayAverage = accelerationTotal / 1.0;
        
        double PercentChangeModalPosition = 100 * (modeDayAverage - modeWeekAverage) / modeWeekAverage;
        double PercentChangeAcceleration = -100 * (accelerationDayAverage - accelerationWeekAverage) / accelerationWeekAverage;
        double PercentUsageChange = usageWeekAverage / usageTotal;
        
        double DeteriorationIndex = (PercentChangeModalPosition + PercentChangeAcceleration + tremorsTotal) * PercentUsageChange;
        DeteriorationIndex = (double)Math.round(DeteriorationIndex * 1000.0) / 1000.0;

        return "Your deterioration index is " + Double.toString(DeteriorationIndex);
    }
    
    private Double calculateTotalDistance(String rawDataFilePath, String accelerationFilePath)
    {
        Double totalDistance = 0.0;
        
        if(new File(accelerationFilePath).exists()) //if acceleration data exists, just read that
        {
            try 
            {
                BufferedReader reader = new BufferedReader(new FileReader(accelerationFilePath));
                String line = reader.readLine();
                
                while(line != null)
                {
                    totalDistance += Double.parseDouble(line); //read distance from file and add it to totalDistance
                    line = reader.readLine();
                }
                reader.close();
            } 
            catch (IOException | NumberFormatException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
        else //otherwise, calculate it ourselves
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(rawDataFilePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(accelerationFilePath));
                String line = reader.readLine();
                String[] lastDataPoint = line.split(",");
                line = reader.readLine();
                while (line != null)
                {
                    String[] data = line.split(",");

                    Double[] distanceComponents = {Double.parseDouble(data[0]) - Double.parseDouble(lastDataPoint[0]),
                        Double.parseDouble(data[1]) - Double.parseDouble(lastDataPoint[1])};
                    Double distance = Math.sqrt(Math.pow(distanceComponents[0], 2) + Math.pow(distanceComponents[1], 2));
                    
                    writer.write(distance.toString()); //write to file
                    writer.newLine();
                    
                    totalDistance += distance;
                    line = reader.readLine();
                }
                reader.close();
                writer.close();
            }
            catch(IOException | NumberFormatException ex)
            {
                System.out.println(ex.getMessage());
            }
        }
        return totalDistance;
    }
    
    private Double d2_distance_formula(ArrayList<Double> point){ //distance formula for 2 dimensional
        return Math.sqrt(Math.pow(point.get(0), 2) + Math.pow(point.get(1), 2));
    }
    
}
