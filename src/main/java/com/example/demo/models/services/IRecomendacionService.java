package com.example.demo.models.services;

import java.util.List;

import com.example.demo.models.entity.Recomendacion;

public interface IRecomendacionService {

	public List<Recomendacion> findAll();
	public Recomendacion findById(Long id);
	public Recomendacion save(Recomendacion cliente);
	public void delete(Long id);
	
	public List<Recomendacion> buscar(int id_clientes);
}
