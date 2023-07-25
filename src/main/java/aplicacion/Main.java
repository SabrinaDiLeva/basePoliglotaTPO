package aplicacion;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;


import dao.*;
import org.bson.Document;
import pojos.*;

public class Main {

	public static void main(String[] args) {

		System.out.println("\u001B[30;47mECOMMERCE DE INDUMENTARIA\u001B[0m");

		Usuario usuario = ingresarUsuario();

		if(usuario.getNombreUsuario().equalsIgnoreCase("Admin")) {
			cambiarCatalogo();
		}
		else {
			Scanner scanner = new Scanner(System.in);
			String ingreso;
			do {
				System.out.println("¿Qué operación desea realizar hoy?");
				System.out.println("Por favor, seleccione una opción: ");
				System.out.println("1. Ver y modificar el carrito");
				System.out.println("2. Pagar facturas pendientes");
				System.out.println("0. Finalizar ejecucion");

				ingreso = scanner.next();
				switch (ingreso){
					case "1":
						carrito(usuario);
						break;
					case "2":
						pagarFacturas(usuario);
						break;
					case "0":
						break;
					default:
						System.out.println("Opción inválida. Por favor, seleccione nuevamente.");
						break;
				}
			}
			while (!ingreso.equals("0"));
			cortarConexionUsuario();
		}
		guardarActividadUsuario(usuario);
	}
	public static void carrito(Usuario usuario) {

		Scanner scanner = new Scanner(System.in);
		int ingreso = -1;
		String user = usuario.getNombreUsuario();

		do {

			CatalogoDAO.getInstancia().buscarCatalogo();
			mostrarCarrito(user);

			System.out.println("Ingresa una de las siguientes opciones:\n" +
					"1. Agregar un nuevo producto\n" +
					"2. Eliminar un producto\n" +
					"3. Cambiar la cantidad de un producto ingresado\n" +
					"4. Eliminar el último producto agregado\n" +
					"5. Vaciar el carrito\n" +
					"6. Hacer pedido\n" +
					"7. Volver al menú principal");

			if (scanner.hasNextInt()) {
				ingreso = scanner.nextInt();

				switch (ingreso) {
					case 1:
						agregarProducto(user);
						break;
					case 2:
						sacarProducto(user);
						break;
					case 3:
						cambiarProducto(user);
						break;
					case 4:
						estadoAnterior(user);
						break;
					case 5:
						vaciarCarrito(user);
						break;
					case 6:
						hacerPedido(usuario);
						break;
					case 7:
						break;
				}
			}else {
				scanner.next();
				System.out.println("Opción inválida. Por favor, ingrese nuevamente.");
			}
		} while (ingreso != 7);
	}

	public static void agregarProducto(String usuario) {

		String prod;
		int cant;
		Scanner scanner = new Scanner(System.in);

		do
		{
			System.out.print("Nombre del producto: ");
			prod = scanner.nextLine();
		}
		while(!CatalogoDAO.getInstancia().isProducto(prod));

		do {
			System.out.print("Cantidad a agregar: ");

			if (scanner.hasNextInt()) {
				cant = scanner.nextInt();
			} else {
				cant = -1;
				scanner.next();
			}
		} while (cant == -1);

		Item prodYcant = new Item(prod, cant);
		CarritoDAO.getInstancia().agregarCarrito(prodYcant, usuario);

	}

	public static void sacarProducto(String usuario) {

		String prod;
		Scanner scanner = new Scanner(System.in);

		boolean verificar = CarritoDAO.getInstancia().verificarCarritoVacio(usuario);
		if (verificar == true){
			System.out.println("El carrito se encuntra vacio");
		}else {
			do {
				System.out.print("Ingrese el nombre del producto que desea eliminar: ");
				prod = scanner.nextLine();
			}
			while (!CatalogoDAO.getInstancia().isProducto(prod));

			CarritoDAO.getInstancia().eliminarCarrito(usuario, prod);
		}
	}

	public static void cambiarProducto(String usuario) {

		String prod;
		int cant;
		Scanner scanner = new Scanner(System.in);

		boolean verificar = CarritoDAO.getInstancia().verificarCarritoVacio(usuario);
		if (verificar == true){
			System.out.println("El carrito se encuntra vacio");
		}else {
			do {
				System.out.print("Ingrese el nombre del producto que desea cambiar: ");
				prod = scanner.nextLine();
			}
			while (!CatalogoDAO.getInstancia().isProducto(prod));

			do {
				System.out.print("Cantidad a agregar: ");

				if (scanner.hasNextInt()) {
					cant = scanner.nextInt();
				} else {
					cant = -1;
					scanner.next();
				}
			} while (cant == -1);

			CarritoDAO.getInstancia().cambiarCarrito(cant, usuario, prod);
		}

	}

