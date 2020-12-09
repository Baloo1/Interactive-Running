package com.example.interactiverunning;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;


public class InteractiveRunning {

    // TODO: 10 seconds
    // 700 values in array, approx 10 steps
    // Frequency for peaks from user input
    // Running speed from user input

    public static double[] calculateData(double[] dataX, double[] dataY, double[] dataZ, double[] dataSensorT) {
        // Time data
        double t0 = dataSensorT[0];
        double[] dataT = new double[dataSensorT.length];
        for (int i = 0; i < dataSensorT.length; i++) {
            dataT[i] = ((dataSensorT[i] - t0) * Math.pow(10, -9));
        }

        // --------- Filter the data ---------
        double[] dataXFiltered = lowPassFilter(dataX, 10, dataT);
        double[] dataYFiltered = lowPassFilter(dataY, 10, dataT);
        double[] dataZFiltered = lowPassFilter(dataZ, 10, dataT);

        // ---------  Get the vector magnitude ---------
        ArrayList<Double> accNorm_list = new ArrayList<>();
        for (int j = 0; j < dataT.length; j++) {
            double a = Math.pow(dataXFiltered[j], 2) + Math.pow(dataYFiltered[j], 2) + Math.pow(dataZFiltered[j], 2);
            accNorm_list.add(Math.sqrt(a));
        }

        Double[] accNorm = new Double[accNorm_list.size()];
        accNorm = accNorm_list.toArray(accNorm);

        // --------- Cadence ---------
        // Get all peaks and time for every peak
        Double[] pks = findPeak(accNorm);
        Double[] t = findTimeOfPeak(accNorm, dataT);

        // Remove to low peaks
        double threshold = 7;
        ArrayList<Double> peaksList = new ArrayList<>();
        ArrayList<Double> timeList = new ArrayList<>();

        for (int ind = 0; ind < pks.length; ind++) {
            if (pks[ind] > threshold) {
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
        double timeElapsed = peakTime[numPeaks - 1] - peakTime[0]; // time between first and last
        double cadence = Math.round(numPeaks * 60 / timeElapsed); // [steps/min]

        // print result
        System.out.println("The cadence is:");
        System.out.print(cadence);
        System.out.print(" steps/min");
        System.out.println(" ");

        // --------- stride length ---------
        // INPUT TODO
        double speed = 5;
        speed = speed / 3.6;
        // Time difference between the peaks
        double[] strideLength = new double[numPeaks - 1];
        double accStrideLength = 0;
        for (int i = 0; i < numPeaks - 1; i++) {
            // Stride length (s=v*t)
            strideLength[i] = speed * (peakTime[i + 1] - peakTime[i]);
            accStrideLength += strideLength[i];
        }

        // Calculate average stride length
        double meanStridelength = accStrideLength / (numPeaks - 1);
        System.out.println("The mean stride length is:");
        System.out.print(meanStridelength * 100); // fixing the unit to cm
        System.out.print(" cm");
        System.out.println(" ");

        // --------- Ground contact time (GCT) ---------
        // Find the valleys time for every valley
        Double[] vly = findValley(accNorm);
        Double[] timeVly = findTimeOfValley(accNorm, dataT);
        // Remove the low valleys
        double threshold2 = 1.2;
        ArrayList<Double> valleyList = new ArrayList<>();
        ArrayList<Double> timeValleyList = new ArrayList<>();

        for (int ind = 0; ind < vly.length; ind++) {
            if (vly[ind] < threshold2) {
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
            if (delta < 0.1) {
                // replace two (closely-)neighbouring valleys with the mean (in time and the value of the time)

                valleyList.set(i, (valleyList.get(i + 1) + valleyList.get(i)) / 2);
                valleyList.remove(i + 1);

                timeValleyList.set(i, (timeValleyList.get(i + 1) + timeValleyList.get(i)) / 2);
                timeValleyList.remove(i + 1);
            }
        }

        // make array
        Double[] valley = new Double[valleyList.size()];
        valley = valleyList.toArray(valley);
        Double[] valleyTime = new Double[timeValleyList.size()];
        valleyTime = timeValleyList.toArray(valleyTime);

        // remove excess valleys (depend on which foot began)
        while (timeValleyList.size() > timeList.size()) {
            timeValleyList.remove(timeValleyList.size() - 1);
        }

        // Calculate GCT ( = time_valley-time_peaks)
        double[] GCT = new double[timeValleyList.size()];
        double GCT_mean = 0;

        for (int i = 0; i < timeValleyList.size(); i++) {
            GCT[i] = timeValleyList.get(i) - timeList.get(i);
            GCT_mean += GCT[i];
        }

        // Calculate average GCT and fix unit
        GCT_mean = GCT_mean / (GCT.length); // [s]

        System.out.println("The average ground contact time is:");
        System.out.print(-GCT_mean);
        System.out.print(" s");
        return new double[]{cadence, meanStridelength, GCT_mean};
    }

    static Double[] findPeak(Double[] data) {
        ArrayList<Double> peaks_list = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i - 1 >= 0 && data[i] < data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] < data[i + 1]) { // check right
                continue; // no peak
            }

            // if peak is found, save it
            peaks_list.add(data[i]);
        }

        // make array
        Double[] peaks = new Double[peaks_list.size()];
        peaks = peaks_list.toArray(peaks);
        return peaks;
    }

