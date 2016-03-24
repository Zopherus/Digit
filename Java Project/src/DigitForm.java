/*  NOTES
    
    -import data temporarily modified for testing so that it splits up whatever
    file is being used into 24 parts of 1000 data points each (to simulate
    24 hours of data) --> TODO: implement actual 24 hour/multiple day file read (and write)

    -TODO: figure out IO exception handling (and exception handling in general)*/

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Eric and Simon
 */



// TODO: MAKE RESIZABLE FALSE FOR FORM
public class DigitForm extends javax.swing.JFrame {

    //class variables
    private String fileOutput = "ALS_Computations_Output.txt";
    private String fileInputDirectory = "DataFiles";
    private String fileMetricDirectory = "DataMetrics";
    private Double LENIENCY = 0.5;
    private Integer MEASURESPERSECOND = 100; //number of data points recorded every second
    //hourlyData is lists of pairs of data points (1 list per hour)
    private ArrayList<ArrayList<ArrayList<Double>>> hourlyData = new ArrayList<ArrayList<ArrayList<Double>>>();
    
    /**
     * Creates new form DigitForm
     */
    public DigitForm() {
        initComponents();
        Initialize();
    }
    
    private void Initialize()
    {
        setSize(900, 550);
        setLocationRelativeTo(null);
        
        graphDisplayButtons.add(xRangeButton);
        graphDisplayButtons.add(yRangeButton);
        graphDisplayButtons.add(xModeButton);
        graphDisplayButtons.add(yModeButton);
        graphDisplayButtons.add(smoothnessButton);
        graphDisplayButtons.add(tremorsButton);
        
        refreshFromDataDirectory();
    }
    