	public static void estadoAnterior(String usuario) {

		boolean verificar = CarritoDAO.getInstancia().verificarCarritoVacio(usuario);
		if (verificar == true){
			System.out.println("El carrito se encuntra vacio");
		}else {
			CarritoDAO.getInstancia().undo(usuario);
			System.out.println("El producto ha sido eliminado del carrito");
		}
	}

	public static void vaciarCarrito(String usuario) {

		boolean verificar = CarritoDAO.getInstancia().verificarCarritoVacio(usuario);

		if (verificar == true){
			System.out.println("El carrito ya se encuntra vacio");
		} else{
			CarritoDAO.getInstancia().truncateCarrito(usuario);
			System.out.println("El carrito ha sido vaciado");
		}
	}

	public static void hacerPedido(Usuario usuario) {

		boolean verificar = CarritoDAO.getInstancia().verificarCarritoVacio(usuario.getNombreUsuario());

		if (verificar == true){
			System.out.println("El carrito se encuntra vacio");
		}else{

			Scanner scanner = new Scanner(System.in);
			Pedido pedido = new Pedido();

			pedido.setNombre(usuario.getNombre());
			pedido.setApellido(usuario.getApellido());
			pedido.setDireccion(usuario.getDireccion());
			System.out.println("Condicion ante el IVA: ");
			String condicionIva = condicionAnteIva();
			pedido.setCondicionIva(condicionIva);

			double importe = getImporte(usuario.getNombreUsuario());
			pedido.setImporte(importe);

			System.out.println("% de descuento aplicado: ");
			double descuentoPorcentaje = scanner.nextDouble();

			System.out.println("% de impuestos aplicados: ");
			double impuestoPorcentaje = scanner.nextDouble();

			ArrayList<Document> carrito = parseDoc(CarritoDAO.getInstancia().getCarrito(usuario.getNombreUsuario()));
			pedido.setCarrito(carrito);

			System.out.println();
			System.out.println("\u001B[30;47mEl pedido fue realizado correctamente\u001B[0m");
			System.out.println();

			double descuento = importe * (descuentoPorcentaje / 100.0);
			double importeConDescuento = importe - descuento;
			double impuesto = importeConDescuento * (impuestoPorcentaje / 100.0);
			double importeTotal = importeConDescuento + impuesto;

			pedido.setDescuento(descuento);
			pedido.setImpuestos(impuesto);

			PedidosDAO.getInstancia().agregarPedido(pedido);
			CarritoDAO.getInstancia().truncateCarrito(usuario.getNombreUsuario());

			pasarAFacturas(usuario, importeTotal);
		}
	}

	public static void mostrarCarrito(String usuario) {

		ArrayList<Item> carritoActual = CarritoDAO.getInstancia().getCarrito(usuario);

		System.out.println();
		System.out.println("\u001B[30;47mCARRITO\u001B[0m");
		for (Item i : carritoActual) {
			String producto = i.getNombreProd();
			String cantidad = Integer.toString(i.getCantidad());
			System.out.println(producto+", "+ cantidad);
		}
		System.out.println();


	}
	public static String condicionAnteIva() {

		Scanner scanner = new Scanner(System.in);
		String condicionIva = "";
		char ingreso;
		do
		{
			System.out.println("(R) Responsable inscripto");
			System.out.println("(C) Consumidor final");

			ingreso = Character.toUpperCase(scanner.next().charAt(0));
			switch (ingreso){
				case 'R':
					condicionIva = "Responsable inscripto";
					break;
				case 'C':
					condicionIva = "Consumidor final";
					break;
				default:
					System.out.println("No es ninguna de las opciones");
					break;
			}
		}
		while(ingreso != 'R' && ingreso != 'C');
		return condicionIva;
	}

