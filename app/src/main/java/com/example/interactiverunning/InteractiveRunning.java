package com.example.interactiverunning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;


public class InteractiveRunning {

    // TODO: 10 seconds
    // 700 values in array, aprox 10 steps
    //

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // --------- variables ---------
        String filenameDataX="dataX.txt";

        ArrayList<Double> dataX_list = new ArrayList<Double>();
        String filenameDataY="dataY.txt";
        Double[] dataXFilt;

        ArrayList<Double> dataY_list = new ArrayList<Double>();
        String filenameDataZ="dataZ.txt";
        Double[] dataYFilt;

        ArrayList<Double> dataZ_list = new ArrayList<Double>();
        String filenameDataT="timestamp.txt";
        Double[] dataZFilt;
        ArrayList<Double> dataT_list = new ArrayList<Double>();
        Double[] datatFilt;

        ArrayList<Double> dataTemp_list = new ArrayList<Double>();

        // ------------------ code ------------------

        // --------- load recorded data ---------
        // time-data
        double tCurr;
        double t0=0;
        File fileT = new File(filenameDataT);
        Scanner scanT = new Scanner(fileT);
        int lineNum = 1;
        while (scanT.hasNextLine()) {
            tCurr=Double.parseDouble((String) scanT.nextLine());
            if (lineNum==1) {
                t0=tCurr;
            }
            dataT_list.add((tCurr-t0)*Math.pow(10, -9));
            lineNum++;
        }
        // x-acceleration
        File fileX = new File(filenameDataX);
        Scanner scanX = new Scanner(fileX);
        while (scanX.hasNextLine()) {
            dataX_list.add(Double.parseDouble((String) scanX.nextLine()));
        }

        // y-acceleration
        File fileY = new File(filenameDataY);
        Scanner scanY = new Scanner(fileY);

        while (scanY.hasNextLine()) {
            dataY_list.add(Double.parseDouble((String) scanY.nextLine()));
        }

        // z-acceleration
        File fileZ = new File(filenameDataZ);
        Scanner scanZ = new Scanner(fileZ);
        while (scanZ.hasNextLine()) {
            dataZ_list.add(Double.parseDouble((String) scanZ.nextLine()));
        }

        // turn lists to arrays
        Double[] dataX = new Double[dataX_list.size()];
        dataX = dataX_list.toArray(dataX);
        Double[] dataY = new Double[dataY_list.size()];
        dataY = dataY_list.toArray(dataY);
        Double[] dataZ = new Double[dataZ_list.size()];
        dataZ = dataZ_list.toArray(dataZ);
        Double[] dataT = new Double[dataT_list.size()];
        dataT = dataT_list.toArray(dataT);

        // --------- Filter the data ---------
        // TODO ????
        // TEMP
        // calculate sampling frequency
        double fs=1/(dataT[1]-dataT[1]);

        //dataXFilt=LowPassFilter(dataX,4,fs);
        dataXFilt=dataX;
        dataYFilt=dataY;
        dataZFilt=dataZ;
        datatFilt=dataT;
        // END OF TEMP


        // ---------  Get the vector magnitude ---------
        ArrayList<Double> accNorm_list = new ArrayList<Double>();
        for (int j=0;j<datatFilt.length;j++) {
            Double a=Math.pow(dataXFilt[j],2) + Math.pow(dataYFilt[j],2) +Math.pow(dataZFilt[j],2);
            accNorm_list.add(Math.sqrt(a));
        }

        Double[] accNorm = new Double[accNorm_list.size()];
        accNorm = accNorm_list.toArray(accNorm);



        // --------- Cadence ---------
        // Get all peaks and time for every peak
        Double[] pks = findPeak(accNorm);
        Double[] t = findTimeOfPeak(accNorm,datatFilt);



        // Remove to low peaks
        double threshold = 18;
        ArrayList<Double> peaksList = new ArrayList<Double>();
        ArrayList<Double> timeList = new ArrayList<Double>();


