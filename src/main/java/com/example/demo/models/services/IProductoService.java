package com.example.demo.models.services;

import java.util.List;

import com.example.demo.models.entity.Producto;

public interface IProductoService {
	public List<Producto> findAll();
	public Producto findById(Long id);
	public Producto save(Producto cliente);
	public void delete(Long id);
}