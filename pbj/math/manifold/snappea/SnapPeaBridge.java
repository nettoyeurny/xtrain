package pbj.math.manifold.snappea;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import pbj.math.graph.train.MappingTorus;
import pbj.math.graph.train.TrainTrack;
import pbj.math.manifold.ThreeComplex;

public class SnapPeaBridge {

	private static File infoScript, isomScript;

	private String triangulation = null, info = null;
	private double volume = 0;


	public SnapPeaBridge(TrainTrack g) {
		if (g==null) {
			throw new IllegalArgumentException("train track is null");
		}
		ThreeComplex cx = new ThreeComplex();
		new MappingTorus(g).triangulate(cx);
		StringWriter sw = new StringWriter();
		cx.toSnapPea(sw);
		triangulation = sw.toString();
		try {
			triangulation = getInfo("-t");
		} catch(Exception e) {
			// SnapPeaPython not available
		}
	}

	public String getTriangulation() {
		return triangulation;
	}
	
	public double getVolume() throws IOException {
		if (volume==0) {
			StringTokenizer st = new StringTokenizer(getInfo(), ": \n");
			while (!st.nextToken().equals("volume")); // do nothing
			volume = Double.valueOf(st.nextToken());
		}
		return volume;
	}

	public String getInfo() throws IOException {
		if (info==null) {
			info = getInfo(null);
		}
		return info;
	}

	private String getInfo(String flags) throws IOException {
		if (infoScript==null || !infoScript.exists()) {
			infoScript = copyToTmpFile("snappea_info.py");
		}
		return runScript(infoScript, flags);
	}

	public boolean compareToCensus(String name) throws IOException {
		return compare("-c "+name);
	}

	public boolean compareToFile(String name) throws IOException {
		return compare(name);
	}

	private boolean compare(String name) throws IOException {
		if (isomScript==null || !isomScript.exists()) {
			isomScript = copyToTmpFile("snappea_isom.py");
		}
		String result = runScript(isomScript, name);
		return result.indexOf(" not ")<0;
	}

	private String runScript(File f, String args) throws IOException {
		if (f==null) {
			throw new IOException("SnapPea not available");
		}
		Process p = null;
		StringBuilder sb = new StringBuilder();
		try {
			p = Runtime.getRuntime().exec("python "+f.getCanonicalPath()+((args!=null) ? " "+args : ""));
			OutputStreamWriter osr = new OutputStreamWriter(p.getOutputStream());
			osr.write(triangulation);
			osr.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s;
			while ((s = br.readLine())!=null) {
				sb.append(s+'\n');
			}
			p.waitFor();
		} catch(Exception e) {
			throw new IOException(e.getMessage());
		}
		if (p.exitValue()!=0) {
			throw new IOException("python exit value: "+p.exitValue());
		}
		return sb.toString();
	}

	private static File copyToTmpFile(String fn) {
		File file;
		try {
			file = File.createTempFile("xtrain_snappea", ".py");
			file.deleteOnExit();
			InputStream is = SnapPeaBridge.class.getResourceAsStream(fn);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String s;
			while ((s = br.readLine())!=null) {
				bw.write(s+'\n');
			}
			bw.close();
		} catch (IOException e) {
			return null;
		}
		return file;
	}
}
