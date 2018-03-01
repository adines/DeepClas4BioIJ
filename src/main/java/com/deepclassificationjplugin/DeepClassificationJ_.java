package com.deepclassificationjplugin;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author adines
 */
@Plugin(type = Command.class)
public class DeepClassificationJ_ implements Command {

    @Parameter
    private ImagePlus imp;
    @Parameter
    private String pathAPI;
    
    GenericDialog gd;
    FilenameFilterFramework filter;
    Choice frameworkChoices;
    Choice modelChoices;
    String[] opcionesModel = {"VGG16", "VGG19", "Inception", "Resnet50", "ResNetISIC"};

    @Override
    public void run() {
        try {


            String[] opcionesFramework = {"Keras", "Caffe", "DL4J", "TensorFlow"};
            String fr = "Keras";
            filter = new FilenameFilterFramework(fr);

            imp = IJ.getImage();

            String path = IJ.getDirectory("image");
            String name = imp.getTitle();
            String image = path + name;
            gd = new GenericDialog("Select Input");
            gd.addChoice("Framework", opcionesFramework, "Keras");
            gd.addChoice("Model", opcionesModel, "VGG16");

            Vector v = gd.getChoices();
            frameworkChoices = (Choice) v.get(0);
            modelChoices = (Choice) v.get(1);
            File directory = new File(pathAPI + fr);
            String[] files = directory.list(filter);
            List<String> f = new ArrayList<String>();

            for (String s : files) {
                int i = s.indexOf("ModelClassification.py");
                f.add(s.substring(fr.length(), i));
            }
            modelChoices.removeAll();
            for (String s : f) {
                modelChoices.add(s);
            }
            frameworkChoices.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    String frameworkSelected = frameworkChoices.getSelectedItem();
                    if (!frameworkSelected.equals("DL4J")) {
                        filter = new FilenameFilterFramework(frameworkSelected);
                        File directory = new File(pathAPI + frameworkSelected);
                        String[] files = directory.list(filter);
                        List<String> f = new ArrayList<String>();
                        for (String s : files) {
                            int i = s.indexOf("ModelClassification.py");
                            f.add(s.substring(frameworkSelected.length(), i));
                        }
                        modelChoices.removeAll();
                        for (String s : f) {
                            modelChoices.add(s);
                        }
                        modelChoices.doLayout();
                        gd.doLayout();
                    } else {
                        try {
                            String comando = "java -jar " + pathAPI + "PredictDL4JMaven-1.0-SNAPSHOT.jar";
                            Process p = Runtime.getRuntime().exec(comando);

                            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));

                            ReadErrorThread rerror = new ReadErrorThread(p.getErrorStream());
                            ReadOutThread rOut = new ReadOutThread(stdout);

                            rerror.start();
                            rOut.start();
                            rerror.join();
                            rOut.join();
                            String ultimaLinea = rOut.getLinea();
                            String modelos[] = ultimaLinea.split(",");

                            modelChoices.removeAll();
                            for (String s : modelos) {
                                modelChoices.add(s);
                            }
                            modelChoices.doLayout();
                            gd.doLayout();
                        } catch (IOException ex) {
                            Logger.getLogger(DeepClassificationJ_.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DeepClassificationJ_.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            });

            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }

            String framework = gd.getNextChoice();
            System.out.println(framework);
            String model = gd.getNextChoice();
            System.out.println(model);

            String comando = "python " + pathAPI + "predict.py -i " + image + " -f " + framework + " -m " + model;
            System.out.println(comando);
            Process p = Runtime.getRuntime().exec(comando);

            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));

            ReadErrorThread rerror = new ReadErrorThread(p.getErrorStream());
            ReadOutThread rOut = new ReadOutThread(stdout);

            rerror.start();
            rOut.start();
            rerror.join();
            rOut.join();
            String ultimaLinea = rOut.getLinea();

            IJ.showMessage("Prediction", "The class which the image belongs is " + ultimaLinea);
            System.out.println(ultimaLinea);

        } catch (FileNotFoundException ex) {
            IJ.showMessage("Error", "You need to indicate the path of the API in the config file.");
        } catch (IOException ex) {
            Logger.getLogger(DeepClassificationJ_.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeepClassificationJ_.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
