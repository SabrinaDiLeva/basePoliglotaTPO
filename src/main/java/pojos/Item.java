package pojos;

public class Item {
	private String nombreProd;
	private int cantidad;
	
	public Item(String nombre, int cant){
		this.nombreProd = nombre;
		this.cantidad = cant;
	}
	
	public String getNombreProd() {
		return nombreProd;
	}
	public int getCantidad() {
		return cantidad;
	}
	
	public void setNombreProd(String nombreProd) {
		this.nombreProd = nombreProd;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
}
