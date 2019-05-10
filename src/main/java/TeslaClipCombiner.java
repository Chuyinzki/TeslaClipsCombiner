import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeslaClipCombiner {

    public final static String FRONT_MODE = "front";
    public final static String LEFT_REPEATER_MODE = "left_repeater";
    public final static String RIGHT_REPEATER_MODE = "right_repeater";
    public final static String[] ALL_MODES = {FRONT_MODE, LEFT_REPEATER_MODE, RIGHT_REPEATER_MODE};

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        final File[] sourceFolder = {null};
        final File[] outputFolder = {null};

        JFrame frame = new JFrame("Tesla Clip Combiner");
        frame.setSize(500, 100);
        frame.setVisible(true);

        File tempListFile = new File("TeslaClipCombiner_TEMP_FILE.txt");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                JFileChooser f = new JFileChooser();
                f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f.setDialogTitle("Select folder with Tesla video files");

                if (f.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    sourceFolder[0] = f.getSelectedFile();
                }
            }
        };

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                JFileChooser f2 = new JFileChooser();
                f2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                f2.setDialogTitle("Select output folder to put combined videos");
                if (f2.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    outputFolder[0] = f2.getSelectedFile();
                }
            }
        };

        SwingUtilities.invokeAndWait(r);
        SwingUtilities.invokeAndWait(r2);


        JPanel p = new JPanel();
        JProgressBar b = new JProgressBar();
        b.setValue(0);
        b.setStringPainted(true);
        p.add(b);
        frame.add(p);

        if (sourceFolder[0] != null && outputFolder[0] != null) {
            try {
                for (String mode : ALL_MODES) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            b.setValue(Math.min(b.getValue() + (100 / ALL_MODES.length), 100));
                        }
                    });
                    TeslaClipCombiner.combineClips(sourceFolder[0], outputFolder[0], mode, tempListFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Folders were not chosen, please try again.",
                    "Folders not selected", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
        b.setValue(100);
        tempListFile.delete();
        JOptionPane.showMessageDialog(null, "Process Complete", "Finished", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    public static void combineClips(File sourceFolder, File outputFolder, String mode, File tempListFile) throws IOException {
        //https://trac.ffmpeg.org/wiki/Concatenate#demuxer

        List<String> filePaths = new ArrayList<String>();
        for (File videoFile : sourceFolder.listFiles())
            if (videoFile.getName().contains(mode) && videoFile.length() != 0)
                filePaths.add(videoFile.getAbsoluteFile().toString());

        Collections.sort(filePaths);
        PrintWriter writer = new PrintWriter(tempListFile, "UTF-8");
        for (String filePath : filePaths)
            writer.println(String.format("file '%s'", filePath));
        writer.close();

        String filePath = TeslaClipCombiner.class
                .getClassLoader().getResource("ffmpeg").getFile();
        if (!outputFolder.mkdir())
            for (File file : outputFolder.listFiles())
                if (file.getName().contains(mode)) {
                    JOptionPane.showMessageDialog(null, "Output folder already includes " +
                            "file \"" + mode + "\"", "Failed to create file for " + mode, JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

        runCommand(String.format("%s -f concat -safe 0 -i %s -c copy %s/%s.mp4", filePath, tempListFile, outputFolder, mode));
    }

    //This code from: https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
    public static void runCommand(String command) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

// read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

// read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
}
