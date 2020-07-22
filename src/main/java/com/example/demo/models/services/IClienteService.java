package com.example.demo.models.services;

import java.util.List;

import com.example.demo.models.entity.Cliente;

public interface IClienteService {
	public List<Cliente> findAll();
	public Cliente findById(Long id);
	public Cliente save(Cliente cliente);
	public void delete(Long id);
	
	/*public List<Cliente> buscar(String nombre, String apellido);*/
}

