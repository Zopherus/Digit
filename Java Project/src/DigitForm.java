/*  NOTES
    
    -import data temporarily modified for testing so that it splits up whatever
    file is being used into 24 parts of 1000 data points each (to simulate
    24 hours of data) --> TODO: implement actual 24 hour/multiple day file read (and write)

    -TODO: figure out IO exception handling (and exception handling in general)*/

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
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
    private final String fileInputDirectory = "DataFiles";
    private final String fileMetricDirectory = "DataMetrics";
    private final Integer MEASURESPERSECOND = 100; //number of data points recorded every second
    //hourlyData is lists of pairs of data points (1 list per hour)
    private final ArrayList<ArrayList<ArrayList<Double>>> hourlyData = new ArrayList<>();
    
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
    
    
    //import data from a given file into data variable
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
    
    private ArrayList<Double> getMetricData(String metric, String fileName) //looks if metric file already exists and reads relevant
    {
        // If the file doesn't exist, calculate all the metrics
        if(!new File(fileMetricDirectory + "\\" + fileName).exists())
        {
            
        }
        ArrayList<Double> metricResult = new ArrayList<>();
        try 
        {
            BufferedReader reader = new BufferedReader(new FileReader(fileMetricDirectory + "\\" + fileName));
            String line = reader.readLine();
            while(line != null) //for each line, split into component metrics and add relevant metric to result
            {
                String[] tempResult = line.split(",");
                switch (metric) {
                    case "xrange":
                        if(!tempResult[0].equals(""))
                        {
                            metricResult.add(Double.parseDouble(tempResult[0]));
                        }   break;
                    case "yrange":
                        if(!tempResult[1].equals(""))
                        {
                            metricResult.add(Double.parseDouble(tempResult[1]));
                        }   break;
                    case "xmode":
                        if(!tempResult[2].equals(""))
                        {
                            metricResult.add(Double.parseDouble(tempResult[2]));
                        }   break;
                    case "ymode":
                        if(!tempResult[3].equals(""))
                        {
                            metricResult.add(Double.parseDouble(tempResult[3]));
                        }   break;
                    case "smoothness":
                        if(!tempResult[4].equals(""))
                        {
                            metricResult.add(Double.parseDouble(tempResult[4]));
                        }   break;
                    case "tremors":
                        if(!tempResult[5].equals(""))
                        {
                            metricResult.add(Double.parseDouble(tempResult[5]));
                        }   break;
                }

                line = reader.readLine();
            }
            reader.close();
            return metricResult;
        }
        catch (Exception ex) 
        {
            return null;
        }
    }
    
    private void computeMetricsFromRadioButtons()
    {
        if (day_selector.getSelectedIndex() == -1)
        {
            JOptionPane.showConfirmDialog(this, "Please select a date", "Error", 2);
        }
        
        String[] tempSplitString = day_selector.getSelectedItem().split("/"); //reformat file name correctly
        String formattedFileString = tempSplitString[2] + "_" + tempSplitString[0] + "_"
                                   + tempSplitString[1] + ".txt";
        
        if (xRangeButton.isSelected())
        {
            getMetricData("xrange", formattedFileString);
        }
        else if (yRangeButton.isSelected())
        {
            getMetricData("yrange", formattedFileString);
        }
        else if(xModeButton.isSelected())
        {
            getMetricData("xmode", formattedFileString);
        }
        else if (yModeButton.isSelected())
        {
            getMetricData("ymode", formattedFileString);
        }
        else if (smoothnessButton.isSelected())
        {
            getMetricData("smoothness", formattedFileString);
        }
        else if (tremorsButton.isSelected())
        {
            getMetricData("tremors", formattedFileString);
        }
        
        /*boolean compute = false;
        String[] tempSplitString = day_selector.getSelectedItem().split("/"); //reformat file name correctly
        String formattedFileString = tempSplitString[2] + "_" + tempSplitString[0] + "_"
                                   + tempSplitString[1] + ".txt";
        if(getMetricData("xrange", formattedFileString) == null) //if metrics don't exist, tell code to compute metrics
        {                                                        //(boolean variable) and load data for later
            compute = true;
            try
            {
                importDataFromFile(fileInputDirectory + "\\" + formattedFileString);
            }
            catch (IOException ex)
            {
                //Uh-Oh! an error occurred... find a way to fix it, I guess
                System.out.println(ex.getMessage());
            }
        }
            
        if(xRangeButton.isSelected())
        {
            ArrayList<Double> xRange = new ArrayList();
            if(compute == false)
            {
                xRange = getMetricData("xrange", formattedFileString);
            }
            else
            {
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    xRange.add(Computation.find_range_of_motion(hourlyData.get(i)).get(0));
                }
            }
                    generateGraph("X Range Data", "Time (Hours)", "X Range (Units)", xRange);
        }
        
        else if(yRangeButton.isSelected())
        {
            ArrayList<Double> yRange = new ArrayList();
            if(compute == false)
            {
                yRange = getMetricData("yrange", formattedFileString);
            }
            else
            {
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    yRange.add(Computation.find_range_of_motion(hourlyData.get(i)).get(1));
                }
            }
            generateGraph("Y Range Data", "Time (Hours)", "Y Range (Units)", yRange);
        }
        
        else if(xModeButton.isSelected())
        {
            ArrayList<Double> xMode = new ArrayList();
            if(compute == false)
            {
                xMode = getMetricData("xmode", formattedFileString);
            }
            else
            {
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    xMode.add(Computation.find_mode(hourlyData.get(i)).get(0));
                }
            }
            generateGraph("X Mode Data", "Time (Hours)", "X Mode (Units)", xMode);
        }
        
        else if(yModeButton.isSelected())
        {
            ArrayList<Double> yMode = new ArrayList();
            if(compute == false)
            {
                yMode = getMetricData("ymode", formattedFileString);
            }
            else
            {
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    yMode.add(Computation.find_mode(hourlyData.get(i)).get(1));
                }
            }
            generateGraph("Y Mode Data", "Time (Hours)", "Y Mode (Units)", yMode);
        }
        
        else if(smoothnessButton.isSelected())
        {
            ArrayList<Double> smoothness = new ArrayList();
            if(compute == false)
            {
                smoothness = getMetricData("smoothness", formattedFileString);
            }
            else
            {
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    smoothness.add(Computation.find_muscle_smoothness(hourlyData.get(i)));
                }
            }
            generateGraph("Muscle Smoothness Data", "Time (Hours)", "Muscle Smoothness (Units)", smoothness);
        }
        
        else if(tremorsButton.isSelected())
        {
            ArrayList<Double> tremors = new ArrayList();
            if(compute == false)
            {
                tremors = getMetricData("tremors", formattedFileString);
            }
            else
            {
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    tremors.add((double)Computation.find_tremors(hourlyData.get(i))); //casts int to Double
                }
            }
            generateGraph("Tremors Data", "Time (Hours)", "Tremors (Number of Tremors)", tremors);      
        }*/
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
        compute_metrics.setLabel("Display Graph");
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

        graphDisplayButtons.add(xRangeButton);
        xRangeButton.setSelected(true);
        xRangeButton.setText("X Range");

        graphDisplayButtons.add(yRangeButton);
        yRangeButton.setText("Y Range");

        graphDisplayButtons.add(xModeButton);
        xModeButton.setText("X Mode");

        graphDisplayButtons.add(yModeButton);
        yModeButton.setText("Y Mode");

        graphDisplayButtons.add(smoothnessButton);
        smoothnessButton.setText("Muscle Smoothness");

        graphDisplayButtons.add(tremorsButton);
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
