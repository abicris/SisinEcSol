package com.example.demo.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.entity.Recomendacion;

public interface IRecomendacionDao extends CrudRepository<Recomendacion, Long>{

	@Query(value="select * from recomendaciones where id_clientes = :id_clientes", nativeQuery=true)
	public List<Recomendacion> buscar(int id_clientes);
}

