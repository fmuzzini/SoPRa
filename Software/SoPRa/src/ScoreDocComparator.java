import java.util.Comparator;

import org.apache.lucene.search.ScoreDoc;

/**
 * Classe utilizzata per ordinamento
 */
public class ScoreDocComparator implements Comparator<ScoreDoc> {


	@Override
	public int compare(ScoreDoc o1, ScoreDoc o2) {
		return Float.compare(o2.score, o1.score);
	}

}
