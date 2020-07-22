package com.example.demo.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.models.dao.IRecomendacionDao;
import com.example.demo.models.entity.Recomendacion;

@Service
public class RecomendacionServiceImpl implements IRecomendacionService {

	@Autowired
	private IRecomendacionDao recomendacionDao;
	
	@Override
	@Transactional(readOnly=true)
	public List<Recomendacion> findAll() {
		return (List<Recomendacion>) recomendacionDao.findAll();
	}

	@Override
	@Transactional(readOnly=true)
	public Recomendacion findById(Long id) {
		return recomendacionDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public Recomendacion save(Recomendacion recomendacion) {
		return recomendacionDao.save(recomendacion);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		recomendacionDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Recomendacion> buscar(int id_clientes) {
		return recomendacionDao.buscar(id_clientes);
	}
}
