
Parse.Cloud.define("smsrx", function(request, response){
	console.log('got sms');
	var jsonObject = {
    		"code": 200
  	};
	response.success(jsonObject);
});
