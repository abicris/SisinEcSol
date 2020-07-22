package com.example.demo.models.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="recomendaciones")
public class Recomendacion implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="id_productos")
	private int id_productso;

	@Column(name="id_clientes")
	private int id_clientes;
	
	@Column(name="calificacion")
	private int calificacion;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getId_productso() {
		return id_productso;
	}

	public void setId_productso(int id_productso) {
		this.id_productso = id_productso;
	}

	public int getId_clientes() {
		return id_clientes;
	}

	public void setId_clientes(int id_clientes) {
		this.id_clientes = id_clientes;
	}

	public int getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(int calificacion) {
		this.calificacion = calificacion;
	}

}