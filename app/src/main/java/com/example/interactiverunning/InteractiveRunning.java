package com.example.interactiverunning;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Collections;

public final class InteractiveRunning {
    private InteractiveRunning() {
        //Disable default constructor
    }

    // 400 values in input array, approx 8 steps
    // Frequency for peaks from user input
    // Running speed from user input
    public static double[] calculateData(final double[] dataX, final double[] dataY,
                                         final double[] dataZ, final double[] dataSensorT, final double userSpeed) {
        // Time data
        double t0 = dataSensorT[0];
        double[] dataT = new double[dataSensorT.length];
        final int BASE = 10;
        final int nanoExponent = -9;
        for (int i = 0; i < dataSensorT.length; i++) {
            dataT[i] = ((dataSensorT[i] - t0) * Math.pow(BASE, nanoExponent));
        }

        // --------- Filter the data ---------
        double fc = 4.129;
        double[] dataXFiltered = lowPassFilter(dataX, fc, dataT);
        double[] dataYFiltered = lowPassFilter(dataY, fc, dataT);
        double[] dataZFiltered = lowPassFilter(dataZ, fc, dataT);

        // ---------  Get the vector magnitude ---------
        double timeToRemove = 3;
        double indexToRemove = Math.abs(Math.round(timeToRemove / (dataSensorT[2] - dataSensorT[1])));
        ArrayList<Double> accNormList = new ArrayList<>();
        for (int j = 0; j < dataT.length - indexToRemove; j++) {
            double a = Math.pow(dataXFiltered[j], 2) + Math.pow(dataYFiltered[j], 2) + Math.pow(dataZFiltered[j], 2);
            accNormList.add(Math.sqrt(a));
        }

        Double[] accNorm = new Double[accNormList.size()];
        double max = Collections.max(accNormList);

        for (int j = 0; j < accNorm.length; j++) {
            accNorm[j] = accNormList.get(j) / max;
        }

        // --------- Cadence ---------
        // Get all peaks and time for every peak
        Double[] pks = findPeak(accNorm);
        Double[] t = findTimeOfPeak(accNorm, dataT);

        // Remove to low peaks
        double peakThreshold = 0.75;
        ArrayList<Double> peaksList = new ArrayList<>();
        ArrayList<Double> timeList = new ArrayList<>();

        for (int ind = 0; ind < pks.length; ind++) {
            if (pks[ind] > peakThreshold) {
                peaksList.add(pks[ind]);
                timeList.add(t[ind]);
            }
        }

        // make array of list
        Double[] peaks = new Double[peaksList.size()];
        peaks = peaksList.toArray(peaks);
        Double[] peakTime = new Double[timeList.size()];
        peakTime = timeList.toArray(peakTime);

        // Count the peaks
        int numPeaks = peaks.length;
        System.out.println("The number of detected steps are: ");
        System.out.println(numPeaks);
        // recalculate the unit of the cadence
        int seconds = 60;
        double timeElapsed = peakTime[numPeaks - 1] - peakTime[0]; // time between first and last
        double cadence = Math.round(numPeaks * seconds / timeElapsed); // [steps/min]

        // print result
        System.out.println("The cadence is:");
        System.out.print(cadence);
        System.out.print(" steps/min");
        System.out.println(" ");

        // --------- stride length ---------
        double localSpeed = userSpeed / 3.6;
        // Time difference between the peaks
        double[] strideLength = new double[numPeaks - 1];
        double accStrideLength = 0;
        for (int i = 0; i < numPeaks - 1; i++) {
            // Stride length (s=v*t)
            strideLength[i] = localSpeed * (peakTime[i + 1] - peakTime[i]) / 2;
            accStrideLength += strideLength[i];
        }

        // Calculate average stride length
        double meanStrideLength = accStrideLength / (numPeaks - 1);
        System.out.println("The mean stride length is:");
        int cm = 100;
        System.out.print(meanStrideLength * cm); // fixing the unit to cm
        System.out.print(" cm");
        System.out.println(" ");

        // --------- Ground contact time (GCT) ---------
        // Find the valleys time for every valley
        Double[] vly = findValley(accNorm);
        Double[] timeVly = findTimeOfValley(accNorm, dataT);
        // Remove the low valleys
        double valleyThreshold = 0.75;
        ArrayList<Double> valleyList = new ArrayList<>();
        ArrayList<Double> timeValleyList = new ArrayList<>();

        for (int ind = 0; ind < vly.length; ind++) {
            if (vly[ind] < valleyThreshold) {
                valleyList.add(vly[ind]);
                timeValleyList.add(timeVly[ind]);
            }
        }

        // Remove double-valleys (=false valleys)
        // calculate the time differences
        double delta;
        for (int i = 0; i < timeValleyList.size() - 1; i++) {
            delta = timeValleyList.get(i + 1) - timeValleyList.get(i);
            // remove the valleys that are to close to each other (=false valleys)
            if (delta < 0.5) {
                // replace two (closely-)neighbouring valleys with the mean (in time and the value of the time)

                valleyList.set(i, (valleyList.get(i + 1) + valleyList.get(i)) / 2);
                valleyList.remove(i + 1);

                timeValleyList.set(i, (timeValleyList.get(i + 1) + timeValleyList.get(i)) / 2);
                timeValleyList.remove(i + 1);
            }
        }

        // remove excess valleys (depend on which foot began)
        while (timeValleyList.size() > timeList.size()) {
            timeValleyList.remove(timeValleyList.size() - 1);
        }

        // Calculate GCT ( = time_valley-time_peaks)
        double[] GCT = new double[timeValleyList.size()];
        double meanGCT = 0;

        for (int i = 0; i < timeValleyList.size(); i++) {
            GCT[i] = Math.abs(timeValleyList.get(i) - timeList.get(i));
            meanGCT += GCT[i];
        }

        // Calculate average GCT and fix unit
        meanGCT = meanGCT / (GCT.length); // [s]

        System.out.println("The average ground contact time is:");
        System.out.print(meanGCT);
        System.out.print(" s");
        return new double[]{cadence, meanStrideLength, meanGCT};
    }

