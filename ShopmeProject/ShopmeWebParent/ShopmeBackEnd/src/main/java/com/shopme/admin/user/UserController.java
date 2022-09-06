//package com.shopme.admin.user;
//
//import java.io.IOException;
//import java.util.List;
//
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import com.shopme.admin.FileUploadUtil;
//import com.shopme.common.entity.Role;
//import com.shopme.common.entity.User;
//
//
//@Controller
//public class UserController {
//
//	@Autowired
//	private UserService userService;
//	
//	@GetMapping("/users")
//	public String listFirstPage(Model model) {
//		return listByPage(1, model, "firstName", "asc", null);
//	}
//	
//	@GetMapping("/users/page/{pageNumber}")
//	public String listByPage(
//			@PathVariable(name = "pageNumber") int pageNumber, Model model,
//			@Param("sortField") String sortField, @Param("sortDir") String sortDir,
//			@Param("keyword") String keyword) {
//		
//		System.out.println("Sort Field: " + sortField);
//		System.out.println("Sort Dir: " + sortDir);
//		
//		Page<User> page = userService.listByPage(pageNumber, sortField, sortDir, keyword);
//		List<User> listUsers = page.getContent();
//		
//		long startCount = (pageNumber - 1) * UserService.USER_PER_PAGE + 1;
//		long endCount = startCount + UserService.USER_PER_PAGE - 1;
//		if (endCount > page.getTotalElements()) {
//			endCount = page.getTotalElements();
//		}
//		
//		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
//		
////		System.out.println("start count: " + startCount);
////		System.out.println("end count: " + endCount);
////		System.out.println("gettotalElement: " + page.getTotalElements());
////		System.out.println("Page Number: " + page.getNumber());
////		System.out.println("Page Total Element: " + page.getTotalElements());
////		System.out.println("Page total page: " + page.getTotalPages());
//		
//		model.addAttribute("currentPage", pageNumber);
//		model.addAttribute("totalPages", page.getTotalPages());
//		model.addAttribute("startCount", startCount);
//		model.addAttribute("endCount", endCount);
//		model.addAttribute("totalItems", page.getTotalElements());
//		model.addAttribute("listUsers", listUsers);
//		model.addAttribute("sortField", sortField);
//		model.addAttribute("sortDir", sortDir);
//		model.addAttribute("reverseSortDir", reverseSortDir);
//		model.addAttribute("keyword", keyword);
//		
//		return "users";
//		
//	}
//	
//	@GetMapping("/users/new")
//	public String newUser(Model model) {
//		List<Role> listRoles = userService.listRoles();
//		User user = new User();
//		
//		user.setEnabled(true);
//		model.addAttribute("user", user);
//		model.addAttribute("listRoles", listRoles);
//		model.addAttribute("pageTitle", "Create New User");
//		return "user_form";
//	}
//	
//	@PostMapping("users/save")
//	public String saveUser(User user, RedirectAttributes redirectAttributes, @RequestParam("image") MultipartFile multipartFile) throws IOException {
//
//		if (!multipartFile.isEmpty()) {
//			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
//			user.setPhotos(fileName);
//			
//			User savedUser = userService.save(user);
//			
//			String uploadDir = "user-photos/" + savedUser.getId();
//			
//			FileUploadUtil.cleanDir(uploadDir);
//			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
//		} else {
//			if (user.getPhotos().isEmpty()) {
//				user.setPhotos(null);
//			}
//			userService.save(user);
//		}
//		
//		redirectAttributes.addFlashAttribute("message", "The user has been save successfully.");
//		return getRedirectURLtoAffectedUser(user);
//	}
//	
//	private String getRedirectURLtoAffectedUser(User user) {
//		String firstPartOfEmail = user.getEmail().split("@")[0];
//		return "redirect:/users/page/1?sortField=id&sortDir=asc&keyword=" + firstPartOfEmail;
//	}
//	
//	@GetMapping("/users/export/csv")
//	public void exportToCSV(HttpServletResponse response) throws IOException {
//		List<User> listUsers = userService.listAll();
//		UserCsvExporter exporter = new UserCsvExporter();
//		exporter.export(listUsers, response);
//	}
//	
//	@GetMapping("/users/export/excel")
//	public void exportToExcel(HttpServletResponse response) throws IOException {
//		List<User> listUsers = userService.listAll();
//		
//		UserExcelExporter exporter = new UserExcelExporter(); 
//		exporter.export(listUsers, response);
//	}
//	
//	@GetMapping("/users/export/pdf")
//	public void exportToPDF(HttpServletResponse response) throws IOException {
//		List<User> listUsers = userService.listAll();
//		
//		UserPDFExporter exporter = new UserPDFExporter(); 
//		exporter.export(listUsers, response);
//	}
//	
//	@GetMapping("/users/edit/{id}")
//	public String editUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
//		try {
//			List<Role> listRoles = userService.listRoles();
//			User user =  userService.get(id);
//			model.addAttribute("user", user);
//			model.addAttribute("pageTitle", "Update User with (ID: " + id + " )");
//			model.addAttribute("listRoles", listRoles);
//			return "user_form";
//		} catch (UserNotFoundException ex) {
//			redirectAttributes.addFlashAttribute("message", ex.getMessage());
//			return "redirect:/users";
//		}	
//	}
//	
//	@GetMapping("/users/delete/{id}")
//	public String deleteUser(@PathVariable(name = "id") Integer id, Model model, RedirectAttributes redirectAttributes) {
//		try {
//			userService.delete(id);
//			redirectAttributes.addFlashAttribute("message", "The User ID " + id + " has been deleted successfully");
//		} catch (UserNotFoundException ex) {
//			redirectAttributes.addFlashAttribute("message", ex.getMessage());
//		}
//		return "redirect:/users";
//	}
//	
//	@GetMapping("/users/{id}/enabled/{status}")
//	public String updateUserEnabledStatus(@PathVariable(name = "id") Integer id, @PathVariable(name = "status") boolean enabled, 
//			RedirectAttributes redirectAttributes) {
//		userService.updateUserEnableStatus(id, enabled);
//		String status = enabled ? "enabled" : "disabled";
//		String message = "The User ID " + id + " has been " + status;
//		redirectAttributes.addFlashAttribute("message", message);
//		return "redirect:/users";
//	}
//}
