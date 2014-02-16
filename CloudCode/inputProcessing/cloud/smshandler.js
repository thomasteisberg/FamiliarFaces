Parse.Cloud.job("sms", function(request, status){
	var nexmo = require('./lib/nexmo');
	nexmo.initialize(064c1dc8,ic6a69221);
	nexmo.sendTextMessage(14844409626,14343220360,'testing',consolelog);
	function consolelog (err,messageResponse) {
		if (err) {
			console.log(err);
		} else {
			console.dir(messageResponse);
		}
	}
	nexmo.checkBalance(consolelog);
	nexmo.getPricing('US',consolelog);
	nexmo.getNumbers(consolelog);
	nexmo.searchNumbers('US',consolelog);
	nexmo.searchNumbers('US',303,consolelog);
	status.success();
});