    //generate the graph based on data
    private void generateGraph(String title, String x_label, String y_label, ArrayList<Double> data)
    {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Data");
        for(int i = 0; i < data.size(); i++)
        {
            series.add(i, data.get(i));
        }
        dataset.addSeries(series);
        ChartPanel chartPanel = new ChartPanel(ChartFactory.createXYLineChart(title, x_label, y_label, dataset));
        chartPanel.setMaximumSize(new java.awt.Dimension(500,250));
        graphPanel.removeAll(); //clears the graph Panel
        graphPanel.setSize((chartPanel.getSize())); //resize panel so the chart fits and is visible
        graphPanel.add(chartPanel, BorderLayout.CENTER); //adds graph to empy graph Panel
        graphPanel.validate();
        this.setSize(this.getSize().width + 1, this.getSize().height + 1); //a good workaround... let's call it a dynamic solution
        this.setSize(this.getSize().width - 1, this.getSize().height - 1); // For some reason the graph will show when the window is resized
    }
    
    
    //import data from a given file
    private void importDataFromFile(String filePath) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
        String line = reader.readLine();
        int numLinesRead = 0;
        ArrayList<ArrayList<Double>> hourDataTemp = new ArrayList();
        while(line != null) //iterate through each hour and add to daily list
        {
            while(numLinesRead < 3600 * MEASURESPERSECOND)
            {
                String[] fields = line.split(" ");
                ArrayList<Double> temp_point = new ArrayList<>();
                temp_point.add(Double.parseDouble(fields[0]));
                temp_point.add(Double.parseDouble(fields[1]));
                hourDataTemp.add(temp_point);
                numLinesRead++;
                line = reader.readLine();
                if(line == null) break;
            } 
           if (line == null)
                break;
            
            hourlyData.add(hourDataTemp);
            hourDataTemp.clear();
            numLinesRead = 0;
        }
        reader.close();
    }

    public ArrayList<Double> find_range_of_motion(ArrayList<ArrayList<Double>> data){ //calculate the range for x and y (seperate)
        ArrayList<Double> data_x = new ArrayList<>();
        ArrayList<Double> data_y = new ArrayList<>();
    
        System.out.println(data.size());
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
    
    private void refreshFromDataDirectory()
    {
        day_selector.removeAll(); //reset list
        File directory = new File(fileInputDirectory);
        File[] files = directory.listFiles();
        for(int i = 0; i < files.length; i++)
        {
            String fileName = files[i].getName().replace(".txt", ""); //read file name and remove .txt
            String[] fileNameSplit = fileName.split("_"); 
            fileName = fileNameSplit[1] + "/" + fileNameSplit[2] + "/" + fileNameSplit[0]; //reverse the order (should now be dd/mm/yy);
            day_selector.add(fileName); //add each file name to list
        }
    }
    
    private void computeMetricsFromRadioButtons()
    {
        if (day_selector.getSelectedIndex() == -1)
            return;
        try
        {
            String[] tempSplitString = day_selector.getSelectedItem().split("/"); //reformat file name correctly
            String formattedFileString = tempSplitString[2] + "_" + tempSplitString[0] + "_"
                                       + tempSplitString[1] + ".txt";
            importDataFromFile(fileInputDirectory + "\\" + formattedFileString);
            System.out.println(hourlyData.size());
        }
        catch (IOException ex)
        {
            //Uh-Oh! an error occurred... find a way to fix it, I guess
            System.out.println(ex.getMessage());
        }
        if(xRangeButton.isSelected())
        {
            ArrayList<Double> xRange = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++)
            {
                xRange.add(find_range_of_motion(hourlyData.get(i)).get(0));
            }
            generateGraph("X Range Data", "Time (Hours)", "X Range (Units)", xRange);
        }
        else if(yRangeButton.isSelected())
        {
            ArrayList<Double> yRange = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++)
            {
                yRange.add(find_range_of_motion(hourlyData.get(i)).get(1));
            }
            generateGraph("Y Range Data", "Time (Hours)", "Y Range (Units)", yRange);
        }
        else if(xModeButton.isSelected())
        {
            ArrayList<Double> xMode = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++)
            {
                xMode.add(find_mode(hourlyData.get(i)).get(0));
            }
            generateGraph("X Mode Data", "Time (Hours)", "X Mode (Units)", xMode);
        }
        else if(yModeButton.isSelected())
        {
            ArrayList<Double> yMode = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++)
            {
                yMode.add(find_mode(hourlyData.get(i)).get(1));
            }
            generateGraph("Y Mode Data", "Time (Hours)", "Y Mode (Units)", yMode);
        }
        else if(smoothnessButton.isSelected())
        {
            ArrayList<Double> smoothness = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++)
            {
                smoothness.add(find_muscle_smoothness(hourlyData.get(i)));
            }
            generateGraph("Muscle Smoothness Data", "Time (Hours)", "Muscle Smoothness (Units)", smoothness);
        }
        else if(tremorsButton.isSelected())
        {
            ArrayList<Double> tremors = new ArrayList();
            for(int i = 0; i < hourlyData.size(); i++)
            {
                tremors.add((double)find_tremors(hourlyData.get(i))); //casts int to Double
            }
            generateGraph("Tremors Data", "Time (Hours)", "Tremors (Units)", tremors);
        }
    }
    
    public void DisplayDeteriorationIndex()
    {
        if (new File("DataMetrics").listFiles().length < 14)
        {
            String result = "Deterioration Index will not display until after the first two weeks of usage.";
        }
        else
        {
            File[] files = new File("DataMetrics").listFiles();
            Arrays.sort(files);
            for (int counter = 0; counter < 14; counter++)
            {
                
            }
        }
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
        graphPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 500));
        setPreferredSize(new java.awt.Dimension(900, 500));

        day_selector.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N

        compute_metrics.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        compute_metrics.setLabel("Recompute");
        compute_metrics.setName(""); // NOI18N
        compute_metrics.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                compute_metricsMouseClicked(evt);
            }
        });

        refresh.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        refresh.setLabel("Refresh");
        refresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                refreshMouseClicked(evt);
            }
        });

        xRangeButton.setSelected(true);
        xRangeButton.setText("X Range");

        yRangeButton.setText("Y Range");

        xModeButton.setText("X Mode");

        yModeButton.setText("Y Mode");

        smoothnessButton.setText("Muscle Smoothness");

        tremorsButton.setText("Tremors");

        graphPanel.setLayout(new java.awt.BorderLayout());

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(compute_metrics, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(day_selector, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(compute_metrics, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(xRangeButton)
                            .addComponent(yRangeButton)
                            .addComponent(xModeButton)
                            .addComponent(yModeButton)
                            .addComponent(tremorsButton)
                            .addComponent(smoothnessButton))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        compute_metrics.getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //when compute metrics button is pressed, compute metrics based on radio button
    private void compute_metricsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_compute_metricsMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_compute_metricsMouseClicked

    //when refresh button is clicked, take all file names from directory and put in list
    private void refreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshMouseClicked
        refreshFromDataDirectory();
    }//GEN-LAST:event_refreshMouseClicked

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
    private javax.swing.JPanel graphPanel;
    private java.awt.Button refresh;
    private javax.swing.JRadioButton smoothnessButton;
    private javax.swing.JRadioButton tremorsButton;
    private javax.swing.JRadioButton xModeButton;
    private javax.swing.JRadioButton xRangeButton;
    private javax.swing.JRadioButton yModeButton;
    private javax.swing.JRadioButton yRangeButton;
    // End of variables declaration//GEN-END:variables
}
