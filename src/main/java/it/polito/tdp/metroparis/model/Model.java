package it.polito.tdp.metroparis.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	private Graph<Fermata, DefaultEdge> graph;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	
	public Model() {
		this.graph = new SimpleDirectedGraph<Fermata, DefaultEdge>(DefaultEdge.class);
		
		// Dobbiamo aggiungere vertici 
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.getAllFermate();
		this.fermateIdMap = new HashMap<>();
		
		for(Fermata f : fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		Graphs.addAllVertices(this.graph, this.fermate);
		
		//System.out.println(this.graph);
		
		// Dobbiamo creare i vertici
		// METODO 1: meno efficace, valido solo per piccoli grafi
		
		/*
		for(Fermata fp : this.fermate) {
			for(Fermata fa : this.fermate) {
				if(dao.fermateConnesse(fp, fa)) {
					this.graph.addEdge(fp, fa);
				}
			}
		}
		*/
		
		// METODO 2:
		// Potrei ridurre notevolmente il costo chiedendo per una stazione quali sono
		// quelle collegate ad esso. Quindi la complessità passa da [n^2] a [n]
		
		/*
		for(Fermata fp : this.fermate) {
			List<Fermata> connesse = dao.fermateSuccessive(fp, fermateIdMap);
			
			for(Fermata fa: connesse) {
				this.graph.addEdge(fp, fa);
			}
		}
		*/
		
		// METODO 3: 
		List<CoppiaFermate> coppie = dao.coppieFermate(fermateIdMap);
		for(CoppiaFermate c : coppie) {
			this.graph.addEdge(c.getFp(), c.getFa());
		}
		
		//System.out.println(this.graph);
		System.out.format("Grafo caricato correttamente con %d vertici e %d archi.", 
				this.graph.vertexSet().size(), this.graph.edgeSet().size());
		
		
		
	}
	
	public static void main (String args[]) {
		Model m = new Model();
	}
	
}
