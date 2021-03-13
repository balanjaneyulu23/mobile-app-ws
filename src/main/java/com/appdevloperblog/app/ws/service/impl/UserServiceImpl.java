package com.appdevloperblog.app.ws.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.appdevloperblog.app.ws.exceptions.UserServiceException;
import com.appdevloperblog.app.ws.io.entity.PasswordResetTokenEntity;
import com.appdevloperblog.app.ws.io.entity.RoleEntity;
import com.appdevloperblog.app.ws.io.entity.UserEntity;
import com.appdevloperblog.app.ws.io.repository.PasswordResetTokenRepository;
import com.appdevloperblog.app.ws.io.repository.RoleRepository;
import com.appdevloperblog.app.ws.io.repository.UserRepository;
import com.appdevloperblog.app.ws.security.UserPrincipal;
import com.appdevloperblog.app.ws.service.UserService;
import com.appdevloperblog.app.ws.shared.AmazonSES;
import com.appdevloperblog.app.ws.shared.Utils;
import com.appdevloperblog.app.ws.shared.dto.AddressDTO;
import com.appdevloperblog.app.ws.shared.dto.UserDto;
import com.appdevloperblog.app.ws.ui.model.response.ErrorMessages;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	AmazonSES amazonSES;

	@Autowired
	RoleRepository roleRepository;

	@Override
	public UserDto createUser(UserDto userDto) {
		if (Objects.nonNull(userRepository.findByEmail(userDto.getEmail()))) throw new UserServiceException("Record already exist!");

		for (AddressDTO address : userDto.getAddresses()) {
			address.setUserDetails(userDto);
			address.setAddressId(utils.generateAddressId(30));
		}

		//BeanUtils.copyProperties(userDto, userEntity);
		ModelMapper mapper = new ModelMapper();
		UserEntity userEntity  = mapper.map(userDto, UserEntity.class);

		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		String userId=utils.generateUserId(30);
		userEntity.setUserId(userId);
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(userId));
		userEntity.setEmailVerificationStatus(false);

		//set roles
		Collection<RoleEntity> roleEntities = new HashSet<>();
		for (String role : userDto.getRoles()) {

			RoleEntity roleEntity = roleRepository.findByName(role);
			if (Objects.nonNull(roleEntity)) {
				roleEntities.add(roleEntity);
			}
		}

		userEntity.setRoles(roleEntities);

		UserEntity storedUserDetails = userRepository.save(userEntity);
		//BeanUtils.copyProperties(storedUserDetails, returnValue);
		ModelMapper map = new ModelMapper();
		UserDto returnValue  = map.map(storedUserDetails, UserDto.class);

		//Send email message to user to verify their email address
		//amazonSES.verifyEmail(returnValue);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);

		if (Objects.isNull(userEntity)) throw new UsernameNotFoundException(email);

		return new UserPrincipal(userEntity);

	}

	@Override
	public UserDto getUser(String email) {
		UserEntity userEntity = userRepository.findByEmail(email);

		if (Objects.isNull(userEntity)) throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (Objects.isNull(userEntity)) throw new UsernameNotFoundException("User with ID: "+userId+" not found");

		//BeanUtils.copyProperties(userEntity, returnValue);
		ModelMapper map = new ModelMapper();
		UserDto returnValue   = map.map(userEntity, UserDto.class);
		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto userDto) {
		UserDto returnValue = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (Objects.isNull(userEntity)) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(userDto.getFirstName());
		userEntity.setLastName(userDto.getLastName());

		UserEntity updatedUserDetails = userRepository.save(userEntity);

		BeanUtils.copyProperties(updatedUserDetails, returnValue);
		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (Objects.isNull(userEntity)) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		System.out.println("getUsers method is invoked");
		List<UserDto> returnValue = new ArrayList<>();

		Pageable pageable = PageRequest.of(page, limit);
		Page<UserEntity> usersPage =userRepository.findAll(pageable);
		List<UserEntity> users = usersPage.getContent();

		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}

		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		boolean returnValue = false;

		//find user by token
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		if (!Objects.isNull(userEntity)) {
			boolean hasTokenExpired = Utils.hasTokenExpired(token);
			if (!hasTokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}
		return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		boolean returnValue = false;

		UserEntity userEntity = userRepository.findByEmail(email);

		if (Objects.isNull(userEntity)) {
			return returnValue;
		}

		String token = new Utils().generatePasswordResetToken(userEntity.getUserId());

		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetTokenEntity);

		returnValue = new AmazonSES()
				.sendPasswordResetRequest(userEntity.getFirstName(), userEntity.getEmail(), token);

		return returnValue;
	}

	@Override
	public boolean resetPassword(String token, String password) {
		boolean returnValue =false;

		if (Utils.hasTokenExpired(token)) {
			return returnValue;	
		}

		PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
		if (Objects.isNull(passwordResetTokenEntity)) {
			return returnValue;
		}

		//prepare new password
		String encodedPassword = bCryptPasswordEncoder.encode(password);

		//update User password in database
		UserEntity userEntity =passwordResetTokenEntity.getUserDetails();
		userEntity.setEncryptedPassword(encodedPassword);
		UserEntity savedUserEntity = userRepository.save(userEntity);

		//verify if password was saved successfully
		if (Objects.nonNull(savedUserEntity) && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
			returnValue = true;	
		}

		//Remove password reset token from database
		passwordResetTokenRepository.delete(passwordResetTokenEntity);

		return returnValue;
	}
}
