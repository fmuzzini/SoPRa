import org.tartarus.snowball.ext.PorterStemmer;


/**
 * Vettore che rappresenta la query
 */
@SuppressWarnings("serial")
public class VettoreQuery extends Vettore {
	
	public VettoreQuery(String q){
		super();
		String token[] = q.split(" ");
		for (int i=0; i<token.length; i++){
			PorterStemmer stem = new PorterStemmer();
			stem.setCurrent(token[i]);
			stem.stem();
			this.add(new TermScore(stem.getCurrent(), 1));
		}
		
		float norm = (float) Math.sqrt(this.size());
		super.setNorm(norm);
	}
}
