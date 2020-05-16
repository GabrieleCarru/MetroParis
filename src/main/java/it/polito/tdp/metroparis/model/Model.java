package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

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
		System.out.format("Grafo caricato correttamente con %d vertici e %d archi. \n", 
				this.graph.vertexSet().size(), this.graph.edgeSet().size());
		
		
		
		
	}
	
	/**
	 *  Visita l'intero grafo con la strategia Breadth First
	 *  e ritorna l'insieme dei vertici incontrati.
	 *  (Una serie di cerchi che aumentano di diametro analizzando 
	 *  ogni volta le adiacenze delle fermate al livello precedente.)
	 *  @param source : fermata sorgente
	 *  @return insieme vertici incontrati
	 */
	public List<Fermata> visitaAmpiezza(Fermata source) {
		
		List<Fermata> visita = new ArrayList<>();
		
		BreadthFirstIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(graph, source);
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		
		return visita;
	}
	
	public Map<Fermata, Fermata> albertoVista(Fermata source) {
		
		Map<Fermata, Fermata> albero = new HashMap<>();
		albero.put(source, null);
		
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<Fermata, DefaultEdge>(graph, source);
		
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {
			
			// tutti questi metodi vanno definiti perchè necessari per l'interfaccia, 
			// qualora non ci interessi averli, li lasciamo vuoti.
			
			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {}
			
			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {}
			
			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				
				// la visita sta considerando un arco
				// questo arco ha scoperto un nuovo vertice?
				// se si, provenendo da dove?
				DefaultEdge edge = e.getEdge();	// (a,b) : ho scoperto 'a' partendo da 'b' oppure 'b' partendo da 'a'
				
				Fermata a = graph.getEdgeSource(edge);
				Fermata b = graph.getEdgeTarget(edge);
				if(albero.containsKey(a)) {
					albero.put(b, a);
				} else {
					albero.put(a, b);
				}
				
			}
			
			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}
		});
		
		while(bfv.hasNext()) {
			bfv.next(); // estrai l'elemento e ignoralo
		}
		
		return albero;
		
	}
	
	public List<Fermata> visitaProfondita(Fermata source) {
		
		List<Fermata> visita = new ArrayList<>();
		
		// Ricorsiva anche se non visibile a noi!
		DepthFirstIterator<Fermata, DefaultEdge> dfv = new DepthFirstIterator<>(graph, source);
		while(dfv.hasNext()) {
			visita.add(dfv.next());
		}
		
		return visita;
	}
	
	public static void main (String args[]) {
		Model m = new Model();
		List<Fermata> visita = m.visitaAmpiezza(m.fermate.get(0)); // SOLUZIONE MOLTO BRUTTA!
		System.out.println(visita);
		List<Fermata> visita2 = m.visitaProfondita(m.fermate.get(0)); 
		System.out.println(visita2);
		
		Map<Fermata, Fermata> alberoMap = m.albertoVista(m.fermate.get(0));
		for(Fermata f : alberoMap.keySet()) {
			System.out.format("%s <- %s \n", f, alberoMap.get(f));
		}
		
	}
	
}
