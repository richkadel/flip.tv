function loadProgram(programId) {
	YAHOO.util.Connect.asyncRequest('GET', contextPath + '/search/products.action?programIds=' + 
		programId, { success: updateProgramProducts });	
}

function loadProducts(programIds, keywords) {
	YAHOO.util.Connect.asyncRequest('GET', contextPath + '/search/products.action?keywords=' + 
		escape(keywords), { success: updateProductAds });
	YAHOO.util.Connect.asyncRequest('GET', contextPath + '/search/products.action?programIds=' + 
		programIds, { success: updateProgramProducts });
}

function productFailure(o) {
	//alert("product load failed " + o.statusText);
}

function updateProductAds(o) {
	if (o.responseText != null) {
		var featuredNode = document.getElementById("amazonAdSection");
		if (featuredNode != null) {
			featuredNode.innerHTML = o.responseText;
			checkAdOverlap();
		}
	}
}

function updateProgramProducts(o) {
	if (o.responseText != null) {
		var items = document.createElement("div");
		items.innerHTML = o.responseText;
		
		for (i=0; i < items.childNodes.length; i++) {
			var item = items.childNodes[i];
			if (item.nodeType == 1) {
				var programId = item.getAttribute("programId");
				var programNode = document.getElementById(programId);
				if (programNode != null) {
					var arr = new Array();
					var size = item.childNodes.length;
					for (j=0; j < size; j++) {
						arr[j] = item.childNodes[0];
						item.removeChild(item.childNodes[0]);
					}
					for (j=0; j < arr.length; j++) {
						if (arr[j]) {
							programNode.appendChild(arr[j]);
						}
					}
				}
			}
		}
	}
}