/*  NOTES
    
    -import data temporarily modified for testing so that it splits up whatever
    file is being used into 24 parts of 1000 data points each (to simulate
    24 hours of data) --> TODO: implement actual 24 hour/multiple day file read (and write)

    -TODO: figure out IO exception handling (and exception handling in general)*/

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        ArrayList<ArrayList<Double>> metricResultAll = new ArrayList<>();
        try
        {
            // If the file doesn't exist, calculate all the metrics and write to file
            if(!new File(fileMetricDirectory + "\\" + fileName).exists())
            {
                importDataFromFile(fileInputDirectory + "\\" + fileName);
                
                Computation compute = new Computation();
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileMetricDirectory + "\\" + fileName));

                ArrayList<Double> rangeX = new ArrayList<>();
                ArrayList<Double> rangeY = new ArrayList<>();
                ArrayList<Double> modeX = new ArrayList<>();
                ArrayList<Double> modeY = new ArrayList<>();
                ArrayList<Double> smoothness = new ArrayList<>();
                ArrayList<Double> tremors = new ArrayList<>();
                
                for(int i = 0; i < hourlyData.size(); i++)
                {
                    String output = "";
                    
                    ArrayList<Double> rangePoint = compute.find_range_of_motion(hourlyData.get(i));
                    rangeX.add(rangePoint.get(0));
                    output += rangePoint.get(0).toString() + ",";
                    
                    rangeY.add(rangePoint.get(1));
                    output += rangePoint.get(1).toString() + ",";
                    
                    ArrayList<Double> modePoint = compute.find_mode(hourlyData.get(i));
                    modeX.add(modePoint.get(0));
                    output += modePoint.get(0).toString() + ",";
                    
                    modeY.add(modePoint.get(1));
                    output += modePoint.get(1).toString() + ",";
                    
                    smoothness.add(compute.find_muscle_smoothness(hourlyData.get(i)));
                    output += smoothness.get(i).toString() + ",";
                    
                    tremors.add((double)compute.find_tremors(hourlyData.get(i)));
                    output += tremors.get(i).toString();
                    
                    writer.write(output);                   
                    writer.newLine();
                }                
                metricResultAll.add(rangeX);
                metricResultAll.add(rangeY);
                metricResultAll.add(modeX);
                metricResultAll.add(modeY);
                metricResultAll.add(smoothness);
                metricResultAll.add(tremors);
                
                writer.close();
                
                switch (metric) {
                        case "xrange":
                            return metricResultAll.get(0);
                        case "yrange":
                            return metricResultAll.get(1);
                        case "xmode":
                            return metricResultAll.get(2);
                        case "ymode":
                            return metricResultAll.get(3);
                        case "smoothness":
                            return metricResultAll.get(4);
                        case "tremors":
                            return metricResultAll.get(5);
                    }
                return null;
            }
            else
            {
                ArrayList<Double> metricResult = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(fileMetricDirectory + "\\" + fileName));
                String line = reader.readLine();
                
                while(line != null)
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
        }
        catch (Exception ex) 
        {
            System.out.println(ex.toString());
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
            generateGraph("X Range Data", "Time (Hou^rs)", "X Range (Units)", getMetricData("xrange", formattedFileString));
        }
        else if (yRangeButton.isSelected())
        {
            generateGraph("Y Range Data", "Time (Hours)", "Y Range (Units)", getMetricData("yrange", formattedFileString));
        }
        else if(xModeButton.isSelected())
        {
            generateGraph("X Mode Data", "Time (Hours)", "X Mode (Units)", getMetricData("xmode", formattedFileString));
        }
        else if (yModeButton.isSelected())
        {
            generateGraph("Y Mode Data", "Time (Hours)", "Y Mode (Units)", getMetricData("yrange", formattedFileString));
        }
        else if (smoothnessButton.isSelected())
        {
            generateGraph("Smoothness Data", "Time (Hours)", "Smoothness (Units)", getMetricData("smoothness", formattedFileString));
        }
        else if (tremorsButton.isSelected())
        {
            generateGraph("Tremors Data", "Time (Hours)", "Tremors (Number of Tremors)", getMetricData("tremors", formattedFileString));
        }
        Computation computation = new Computation();
        try {
            deteriorationLabel.setText(computation.deteriorationIndex());
        } catch (IOException ex) {
            Logger.getLogger(DigitForm.class.getName()).log(Level.SEVERE, null, ex);
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
        refresh = new java.awt.Button();
        xRangeButton = new javax.swing.JRadioButton();
        yRangeButton = new javax.swing.JRadioButton();
        xModeButton = new javax.swing.JRadioButton();
        yModeButton = new javax.swing.JRadioButton();
        smoothnessButton = new javax.swing.JRadioButton();
        tremorsButton = new javax.swing.JRadioButton();
        graphPanel = new javax.swing.JPanel();
        deteriorationLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 500));
        setPreferredSize(new java.awt.Dimension(900, 500));

        day_selector.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        day_selector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                day_selectorItemStateChanged(evt);
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
        xRangeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xRangeButtonMouseClicked(evt);
            }
        });

        graphDisplayButtons.add(yRangeButton);
        yRangeButton.setText("Y Range");
        yRangeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yRangeButtonMouseClicked(evt);
            }
        });

        graphDisplayButtons.add(xModeButton);
        xModeButton.setText("X Mode");
        xModeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                xModeButtonMouseClicked(evt);
            }
        });

        graphDisplayButtons.add(yModeButton);
        yModeButton.setText("Y Mode");
        yModeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                yModeButtonMouseClicked(evt);
            }
        });

        graphDisplayButtons.add(smoothnessButton);
        smoothnessButton.setText("Muscle Smoothness");
        smoothnessButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                smoothnessButtonMouseClicked(evt);
            }
        });

        graphDisplayButtons.add(tremorsButton);
        tremorsButton.setText("Tremors");
        tremorsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tremorsButtonMouseClicked(evt);
            }
        });

        graphPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(day_selector, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
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
                                .addComponent(refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(deteriorationLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(graphPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deteriorationLabel)
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(xRangeButton)
                                    .addComponent(yRangeButton)
                                    .addComponent(xModeButton)
                                    .addComponent(yModeButton)
                                    .addComponent(tremorsButton)
                                    .addComponent(smoothnessButton))
                                .addGap(0, 31, Short.MAX_VALUE))
                            .addComponent(refresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(day_selector, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    //when refresh button is clicked, take all file names from directory and put in list
    private void refreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_refreshMouseClicked
        refreshFromDataDirectory();
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_refreshMouseClicked

    private void day_selectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_day_selectorItemStateChanged
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_day_selectorItemStateChanged

    private void xRangeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xRangeButtonMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_xRangeButtonMouseClicked

    private void yRangeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yRangeButtonMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_yRangeButtonMouseClicked

    private void xModeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xModeButtonMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_xModeButtonMouseClicked

    private void yModeButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yModeButtonMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_yModeButtonMouseClicked

    private void smoothnessButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_smoothnessButtonMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_smoothnessButtonMouseClicked

    private void tremorsButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tremorsButtonMouseClicked
        computeMetricsFromRadioButtons();
    }//GEN-LAST:event_tremorsButtonMouseClicked

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
    private java.awt.List day_selector;
    private javax.swing.JLabel deteriorationLabel;
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