	public static void pasarAFacturas(Usuario usuario, double importe) {

		Scanner scanner = new Scanner(System.in);
		String medioDePago = "";
		char medioPago;
		System.out.println("Medio de pago: ");
		do
		{
			System.out.println("(E) Efectivo");
			System.out.println("(D) Debito");
			System.out.println("(C) Credito");
			System.out.println("(T) Transferencia bancaria");

			medioPago = Character.toUpperCase(scanner.next().charAt(0));
			switch (medioPago){
				case 'E':
					medioDePago = "Efectivo";
					break;
				case 'D':
					medioDePago = "Debito";
					break;
				case 'C':
					medioDePago = "Credito";
					break;
				case 'T':
					medioDePago = "Transferencia bancaria";
					break;
				default:
					System.out.println("No es ninguna de las opciones");
					break;
			}
		}
		while(medioPago != 'E' && medioPago != 'D' && medioPago != 'C' && medioPago != 'T');

		Factura aux = new Factura(usuario.getDocumento(), medioDePago, importe);

		FacturasDAO.getInstancia().guardarFactura(aux);
	}

	public static void pagar(Factura factura) {

		Scanner input = new Scanner(System.in);
		System.out.println("Ingrese el nombre del operador involucrado, omita en caso de que no corresponda: ");
		String operador = input.nextLine();
		if(operador.isEmpty()) {
			operador = "N/A";
		}

		Pago pago = new Pago(factura.getIdFactura(), factura.getFormaDePago(), operador, factura.getImporte());
		PagosDAO.getInstancia().pagar(pago);
	}

	public static boolean verificarFactura(int ingreso, ArrayList<Factura> facturas) {

		for (Factura i: facturas) {
			if (i.getIdFactura() == ingreso)
				return true;
		}
		return false;
	}

	public static void pagarFacturas(Usuario usuario) {

		Scanner scanner = new Scanner(System.in);
		ArrayList<Factura> facturas = FacturasDAO.getInstancia().facturasUsuario(usuario.getDocumento());

		Iterator<Factura> iterator = facturas.iterator();
		while (iterator.hasNext()) {
			Factura factura = iterator.next();
			boolean verificarPago = PagosDAO.getInstancia().verificarPagado(factura.getIdFactura());
			if (verificarPago) {
				iterator.remove();
			}
		}

		if(!facturas.isEmpty()) {
			System.out.println("\u001B[30;47mFACTURAS PENDIENTES\u001B[0m");
			for (Factura i : facturas) {
				String idFactura = String.valueOf(i.getIdFactura());
				String dni = String.valueOf(i.getDNIusuario());
				String medioPago = i.getFormaDePago();
				String total = String.valueOf(i.getImporte());

				System.out.println("ID: "+idFactura +", DNI: "+ dni + ", Medio de pago: "+medioPago  +", Total: "+ total );
			}
			System.out.println();
			int ingreso;
			do {
				System.out.print("Ingrese el ID de la factura que desea pagar: ");
				ingreso = scanner.nextInt();
			} while (!verificarFactura(ingreso, facturas));

			pagar(FacturasDAO.getInstancia().buscarFactura(ingreso));
		}
		else{
			System.out.println();
			System.out.println("\u001B[30;47mTodas las facturas se encuentan pagas\u001B[0m");
		}
	}


	public static void cambiarCatalogo() {

		Scanner scanner = new Scanner(System.in);
		String prod;

		System.out.print("Identifiquese antes de continuar para registrar quién está haciendo los cambios: ");
		String operador = scanner.nextLine();

		CatalogoDAO.getInstancia().buscarCatalogoAdmin();

		do
		{
			System.out.print("Ingrese el nombre del producto que quiere modificar: ");
			prod = scanner.nextLine();
		}
		while(!CatalogoDAO.getInstancia().isProducto(prod));

		char ingreso;
		do
		{
			System.out.println("Opciones disponibles:\n" +
					"(D) Cambiar la descripción\n" +
					"(F) Cambiar las fotos\n" +
					"(C) Cambiar los comentarios\n" +
					"(V) Cambiar los videos\n" +
					"(P) Cambiar el precio\n" +
					"(E) Salir");

			ingreso = Character.toUpperCase(scanner.next().charAt(0));

			switch (ingreso){
				case 'D':
					cambiarDescripcion(prod, operador);
					break;
				case 'F':
					cambiarFotos(prod, operador);
					break;
				case 'C':
					cambiarComentarios(prod, operador);
					break;
				case 'V':
					cambiarVideos(prod, operador);
					break;
				case 'P':
					cambiarPrecio(prod, operador);
					break;
				case 'E':
					break;
				default:
					System.out.println("No es ninguna de las opciones");
					break;
			}
		}
		while(ingreso != 'D' && ingreso != 'F' && ingreso != 'C' && ingreso != 'V' && ingreso != 'P' && ingreso != 'E');

	}

