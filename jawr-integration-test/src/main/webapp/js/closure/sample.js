function showTotal(subtotal, shipping) {
	var total = calculateTotal(subtotal, shipping);
	showAmount(total);
}
 
function calculateTotal(total, shipping) {
	return total+shipping;
}
 
function showAmount(total) {
	$("#amount").text("$ "+total);
}

function initAmount() {
	
	showTotal(120, 30);
}

window['initAmount'] = initAmount;

jQuery.fn.data = function(name) {
	var first = $(this).eq(0);
	if (first) {
		return first.attr('data-'+name);
	}
};