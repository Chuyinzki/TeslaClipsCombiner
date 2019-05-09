import java.io.File;

public class TeslaClipCombiner {

    public final static int FRONT_MODE = 0;
    public final static int LEFT_REPEATER_MODE = 1;
    public final static int RIGHT_REPEATER_MODE = 2;

    public static void main(String[] args) {

        Runtime.getRuntime().exec("ffmpeg -r 1 -i sample%d.png -s 320x240 -aspect 4:3 output.flv");

    }

    public static File combineClips
}
