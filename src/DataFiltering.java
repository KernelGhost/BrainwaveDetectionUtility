import java.util.ArrayList;

public class DataFiltering {
    //what index we set the int array of force trainer data to in InputStreamHandler
    public static final int ATTENTION_INDEX = 1;
    public static final int MEDITATION_INDEX = 2;
    public static int FRAME_SIZE = 5;
    public ArrayList<Double> averageDifferences = new ArrayList<>();
    private final InputStreamHandler instance = Main.input_stream_handler;

    /**
     * Uses FRAME_SIZE to determine the average difference between attention and meditation.
     * @return the average difference between attention and meditation, where a positive number means more attention
     */
    public double averageDifferenceCurrent(){
        Pair<Integer, Integer>[] snapshot = attentionMeditationSnapshot(FRAME_SIZE);
        int totalDifference = 0;
        for(Pair<Integer, Integer> pair : snapshot){
            totalDifference += pair.first - pair.second;
        }
        return (double) totalDifference / FRAME_SIZE;
    }

    private Pair<Integer, Integer>[] attentionMeditationSnapshot(int length){
        Pair<Integer, Integer>[] snapshot = new Pair[length];
        for(int i = 0; i<length; i++){
            int[] currentValues = instance.arrlistForceData.get(instance.arrlistForceData.size()-(length - i - 1));
            snapshot[i] = new Pair<>(currentValues[ATTENTION_INDEX], currentValues[MEDITATION_INDEX]);
        }
        return snapshot;
    }
}
