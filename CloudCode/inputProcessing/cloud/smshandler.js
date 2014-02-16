
Parse.Cloud.define("smsmap", function(request, response){
	console.log(request.params.number);
	console.log('got sms');
	response.success("14349960643");
});
