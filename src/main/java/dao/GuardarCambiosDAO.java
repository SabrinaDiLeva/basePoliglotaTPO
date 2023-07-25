package dao;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import conexiones.ConexionMongo;

public class GuardarCambiosDAO {
	private static GuardarCambiosDAO instancia;
	
	public static GuardarCambiosDAO getInstancia() {
		if (instancia == null)
			instancia = new GuardarCambiosDAO();
		return instancia;
	} 
	
	public void guardarCambio(Document doc) {
	    MongoDatabase database = ConexionMongo.getInstancia().getCliente().getDatabase("aplicacion");
		MongoCollection<Document> colecion = database.getCollection("cambios"); 
		
		colecion.insertOne(doc);
	}
}
