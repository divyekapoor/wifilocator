package in.iitr.ernet.divyeuec.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import android.os.Environment;
import android.util.Log;

public class FileDB implements IFingerprintDB {
	private static final String TAG = "FileDB";
	private static final String SAMPLES_DIR = Environment.getExternalStorageDirectory()
				+ File.separator + "samples";

	private static final FileDB mInstance = new FileDB();
	private static final int KNN_CONSTANT = 7;
	
	private FileDB() {
		
	}
	
	public static FileDB getInstance() {
		return mInstance;
	}
	
	public boolean persist(LocationFingerprint locationFingerprint) {
		File sampleFile = generateSampleFile(locationFingerprint);
		return writeSampleToFile(sampleFile, locationFingerprint);
	}

	@Override
	public LocationFingerprint query(int id) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method not implemented");
		// return null;
	}

	private class KNNFile {
		public File f;
		public float distance;
		
		public KNNFile(File f, float distance) {
			super();
			this.f = f;
			this.distance = distance;
		}
	}
	
	
	// Locate fingerprints around a particular region
	@Override
	public List<LocationFingerprint> query(Float X, Float Y, Float angle) {
		File samplesDir = new File(SAMPLES_DIR);
		ArrayList<KNNFile> knnFiles = new ArrayList<KNNFile>();
		
		File[] jsonFiles = listSampleFiles(samplesDir);
		
		for(File sample : jsonFiles) {
			String[] parts = sample.getName().split("/\\./");
			Log.d(TAG, parts[0]);
			Log.d(TAG, parts[1]);
			Log.d(TAG, parts[2]);
			Log.d(TAG, parts[3]);
			Log.d(TAG, parts[4]);
			Log.d(TAG, parts[5]);
			
			float x = Float.parseFloat(parts[1])/1000000.f;
			float y = Float.parseFloat(parts[2])/1000000.f;
			float a = Float.parseFloat(parts[3]);
			knnFiles.add(new KNNFile(sample, distance(X, Y, angle, x, y, a)));
		}
		
		// Compare on distances from Dead Reckoning position
		Collections.sort(knnFiles, new Comparator<KNNFile>() {
			@Override
			public int compare(KNNFile file1, KNNFile file2) {
				return Float.compare(file1.distance, file2.distance);
			}
		});
		
		// Get rid of extra entries
		knnFiles.subList(Math.min(KNN_CONSTANT, knnFiles.size()), knnFiles.size()).clear();
		
		for(KNNFile k : knnFiles) {
			loadLocationFingerprint(k);
		}
		
		return null;
	}

	private LocationFingerprint loadLocationFingerprint(KNNFile k) {
		return new LocationFingerprint(k.f);
	}

	private File[] listSampleFiles(File samplesDir) {
		FileFilter sampleFiles = sampleFileFilter();
		
		Log.i(TAG, "Trying to list sample files");
		File[] jsonFiles = samplesDir.listFiles(sampleFiles);
		Log.i(TAG, "Sample files listed");
		return jsonFiles;
	}

	private FileFilter sampleFileFilter() {
		FileFilter nearbyFiles = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(!pathname.isFile()) return false;
				
				String[] parts = pathname.getName().split("\\.");
				if(!parts[parts.length-1].equalsIgnoreCase("json")) 
					return false;
				
				return true;
			}
			
		};
		return nearbyFiles;
	}

	private float distance(Float x, Float y, Float angle, float x2, float y2,
			float a) {
		return (float) Math.sqrt((x2-x)*(x2-x) + (y2-y)*(y2-y));
	}

	private File generateSampleFile(LocationFingerprint fingerprint) {
		Date sampleTime = fingerprint.getmSampleTime();
		File sampleFile = new File(SAMPLES_DIR, "sample_" + fingerprint.getmPrefix() + "."
				+ Math.round(fingerprint.getmX() * 1000000) + "." + Math.round(fingerprint.getmY() * 1000000) + "."
				+ Math.round(fingerprint.getmAngle()) + "." + sampleTime.getDate() + "-" + sampleTime.getMonth()
				+ "-" + (sampleTime.getYear() + 1900) + "-" + sampleTime.getHours() + "-"
				+ sampleTime.getMinutes() + "-" + sampleTime.getSeconds() + ".json");
		return sampleFile;
	}

	private boolean writeSampleToFile(File sampleFile, LocationFingerprint locationFingerprint) {
		try {
			if(!sampleFile.createNewFile())
				throw new RuntimeException("File Creation failed on disk. Another file may be present?");
			
			// Open the sample file for writing and write the serialized result to file
			PrintWriter pw = new PrintWriter(new FileOutputStream(sampleFile));
			pw.print(locationFingerprint.getmFingerprintData().toString());
			pw.flush();
			pw.close();
			
		} catch (IOException e) {
			Log.e(TAG, "File Writing failed", e);
			throw new RuntimeException("Sample File Creation failed on disk. Another file may be present?");
		}
		return true;
	}

	@Override
	public List<LocationFingerprint> getAllSamples() {
		Log.d(TAG, "getAllSamples called");
		File sampleDir = new File(SAMPLES_DIR);
		File[] fingerprintFilesJSON = sampleDir.listFiles(sampleFileFilter());
		Log.d(TAG, "total files: " + fingerprintFilesJSON.length);
		
		ArrayList<LocationFingerprint> allSamples = new ArrayList<LocationFingerprint>();
		for(File f : fingerprintFilesJSON) {
			Log.d(TAG, "Opening file " + f.getName());
			allSamples.add(new LocationFingerprint(f));
		}
		return allSamples;
	}
}
