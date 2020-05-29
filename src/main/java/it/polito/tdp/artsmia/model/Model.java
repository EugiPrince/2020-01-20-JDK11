package it.polito.tdp.artsmia.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.artsmia.db.ArtsmiaDAO;

public class Model {
	
	private Graph<Integer, DefaultWeightedEdge> grafo;
	private ArtsmiaDAO dao;
	private List<Adiacenza> adiacenze;
	
	//Problema di ottimizzazione (2 - ricorsione)
	private List<Integer> best;
	
	public Model() {
		this.dao = new ArtsmiaDAO();
	}
	
	public List<String> getRuoli() {
		return this.dao.getRuoli();
	}
	
	public int vertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int archi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Adiacenza> getAdiacenze() {
		return this.adiacenze;
	}
	
	public void creaGrafo(String ruolo) {
		this.grafo = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//O mi faccio dare tutti i vertici dal DB e poi dopo aggiungo gli archi -> MEGLIO SE CHIEDONO COMP CONNESSE
		//Facendo così avrò più vertici, perché ci saranno quelli isolati.. caso più generico
		Graphs.addAllVertices(this.grafo, this.dao.getArtisti(ruolo));
		
		adiacenze = this.dao.getAdiacenze(ruolo);
		
		//Oppure avendo nelle adiacenze gli id, e controllo se tali vertici esistono gia' nel grafo oppure no e se non
		//ci sono li aggiungo direttamente -> MEGLIO SE LE RICHIESTE SONO RELATIVE A VERTICI CHE HANNO UN'ADIACENZA (qui)
		//Così inserisco solo i vertici che recuperiamo dalle adiacenze (quelli isolati ciao ma qua non servono)
		
		for(Adiacenza a : this.adiacenze) {
			/*if(!this.grafo.containsVertex(a.getA1()))
				this.grafo.addVertex(a.getA1());
			
			if(!this.grafo.containsVertex(a.getA2()))
				this.grafo.addVertex(a.getA2());
			*/
			if(this.grafo.getEdge(a.getA1(), a.getA2()) == null)
				Graphs.addEdgeWithVertices(this.grafo, a.getA1(), a.getA2(), a.getPeso());
		}
		
		System.out.println("Grafo creato:");
		System.out.println("Num vertici: "+this.grafo.vertexSet().size());
		System.out.println("Num archi: "+this.grafo.edgeSet().size());
	}
	
	public boolean grafoContiene(Integer id) {
		if(this.grafo.containsVertex(id))
			return true;
		return false;
	}
	
	public List<Integer> trovaPercorso(Integer sorgente) {
		this.best = new ArrayList<>();
		List<Integer> parziale = new ArrayList<>(); //Lista che tiene traccia della soluzione parziale
		parziale.add(sorgente);
		
		//Ricorsione
		ricorsione(parziale, -1); //Metto un peso che sicuramente non esiste nel grafo così da discriminare il fatto
		//che siamo nel primo livello della ricorsione
		
		return this.best;
	}
	
	private void ricorsione(List<Integer> parziale, int peso) {
		
		//Se so che il peso è -1 so che è il primo giro di ricorsione, quindi devo salvarmi il peso che c'è tra il nodo
		//e i suoi vicini e continuare la ricorsione solo con questo peso
		Integer ultimo = parziale.get(parziale.size()-1);
		List<Integer> vicini = Graphs.neighborListOf(this.grafo, ultimo); //Ottengo tutti i vicini
		
		for(Integer vicino : vicini) {
			//Se non ci fosse il vincolo non metterei if e non ci sarebbe il valore del peso, ma controllo comunque
			//che non ci siano cicli ecc -> controllo che non ci siano già i vicini che sto considerando 
			if(!parziale.contains(vicino) && peso == -1) {
				parziale.add(vicino);
				ricorsione(parziale, (int)this.grafo.getEdgeWeight(this.grafo.getEdge(ultimo, vicino))); //solo archi con peso
				parziale.remove(vicino);
			}
			else {
				//Aggiungiamo solo i vicini che sono collegati con archi del peso corretto (impostato sopra), se e' uguale
				//continuiamo il percorso
				if(!parziale.contains(vicino) && this.grafo.getEdgeWeight(this.grafo.getEdge(ultimo, vicino)) == peso) {
					parziale.add(vicino);
					ricorsione(parziale, peso);
					parziale.remove(vicino);
				}
			}
		}
		
		if(parziale.size() > best.size()) {
			this.best = new ArrayList<>(parziale);
		}
	}
}
