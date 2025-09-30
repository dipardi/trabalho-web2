package com.web2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web2.model.Professor;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Integer>{
	List<Professor> findProfessorByNomeLike(String nome);
}
