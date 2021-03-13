package com.appdevloperblog.app.ws;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.appdevloperblog.app.ws.io.entity.AuthorityEntity;
import com.appdevloperblog.app.ws.io.entity.RoleEntity;
import com.appdevloperblog.app.ws.io.entity.UserEntity;
import com.appdevloperblog.app.ws.io.repository.AuthorityRepository;
import com.appdevloperblog.app.ws.io.repository.RoleRepository;
import com.appdevloperblog.app.ws.io.repository.UserRepository;
import com.appdevloperblog.app.ws.shared.Roles;
import com.appdevloperblog.app.ws.shared.Utils;

@Component
public class InitialUsersSetup {

	@Autowired
	AuthorityRepository authorityRepository;

	@Autowired
	RoleRepository roleRepository; 
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	UserRepository userRepository;

	@EventListener
	@Transactional
	public void onApplicationEvent(ApplicationReadyEvent event) {
		System.out.println("From application ready event..");

		AuthorityEntity readAuthority = createAuthority("READ_AUTHORITY");
		AuthorityEntity writeAuthority = createAuthority("WRITE_AUTHORITY");
		AuthorityEntity deleteAuthority = createAuthority("DELETE_AUTHORITY");

		createRole(Roles.ROLE_USER.name(), Arrays.asList(readAuthority, writeAuthority));
		RoleEntity roleAdmin = createRole(Roles.ROLES_ADMIN.name(), Arrays.asList(readAuthority, writeAuthority,deleteAuthority));
		
		if(roleAdmin ==null) return;
		
		UserEntity adminUser = new UserEntity();
		adminUser.setFirstName("LeelaBala");
		adminUser.setLastName("pandrangi");
		adminUser.setEmail("balanjaneyulu25@gmail.com");
		adminUser.setEmailVerificationStatus(true);
		adminUser.setUserId(utils.generateUserId(30));
		adminUser.setEncryptedPassword(bCryptPasswordEncoder.encode("12345678"));
		adminUser.setRoles(Arrays.asList(roleAdmin));
		
		userRepository.save(adminUser);
		

	}

	@Transactional
	private AuthorityEntity createAuthority(String name) {
		AuthorityEntity authority = authorityRepository.findByName(name);

		if (!Objects.nonNull(authority)) {
			authority = new AuthorityEntity(name);
			authorityRepository.save(authority);
		}

		return authority;

	}

	@Transactional
	private RoleEntity createRole(String name,
			Collection<AuthorityEntity> authorities) {
		RoleEntity role = roleRepository.findByName(name);

		if (!Objects.nonNull(role)) {
			role = new RoleEntity(name);
			role.setAuthorities(authorities);
			roleRepository.save(role);
		}

		return role;

	}

}
