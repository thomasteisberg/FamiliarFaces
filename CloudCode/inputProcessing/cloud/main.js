
var bucketSize = .00005;

var buckets = {};

Parse.Cloud.define("storeLocation", function(request, response){

   // var userId = request.user.id; // or could user Parse.User.current().id?
    var userId = request.params.userId.toString();
    var posLat = parseFloat(request.params.posLat);
    var posLong = parseFloat(request.params.posLong);
    var timestamp = request.params.timestamp.toString();
    
    var bucketLat = Math.floor((1.00000/bucketSize) * posLat) / (1.00000 / bucketSize);
    var bucketLong = Math.floor((1.00000/bucketSize) * posLong) / (1.00000 / bucketSize);

    var bucketName = bucketLat.toString() + "," + bucketLong.toString();
    
    var Bucket = Parse.Object.extend("Bucket");
    var query = new Parse.Query(Bucket);
    query.equalTo("bucketName", bucketName);

    query.find({
        success: function(results){
            if (results.length > 0){
                var bucket = results[0];
                var crumbs = bucket.get("crumbs");
                crumbs[userId] = timestamp;
                bucket.set("crumbs",crumbs);
            }
            else{
                var bucket = new Bucket;
                bucket.set("bucketName", bucketName);
                var crumbs = new Object();
                crumbs[userId] = timestamp;
                bucket.set("crumbs",crumbs);
            }
            var test = JSON.stringify(crumbs);
            console.log(test);
            bucket.save(null,{
                success: function(bucket){
                    bucket.save();
                },
            error: function(bucket, error){
                alert("failed");
            }
        });
    },
    error: function(error){
        alert("Error: "+ error.code + " " + error.message);
    }
    });
 
});

Parse.Cloud.define("findNumMatches", function(request, response){
    var username = request.params.username;
    response.success(Math.floor(Math.random() * 5)); // TODO: Just temporary of couse
});