        for (int ind=0; ind < pks.length; ind++) {
            if (pks[ind]>threshold) {
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
        int numPeaks=peaks.length;
        //System.out.println(numPeaks);
        // recalculate the unit of the cadence
        double timeElapsed = peakTime[numPeaks-1]-peakTime[0]; // time between first and last
        double cadence = Math.round(numPeaks*60/timeElapsed); // [steps/min]

        // print result
        System.out.println("The cadence is:");
        System.out.print(cadence);
        System.out.print(" steps/min");
        System.out.println(" ");

        // --------- stride length ---------
        // INPUT TODO
        double speed = 5;
        speed=speed/3.6;
        // Time difference between the peaks
        double[] strideLength = new double[numPeaks-1];
        double accStridelength=0;
        for (int i=0; i<numPeaks-1;i++) {
            // Stridelength (s=v*t)
            strideLength[i]=speed*(peakTime[i+1]-peakTime[i]);
            accStridelength+= strideLength[i];
        }

        // Calculate average stride length
        double meanStridelength = accStridelength/(numPeaks-1);
        System.out.println("The mean stride length is:");
        System.out.print(meanStridelength*100); // fixing the unit to cm
        System.out.print(" cm");
        System.out.println(" ");

        // --------- Ground contact time (GCT) ---------

        // Find the valleys time for every valley
        Double[] vly = findValley(accNorm);
        Double[] timeVly = findTimeOfValley(accNorm, datatFilt);
        // Remove the low valleys
        threshold = 5;
        ArrayList<Double> valleyList = new ArrayList<Double>();
        ArrayList<Double> timeValleyList = new ArrayList<Double>();


        for (int ind=0; ind < vly.length; ind++) {
            if (vly[ind]<threshold) {
                valleyList.add(vly[ind]);
                timeValleyList.add(timeVly[ind]);
            }
        }



        // Remove double-valleys (=false valleys)
        // calculate the time differeances
        double delta=0;
        for (int i=0;i<timeValleyList.size()-1; i++) {
            delta=timeValleyList.get(i+1)-timeValleyList.get(i);
            // remove the valleys that are to close to each other (=false valleys)
            if (delta<0.1){
                // replace two (closely-)neighbouring valleys with the mean (in time and the value of the time)

                valleyList.set(i, (valleyList.get(i+1)+valleyList.get(i))/2);
                valleyList.remove(i+1);

                timeValleyList.set(i,(timeValleyList.get(i+1)+timeValleyList.get(i))/2);
                timeValleyList.remove(i+1);
            }
        }

        // make array
        Double[] valley = new Double[valleyList.size()];
        valley = valleyList.toArray(valley);
        Double[] valleyTime = new Double[timeValleyList.size()];
        valleyTime = timeValleyList.toArray(valleyTime);

        // remove excess valleys (depend on which foot began)
        if (timeValleyList.size()>timeList.size()) {
            timeValleyList.remove(timeValleyList.size()-1);
        }

        // Calculate GCT ( = time_valley-time_peaks)
        double[] GCT= new double[timeValleyList.size()];
        double GCT_mean=0;

        for (int i=0;i<timeValleyList.size();i++) {
            GCT[i] = timeValleyList.get(i)-timeList.get(i);
            GCT_mean+= GCT[i];
        }


        // Calculate average GCT and fix unit
        GCT_mean=GCT_mean*1000/(GCT.length); // [ms]

        System.out.println("The average ground contact time is:");
        System.out.print(-GCT_mean);
        System.out.print(" ms");

    }

    static Double[] findPeak(Double data[])  {
        ArrayList<Double> peaks_list = new ArrayList<Double>();

        for (int i=0; i<data.length;i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i-1 >= 0 && data[i] < data[i-1]) { //check left
                continue; // no peak
            }
            if (i+1 <= data.length-1  && data[i]<data[i+1]) { // check right
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

    static Double[] findTimeOfPeak(Double data[], Double time[])  {
        ArrayList<Double> time_list = new ArrayList<Double>();

        for (int i=0; i<data.length;i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i-1 >= 0 && data[i] < data[i-1]) { //check left
                continue; // no peak
            }
            if (i+1 <= data.length-1  && data[i]<data[i+1]) { // check right
                continue; // no peak
            }


            // if peak is found, save it
            time_list.add(time[i]);
        }

        // make array
        Double[] timeOfPeaks = new Double[time_list.size()];
        timeOfPeaks = time_list.toArray(timeOfPeaks);
        return timeOfPeaks;
    }

    static Double[] findValley(Double data[])  {
        ArrayList<Double> valley_list = new ArrayList<Double>();

        for (int i=1; i<data.length-1;i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i-1 >= 0 && data[i] > data[i-1]) { //check left
                continue; // no peak
            }
            if (i+1 <= data.length-1  && data[i] > data[i+1]) { // check right
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



    static Double[] findTimeOfValley(Double data[], Double time[])  {
        ArrayList<Double> time_list = new ArrayList<Double>();

        for (int i=1; i<data.length-1;i++) {
            // if data[i] is greater than its surrounding values it is a peak

            if (i-1 >= 0 && data[i] > data[i-1]) { //check left
                continue; // no peak
            }
            if (i+1 <= data.length-1  && data[i] > data[i+1]) { // check right
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

    public static double[] LowPassFilter(Double[] dataX, double lowPass, double frequency){
        //data: input data, must be spaced equally in time.
        //lowPass: The cutoff frequency at which
        //frequency: The frequency of the input data.

        //The apache Fft (Fast Fourier Transform) accepts arrays that are powers of 2.
        int minPowerOf2 = 1;
        while(minPowerOf2 < dataX.length)
            minPowerOf2 = 2 * minPowerOf2;

        //pad with zeros
        double[] padded = new double[minPowerOf2];
        for(int i = 0; i < dataX.length; i++)
            padded[i] = dataX[i];


        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fourierTransform = transformer.transform(padded, TransformType.FORWARD);

        //build the frequency domain array
        double[] frequencyDomain = new double[fourierTransform.length];
        for(int i = 0; i < frequencyDomain.length; i++)
            frequencyDomain[i] = frequency * i / (double)fourierTransform.length;

        //build the classifier array, 2s are kept and 0s do not pass the filter
        double[] keepPoints = new double[frequencyDomain.length];
        keepPoints[0] = 1;
        for(int i = 1; i < frequencyDomain.length; i++){
            if(frequencyDomain[i] < lowPass)
                keepPoints[i] = 2;
            else
                keepPoints[i] = 0;
        }

        //filter the fft
        for(int i = 0; i < fourierTransform.length; i++)
            fourierTransform[i] = fourierTransform[i].multiply((double)keepPoints[i]);

        //invert back to time domain
        Complex[] reverseFourier = transformer.transform(fourierTransform, TransformType.INVERSE);

        //get the real part of the reverse
        double[] result = new double[dataX.length];
        for(int i = 0; i< result.length; i++){
            result[i] = reverseFourier[i].getReal();
        }

        return result;
    }

}