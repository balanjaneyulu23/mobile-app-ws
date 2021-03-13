package com.appdevloperblog.app.ws.io.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.appdevloperblog.app.ws.io.entity.AuthorityEntity;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
	AuthorityEntity findByName(String name);
}
