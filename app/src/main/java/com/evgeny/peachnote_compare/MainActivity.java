package com.evgeny.peachnote_compare;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Void> {
    private final String alignUrl = "http://alignment-demo.s3.amazonaws.com/IMSLP-YT-AlignmentQuality.json";


    private final String[] scoreSyncPairs = {
            "http://alignment-demo.s3.amazonaws.com/alignments/IMSLP00030_27btQJaXCsE.json",
            "http://alignment-demo.s3.amazonaws.com/alignments/IMSLP00030_HxB1MKOo9FI.json",
            "http://alignment-demo.s3.amazonaws.com/alignments/IMSLP00030_MEeO8uULTKY.json",
            "http://alignment-demo.s3.amazonaws.com/alignments/IMSLP00030_mUif2NDX-NI.json",
            "http://alignment-demo.s3.amazonaws.com/alignments/IMSLP00030_oiycDv0uu-s.json"
    };

    private Map<String, Alignment> alignments = Maps.newHashMap();

    public final int VELOCITY_WINDOW = 5;  // in seconds

    private Gson gson = new Gson();

    /**
     * map videoid to [segment][velocity]
     * TODO may be map alignment to [segment][velocity] would be better
     */
    private Map<String, double[][]> velocities;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    /**
     * TODO input values coming from peacnote app, for development use a simple url array{@link #scoreSyncPairs}
     * @param scoreId
     * @param videoId
     * @param jsonPath
     * @param counter
     */
    private void fetchAlignmentData(String scoreId, String videoId, String jsonPath, int counter) {
        final OkHttpClient httpClient = new OkHttpClient();
        for (String scoreSyncPair : scoreSyncPairs) {
            Request request = new Request.Builder()
                    .url(scoreSyncPair)
                    .build();
            try {
                Response response = httpClient.newCall(request).execute();
                Alignment alignment = gson.fromJson(response.body().charStream(), Alignment.class);
                alignments.put(scoreSyncPair, alignment);
//                System.out.println("response = " + alignment.localTimeMaps[0][0][2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Void>(this) {
            @Override
            public Void loadInBackground() {
                fetchAlignmentData("", "", "", 1);
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        calculateSegmentVelocity();
        createAlignmentSegmentRepresentation();
        CompareView compareView = (CompareView) findViewById(R.id.compare_view);
        compareView.setAlignments(alignments.get(scoreSyncPairs[0]));
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        //do nothing here
    }

    /**
     * Calculate segments velocity of each video record
     */
    private void calculateSegmentVelocity() {
        velocities = Maps.newHashMap();

        // for each segment of each alignment compute the velocity
        for (String scoreSyncPair : scoreSyncPairs) {
            Alignment alignment = alignments.get(scoreSyncPair);

            double[][] segmentVelocityMap = new double[alignment.localTimeMaps.length][];
            for (int segment = 0; segment < alignment.localTimeMaps.length; segment++) {
                double[][] segmentTimeMap = alignment.localTimeMaps[segment];

                System.out.println("segment time map = " + segmentTimeMap.length);
                if (segmentTimeMap[0].length < 2) continue;

                ArrayList<double[]> av = updateBinVelocities(segmentTimeMap);
                double[] binV = averageBinVelocities(av);
                System.out.println("binV=" + Arrays.toString(binV));
                segmentVelocityMap[segment] = binV;
            }

            velocities.put(alignment.uri1, segmentVelocityMap);
        }
    }

    private ArrayList<double[]> updateBinVelocities(double[][] segmentTimeMap) {
//        var av = [],
        ArrayList<double[]> av = new ArrayList<>();
        double tScorePrev = segmentTimeMap[0][0];
        double tVideoPrev = segmentTimeMap[1][0];
        double prevBin = Math.floor(tScorePrev / VELOCITY_WINDOW);
//                moment, tScore, tVideo, currentBin, velocity, bin, binIntervalShare;

        double tScore, tVideo;
        for (int moment = 1; moment < segmentTimeMap[0].length; moment++) {
            tScore = segmentTimeMap[0][moment];
            tVideo = segmentTimeMap[1][moment];

            double currentBin = Math.floor(tScore / VELOCITY_WINDOW);

            double velocity;
            if (tVideo > tVideoPrev) {
                velocity = (tScore - tScorePrev) / (tVideo - tVideoPrev);
            } else {
                continue;
            }

            if (moment == 1) {
                //av.push([prevBin, velocity, tScore / CONSTANTS.VELOCITY_WINDOW - prevBin]);
                double[] item = {prevBin, velocity, tScore / VELOCITY_WINDOW - prevBin};
                av.add(item);
            }

            double binIntervalShare;
            for (int bin = (int) prevBin; bin <= currentBin; bin++) {
                binIntervalShare = Math.min(tScore / VELOCITY_WINDOW, bin + 1) - Math.max(tScorePrev / VELOCITY_WINDOW, bin);
                av.add(new double[]{bin, velocity, binIntervalShare});
            }

            if (moment == segmentTimeMap[0].length - 1) {
                av.add(new double[]{currentBin, velocity, currentBin + 1 - tScore / VELOCITY_WINDOW});
            }


            tScorePrev = tScore;
            tVideoPrev = tVideo;
            prevBin = currentBin;
        }
        return av;
    }

    private double[] averageBinVelocities(ArrayList<double[]> av) {
        double[] binV = new double[av.size()];
        int bin;
        double v, s;

        for (double[] t : av) {
            bin = (int) t[0];
            v = t[1];
            s = t[2];

            binV[bin] += (v * s);
        }

        return binV;
    }

    private double[] getGradientValues(double[] tickVelocities) {
        double currentVel;
        double[] gradientValues = new double[tickVelocities.length];
        for (int tick = 0; tick < tickVelocities.length; tick++) {
            currentVel = tickVelocities[tick];
            gradientValues[tick] = (Math.atan(currentVel)/(Math.PI/2)) * 0.5;
        }

        return gradientValues;
    }

    public void createAlignmentSegmentRepresentation() {
        for (Map.Entry<String, double[][]> entry : velocities.entrySet()) {
            System.out.println("compute gradient for video id=" + entry.getKey());
            for (double[] segment : entry.getValue()) {
                double[] gradients = getGradientValues(segment);
                System.out.println("gradients are=" + Arrays.toString(gradients));
            }
        }
    }

    public static class Alignment {
        String uri0;
        String uri1;
        String source0;
        String source1;
        double[][][] localTimeMaps;
    }

}
