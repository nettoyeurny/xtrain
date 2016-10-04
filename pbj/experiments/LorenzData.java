package pbj.experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import pbj.math.graph.LorenzKnot;
import pbj.math.graph.train.TrainTrack;
import pbj.math.manifold.snappea.SnapPeaBridge;
import pbj.math.numerical.IntMatrix;

public class LorenzData {

	private static enum Match {yes, no, unknown};

	private String name, censusEntry;
	private TrainTrack tt;
	private int[] r, s;
	private int braidIndex, genus;
	private double growthRate = 0, volume = 0;
	private Match match = Match.unknown;


	public static LorenzData readKnot(String line) {
		LorenzData lk = new LorenzData();
		StringTokenizer st = new StringTokenizer(line, ", ");
		lk.name = st.nextToken();
		String v = st.nextToken();
		lk.braidIndex = Integer.valueOf(st.nextToken());
		lk.censusEntry = st.nextToken();
		st = new StringTokenizer(v, ".");
		lk.r = new int[st.countTokens()];
		lk.s = new int[st.countTokens()];
		for(int i=0; st.hasMoreTokens(); i++) {
			StringTokenizer f = new StringTokenizer(st.nextToken(), "^");
			lk.r[i] = Integer.valueOf(f.nextToken());
			lk.s[i] = f.hasMoreTokens() ? Integer.valueOf(f.nextToken()) : 1;
		}
		return lk;
	}

	public static List<LorenzData> readFile(File f) throws IOException {
		List<LorenzData> list = new ArrayList<LorenzData>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String s;
		while ((s=br.readLine())!=null) {
			list.add(readKnot(s));
		}
		return list;
	}

	public void analyze() throws IOException {
		tt = new TrainTrack(new LorenzKnot(r, s).getMonodromy());
		tt.setLabel(name);
		tt.unmark();
		genus = tt.getRank()/2;
		tt.trainTrackMap();
		growthRate = tt.growthRate();
	}

	private void verifyWithSnapPea() throws IOException {
		SnapPeaBridge spb = new SnapPeaBridge(tt);
		volume = spb.getVolume();
		match = spb.compareToCensus(censusEntry) ? Match.yes : Match.no;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("name ");
		sb.append(name);
		sb.append(", vector ");
		for(int i=0; i<r.length; i++) {
			sb.append(r[i]);
			sb.append("^");
			sb.append(s[i]);
			if (i<r.length-1) {
				sb.append("_");
			}
		}
		sb.append(", bi ");
		sb.append(braidIndex);
		sb.append(", genus ");
		sb.append(genus);
		if (growthRate>0) {
			sb.append(", lambda ");
			sb.append(IntMatrix.PFForm.format(growthRate));
		}
		if (volume>0) {
			sb.append(", volume ");
			sb.append(volume);
			if (match==Match.yes) {
				sb.append(", match ");
			} else {
				sb.append(", nomatch ");
			}
			sb.append(censusEntry);
		}
		return sb.toString();
	}

	private static synchronized void syncPrint(final LorenzData ld) {
		System.out.println(ld);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		List<LorenzData> list = readFile(new File("pbj/experiments/knots.txt"));
		for(LorenzData lk: list) {
			final LorenzData ld = lk;
			new Thread() {
				@Override
				public void run() {
					try {
						ld.analyze();
//						ld.verifyWithSnapPea();
						syncPrint(ld);
					} catch (Exception e) {
						System.err.println("exception: "+e.getMessage());
					}
				}
			}.start();
		}
	}
}
