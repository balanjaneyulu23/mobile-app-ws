package com.appdevloperblog.app.ws.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdevloperblog.app.ws.APPConstants;
import com.appdevloperblog.app.ws.exceptions.UserServiceException;
import com.appdevloperblog.app.ws.service.AddressService;
import com.appdevloperblog.app.ws.service.UserService;
import com.appdevloperblog.app.ws.shared.Roles;
import com.appdevloperblog.app.ws.shared.dto.AddressDTO;
import com.appdevloperblog.app.ws.shared.dto.UserDto;
import com.appdevloperblog.app.ws.ui.model.request.PasswordResetModel;
import com.appdevloperblog.app.ws.ui.model.request.PasswordResetRequestModel;
import com.appdevloperblog.app.ws.ui.model.request.UserDetailsRequestModel;
import com.appdevloperblog.app.ws.ui.model.response.AddressesRest;
import com.appdevloperblog.app.ws.ui.model.response.ErrorMessages;
import com.appdevloperblog.app.ws.ui.model.response.OperationStatusModel;
import com.appdevloperblog.app.ws.ui.model.response.RequestOperationName;
import com.appdevloperblog.app.ws.ui.model.response.RequestOperationStatus;
import com.appdevloperblog.app.ws.ui.model.response.UserRest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = APPConstants.API_REQUEST_MAPPING_USERS)
//@CrossOrigin(origins= {"http://localhost:8083", "http://localhost:8084"})
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;

	@ApiOperation(value="The Get User Details Web Service Endpoint",
			notes="${userController.GetUser.ApiOperation.Notes}")
	
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
	})
	//@PostAuthorize("('ROLE_ADMIN') or returnObject.userId == principal.userId") this will allow to access returned object properties only admit user or logged in user others won't access it
	//this annotation will impose restrictions on returned object not method execution
	@GetMapping(path = "/{id}")
	public UserRest getUser(@PathVariable String id) {
		UserRest returnVal = new UserRest();
		UserDto userDto= userService.getUserByUserId(id);
		ModelMapper modelMapper = new ModelMapper();
		returnVal = modelMapper.map(userDto, UserRest.class);
		return returnVal;
	}

	@PostMapping
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws UserServiceException, Exception {
		System.out.println("createUser Controller is called!");

		if (!StringUtils.hasText(userDetails.getEmail())) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		if (!StringUtils.hasText(userDetails.getFirstName())) throw new NullPointerException("data should not be null");

		//BeanUtils.copyProperties(userDetails, userDto);
		ModelMapper mapper = new ModelMapper();
		UserDto userDto = mapper.map(userDetails, UserDto.class);
		userDto.setRoles(new HashSet<>(Arrays.asList(Roles.ROLE_USER.name())));

		UserDto createdUser = userService.createUser(userDto);
		//BeanUtils.copyProperties(createdUser, returnValue);

		ModelMapper map = new ModelMapper();
		UserRest returnValue = map.map(createdUser, UserRest.class);

		return returnValue;
	}
	
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
	})
	@PutMapping(path = "/{id}")
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		System.out.println("Update User method is being invoked!");
		UserRest returnValue = new UserRest();
		UserDto userDto = new UserDto();

		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updatedUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updatedUser, returnValue);

		return returnValue;
	}
	
	// PreAuthorize will validate before method execution and PostAuthorize will validate after method gets executed
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
	})
	//@Secured("DELETE_AUTHORITY") or @Secured("ROLE_ADMIN") users which is having delete authority can be allowed to access this method it's called method level security
	//@PreAuthorize("('ROLE_ADMIN') or #id == principal.userId") users which is having delete authority and path value id should match user Id from principle object can be allowed to access this method it's called method level security
	//@PostAuthorize() users which is having delete authority can be allowed to access this method it's called method level security but it will verify authorization after method
	//gets executed it means it will check the method return information is accessible to correct user or not
	@DeleteMapping(path = "/{id}")
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());
		userService.deleteUser(id);
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}

	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
	})
	@GetMapping()
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "25") int limit){
		if(page>0) page=page-1;

		List<UserRest> returnValue = new ArrayList<>();
		List<UserDto> users = userService.getUsers(page, limit);

		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}
		return returnValue;
	}

	//http://localhost:8080/mobile-app-ws/users/9VzBxUSSVAJMSXqNRqAzx12EFJRks3
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
	})
	@GetMapping(path = "/{id}/addresses")
	public CollectionModel<AddressesRest> getAddresses(@PathVariable String id) {
		List<AddressesRest> returnValue  =new ArrayList<>();
		List<AddressDTO> addressesDTO= addressService.getAddresses(id);

		if (Objects.nonNull(addressesDTO) && !addressesDTO.isEmpty()) {
			Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			returnValue = new ModelMapper().map(addressesDTO, listType);

			for (AddressesRest addressesRest : returnValue) {
				Link selfLink =WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddress(id,
						addressesRest.getAddressId()))
						.withSelfRel();
				addressesRest.add(selfLink);
			}
		}

		Link userLink =WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink =WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddresses(id))
				.withSelfRel();
		return CollectionModel.of(returnValue, userLink,selfLink);
	}

	//http://localhost:8080/mobile-app-ws/users/9VzBxUSSVAJMSXqNRqAzx12EFJRks3/addresses/hL32C7vxsuWm8emIh25itxceLFqhTQ
	@ApiImplicitParams({
		@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header")
	})
	@GetMapping(path = "/{userId}/addresses/{addressId}")
	public EntityModel<AddressesRest> getAddress(@PathVariable String userId, 
			@PathVariable String addressId) {
		AddressDTO address= addressService.getAddress(addressId);
		AddressesRest returnVal = new ModelMapper().map(address, AddressesRest.class);

		Link userLink =WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link userAddressesLink =WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddresses(userId))
				.withRel("addresses");
		Link selfLink =WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).getAddress(userId, addressId))
				.withSelfRel();

		return EntityModel.of(returnVal, Arrays.asList(userLink,userAddressesLink,selfLink));
	}

	//http://localhost:8080/mobile-app-ws/users/email-verification/token=miook
	@GetMapping(path = "/email-verification")
	public OperationStatusModel verifyEmailToken(@RequestParam(value = "token") String token) {
		OperationStatusModel returnVal = new OperationStatusModel();
		returnVal.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

		boolean isVerified = userService.verifyEmailToken(token);

		if (isVerified) {
			returnVal.setOperationResult(RequestOperationStatus.SUCCESS.name());
		} else {
			returnVal.setOperationResult(RequestOperationStatus.ERROR.name());
		}
		return returnVal;
	}

	// http://localhost:8080/mobile-app-ws/users/password-reset-request
	@PostMapping(path = "/password-reset-request")
	public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel)  {
		System.out.println("com.appdevloperblog.app.ws.ui.controller.UserController.requestReset(PasswordResetRequestModel) being invoked");
		OperationStatusModel returnValue = new OperationStatusModel();
		
		boolean OperationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
		
		returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		
		if (OperationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}

		return returnValue;
	}
	
	   // http://localhost:8080/mobile-app-ws/users/password-reset
		@PostMapping(path = "/password-reset")
		public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel)  {
			System.out.println("com.appdevloperblog.app.ws.ui.controller.UserController.resetPassword(PasswordResetRequestModel) being invoked");
			OperationStatusModel returnValue = new OperationStatusModel();
			
			boolean OperationResult = userService.resetPassword(passwordResetModel.getToken(),
					passwordResetModel.getPassword());
			
			returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
			
			if (OperationResult) {
				returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
			}

			return returnValue;
		}


}
