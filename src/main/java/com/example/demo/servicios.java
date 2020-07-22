package com.example.demo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.neighboursearch.LinearNNSearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.backend.recomendations.entity.RecommendationRecord;
import com.example.demo.models.entity.Cliente;
import com.example.demo.models.entity.Producto;
import com.example.demo.models.entity.Recomendacion;
import com.example.demo.models.services.IClienteService;
import com.example.demo.models.services.IProductoService;
import com.example.demo.models.services.IRecomendacionService;

/**
 * Area
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("prueba")
public class servicios {

	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IProductoService productoService;
	
	@Autowired
	private IRecomendacionService recomendacionService;
	
	@GetMapping("/clientes")
	private List<Cliente> index(){
		return clienteService.findAll();
	}
	
	@GetMapping("/productos")
	private List<Producto> index2(){
		return productoService.findAll();
	}
	
	@GetMapping("/recomendaciones")
	private List<Recomendacion> index3(){
		return recomendacionService.findAll();
	}
	
	@GetMapping("/clientes/{id}")
	public Cliente show(@PathVariable Long id) {
		return clienteService.findById(id);
	}
	
	@GetMapping("/recomendaciones/{id_cliente}")
	public List<Recomendacion> show2(@PathVariable int id_cliente) {
		return recomendacionService.buscar(id_cliente);
	}
	
	@GetMapping("/findAll")
	public ResponseEntity<?> findAll() {
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> json1 = new HashMap<>();
		response.put("coincidencia", "El registro ha sido creado con éxito!");
		response.put("index", json1);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}

	@GetMapping("/findRecomendationsPro/{id_cliente}")
	public ResponseEntity<?> recomendacionBD(@PathVariable int id_cliente) {
		Map<String, Object> coincidencias = new HashMap();
		List<Recomendacion> recomedacionesAnonimas = recomendacionService.findAll();
		List<Producto> productos = productoService.findAll();
		List<Recomendacion> recomendacionPersonal = recomendacionService.buscar(id_cliente);
		Cliente datosPersonales = clienteService.findById(new Long(id_cliente));
		
		
		String recomendacionesGenerales = "@relation productos\r\n" + "\r\n";
		String productosDatos = "";
		String calificacionesAnon = "\r\n" + "@data\r\n";
		String calificacionesCli = "\r\n" + "@data\r\n";
		String recomendacionesUsuario = "@relation usuario\r\n" + "\r\n";

		for (Producto product : productos) {
			productosDatos = productosDatos + "@attribute '" + product.getNombre_producto() + "' NUMERIC\r\n";
		}
		
		int[][] recomendationsAnon = new int[17][productos.size()];
		int[] recomendationsCli = new int[productos.size()];

		
		for (int index = 0; index < 17; index++) {
			for (int iterator = 0; iterator < productos.size(); iterator++) {
				recomendationsAnon[index][iterator] = 0;
			}
		}
		
		
		for (int i = 0; i < productos.size(); i++) {
			recomendationsCli[i] = 0;
		}
		
		for (int index = 1001; index < 1018; index++) {
			for (Recomendacion recom : recomedacionesAnonimas) {
				if (recom.getId_clientes() == index) {
					recomendationsAnon[index - 1001][recom.getId_productso()] = recom.getCalificacion();
				}
			}
		}
		/**ERROR NUEMRO 6**/
		for (Recomendacion recom : recomendacionPersonal) {
			recomendationsCli[recom.getId_productso() - 1 ] = recom.getCalificacion();
		}
		
		
		for (int index = 0; index < 17; index++) {
			for (int iterator = 0; iterator < productos.size(); iterator++) {
				if ((productos.size() - 1) == iterator) {
					calificacionesAnon = calificacionesAnon + recomendationsAnon[index][iterator] + "\r\n";
				} else {
					calificacionesAnon = calificacionesAnon + recomendationsAnon[index][iterator] + ", ";
				}
			}
		}
		for (int index = 0; index < productos.size(); index++) {
			if ((productos.size() - 1) == index) {
				calificacionesCli = calificacionesCli + recomendationsCli[index] + "\r\n";
			} else {
				calificacionesCli = calificacionesCli + recomendationsCli[index] + ", ";
			}
		}
		recomendacionesGenerales = recomendacionesGenerales + productosDatos + calificacionesAnon;
		recomendacionesUsuario = recomendacionesUsuario + productosDatos + calificacionesCli;
		
		/*System.out.print(recomendacionesGenerales);
		System.out.print(recomendacionesUsuario);*/
		InputStream productosInput = new ByteArrayInputStream(recomendacionesGenerales.getBytes());
		InputStream usuarioInput = new ByteArrayInputStream(recomendacionesUsuario.getBytes());
		
		
		
		DataSource source = null;
		Instances dataset = null;
		try {
			// source = new DataSource("/Productos.arff");
			source = new DataSource(productosInput);
			dataset = source.getDataSet();
		} catch (Exception e1) {
			e1.printStackTrace();
			coincidencias.put("OK", false);
			coincidencias.put("Response", "no cogio el productos.arff");
			return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.BAD_REQUEST);
		}
		Instance userData = null;
		try {
			// source = new DataSource("/usuario.arff");
			source = new DataSource(usuarioInput);
			Instances userRating = source.getDataSet();
			userData = userRating.firstInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			coincidencias.put("OK", false);
			coincidencias.put("Response", "no cogio el usuario perra.arff");
			return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.BAD_REQUEST);
		}

		
		LinearNNSearch kNN = new LinearNNSearch(dataset);
		Instances neighbors = null;
		double[] distances = null;

		try {
			neighbors = kNN.kNearestNeighbours(userData, 3);
			distances = kNN.getDistances();
		} catch (Exception e) {
			coincidencias.put("OK", false);
			coincidencias.put("Response", e);
			return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.BAD_REQUEST);
		}

		double[] similarities = new double[distances.length];
		for (int i = 0; i < distances.length; i++) {
			similarities[i] = 1.0 / distances[i];
		}

		Map<String, List<Integer>> recommendations = new HashMap<String, List<Integer>>();
		for (int i = 0; i < neighbors.numInstances(); i++) {
			Instance currNeighbor = neighbors.instance(i);

			for (int j = 0; j < currNeighbor.numAttributes(); j++) {
				if (userData.value(j) < 1) {
					String attrName = userData.attribute(j).name();
					List<Integer> lst = new ArrayList<Integer>();
					if (recommendations.containsKey(attrName)) {
						lst = recommendations.get(attrName);
					}

					lst.add((int) currNeighbor.value(j));
					recommendations.put(attrName, lst);
				}
			}
		}

		
		List<RecommendationRecord> finalRanks = new ArrayList<RecommendationRecord>();

		Iterator<String> it = recommendations.keySet().iterator();

		while (it.hasNext()) {
			String atrName = it.next();
			double totalImpact = 0;
			double weightedSum = 0;
			List<Integer> ranks = recommendations.get(atrName);
			for (int i = 0; i < ranks.size(); i++) {
				int val = ranks.get(i);
				totalImpact += similarities[i];
				weightedSum += (double) similarities[i] * val;
			}

			RecommendationRecord rec = new RecommendationRecord();
			rec.setAttributeName(atrName);
			rec.setScore(weightedSum / totalImpact);

			finalRanks.add(rec);
		}
		Collections.sort(finalRanks);
		coincidencias.put("OK", true);
		List<Map<String, Object>> productosMap = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 3; i++) {
			Map<String, Object> prod = new HashMap();
			prod.put("producto", finalRanks.get(i).getAttributeName());
			prod.put("calificacion", finalRanks.get(i).getScore());
			productosMap.add(prod);
			//System.out.println(productos);
		}
		coincidencias.put("response", productosMap);

		return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.OK);
		
		
	}

	@GetMapping("/findRecomendations")
	public ResponseEntity<?> findRecomendations() {
		Map<String, Object> coincidencias = new HashMap();
		String producto = "@relation productos\r\n" + "\r\n" + "@attribute 'Lavadora' NUMERIC\r\n"
				+ "@attribute 'Cocina' NUMERIC\r\n" + "@attribute 'Horno' NUMERIC\r\n"
				+ "@attribute 'Horno Microondas' NUMERIC\r\n" + "@attribute 'Aire Acondicionado' NUMERIC\r\n"
				+ "@attribute 'Calefactor' NUMERIC\r\n" + "@attribute 'Lamparas' NUMERIC\r\n"
				+ "@attribute 'Luminarias' NUMERIC\r\n" + "@attribute 'Tv 50' NUMERIC\r\n"
				+ "@attribute 'Tv 60' NUMERIC\r\n" + "@attribute 'Equipo de Sonido' NUMERIC\r\n"
				+ "@attribute 'Equipo Dj' NUMERIC\r\n" + "@attribute 'Bombillas de luz' NUMERIC\r\n"
				+ "@attribute 'Planchas' NUMERIC\r\n" + "@attribute 'Tostadoras' NUMERIC\r\n"
				+ "@attribute 'Freidoras' NUMERIC\r\n" + "@attribute 'Copiadoras' NUMERIC\r\n"
				+ "@attribute 'Maquina de Coser' NUMERIC\r\n" + "@attribute 'Maquina de escribir' NUMERIC\r\n"
				+ "@attribute 'Computadora' NUMERIC\r\n" + "@attribute 'Laptop' NUMERIC\r\n"
				+ "@attribute 'Hervidor' NUMERIC\r\n" + "@attribute 'Filtrador de Agua' NUMERIC\r\n"
				+ "@attribute 'Sillas Giratorias' NUMERIC\r\n" + "@attribute 'Cuadros Decorativos' NUMERIC\r\n"
				+ "@attribute 'Muebles' NUMERIC\r\n" + "@attribute 'Mesa de centro' NUMERIC\r\n"
				+ "@attribute 'Mesa decorativa' NUMERIC\r\n" + "@attribute 'Campana de Cocina' NUMERIC\r\n"
				+ "@attribute 'Lavavajillas' NUMERIC\r\n" + "@attribute 'Play Station 4' NUMERIC\r\n"
				+ "@attribute 'Xbox' NUMERIC\r\n" + "@attribute 'Nintendo Switch' NUMERIC\r\n"
				+ "@attribute 'Play Station 5' NUMERIC\r\n" + "@attribute 'Blue Ray' NUMERIC\r\n"
				+ "@attribute 'MiniComponente' NUMERIC\r\n" + "@attribute 'DVD' NUMERIC\r\n"
				+ "@attribute 'Lector de Vinilos' NUMERIC\r\n" + "@attribute 'Ventilador' NUMERIC\r\n"
				+ "@attribute 'Camara Web' NUMERIC\r\n" + "@attribute 'Camara Digital' NUMERIC\r\n"
				+ "@attribute 'Celular Samsung' NUMERIC\r\n" + "@attribute 'Celular Iphone X' NUMERIC\r\n"
				+ "@attribute 'Celular Huawei' NUMERIC\r\n" + "@attribute 'Celular Alcatel' NUMERIC\r\n"
				+ "@attribute 'Celular Nokia' NUMERIC\r\n" + "@attribute 'Celular Iphone SE' NUMERIC\r\n"
				+ "@attribute 'Celular Iphone 8' NUMERIC\r\n" + "@attribute 'Celular Iphone 11' NUMERIC\r\n"
				+ "@attribute 'Celular Iphone 12' NUMERIC\r\n" + "@attribute 'Celular Iphone 7' NUMERIC\r\n"
				+ "@attribute 'Celular Mobile' NUMERIC\r\n" + "@attribute 'Celular HTC' NUMERIC\r\n"
				+ "@attribute 'Celular ZTE' NUMERIC\r\n" + "@attribute 'Celular ASUS' NUMERIC\r\n"
				+ "@attribute 'Celular OnePlus' NUMERIC\r\n" + "@attribute 'Celular LG' NUMERIC\r\n"
				+ "@attribute 'Celular CAT' NUMERIC\r\n" + "@attribute 'Celular Yezz' NUMERIC\r\n"
				+ "@attribute 'Celular Vodafone' NUMERIC\r\n" + "@attribute 'Alfombra Star' NUMERIC\r\n"
				+ "@attribute 'Alfombra Circular' NUMERIC\r\n" + "@attribute 'Alfrombra Cuadrada' NUMERIC\r\n"
				+ "@attribute 'Alfombra Decorativa color Mate' NUMERIC\r\n" + "@attribute 'Ollas' NUMERIC\r\n"
				+ "@attribute 'Airpods' NUMERIC\r\n" + "@attribute 'IPad' NUMERIC\r\n"
				+ "@attribute 'PlayStation Vite' NUMERIC\r\n" + "@attribute 'Apple Tv' NUMERIC\r\n"
				+ "@attribute 'iPod Touch' NUMERIC\r\n" + "@attribute 'Nintendo Wii' NUMERIC\r\n"
				+ "@attribute 'Sony PlayStation 3' NUMERIC\r\n" + "@attribute 'Mac' NUMERIC\r\n"
				+ "@attribute 'Extractor' NUMERIC\r\n" + "@attribute 'Aspirador' NUMERIC\r\n"
				+ "@attribute 'Cafetera' NUMERIC\r\n" + "@attribute 'Escritorio' NUMERIC\r\n"
				+ "@attribute 'Reloj de Pared' NUMERIC\r\n" + "@attribute 'Sofa¡ Cama' NUMERIC\r\n"
				+ "@attribute 'Estanteria' NUMERIC\r\n" + "@attribute 'Frigorifico' NUMERIC\r\n"
				+ "@attribute 'Batidora' NUMERIC\r\n" + "@attribute 'Impresora HP' NUMERIC\r\n"
				+ "@attribute 'Impresora Brother' NUMERIC\r\n" + "@attribute 'Juego de Vasos' NUMERIC\r\n"
				+ "@attribute 'Refrigerador dos puertas' NUMERIC\r\n"
				+ "@attribute 'Refrigerador una puerta' NUMERIC\r\n" + "\r\n" + "@data\r\n"
				+ "8, 5, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "6, 0, 0, 0, 0, 0, 9, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "7, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "9, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "2, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "9, 9, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "10, 6, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "3, 7, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0\r\n"
				+ "5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0";
		InputStream productosInput = new ByteArrayInputStream(producto.getBytes());

		String usuario = "@relation usuario\r\n" + "\r\n" + "@attribute 'Lavadora' NUMERIC\r\n"
				+ "@attribute 'Cocina' NUMERIC\r\n" + "@attribute 'Horno' NUMERIC\r\n"
				+ "@attribute 'Horno Microondas' NUMERIC\r\n" + "@attribute 'Aire Acondicionado' NUMERIC\r\n"
				+ "@attribute 'Calefactor' NUMERIC\r\n" + "@attribute 'Lámparas' NUMERIC\r\n"
				+ "@attribute 'Luminarias' NUMERIC\r\n" + "@attribute 'Tv 50' NUMERIC\r\n"
				+ "@attribute 'Tv 60' NUMERIC\r\n" + "@attribute 'Equipo de Sonido' NUMERIC\r\n"
				+ "@attribute 'Equipo Dj' NUMERIC\r\n" + "@attribute 'Bombillas de luz' NUMERIC\r\n"
				+ "@attribute 'Planchas' NUMERIC\r\n" + "@attribute 'Tostadoras' NUMERIC\r\n"
				+ "@attribute 'Freidoras' NUMERIC\r\n" + "@attribute 'Copiadoras' NUMERIC\r\n"
				+ "@attribute 'Máquina de Coser' NUMERIC\r\n" + "@attribute 'Máquina de escribir' NUMERIC\r\n"
				+ "@attribute 'Computadora' NUMERIC\r\n" + "@attribute 'Laptop' NUMERIC\r\n"
				+ "@attribute 'Hervidor' NUMERIC\r\n" + "@attribute 'Filtrador de Agua' NUMERIC\r\n"
				+ "@attribute 'Sillas Giratorias' NUMERIC\r\n" + "@attribute 'Cuadros Decorativos' NUMERIC\r\n"
				+ "@attribute 'Muebles' NUMERIC\r\n" + "@attribute 'Mesa de centro' NUMERIC\r\n"
				+ "@attribute 'Mesa decorativa' NUMERIC\r\n" + "@attribute 'Campana de Cocina' NUMERIC\r\n"
				+ "@attribute 'Lavavajillas' NUMERIC\r\n" + "@attribute 'Play Station 4' NUMERIC\r\n"
				+ "@attribute 'Xbox' NUMERIC\r\n" + "@attribute 'Nintendo Switch' NUMERIC\r\n"
				+ "@attribute 'Play Station 5' NUMERIC\r\n" + "@attribute 'Blue Ray' NUMERIC\r\n"
				+ "@attribute 'MiniComponente' NUMERIC\r\n" + "@attribute 'DVD' NUMERIC\r\n"
				+ "@attribute 'Lector de Vinilos' NUMERIC\r\n" + "@attribute 'Ventilador' NUMERIC\r\n"
				+ "@attribute 'Cámara Web' NUMERIC\r\n" + "@attribute 'Cámara Digital' NUMERIC\r\n"
				+ "@attribute 'Celular Samsung' NUMERIC\r\n" + "@attribute 'Celular Iphone X' NUMERIC\r\n"
				+ "@attribute 'Celular Huawei' NUMERIC\r\n" + "@attribute 'Celular Alcatel' NUMERIC\r\n"
				+ "@attribute 'Celular Nokia' NUMERIC\r\n" + "@attribute 'Celular Iphone SE' NUMERIC\r\n"
				+ "@attribute 'Celular Iphone 8' NUMERIC\r\n" + "@attribute 'Celular Iphone 11' NUMERIC\r\n"
				+ "@attribute 'Celular Iphone 12' NUMERIC\r\n" + "@attribute 'Celular Iphone 7' NUMERIC\r\n"
				+ "@attribute 'Celular Mobile' NUMERIC\r\n" + "@attribute 'Celular HTC' NUMERIC\r\n"
				+ "@attribute 'Celular ZTE' NUMERIC\r\n" + "@attribute 'Celular ASUS' NUMERIC\r\n"
				+ "@attribute 'Celular OnePlus' NUMERIC\r\n" + "@attribute 'Celular LG' NUMERIC\r\n"
				+ "@attribute 'Celular CAT' NUMERIC\r\n" + "@attribute 'Celular Yezz' NUMERIC\r\n"
				+ "@attribute 'Celular Vodafone' NUMERIC\r\n" + "@attribute 'Alfombra Star' NUMERIC\r\n"
				+ "@attribute 'Alfombra Circular' NUMERIC\r\n" + "@attribute 'Alfrombra Cuadrada' NUMERIC\r\n"
				+ "@attribute 'Alfombra Decorativa color Mate' NUMERIC\r\n" + "@attribute 'Ollas' NUMERIC\r\n"
				+ "@attribute 'Airpods' NUMERIC\r\n" + "@attribute 'IPad' NUMERIC\r\n"
				+ "@attribute 'PlayStation Vite' NUMERIC\r\n" + "@attribute 'Apple Tv' NUMERIC\r\n"
				+ "@attribute 'iPod Touch' NUMERIC\r\n" + "@attribute 'Nintendo Wii' NUMERIC\r\n"
				+ "@attribute 'Sony PlayStation 3' NUMERIC\r\n" + "@attribute 'Mac' NUMERIC\r\n"
				+ "@attribute 'Extractor' NUMERIC\r\n" + "@attribute 'Aspirador' NUMERIC\r\n"
				+ "@attribute 'Cafetera' NUMERIC\r\n" + "@attribute 'Escritorio' NUMERIC\r\n"
				+ "@attribute 'Reloj de Pared' NUMERIC\r\n" + "@attribute 'Sofá Cama' NUMERIC\r\n"
				+ "@attribute 'Estantería' NUMERIC\r\n" + "@attribute 'Frigorífico' NUMERIC\r\n"
				+ "@attribute 'Batidora' NUMERIC\r\n" + "@attribute 'Impresora HP' NUMERIC\r\n"
				+ "@attribute 'Impresora Brother' NUMERIC\r\n" + "@attribute 'Juego de Vasos' NUMERIC\r\n"
				+ "@attribute 'Refrigerador dos puertas' NUMERIC\r\n"
				+ "@attribute 'Refrigerador una puerta' NUMERIC\r\n" + "\r\n" + "@data\r\n"
				+ "0, 0, 9, 8, 10, 10, 2, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0";
		InputStream usuarioInput = new ByteArrayInputStream(usuario.getBytes());

		/** OBTENEMOS LOS DATOS **/
		DataSource source = null;
		Instances dataset = null;
		try {
			// source = new DataSource("/Productos.arff");
			source = new DataSource(productosInput);
			dataset = source.getDataSet();
		} catch (Exception e1) {
			e1.printStackTrace();
			coincidencias.put("OK", false);
			coincidencias.put("Response", "no cogio el productos.arff");
			return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.BAD_REQUEST);
		}
		Instance userData = null;
		try {
			// source = new DataSource("/usuario.arff");
			source = new DataSource(usuarioInput);
			Instances userRating = source.getDataSet();
			userData = userRating.firstInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
			coincidencias.put("OK", false);
			coincidencias.put("Response", "no cogio el usuario perra.arff");
			return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.BAD_REQUEST);
		}

		/** PROCESAMIENTO **/
		LinearNNSearch kNN = new LinearNNSearch(dataset);
		Instances neighbors = null;
		double[] distances = null;

		try {
			neighbors = kNN.kNearestNeighbours(userData, 3);
			distances = kNN.getDistances();
		} catch (Exception e) {
			coincidencias.put("OK", false);
			coincidencias.put("Response", e);
			return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.BAD_REQUEST);
		}

		double[] similarities = new double[distances.length];
		for (int i = 0; i < distances.length; i++) {
			similarities[i] = 1.0 / distances[i];
		}

		Map<String, List<Integer>> recommendations = new HashMap<String, List<Integer>>();
		for (int i = 0; i < neighbors.numInstances(); i++) {
			Instance currNeighbor = neighbors.instance(i);

			for (int j = 0; j < currNeighbor.numAttributes(); j++) {
				if (userData.value(j) < 1) {
					String attrName = userData.attribute(j).name();
					List<Integer> lst = new ArrayList<Integer>();
					if (recommendations.containsKey(attrName)) {
						lst = recommendations.get(attrName);
					}

					lst.add((int) currNeighbor.value(j));
					recommendations.put(attrName, lst);
				}
			}
		}

		/** MOSTRAR **/
		List<RecommendationRecord> finalRanks = new ArrayList<RecommendationRecord>();

		Iterator<String> it = recommendations.keySet().iterator();

		while (it.hasNext()) {
			String atrName = it.next();
			double totalImpact = 0;
			double weightedSum = 0;
			List<Integer> ranks = recommendations.get(atrName);
			for (int i = 0; i < ranks.size(); i++) {
				int val = ranks.get(i);
				totalImpact += similarities[i];
				weightedSum += (double) similarities[i] * val;
			}

			RecommendationRecord rec = new RecommendationRecord();
			rec.setAttributeName(atrName);
			rec.setScore(weightedSum / totalImpact);

			finalRanks.add(rec);
		}
		Collections.sort(finalRanks);
		coincidencias.put("OK", true);
		List<Map<String, Object>> productos = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 3; i++) {
			Map<String, Object> prod = new HashMap();
			prod.put("producto", finalRanks.get(i).getAttributeName());
			prod.put("calificacion", finalRanks.get(i).getScore());
			productos.add(prod);
			System.out.println(productos);
		}
		coincidencias.put("response", productos);

		return new ResponseEntity<Map<String, Object>>(coincidencias, HttpStatus.OK);
	}

}
