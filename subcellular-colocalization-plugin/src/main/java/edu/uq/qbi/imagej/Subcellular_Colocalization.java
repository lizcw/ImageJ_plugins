package edu.uq.qbi.imagej;
import ij.*;
import ij.io.*;
import ij.plugin.FolderOpener;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

import ij.plugin.frame.*;

import javax.swing.*;
import javax.swing.border.Border;

public class Subcellular_Colocalization extends PlugInFrame{
    // plugin parameters
    public String inputdir;
    public String outputdir;
    public Boolean blackbg;
    public Boolean adjustbg;
    public String roiset;
    public ImagePlus img;
    public String title;

    public JButton btnProcess;
    public JButton btnCancel;
    public JButton btnBackground;
    public JButton btnROI;
    public JCheckBox cbBlackbg;
    public JTextField txtROIsuffix;
    public JTextField txtROIfile;
    public JLabel lbROIsuffix;
    public JLabel lbROIfile;
    public JTextField txtOutputdir;
    public JLabel lbOutputdir;
    public JButton btnOutputdir;

    static final int WIDTH = 600;
    static final int HEIGHT = 400;
    static final String TITLE = "Subcellular Colocalization";


    public Subcellular_Colocalization() {
        super(TITLE);
        setLayout(new FlowLayout());
        //this.showConfig();
        this.addButtons();
    }

    private boolean showConfig() {
        GenericDialog gd = new GenericDialog(this.getTitle());
        //gd.setLayout(new BorderLayout());
        gd.addMessage("Select options:");
        gd.addCheckbox("Black background", false);
        gd.addCheckbox("Adjust background", false);
        gd.addStringField("ROI file suffix", "_ROIset.zip");
        gd.showDialog();
        if (gd.wasCanceled())
            return false;

        // get entered values
        this.blackbg = gd.getNextBoolean();
        this.adjustbg = gd.getNextBoolean();
        this.roiset = gd.getNextString();

        return true;
    }
    private String getROIfilename(){
        String suffix = txtROIsuffix.getText();
        if (img == null){
            return suffix;
        }
        String imgtitle = img.getTitle();
        String ext = ".tif";
        if (imgtitle.indexOf(".ome.tif")> 0){
            ext = ".ome.tif";
        }

        String roifile = new String(inputdir + imgtitle.replace(ext, suffix));
        System.out.println("ROIfile: " + roifile);
        return roifile;
    }

