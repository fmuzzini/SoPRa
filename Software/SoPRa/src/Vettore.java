import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Classe astratta che rappresenta un vettore.
 * Utilizzata per il calcolo del coseno.
 */
@SuppressWarnings("serial")
public abstract class Vettore extends ArrayList<TermScore> {
	

	private float norm;
	private boolean normSetted = false;
	

	public void sort() {
		Collections.sort(this, new VettoreComparator());		
	}
	
	protected void setNorm(float n){
		this.norm = n;
		this.normSetted = true;
	}
	
	public float getNorm() {
		if (normSetted)
			return norm;
		
		float n2 = 0f;
		for(int i=0; i<this.size(); i++){
			float s = this.get(i).score;
			n2 += s * s;
		}
		
		this.setNorm((float) Math.sqrt(n2));
		return this.getNorm();
	}

	public String getTerm(int i) {
		return this.get(i).term;
	}

	public float getScore(int i) {
		return this.get(i).score;
	}

	public TermScore getByTerm(String term) {
		for(int i=0; i<this.size(); i++){
			TermScore t = this.get(i);
			if(t.term.equals(term))
				return t;
			else {
				if (t.term.compareToIgnoreCase(term) > 0)
					break;
			}
		}
		
		return null;
	}
	
	
	private class VettoreComparator implements Comparator<TermScore>{

		@Override
		public int compare(TermScore o1, TermScore o2) {
			return o1.term.compareTo(o2.term);
		}
		
	}

	
}



