package dao;

import pojos.Item;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import conexiones.ConexionRedis;

import com.google.gson.Gson;

public class CarritoDAO {
	private static CarritoDAO instancia;
	
	public static CarritoDAO getInstancia() {
		if (instancia == null)
			instancia = new CarritoDAO();
		return instancia;
	} 
	
	public void agregarCarrito(Item ingreso, String usuario) {
		if(!duplicado(ingreso, usuario)) {
		JedisPool pool = ConexionRedis.getInstancia().getJedis();
		Jedis jedis = pool.getResource();

		Gson gson = new Gson();
        String ingresoJson = gson.toJson(ingreso);
        
        jedis.rpush(usuario+"Carrito", ingresoJson);
        
        jedis.close();
        System.out.print("El producto "+ ingreso.getNombreProd()+" se agrego al carrito");
        }
		else {
			System.out.println("El producto ya se encuentra en el carrito, si desea modificarlo ingrese la opcion 3");
		}
	}
	private boolean duplicado(Item ingreso, String usuario) {

		ArrayList<Item> carrito = getCarrito(usuario);
		
		for (Item i: carrito) {
			if(i.getNombreProd().equals(ingreso.getNombreProd()))
				return true;
		}
		return false;
	}
	
	public void eliminarCarrito(String usuario, String nombreProd) {
		JedisPool pool = ConexionRedis.getInstancia().getJedis();
		Jedis jedis = pool.getResource();
		
		String carrito = usuario+"Carrito";
		
		long tope = jedis.llen(carrito);
		Gson gson = new Gson();
		
		for(long i = 0; i < tope; i++){
			String objJson = jedis.lindex(carrito, i);
			Item ingreso = gson.fromJson(objJson, Item.class);
			if(ingreso.getNombreProd().equals(nombreProd)) {
				jedis.lrem(carrito, 0, objJson);
				break;
			}
		}
		System.out.println("El producto"+ nombreProd + "ha sido eliminado del carrito");
		jedis.close();
	}
	
	public void cambiarCarrito(int cantidad, String usuario, String nombreProd) {
		Jedis jedis = ConexionRedis.getInstancia().getJedis().getResource();
		
		String carrito = usuario+"Carrito";
		long tope = jedis.llen(carrito);
		
		Gson gson = new Gson();
		
		for(long i = 0; i < tope; i++){
			String objJson = jedis.lindex(carrito, i);
			Item ingreso = gson.fromJson(objJson, Item.class);
			if (ingreso.getNombreProd().equals(nombreProd)) {
				ingreso.setCantidad(cantidad);
				String objJsonUpdt = gson.toJson(ingreso);
				jedis.lset(carrito, i, objJsonUpdt);
				break;
			}
		}
		
		jedis.close();
	}
	
	public void undo(String usuario) {

		Jedis jedis = ConexionRedis.getInstancia().getJedis().getResource();
		String carrito = usuario+"Carrito";
		jedis.rpop(carrito);
		jedis.close();
	}
	
	public ArrayList<Item> getCarrito(String usuario) {

		ArrayList<Item> lista = new ArrayList<Item>();
 		Jedis jedis = ConexionRedis.getInstancia().getJedis().getResource();
		
		String carrito = usuario+"Carrito";
		
		long tope = jedis.llen(carrito);
		Gson gson = new Gson();
		
		for(long i = 0; i < tope; i++){
			String objJson = jedis.lindex(carrito, i);
			Item ingreso = gson.fromJson(objJson, Item.class);
			lista.add(ingreso);
		}
		
		jedis.close();
		return lista;
	}

	public boolean verificarCarritoVacio(String usuario) {

		Jedis jedis = ConexionRedis.getInstancia().getJedis().getResource();

		String carrito = usuario + "Carrito";

		long tope = jedis.llen(carrito);

		jedis.close();

		if (tope == 0) {
			return true;
		} else {
			return false;
		}
	}

	public void truncateCarrito(String usuario) {
		Jedis jedis = ConexionRedis.getInstancia().getJedis().getResource();

		String carrito = usuario + "Carrito";

		jedis.del(carrito);

		jedis.close();
	}

}