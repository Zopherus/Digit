/*  NOTES
    
    -import data temporarily modified for testing so that it splits up whatever
    file is being used into 24 parts of 1000 data points each (to simulate
    24 hours of data) --> TODO: implement actual 24 hour/multiple day file read (and write)

    -TODO: figure out IO exception handling (and exception handling in general)*/

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.Collections;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Eric and Simon
 */


public class DigitForm extends javax.swing.JFrame {

    //class variables
    private String fileOutput = "ALS_Computations_Output.txt";
    private String fileInput = "ALS_Test_File_Large.txt";
    private Double LENIENCY = 0.5;
    //hourlyData is 24 lists of pairs of data points (1 list per hour)
    //hourlyData should hve a size of 24-3600-2
    private ArrayList<ArrayList<ArrayList<Double>>> hourlyData = new ArrayList();
    
    /**
     * Creates new form DigitForm
     */
    public DigitForm() {
        initComponents();
        Initialize();
    }
    
    private void Initialize()
    {
        setSize(640, 480);
        setLocationRelativeTo(null);
        
        graphDisplayButtons.add(xRangeButton);
        graphDisplayButtons.add(yRangeButton);
        graphDisplayButtons.add(xModeButton);
        graphDisplayButtons.add(yModeButton);
        graphDisplayButtons.add(smoothnessButton);
        graphDisplayButtons.add(tremorsButton);
        
        for(int i = 0; i < 4; i++)
        {
            try 
            {
                hourlyData.add(importDataFromFile(fileInput));
            } 
            catch (IOException ex) {
                //uh oh! IOException (figure out something to do here)
            }
        }
    }
    
    //generate the graph based on data
    //note: data variable should be 24 long (24 hours)
    private void generateGraph(String title, String x_label, String y_label, ArrayList<Double> data)
    {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Data");
        for(int i = 0; i < data.size(); i++)
        {
            series.add(i, data.get(i));
        }
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(title, x_label, y_label, dataset);
        add(new ChartPanel(chart), BorderLayout.CENTER);
    }
    
    
    //import data from a given file
    private ArrayList<ArrayList<Double>> importDataFromFile(String filePath) throws IOException
    {
        ArrayList<ArrayList<Double>> data = new ArrayList<>();
        int tempTEST = 0;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        while(true || tempTEST < 100)
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
            
            tempTEST++;
        }
        reader.close();
        
