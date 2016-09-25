import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;


/** 
 * Partendo dai risultati che matchano la query in lucene
 * computa la formula per il SOcialRAnking
 * score(q,d) = beta * cos(q,td) + (1-beta) * sim(q,d)
 */
public class SoraRisultati {
	
	private final float beta = 0.5f;
	
	private IndexReader r;
	private String query;
	private ScoreDoc[] score;

	public SoraRisultati(LuceneRisultati res) throws IOException{
		this.r = res.getReader();
		this.query = res.getQuery();
		ScoreDoc[] l = res.getScore();
		this.score = new ScoreDoc[l.length];
		
		for (int i=0; i<l.length; i++){
			VettoreQuery qv = new VettoreQuery(query);
			VettoreTag tv = new VettoreTag(r, l[i].doc);
			float s = beta * Coseno.coseno(qv, tv);
			s += (1-beta) * l[i].score;
			score[i] = new ScoreDoc(l[i].doc, s);
		}
	}

	public float getBeta() {
		return beta;
	}

	public IndexReader getReader() {
		return r;
	}

	public String getQuery() {
		return query;
	}

	public ScoreDoc[] getScore() {
		return score;
	}
	
	public ScoreDoc[] getOrderedScore(){
		Arrays.sort(score, new ScoreDocComparator());
		
		return score;
	}
}
