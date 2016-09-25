import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;

/**
 * Computa gli score SoPRa partendo dai risultati SoRa
 * rank(d,q,u) = ep * cos(pu,td) + (1-ep) * sora(q,d)
 */
public class SopraRisultati {
	

	private ScoreDoc[] score;
	private VettoreTag user;
	
	public SopraRisultati(SoraRisultati res, int user, IndexReader ruser) throws IOException{
		this(res,user,ruser,0.5f);
	}

	public SopraRisultati(SoraRisultati res, int user, IndexReader ruser, float ep) throws IOException{
		ScoreDoc hits[] = res.getScore();
		score = new ScoreDoc[hits.length];
		this.user = new VettoreTag(ruser, user);
		
		for(int i=0; i<hits.length; i++){
			VettoreTag t = new VettoreTag(res.getReader(), hits[i].doc);
			float s = Coseno.coseno(this.user, t);
			float sc = (float) ep * s + (1-ep) * hits[i].score;
			score[i] = new ScoreDoc(hits[i].doc, sc);
		}
	}

	public ScoreDoc[] getScore() {
		return score;
	}

}
