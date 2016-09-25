import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;



/**
 * Vettore che rappresenta una serie di tag
 */
@SuppressWarnings("serial")
public class VettoreTag extends Vettore {
	
	
	public VettoreTag(IndexReader s, int doc) throws IOException{
		super();
		TermFreqVector freq = s.getTermFreqVector(doc, "tag");
		if (freq == null){
			return;
		}
		String[] term = freq.getTerms();
		int[] f = freq.getTermFrequencies();
		int r = s.numDocs();
		float norm2 = 0f;
		
		for (int i=0; i<f.length; i++){
			int rt = s.docFreq(new Term("tag", term[i]));
			float score = (float) (f[i] * Math.log10(r/rt));
			this.add(new TermScore(term[i], score));
			
			norm2 += score * score;
		}
		
		this.setNorm((float) Math.sqrt(norm2));
		
	}

}
