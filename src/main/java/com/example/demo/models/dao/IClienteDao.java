package com.example.demo.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.entity.Cliente;

public interface IClienteDao extends CrudRepository<Cliente, Long> {

	/*@Query(value="select * from clientes where nombre = :nombre and apellido = :apellido", nativeQuery=true)
	public List<Cliente> buscar(String nombre, String apellido);*/
}