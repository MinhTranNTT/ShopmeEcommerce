package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import java.util.List;

import com.shopme.common.entity.Role;


@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class RoleRepositoryTest {

	@Autowired
	private RoleRepository repo;
	
	@Test
	public void testCreateFirstRole() {
		Role roleAdmin = new Role("Admin", "manager everything");
		Role savedRolde = repo.save(roleAdmin);
		
		assertThat(savedRolde.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateRestRoles() {
		
		Role roleSalesperson = new Role("Salesperson", "manage product price, " 
									   +"customers, shipping, oders and sales report.");
		Role roleEditor = new Role("Editor", "manage categories, brands, " 
				   +"products, articles and menu.");
		Role roleShipper = new Role("Shipper", "view products, view orders, " 
				   +"and update orders status.");
		Role roleAssistance = new Role("Assistance", "manage questions and reviews.");
		
		repo.saveAll(List.of(roleSalesperson, roleEditor, roleShipper, roleAssistance));
		
	}
}
