package main;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.jtransforms.fft.DoubleFFT_1D;

import common.Constants;
import queues.SoundFile;

public class Main {
	private static double[] window = null;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);		
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] { "*.wav" });
		fileDialog.setText("Open WAV file");
		fileDialog.setFilterPath("C:/");

		String wavFilePath = fileDialog.open();
		if (wavFilePath != null && !wavFilePath.isEmpty()) {
			System.out.println("Loading external WAV file '" + wavFilePath + "'.");
			openWav(wavFilePath);
		}
	}
	
	public static void openWav(String wavFilePath) {
		/* Generate a temporary wav file name */
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String datetime = dateFormat.format(Calendar.getInstance().getTime()) + "_" + Calendar.getInstance().get(Calendar.MILLISECOND);
		SoundFile newWavFile = new SoundFile(System.getProperty("user.dir") + File.separator + Constants.tmpOutputDirectory + File.separator + datetime + ".wav");

		try {
			System.out.println("Save new file '" + newWavFile.getFileAbsolutePath() + "'");
			FileUtils.copyFile(new File(wavFilePath), new File(newWavFile.getFileAbsolutePath()));

			Path path = Paths.get(newWavFile.getFileAbsolutePath());
			byte[] bytes = Files.readAllBytes(path);

			System.out.println("Bytes buffer length: " + bytes.length);
			short[] frequencies = new short[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				frequencies[i] = (short) bytes[i];
			}

			double frequency = extractFrequency(frequencies, 44100);
			System.out.println("frequency : " + frequency);

			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			newWavFile.delete();			
		}
	}
	
	/**
	 * build a Hamming window filter for samples of a given size See
	 * http://www.labbookpages.co.uk/audio/firWindowing.html#windows
	 * 
	 * @param size the sample size for which the filter will be created
	 */
	private static void buildHammWindow(int size) {
		if (window != null && window.length == size) {
			return;
		}
		window = new double[size];
		for (int i = 0; i < size; ++i) {
			window[i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0));
		}
	}

	/**
	 * apply a Hamming window filter to raw input data
	 * 
	 * @param input an array containing unfiltered input data
	 * @return a double array containing the filtered data
	 */
	private static double[] applyWindow(short[] input) {
		double[] res = new double[input.length];

		buildHammWindow(input.length);
		for (int i = 0; i < input.length; ++i) {
			res[i] = (double) input[i] * window[i];
		}
		return res;
	}

	/**
	 * extract the dominant frequency from 16bit PCM data.
	 * 
	 * @param sampleData an array containing the raw 16bit PCM data.
	 * @param sampleRate the sample rate (in HZ) of sampleData
	 * @return an approximation of the dominant frequency in sampleData
	 */
	public static double extractFrequency(short[] sampleData, int sampleRate) {
		/* sampleData + zero padding */
		DoubleFFT_1D fft = new DoubleFFT_1D(sampleData.length + 24 * sampleData.length);
		double[] a = new double[(sampleData.length + 24 * sampleData.length) * 2];

		System.arraycopy(applyWindow(sampleData), 0, a, 0, sampleData.length);
		fft.realForward(a);

		/* find the peak magnitude and it's index */
		double maxMag = Double.NEGATIVE_INFINITY;
		int maxInd = -1;

		for (int i = 0; i < a.length / 2; ++i) {
			double re = a[2 * i];
			double im = a[2 * i + 1];
			double mag = Math.sqrt(re * re + im * im);

			if (mag > maxMag) {
				maxMag = mag;
				maxInd = i;
			}
		}

		/* calculate the frequency */
		return (double) sampleRate * maxInd / (a.length / 2);
	}

}
