import java.util.ArrayList;

public class DataFiltering {
    //what index we set the int array of force trainer data to in InputStreamHandler
    public static final int ATTENTION_INDEX = 1;
    public static final int MEDITATION_INDEX = 2;
    public static int FRAME_SIZE = 10;
    public static int ANALYSIS_PERIOD = 3;
    public ArrayList<Double> averageDifferences = new ArrayList<>();
    private final InputStreamHandler instance = Main.input_stream_handler;

    /**
     * Uses FRAME_SIZE to determine the average difference between attention and meditation.
     * @return the average difference between attention and meditation, where a positive number means more attention
     */

    final int MEDITATION_SUBTRACT = 10;
    public double averageDifferenceCurrent(){
        Pair<Integer, Integer>[] snapshot = attentionMeditationSnapshot(FRAME_SIZE);
        int totalDifference = 0;
        for(Pair<Integer, Integer> pair : snapshot){
            totalDifference += pair.first - (pair.second - MEDITATION_SUBTRACT);

        }
        return (double) totalDifference / FRAME_SIZE;
    }

    private Pair<Integer, Integer>[] attentionMeditationSnapshot(int length){
        Pair<Integer, Integer>[] snapshot = new Pair[length];
        for(int i = 0; i<length; i++){
            int[] currentValues = instance.arrlistForceData.get(instance.arrlistForceData.size() - length + i);
            snapshot[i] = new Pair<>(currentValues[ATTENTION_INDEX], currentValues[MEDITATION_INDEX]);
        }
        return snapshot;
    }


    final int CHANGE_THRESH = 20;
    final float DOWNWARDS_MULTIPLIER = 0.8f;
    final float UPWARDS_MULTIPLIER = 1f;
    final float PATTERN_BREAK_THRESHOLD = 5f;
    //running total of differences between snapshots
    double runningTotalDifference = 0;

    /**
     * Compares current snapshot to previous snapshot
     */
    public double diffOfDiffs(int index){
        return averageDifferences.get(index) - averageDifferences.get(index - 1);
    }

    public void analyzeForSpike(int window){
        assert(averageDifferences.size() > 1);

        
    }

    public void checkTrigger(){

    }

    public void checkForAttentionSpike(int window){

    }

    final int MOVING_AVERAGE_SIZE = 20;
    double focusAverage = 0;
    ArrayList<Integer> movingAverage = new ArrayList<>();
    public void updateMovingAverage(int focusCurrent){
        movingAverage.add(focusCurrent);
        if(movingAverage.size() > MOVING_AVERAGE_SIZE){
            movingAverage.remove(0);
        }
    }
}
