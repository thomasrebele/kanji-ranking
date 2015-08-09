package kanjiranking.fontanalyzer;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import kanjiranking.Data;

public class Main {
	
	public static void main(String ...args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Mat mat = Mat.eye( 3, 3, CvType.CV_8UC1 );
		System.out.println( "mat = " + mat.dump() );
		
		FontAnalyzer fa = new FontAnalyzer(25);
		
		PriorityQueue<Ideogram> pq = new PriorityQueue<>(new Comparator<Ideogram>(){
			@Override
			public int compare(Ideogram o1, Ideogram o2) {
				return Integer.compare(o1.stat[5], o2.stat[5]);
			}
			
		});
		String characters = Data.conanKanji;
		
		for(int i=0; i<characters.length(); i++) {
			Ideogram gram = fa.ideogram(characters.charAt(i));
			gram.calculateStats();
			pq.add(gram);
		}

		while(!pq.isEmpty()) {
			System.out.println(pq.remove().c);
		}
	}
	
}
