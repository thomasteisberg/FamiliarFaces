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
            bucket.save(null,{
                success: function(bucket){
                    bucket.save();
                    response.success();
                },
            error: function(bucket, error){
                alert("failed");
                response.error(error);
            }
        });
    },
    error: function(error){
        alert("Error: "+ error.code + " " + error.message);
    }
    });
    response.success(); 
});




Parse.Cloud.define("matchupFinder", function(request, response){  

    Parse.Cloud.useMasterKey();

    var Bucket = Parse.Object.extend("Bucket");
    var query = new Parse.Query(Bucket);
   
    var latitude = request.params.latitude;
    var longitude = request.params.longitude;
    var userId = request.params.userId;

    var new_bucket = latitude.toString() + "," + longitude.toString();

    var nearBuckets = new Array();

    for (var i = -1; i <= 1; i++){
        for (var j = -1; j <= 1; j++){
            var new_lat = latitude + (.00005 * i);
            var new_long = longitude + (.00005 * j);
            var new_bucket = new_lat.toString() + "," + new_long.toString();        
            console.log(new_bucket); 
            nearBuckets.push(new_bucket);
        }
    }
    console.log(nearBuckets.toString());
    console.log(nearBuckets.length.toString());
    query.containedIn("bucketName", nearBuckets);
    query.find({
        success: function(results){
            for (var i = 0; i < results.length; i++){
                var bucket = results[i];
                var hits = bucket.get("crumbs");
                console.log(hits);
                
                /** NEW CODE **/
                for (var user in hits){
                   
                    if (userPair != user){
                        var userPair = userId + "," + user;
                        
                        var Connection = Parse.Object.extend("Connection");
                        var connection = new Connection;

                        connection.set("userPair", userPair);
                        connection.set("location", results[i].get("bucketName"));
                        connection.set("timestamp", hits[user]); 

                        connection.save(null,{
                            success: function(connection){
                                console.log("saved.");
                                connection.save();
                                response.success();
                            },
                            error: function(connection, error){
                                alert("failed");
                                response.error();
                            }
                        });    
                    }
                    else{
                        response.success();
                    }
        
                }

                /** END NEW CODE **/
            response.success();
            }
           // status.success("woo!");
        },
        error: function(){
           // status.error("fuck.");
        }

    }); 
response.success();
});


Parse.Cloud.job("findAllMatchups", function(request, status){

    parameters = new Object();
    parameters.userId = "erfb9xd1eoahrkhrrdb77woyf";
    parameters.latitude = 39.9512;
    parameters.longitude = -75.1934;
    
    Parse.Cloud.run("matchupFinder",parameters, {success: function() {status.success("success!")}, error: function(error){ status.error("sad");}});
        
});