	public static void cambiarPrecio(String prod, String operador) {

		Scanner scanner = new Scanner(System.in);
		System.out.print("Nuevo precio: ");
		double precio = scanner.nextDouble();
		double precioViejo = CatalogoDAO.getInstancia().cambiarPrecio(prod, precio);

		Document doc = new Document();
		doc.append("producto", prod);
		doc.append("precioNuevo", precio);
		doc.append("precioViejo", precioViejo);
		doc.append("operador", operador);

		GuardarCambiosDAO.getInstancia().guardarCambio(doc);
	}

	public static void cambiarVideos(String prod, String operador) {

		Scanner scanner = new Scanner(System.in);
		String ingreso;
		do
		{
			System.out.println("(A) Agregar video");
			System.out.println("(Z) Eliminar video");
			ingreso = scanner.nextLine();
		}
		while(!ingreso.equalsIgnoreCase("A") && !ingreso.equalsIgnoreCase("Z"));

		if(ingreso.equalsIgnoreCase("A"))
		{
			System.out.print("URL del video: ");
			String video = scanner.nextLine();

			ArrayList<String> valorViejo = CatalogoDAO.getInstancia().agregarVideos(prod, video);
			ArrayList<String> valorNuevo;

			if(valorViejo != null) {
				valorNuevo = new ArrayList<String>(valorViejo);
			}
			else {
				valorNuevo = new ArrayList<String>();
			}

			valorNuevo.add(video);

			Document doc = new Document();
			doc.append("producto", prod);
			doc.append("videosNuevos", valorNuevo);
			doc.append("videosViejos", valorViejo);
			doc.append("operador", operador);

			GuardarCambiosDAO.getInstancia().guardarCambio(doc);
		}
		else
		{
			System.out.print("URL del video a eliminar: ");
			String video = scanner.nextLine();

			ArrayList<String> valorViejo = CatalogoDAO.getInstancia().sacarVideos(prod, video);
			ArrayList<String> valorNuevo;

			if(valorViejo != null) {
				valorNuevo = new ArrayList<String>(valorViejo);
			}
			else {
				valorNuevo = new ArrayList<String>();
			}

			valorNuevo.remove(video);

			Document doc = new Document();
			doc.append("producto", prod);
			doc.append("videosNuevos", valorNuevo);
			doc.append("videosViejos", valorViejo);
			doc.append("operador", operador);

			GuardarCambiosDAO.getInstancia().guardarCambio(doc);
		}
	}

	public static void cambiarComentarios(String prod, String operador) {

		Scanner scanner = new Scanner(System.in);
		String ingreso;
		do
		{
			System.out.println("(A) Agregar comentario");
			System.out.println("(Z) Eliminar comentario");
			ingreso = scanner.nextLine();
		}
		while(!ingreso.equalsIgnoreCase("A") && !ingreso.equalsIgnoreCase("Z"));
		if(ingreso.equalsIgnoreCase("A")) {

			System.out.print("Nuevo comentario: ");
			String comentario = scanner.nextLine();

			ArrayList<String> valorViejo = CatalogoDAO.getInstancia().agregarComentario(prod, comentario);
			ArrayList<String> valorNuevo;

			if(valorViejo != null) {
				valorNuevo = new ArrayList<String>(valorViejo);
			}
			else{
				valorNuevo = new ArrayList<String>();
			}

			valorNuevo.add(comentario);

			Document doc = new Document();
			doc.append("producto", prod);
			doc.append("comentariosNuevos", valorNuevo);
			doc.append("comentariosViejos", valorViejo);
			doc.append("operador", operador);

			GuardarCambiosDAO.getInstancia().guardarCambio(doc);
		}
		else {
			System.out.print("Comentario a eliminar: ");
			String comentario = scanner.nextLine();

			ArrayList<String> valorViejo = CatalogoDAO.getInstancia().sacarComentario(prod, comentario);
			ArrayList<String> valorNuevo;
			if (valorViejo != null) {
				valorNuevo = new ArrayList<String>(valorViejo);
			}else {
				valorNuevo = new ArrayList<String>();
			}

			valorNuevo.remove(comentario);

			Document doc = new Document();
			doc.append("producto", prod);
			doc.append("comentariosNuevos", valorNuevo);
			doc.append("comentariosViejos", valorViejo);
			doc.append("operador", operador);

			GuardarCambiosDAO.getInstancia().guardarCambio(doc);
		}
	}

