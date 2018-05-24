package com.deepclassificationj;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author adines
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>DeepClassificationJ")
public class DeepClassificationJ implements Command {

    @Parameter
    private ImagePlus imp;
    @Parameter
    private String pathAPI;

    GenericDialog gd;
    Choice frameworkChoices;
    Choice modelChoices;
    String[] opcionesModel = {"VGG16", "VGG19", "Inception", "Resnet50", "ResNetISIC"};
    String[] opcionesFramework;

    @Override
    public void run() {
        
        try {
            String so=System.getProperty("os.name");
            String python;
            if(so.contains("Windows"))
            {
                python="python ";
            }else{
                python="python3 ";
            }
            String comando = python + pathAPI + "listFrameworks.py";
            Process p = Runtime.getRuntime().exec(comando);
            p.waitFor();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("data.json"));
            JSONArray frameworks = (JSONArray) jsonObject.get("frameworks");
            int i = 0;
            opcionesFramework = new String[frameworks.size()];
            for (Object o : frameworks) {
                opcionesFramework[i] = (String) o;
                i++;
            }

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

            comando = python + pathAPI + "listModels.py -f Keras";
            p = Runtime.getRuntime().exec(comando);
            p.waitFor();
            JSONParser parser2 = new JSONParser();
            JSONObject jsonObject2 = (JSONObject) parser2.parse(new FileReader("data.json"));
            JSONArray models = (JSONArray) jsonObject2.get("models");
            modelChoices.removeAll();
            for (Object o : models) {
                modelChoices.add((String) o);
            }

            frameworkChoices.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    try {
                        String frameworkSelected = frameworkChoices.getSelectedItem();
                        String comando = python + pathAPI + "listModels.py -f " + frameworkSelected;
                        Process p = Runtime.getRuntime().exec(comando);
                        p.waitFor();
                        JSONParser parser = new JSONParser();
                        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("data.json"));
                        JSONArray frameworks = (JSONArray) jsonObject.get("models");
                        modelChoices.removeAll();
                        for (Object o : frameworks) {
                            modelChoices.add((String) o);
                        }
                        modelChoices.doLayout();
                        gd.doLayout();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParseException ex) {
                        Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });

            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }

            String framework = gd.getNextChoice();
            String model = gd.getNextChoice();

            comando = python + pathAPI + "predict.py -i " + image + " -f " + framework + " -m " + model;
            p = Runtime.getRuntime().exec(comando);
            p.waitFor();

            JSONParser parser3 = new JSONParser();
            JSONObject jsonObject3 = (JSONObject) parser3.parse(new FileReader("data.json"));
            String classPredict = (String) jsonObject3.get("class");

            IJ.showMessage("Prediction", "The class which the image belongs is " + classPredict);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(DeepClassificationJ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