        return data;
    }

    public ArrayList<Double> find_range_of_motion(ArrayList<ArrayList<Double>> data){ //calculate the range for x and y (seperate)
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
    private ArrayList<Double> find_mode(ArrayList<ArrayList<Double>> data){ //calculate average mode of data
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
    
    private Double find_muscle_smoothness(ArrayList<ArrayList<Double>> data){ //average acceleration between points
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
    
    private Double d2_distance_formula(ArrayList<Double> point){ //distance formula for 2 dimensional
        return Math.sqrt(Math.pow(point.get(0), 2) + Math.pow(point.get(1), 2));
    }

//                find_tremors code is being revised, so for now it is commented out
    
    private int find_tremors(ArrayList<ArrayList<Double>> data){ //count number of tremors (small amplitude patterns over ~6-16 data points (8-12 Hz))
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
            if (acceleration.get(counter) > LENIENCY && acceleration.get(counter + 1) < -LENIENCY 
                    && acceleration.get(counter + 2) > LENIENCY)
                tremorCount++;
        }
        return tremorCount;
    }
    
    private Double mean(ArrayList<Double> list){ //calcualte mean
        Double mean = 0.0;
        for(int i = 0; i < list.size(); i++){
            mean = mean + list.get(i);
        }
        mean = mean / list.size();
        
        return mean;
    }
    
    //very quick implementation of finding mode (improve if time allows)
    private Double mode(ArrayList<Double> list){ //returns mode (or if multiple, modal average)
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        graphDisplayButtons = new javax.swing.ButtonGroup();
        day_selector = new java.awt.List();
        compute_metrics = new java.awt.Button();
        refresh = new java.awt.Button();
        xRangeButton = new javax.swing.JRadioButton();
        yRangeButton = new javax.swing.JRadioButton();
        xModeButton = new javax.swing.JRadioButton();
        yModeButton = new javax.swing.JRadioButton();
        smoothnessButton = new javax.swing.JRadioButton();
        tremorsButton = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        day_selector.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N

        compute_metrics.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        compute_metrics.setLabel("Compute Metrics");
        compute_metrics.setName(""); // NOI18N
        compute_metrics.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                compute_metricsMouseClicked(evt);
            }
        });

        refresh.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        refresh.setLabel("Refresh");

        xRangeButton.setSelected(true);
        xRangeButton.setText("X Range");

        yRangeButton.setText("Y Range");
        yRangeButton.setActionCommand("Y Range");

        xModeButton.setText("X Mode");

        yModeButton.setText("Y Mode");

        smoothnessButton.setText("Muscle Smoothness");

        tremorsButton.setText("Tremors");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(refresh, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                    .addComponent(day_selector, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xRangeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(yRangeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xModeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(yModeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(smoothnessButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tremorsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(compute_metrics, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(day_selector, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(refresh, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                            .addComponent(xRangeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(yRangeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(xModeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(yModeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tremorsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(smoothnessButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(410, Short.MAX_VALUE)
                        .addComponent(compute_metrics, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        compute_metrics.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void compute_metricsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_compute_metricsMouseClicked
        if(xRangeButton.isSelected())
        {
            ArrayList<Double> xRange = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++) //size should be 24
            {
                xRange.add(find_range_of_motion(hourlyData.get(i)).get(0));
            }
            generateGraph("X Range Data", "Time (Hours)", "X Range (Units)", xRange);
        }
        else if(yRangeButton.isSelected())
        {
            ArrayList<Double> yRange = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++) //size should be 24
            {
                yRange.add(find_range_of_motion(hourlyData.get(i)).get(1));
            }
            generateGraph("Y Range Data", "Time (Hours)", "Y Range (Units)", yRange);
        }
        else if(xModeButton.isSelected())
        {
            ArrayList<Double> xMode = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++) //size should be 24
            {
                xMode.add(find_mode(hourlyData.get(i)).get(0));
            }
            generateGraph("X Mode Data", "Time (Hours)", "X Mode (Units)", xMode);
        }
        else if(yModeButton.isSelected())
        {
            ArrayList<Double> yMode = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++) //size should be 24
            {
                yMode.add(find_mode(hourlyData.get(i)).get(1));
            }
            generateGraph("Y Mode Data", "Time (Hours)", "Y Mode (Units)", yMode);
        }
        else if(smoothnessButton.isSelected())
        {
            ArrayList<Double> smoothness = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++) //size should be 24
            {
                smoothness.add(find_muscle_smoothness(hourlyData.get(i)));
            }
            generateGraph("Muscle Smoothness Data", "Time (Hours)", "Muscle Smoothness (Units)", smoothness);
        }
        else if(tremorsButton.isSelected())
        {
            ArrayList<Double> tremors = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++) //size should be 24
            {
                tremors.add((double)find_tremors(hourlyData.get(i))); //casts int to Double
            }
            generateGraph("Tremors Data", "Time (Hours)", "Tremors (Units)", tremors);
        }
    }//GEN-LAST:event_compute_metricsMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DigitForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DigitForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DigitForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DigitForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DigitForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button compute_metrics;
    private java.awt.List day_selector;
    private javax.swing.ButtonGroup graphDisplayButtons;
    private java.awt.Button refresh;
    private javax.swing.JRadioButton smoothnessButton;
    private javax.swing.JRadioButton tremorsButton;
    private javax.swing.JRadioButton xModeButton;
    private javax.swing.JRadioButton xRangeButton;
    private javax.swing.JRadioButton yModeButton;
    private javax.swing.JRadioButton yRangeButton;
    // End of variables declaration//GEN-END:variables
}
