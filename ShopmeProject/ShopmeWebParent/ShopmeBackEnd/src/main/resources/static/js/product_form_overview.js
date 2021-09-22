dropdownBrands = $("#brand");
dropdownCategories = $("#category");

$(document).ready(function() {
	
	$("#shortDescription").richText();	// product_description
	$("#fullDescription").richText();
	
	dropdownBrands.change(function() {
		dropdownCategories.empty();
		getCategories();
	});	
	
	getCategoriesForNewForm();
	
});

function getCategories() {
	brandId = dropdownBrands.val(); 
	url = brandModuleURL + "/" + brandId + "/categories";
	
	$.get(url, function(responseJson) {
		$.each(responseJson, function(index, category) {
			$("<option>").val(category.id).text(category.name).appendTo(dropdownCategories);
		});			
	});
};

function getCategoriesForNewForm() {
	catIdField = $("#categoryId");
	editMode = false;
	
	if (catIdField.length) {
		editMode = true;
	}
	
	if (!editMode) getCategories();
}

function checkUnique(form) {
	productId = $("#id").val();
	productName = $("#name").val();
	
	csrfValue = $("input[name='_csrf']").val();
	
	//url = "[[@{/products/check_unique}]]";
	
	params = {id: productId, name: productName, _csrf: csrfValue};
	
	$.post(checkUniqueUrl, params, function(response) {
		if (response == "OK") {
			form.submit();
		} else if (response == "Duplicate") {
			showWarningModal("There is another product having the name " + productName);	
		} else {
			showErrorModal("Unknown response from server");
		}
		
	}).fail(function() {
		showErrorModal("Could not connect to the server");
	});
	
	return false;
};