    private static Double[] findPeak(final Double[] data) {
        ArrayList<Double> peaksList = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i - 1 >= 0 && data[i] < data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] < data[i + 1]) { // check right
                continue; // no peak
            }

            // if peak is found, save it
            peaksList.add(data[i]);
        }

        // make array
        Double[] peaks = new Double[peaksList.size()];
        peaks = peaksList.toArray(peaks);
        return peaks;
    }

    private static Double[] findTimeOfPeak(final Double[] data, final double[] dataTFiltered) {
        ArrayList<Double> timeList = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i - 1 >= 0 && data[i] < data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] < data[i + 1]) { // check right
                continue; // no peak
            }


            // if peak is found, save it
            timeList.add(dataTFiltered[i]);
        }

        // make array
        Double[] timeOfPeaks = new Double[timeList.size()];
        timeOfPeaks = timeList.toArray(timeOfPeaks);
        return timeOfPeaks;
    }

    private static Double[] findValley(final Double[] data) {
        ArrayList<Double> valleyList = new ArrayList<>();

        for (int i = 1; i < data.length - 1; i++) {
            // if data[i] is greater than its surrounding values it is a peak
            if (data[i] > data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] > data[i + 1]) { // check right
                continue; // no peak
            }

            // if peak is found, save it
            valleyList.add(data[i]);
        }

        // make array
        Double[] valley = new Double[valleyList.size()];
        valley = valleyList.toArray(valley);
        return valley;
    }

    private static Double[] findTimeOfValley(final Double[] data, final double[] time) {
        ArrayList<Double> timeList = new ArrayList<>();

        for (int i = 1; i < data.length - 1; i++) {
            // if data[i] is greater than its surrounding values it is a peak
            if (data[i] > data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] > data[i + 1]) { // check right
                continue; // no peak
            }

            // if peak is found, save it
            timeList.add(time[i]);
        }

        // make array
        Double[] timeOfValley = new Double[timeList.size()];
        timeOfValley = timeList.toArray(timeOfValley);
        return timeOfValley;
    }

    /*
    data: input data, must be spaced equally in time.
    cf: The cutoff frequency at which
    t: The time of the input data.
    */
    private static double[] lowPassFilter(final double[] data, final double fc, final double[] dataT) {
        double fs = 1 / (dataT[1] - dataT[0]);
        int l = data.length;

        int pow = 1;
        while (pow < data.length) {
            pow = 2 * pow;
        }

        // zeropadd
        double[] paddedData = new double[pow];
        int L = paddedData.length;
        double[] f = new double[L];
        System.arraycopy(data, 0, paddedData, 0, data.length);

        // FFT
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] D = transformer.transform(paddedData, TransformType.FORWARD);

        // dataShift=[D(round(length(D)/2)+1:end); D(1:length(D)/2)];
        Complex[] dataShift = new Complex[L];
        // Double[] H = new Double[L];
        Complex[] filterData = new Complex[L];

        Complex[] complexData = new Complex[L];

        int j1 = 0;
        int j2 = (L / 2);
        int j3 = (L / 2);
        int j4 = 0;
        Complex z = new Complex(0.0, 0.0); // = 0
        for (int i = 0; i < L; i++) {
            f[i] = (i + 1) * fs / L;
            // fftshift
            if (i < L / 2) {
                dataShift[i] = D[j2];
                j2++;
            }
            if (i >= L / 2) {
                dataShift[i] = D[j1];
                j1++;

            }

            // create a filter (tophat)
            if (i > L / 2 && i < ((L / 2D) + fc * L / (fs * 2))) {
                //H[i]=1.00000000;
                // mult with filter
                //temp= dataShift[i];
                filterData[i] = dataShift[i]; //temp.multiply(H[i]);
            } else {
                //H[i]=0.0000000;
                filterData[i] = z;
            }
        }

        for (int i = 0; i < L; i++) {
            // mult with filter
            // temp= dataShift[i];
            // filterData[i]=temp.multiply(H[i]);

            // ifft-shift
            if (i < L / 2) {
                complexData[i] = filterData[j3];
                j3++;
            }
            if (i >= L / 2) {
                complexData[i] = filterData[j4];
                j4++;
            }
        }


        // ifft
        // ifft=  transformer.transform(DATA, TransformType.INVERSE);

        // return filtered data in time domain

        //invert back to time domain
        Complex[] reverseFourier = transformer.transform(complexData, TransformType.INVERSE);

        // get real
        double[] result = new double[l];
        for (int i = 0; i < result.length; i++) {
            result[i] = reverseFourier[i].getReal();
        }
        return result;
    }
}