	public static void cambiarFotos(String prod, String operador) {

		Scanner scanner = new Scanner(System.in);
		String ingreso;
		do
		{
			System.out.println("(A) agrager foto");
			System.out.println("(Z) eliminar foto");
			ingreso = scanner.nextLine();
		}
		while(!ingreso.equalsIgnoreCase("A") && !ingreso.equalsIgnoreCase("Z"));

		if(ingreso.equalsIgnoreCase("A")) { //
			System.out.print("URL de la foto: ");
			String foto = scanner.nextLine();

			ArrayList<String> fotosViejas = CatalogoDAO.getInstancia().agregarFoto(prod, foto);
			ArrayList<String> fotosNuevos;

			if(fotosViejas != null) {
				fotosNuevos = new ArrayList<String>(fotosViejas);
			}
			else {
				fotosNuevos = new ArrayList<String>();
			}

			fotosNuevos.add(foto);

			Document doc = new Document();
			doc.append("producto", prod);
			doc.append("fotosNuevas", fotosNuevos);
			doc.append("fotosViejas", fotosViejas);
			doc.append("operador", operador);

			GuardarCambiosDAO.getInstancia().guardarCambio(doc);
		}
		else {
			System.out.print("URL de la foto a eliminar: ");
			String foto = scanner.nextLine();

			ArrayList<String> fotosViejas = CatalogoDAO.getInstancia().sacarFoto(prod, foto);
			ArrayList<String> fotosNuevos;
			if(fotosViejas != null) {
				fotosNuevos = new ArrayList<String>(fotosViejas);
			}
			else {
				fotosNuevos = new ArrayList<String>();
			};
			fotosNuevos.remove(foto);

			Document doc = new Document();
			doc.append("producto", prod);
			doc.append("fotosNuevas", fotosNuevos);
			doc.append("fotosViejas", fotosViejas);
			doc.append("operador", operador);

			GuardarCambiosDAO.getInstancia().guardarCambio(doc);
		}

	}

	public static void cambiarDescripcion(String prod, String op) {

		Scanner scanner = new Scanner(System.in);
		System.out.print("Nueva descripcion: ");
		String desc = scanner.nextLine();
		String descVieja = CatalogoDAO.getInstancia().cambiarDesc(prod, desc);

		Document doc = new Document();
		doc.append("producto", prod);
		doc.append("descripcion nueva", desc);
		doc.append("descripcion vieja", descVieja);
		doc.append("operador", op);

		GuardarCambiosDAO.getInstancia().guardarCambio(doc);
	}

	public static Usuario ingresarUsuario() {

		String usuario, contrasenia;
		Scanner scanner= new Scanner(System.in);

		do {
			System.out.print("Ingrese su nombre de usuario: ");
			usuario = scanner.nextLine();
		}
		while (!UsuarioDAO.getInstancia().verificarUsuario(usuario));

		do {
			System.out.print("Ingrese su contaseña: ");
			contrasenia = scanner.nextLine();
		}
		while(!UsuarioDAO.getInstancia().verificarContrasenia(contrasenia, usuario));

		System.out.println();
		System.out.println("\u001B[30;47mBienvenido " + usuario + "\u001B[0m");

		return UsuarioDAO.getInstancia().guardarDatos(usuario);
	}
	public static double getImporte(String nombreUs) {

		double importe = 0;
		ArrayList<Item> carrito = CarritoDAO.getInstancia().getCarrito(nombreUs);

		for(Item ingreso: carrito){
			importe += CatalogoDAO.getInstancia().precio(ingreso);
		}

		return importe;
	}

	public static ArrayList<Document> parseDoc(ArrayList<Item> carrito){
		ArrayList<Document> pedidoProd = new ArrayList<Document>();
		for(Item i : carrito) {
			Document temp = new Document();
			temp.append("producto", i.getNombreProd());
			temp.append("cantidad", i.getCantidad());
			pedidoProd.add(temp);
		}
		return pedidoProd;
	}

	public static void cortarConexionUsuario() {
		PedidosDAO.getInstancia().cerrarConexion();
		FacturasDAO.getInstancia().cerrarConexion();
	}

	public static void guardarActividadUsuario(Usuario usuario) {
		usuario.setHoraFin(LocalTime.now());
		NivelUsuarioDAO.getInstancia().guardarTiempo(usuario);
		System.out.println();
		System.out.println("\u001B[30;47mFin de la ejecucion\u001B[0m");
		String categoria = NivelUsuarioDAO.getInstancia().getCategoria(usuario.getNombreUsuario());
		System.out.println("El usuario es de categoria: " + categoria);
		NivelUsuarioDAO.getInstancia().cortarConex();
	}
}
