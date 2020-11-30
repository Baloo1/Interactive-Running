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

    public static void calculateData(double[] dataX, double[] dataY, double[] dataZ, double[] dataSensorT) {
        // --------- load recorded data ---------
        // Time data
        // Recalculate sensor timestamp
        double[] dataT = new double[dataSensorT.length];
        double t0 = dataSensorT[0];
        for (int i = 0; i < dataSensorT.length; i++) {
            dataT[i] = (dataSensorT[i] - t0) * Math.pow(10, -9);
        }

        // --------- Filter the data ---------
        double[] dataXFiltered = lowPassFilter(dataX, 10, dataT);
        double[] dataYFiltered = lowPassFilter(dataY, 10, dataT);
        double[] dataZFiltered = lowPassFilter(dataZ, 10, dataT);

        // ---------  Get the vector magnitude ---------
        // --------- Peaks ---------
        ArrayList<Double> peaksTimeList = new ArrayList<>();
        double peakThreshold = 7;
        int numPeaks = 0;

        // --------- Valleys ---------
        ArrayList<Double> valleyList = new ArrayList<>();
        ArrayList<Double> valleyTimeList = new ArrayList<>();
        double valleyThreshold = 1.2;

        // calculate Normalized Acceleration, find peaks, find valleys
        double earlierAccNormalised = normalizeAcceleration(dataXFiltered[0], dataYFiltered[0], dataZFiltered[0]);
        double nextAccNormalised = earlierAccNormalised;
        for (int i = 0; i < dataT.length; i++) {
            double currentAccNormalised = nextAccNormalised;
            if (i < dataT.length - 1) {
                nextAccNormalised = normalizeAcceleration(dataXFiltered[i + 1], dataYFiltered[i + 1], dataZFiltered[i + 1]);
            }

            // --------- Cadence ---------
            // Get all peaks and time for every peak
            // if currentAccNormalised is greater than its surrounding values it is a peak
            if (currentAccNormalised > earlierAccNormalised && currentAccNormalised > nextAccNormalised) { //check for peaks

                // Remove too low peaks
                if (currentAccNormalised > peakThreshold) {
                    peaksTimeList.add(dataT[i]);
                    numPeaks++;
                }
            }

            // Get all valleys and time for every valley
            // if currentAccNormalised is less than its surrounding values it is a valley
            if (currentAccNormalised < earlierAccNormalised && currentAccNormalised < nextAccNormalised) {
                // Remove the low valleys
                if (currentAccNormalised < valleyThreshold) {
                    valleyList.add(currentAccNormalised);
                    valleyTimeList.add(dataT[i]);
                }
            }

            earlierAccNormalised = currentAccNormalised;
        }

        // Make peak array of list
        double[] peakTime = new double[peaksTimeList.size()];
        for (int i = 0; i < peaksTimeList.size(); i++) {
            peakTime[i] = peaksTimeList.get(i);
        }

        System.out.println("The number of detected steps are: " + numPeaks);

        // recalculate the unit of the cadence
        double timeElapsed = peakTime[numPeaks - 1] - peakTime[0]; // time between first and last
        double cadence = Math.round(numPeaks * 60 / timeElapsed); // [steps/min]

        // print result
        System.out.println("The cadence is:" + cadence + " steps/min");


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
        double meanStrideLength = accStrideLength / (numPeaks - 1);
        System.out.println("The mean stride length is:" + meanStrideLength * 100 + "cm");

        // --------- Ground contact time (GCT) ---------

        // Remove double-valleys (=false valleys)
        // calculate the time differences
        double delta;
        for (int i = 0; i < valleyTimeList.size() - 1; i++) {
            delta = valleyTimeList.get(i + 1) - valleyTimeList.get(i);
            // remove the valleys that are to close to each other (=false valleys)
            if (delta < 0.1) {
                // replace two (closely-)neighbouring valleys with the mean (in time and the value of the time)

                valleyList.set(i, (valleyList.get(i + 1) + valleyList.get(i)) / 2);
                valleyList.remove(i + 1);

                valleyTimeList.set(i, (valleyTimeList.get(i + 1) + valleyTimeList.get(i)) / 2);
                valleyTimeList.remove(i + 1);
            }
        }


        while (valleyTimeList.size() > peaksTimeList.size()) {
            valleyTimeList.remove(valleyTimeList.size() - 1);
        }

        // Calculate GCT ( = time_valley-time_peaks)
        double[] GCT = new double[valleyTimeList.size()];
        double GCT_mean = 0;

        for (int i = 0; i < valleyTimeList.size(); i++) {
            GCT[i] = valleyTimeList.get(i) - peaksTimeList.get(i);
            GCT_mean += GCT[i];
        }

        // Calculate average GCT and fix unit
        GCT_mean = GCT_mean / (GCT.length); // [s]

        System.out.println("The average ground contact time is:" + (-GCT_mean) + " s");
    }

    public static double normalizeAcceleration(double x, double y, double z) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
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

//		printToFileComp("dataY_shift.txt", dataShift);
//		printToFileComp("dataY_filt.txt", filterData);

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
        // printToFile("dataY_fft.txt", result);
        return result;
    }

    public static double[] lowPass(double[] input, double[] output) {
        if (output == null) return input;
        double ALPHA = 0.35; // smaller value, more smoothing

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}




