package com.appdevloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdevloperblog.app.ws.io.entity.AddressEntity;
import com.appdevloperblog.app.ws.io.entity.UserEntity;
import com.appdevloperblog.app.ws.io.repository.AddressRepository;
import com.appdevloperblog.app.ws.io.repository.UserRepository;
import com.appdevloperblog.app.ws.service.AddressService;
import com.appdevloperblog.app.ws.shared.dto.AddressDTO;

@Service
public class AddressServiceImpl implements AddressService{

	@Autowired
	UserRepository userRepository;

	@Autowired
	AddressRepository addressRepository;

	@Override
	public List<AddressDTO> getAddresses(String userId) {
		List<AddressDTO> returnValue = new ArrayList<>();
		ModelMapper modelMapper = new ModelMapper();

		UserEntity userEntity = userRepository.findByUserId(userId);
		if(Objects.isNull(userEntity)) return returnValue;

		Iterable<AddressEntity> addressess = addressRepository.findAllByUserDetails(userEntity);
		for (AddressEntity addressEntity : addressess) {
			returnValue.add(modelMapper.map(addressEntity, AddressDTO.class));
		}

		return returnValue;
	}

	@Override
	public AddressDTO getAddress(String addressId) {
		AddressDTO returnValue = null;

		AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
		if (Objects.nonNull(addressEntity)) {
			returnValue = new ModelMapper().map(addressEntity, AddressDTO.class);
		}

		return returnValue;
	}

}
