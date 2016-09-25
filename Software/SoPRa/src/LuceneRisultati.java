import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

/**
 * Attraverso lucene crea un array con i documenti e gli score che matchano la query
 */
public class LuceneRisultati {
	
	private IndexReader r;
	private String query;
	private ScoreDoc[] score;
	private IndexSearcher searcher;

	public LuceneRisultati(String q_, IndexReader r) throws ParseException, IOException{
		this.r = r;
		this.query = q_;
		int hitsPerPage = r.numDocs();
		
		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
		Query q = new QueryParser(Version.LUCENE_36, "text", analyzer).parse(QueryParser.escape(query));
		searcher = new IndexSearcher(r);
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
	    searcher.search(q, collector);
	    this.score = collector.topDocs().scoreDocs;
	    searcher.close();
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

	public IndexSearcher getSearcher() {
		return searcher;
	}
	
}
