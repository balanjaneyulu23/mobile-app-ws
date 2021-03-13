package com.appdevloperblog.app.ws.io.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.appdevloperblog.app.ws.io.entity.AddressEntity;
import com.appdevloperblog.app.ws.io.entity.UserEntity;

public interface AddressRepository extends CrudRepository<AddressEntity, Long> {
	List<AddressEntity> findAllByUserDetails(UserEntity userEntity);
	AddressEntity findByAddressId(String addressId);
}