    static Double[] findTimeOfPeak(Double[] data, double[] datatFilt) {
        ArrayList<Double> time_list = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i - 1 >= 0 && data[i] < data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] < data[i + 1]) { // check right
                continue; // no peak
            }


            // if peak is found, save it
            time_list.add(datatFilt[i]);
        }

        // make array
        Double[] timeOfPeaks = new Double[time_list.size()];
        timeOfPeaks = time_list.toArray(timeOfPeaks);
        return timeOfPeaks;
    }

    static Double[] findValley(Double[] data) {
        ArrayList<Double> valley_list = new ArrayList<>();

        for (int i = 1; i < data.length - 1; i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i - 1 >= 0 && data[i] > data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] > data[i + 1]) { // check right
                continue; // no peak
            }


            // if peak is found, save it
            valley_list.add(data[i]);
        }

        // make array
        Double[] valley = new Double[valley_list.size()];
        valley = valley_list.toArray(valley);
        return valley;
    }

    static Double[] findTimeOfValley(Double[] data, double[] time) {
        ArrayList<Double> time_list = new ArrayList<>();

        for (int i = 1; i < data.length - 1; i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i - 1 >= 0 && data[i] > data[i - 1]) { //check left
                continue; // no peak
            }
            if (i + 1 <= data.length - 1 && data[i] > data[i + 1]) { // check right
                continue; // no peak
            }

            // if peak is found, save it
            time_list.add(time[i]);
        }

        // make array
        Double[] timeOfValley = new Double[time_list.size()];
        timeOfValley = time_list.toArray(timeOfValley);
        return timeOfValley;
    }

    public static double[] lowPassFilter(double[] data, double fc, double[] dataT) {
        //data: input data, must be spaced equally in time.
        //cf: The cutoff frequency at which
        //t: The time of the input data.

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
        for (int i = 0; i < data.length; i++) {
            paddedData[i] = data[i];
        }

        // FFT
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] D = transformer.transform(paddedData, TransformType.FORWARD);

        //debugg
        double[] a = {paddedData[1], paddedData[2]};
        Complex[] aComp = transformer.transform(a, TransformType.FORWARD);

        // DEBUGG
        //		Complex[] d = transformer.transform(D, TransformType.INVERSE);
        //		double[] dreal = new double[L+1];
        //		for(int i = 0; i< dreal.length; i++){
        //			dreal[i] = d[i].getReal();
        //		}


        // dataShift=[D(round(length(D)/2)+1:end); D(1:length(D)/2)];
        Complex[] dataShift = new Complex[L];
        // Double[] H = new Double[L];
        Complex[] filterData = new Complex[L];

        Complex[] DATA = new Complex[L];
        Complex[] ifft = new Complex[L];

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
            if (i > L / 2 && i < (L / 2 + fc * L / (fs * 2))) {
                //H[i]=1.00000000;
                // mult with filter
                //temp= dataShift[i];
                filterData[i] = dataShift[i];//temp.multiply(H[i]);
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
                DATA[i] = filterData[j3];
                j3++;
            }
            if (i >= L / 2) {
                DATA[i] = filterData[j4];
                j4++;
            }
        }


        // ifft
        // ifft=  transformer.transform(DATA, TransformType.INVERSE);

        // return filtered data in time domain

        //invert back to time domain
        Complex[] reverseFourier = transformer.transform(DATA, TransformType.INVERSE);

        // get real
        double[] result = new double[l];
        for (int i = 0; i < result.length; i++) {
            if (i < l) {
                result[i] = reverseFourier[i].getReal();
            }
        }
        return result;
    }
}