    private void addButtons(){
        txtOutputdir = new JTextField();
        txtOutputdir.setSize(30, 200);
        lbOutputdir = new JLabel("Output directory");
        btnOutputdir = new JButton("Find");
        btnOutputdir.setSize(30,50);
        btnOutputdir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser(txtOutputdir.getText());
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (e.getSource() == btnOutputdir) {
                    int returnVal = fc.showOpenDialog(Subcellular_Colocalization.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        outputdir = fc.getSelectedFile().getPath();
                        txtOutputdir.setText(outputdir);
                        System.out.println("Output dir:" + outputdir);
                    }
                }
            }
        });

        cbBlackbg = new JCheckBox("Black background", false);
        lbROIsuffix = new JLabel("ROI file suffix");
        lbROIfile = new JLabel("ROI filename");
        txtROIfile = new JTextField();
        txtROIfile.setSize(30, 500);
        txtROIfile.setEnabled(false);
        txtROIsuffix = new JTextField( "_ROIset.zip");
        txtROIsuffix.setEnabled(false);
        txtROIsuffix.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Update ROI set file");
                txtROIfile.setText(getROIfilename());
            }
        });
        btnBackground = new JButton("Remove heavy background");
        btnBackground.setSize(30,100);
        btnBackground.setEnabled(false);
        btnBackground.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Processing " + actionEvent.getActionCommand());
                adjustBackground();
            }
        });
        btnROI = new JButton("Extract ROI images from zip");
        btnROI.setSize(30,100);
        btnROI.setEnabled(false);
        btnROI.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Processing " + actionEvent.getActionCommand());
                extractROIImages();
            }
        });
        btnProcess = new JButton("Open Image");
        btnProcess.setSize(30,100);
        btnProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Processing " + actionEvent.getActionCommand());
                openImage();
                txtROIfile.setEnabled(true);
                txtROIsuffix.setEnabled(true);
                btnROI.setEnabled(true);
                btnBackground.setEnabled(true);
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.setSize(30,100);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("Cancelled " + actionEvent.getActionCommand());
                close();
            }
        });

    }

    public void run(String arg) {
        //Add buttons to start/cancel

        JPanel p = new JPanel(new GridLayout(4, 1, 3, 3));
        JPanel p1 = new JPanel(new GridLayout(2, 3, 3, 3));
        JPanel p2 = new JPanel(new GridLayout(1, 3, 3, 3));
        JPanel p3 = new JPanel(new GridLayout(3, 3, 3, 3));
        JPanel p4 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("Background"));
        p2.add(cbBlackbg);
        p2.add(btnBackground);
        p3.setBorder(BorderFactory.createTitledBorder("ROI set"));
        p3.add(lbROIsuffix);
        p3.add(txtROIsuffix);
        p3.add(lbROIfile);
        p3.add(txtROIfile);
        p3.add(btnROI);
        p1.setBorder(BorderFactory.createTitledBorder("Directories"));
        p1.add(lbOutputdir);
        p1.add(txtOutputdir);
        p1.add(btnOutputdir);
        p1.add(btnProcess);
        p1.add(btnCancel);
       //p1.add(new ImageCanvas(img));
        p.add(p1 );
        p.add(p2);
        p.add(p3);
        //p.add(p4,BorderLayout.SOUTH);

        //this.setLayout(new FlowLayout());
        this.add(p);
        this.pack();
        this.setTitle(TITLE);
        this.setSize(WIDTH, HEIGHT);
        this.setResizable(false);
        this.setLocationByPlatform(true);
        this.setVisible(true);

    }


    public void showAbout() {
        IJ.showMessage(this.getTitle(),
                "Colocalization of subcellular particles"
        );
    }

    public void adjustBackground(){
        //Check for multi-channel
        int s = img.getImageStackSize();
        int channels = img.getNChannels();
        int slices = img.getNSlices();
        int frames = img.getNFrames();
        //Stack.getDimensions(width, height, channels, slices, frames);
        System.out.println("Channels: " + channels + " Slices: " + slices + " Frames: " + frames);
        //IJ.log("Running background adjustment ... ");
        for (int t=1; t<=frames; t++) {
            for (int z=1; z<=slices; z++) {
                for (int c=1; c<=channels; c++) {
                    img.setPosition(c, z, t);
                    IJ.run("Subtract Background...", "rolling=50");
                    IJ.run("Window/Level...");
                    IJ.run("Enhance Contrast", "saturated=0.35");
                    //run("Apply LUT");
                }
            }
        }
    }

    public void extractROIImages(){
        String roifile = txtROIfile.getText();
        RoiManager roiM = new RoiManager();
        roiM.reset();
        int n = 0;
        try{
            roiM.runCommand("Open",roifile);
            n = roiM.getCount();
            System.out.println("Loaded ROIs: " + n);
        }catch(Exception e){
            System.out.println("Cannot load ROIs: " + e.getMessage());
            throw e;
        }
        //Set background
        Boolean blackbg = cbBlackbg.isSelected();
        if (blackbg == true){
            IJ.setBackgroundColor(0,0,0);
            System.out.println("setting bg to black");
        }else{
            IJ.setBackgroundColor(255,255,255);
            System.out.println("setting bg to white");
        }

        //selectWindow(title);
        //Stack.getDimensions(width, height, channels, slices, frames);
        //Process ROIs (must be manually drawn and added to ROI Manager)
        for (int i=0; i< n; i++){
            //only crops one channel and not cleared outside
            ImagePlus im2 = (ImagePlus) img.clone();
            roiM.select(im2, i);
            String seln = roiM.getName(i);
            String tiff = getCroppedImageFilename(seln);
            FileSaver fs = new FileSaver(im2);
            fs.saveAsTiff(tiff);
            System.out.println("ROI saved:" + tiff);
        }

        //Save area measurements to CSV
//        nameOfSummaryTable = "ROISummary";
//        IJ.renameResults(nameOfSummaryTable);
//        selectWindow(nameOfSummaryTable);
//        saveAs("Text", outputdir + filesep + basename + "_" + nameOfSummaryTable + ".csv");
//        //Clear ROI manager - uncomment this if single mode
//        roiManager("reset");*/
    }

    private String getCroppedImageFilename(String seln) {
        String basename = img.getTitle();
        String ext = ".tif";
        if (basename.contains(".ome.tif")){
            ext = ".ome.tif";
        }
        basename = basename.replace(ext, "_" + seln + ".tiff");
        System.out.println("Cropped filename=" + basename);
        return new String(outputdir + File.separator + basename);
    }

    public void openImage(){
        System.out.println("Processing images");
        Opener op = new Opener();
        op.open();
        img = IJ.getImage();
        inputdir = img.getOriginalFileInfo().directory;
        System.out.println("Name:" + img.getTitle());
        System.out.println("Dir:" + inputdir);
        //Also run ROI set
        txtROIfile.setText(getROIfilename());


        //Close
        //WindowManager wm = IJ.WindowManager;
        //IJ.getWindowManager().closeAllWindows();
    }
   /* public void process(){
        ImagePlus filelist = new FolderOpener();
        for(j=0;j<filelist.length;j++){
            inputfile = dir1 + filelist[j];
            if (!File.isDirectory(inputfile)){
                if (endsWith(filelist[j], ".tif")){
                    showProgress(j+1, filelist.length);
                    //If normal tiff file - use this: open(dir1 + filelist[j]);
                    //If tiled tiff file  - use Bioformats
                    if (endsWith(filelist[j], ".ome.tif")){
                        run("Bio-Formats", "open=[" + inputfile + "] color_mode=Default split_channels open_files view=Hyperstack stack_order=XYCZT use_virtual_stack contains=[] name=[" + inputfile + "]");
                    }else{
                        open(inputfile);
                    }
                    //get Image refs
                    title = getTitle();
                    basename = File.nameWithoutExtension;
                    print("***Image opened: ", title);
                    if (adjustbg == true){
                        print("Adjusting Background");
                        adjustBackground(outputdir);
                    }

                    //Extract ROIs
                    print("Extracting ROIs");
                    extractROIImages(outputdir, blackbg, roiset);

                    //Clean up windows
                    run("Close All");
                }
            }
        }
    }*/

    /**
     * Main method for debugging.
     *
     * For debugging, it is convenient to have a method that starts ImageJ, loads
     * an image and calls the plugin, e.g. after setting breakpoints.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = Subcellular_Colocalization.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // open the Clown sample
        //ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
       // image.show();

        // run the plugin
        IJ.runPlugIn(clazz.getName(), "");
    }
}
