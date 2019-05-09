import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeslaClipCombiner {

    public final static String FRONT_MODE = "front";
    public final static String LEFT_REPEATER_MODE = "left_repeater";
    public final static String RIGHT_REPEATER_MODE = "right_repeater";
    public final static String[] ALL_MODES = {FRONT_MODE, LEFT_REPEATER_MODE, RIGHT_REPEATER_MODE};

    public static void main(String[] args) throws IOException {
        for (String mode : ALL_MODES)
            TeslaClipCombiner.combineClips(new File(TeslaClipCombiner.class
                    .getClassLoader().getResource("sample_files").getFile()), mode);

    }

    public static File combineClips(File folderFiles, String mode) throws IOException {
        //https://trac.ffmpeg.org/wiki/Concatenate#demuxer

        List<String> filePaths = new ArrayList<String>();
        for (File videoFile : folderFiles.listFiles())
            if (videoFile.getName().contains(mode) && videoFile.length() != 0)
                filePaths.add(videoFile.getAbsoluteFile().toString());

        Collections.sort(filePaths);
        PrintWriter writer = new PrintWriter("mylist.txt", "UTF-8");
        for (String filePath : filePaths)
            writer.println(String.format("file '%s'", filePath));
        writer.close();

        String filePath = TeslaClipCombiner.class
                .getClassLoader().getResource("ffmpeg").getFile();
        File outputFolder = new File("outputFolder");
        if (!outputFolder.mkdir())
            for (File file : outputFolder.listFiles())
                if(file.getName().contains(mode))
                    file.delete();

        runCommand(String.format("%s -f concat -safe 0 -i mylist.txt -c copy %s/%s.mp4", filePath, outputFolder, mode));
        return null; //return combined file
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
