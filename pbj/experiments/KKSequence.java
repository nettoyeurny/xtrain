package pbj.experiments;

import java.io.IOException;

import pbj.math.graph.GraphMap;
import pbj.math.graph.Word;
import pbj.math.graph.train.TrainTrack;
import pbj.math.manifold.snappea.SnapPeaBridge;
import pbj.math.numerical.IntMatrix;

public class KKSequence {
/*
	x_0 -> x_1
	...
	x_n-1 -> X_0X_n
	x_n -> x_n+1
	...
	x_2n-3 -> X_n-1X_n

	X0 X_nX_1...X_2n-3X_n-2 x_n...x_2n-3 X_n-1 x_0...x_n-1
*/

	public static GraphMap getMap(int n) {
		String fix = ""+Word.indexToChar(0, true);
		for(int i = 0; i<n-2; i++)
			fix += Word.indexToChar(i+n, true)+""+Word.indexToChar(i+1, true);
		for(int i = 0; i<n-2; i++)
			fix += Word.indexToChar(i+n, false);
		fix += Word.indexToChar(n-1, true);
		for(int i = 0; i<n; i++)
			fix += Word.indexToChar(i, false);
		
		GraphMap g = GraphMap.identityFromFixed(fix);
		for(int i = 0; i<2*n-2; i++)
			g.setImage(i, ""+Word.indexToChar(i+1, false));
		g.setImage(n-1, Word.indexToChar(0, true)+""+Word.indexToChar(n, true));
		g.setImage(2*n-3, Word.indexToChar(n-1, true)+""+Word.indexToChar(n, true));
		
		if (!g.isGoodMap())
			throw new IllegalStateException("bad map!");
		
		return g;
	}
	
	public static void main(String[] args) throws IOException {
		for(int i = 100; i<120; i++) {
			TrainTrack tt = new TrainTrack(getMap(i));
			tt.trainTrackMap();
//			SnapPeaBridge spb = new SnapPeaBridge(tt);
//			System.out.println(tt);
			double q = tt.growthRate();
//			System.out.println("dilatation: "+q);
//			System.out.println("char poly: "+IntMatrix.polyString(tt.transitionMatrix().reducedCharPoly()));
			int g = tt.getRank()/2;
//			System.out.println("genus: "+g);
//			double v = spb.getVolume();
//			System.out.println("volume: "+v);
			System.out.println("genus*log(dilatation): "+(g*Math.log(q)));
//			System.out.println();
//			System.out.println();
//			System.out.println();
		}
	}
}
