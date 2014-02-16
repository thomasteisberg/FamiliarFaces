require('cloud/smshandler.js')

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

});


Parse.Cloud.define("matchupFinder", function(request, response){  

    Parse.Cloud.useMasterKey();

    var latitude = request.params.latitude;
    var longitude = request.params.longitude;
    var userId = request.params.userId;

    
    var Bucket = Parse.Object.extend("Bucket");
    var query = new Parse.Query(Bucket);
    
    console.log("Running " + userId + " on lat " + latitude + " and long " + longitude);

    var new_bucket = latitude.toString() + "," + longitude.toString();

    var nearBuckets = new Array();

    for (var i = -1; i <= 1; i++){
        for (var j = -1; j <= 1; j++){
            var new_lat = parseFloat(latitude) + (.00005 * i);
            var new_long = parseFloat(longitude) + (.00005 * j);
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
            console.log("in dis query shit");
            for (var i = 0; i < results.length; i++){
                var bucket = results[i];
                var hits = bucket.get("crumbs");
                console.log("hits " + hits);
                
                for (var user in hits){
                    if (userId != user){
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
                              
                            },
                            error: function(connection, error){
                                alert("failed");
                                response.error();
                            }
                        });    
                    }
                    else{
                       // 
                    }
        
                }
            }
        
        },
        error: function(){
           response.error();
        }
    }).then(function(){
        response.success();
    }, function(error){
        response.error();
    }); 
});


Parse.Cloud.define("findMatchupsForBucket", function(request, response){
    var crumbs = request.params.crumbs;
    var loc = request.params.loc;
    var counter = 0;
    for (var user in crumbs){
        parameters = new Object();
        parameters.userId = user;
        parameters.latitude = loc.split(',')[0];
        parameters.longitude = loc.split(',')[1];
        
        counter += 1;
        console.log("Calling matchupFinder on " + user + " with " + parameters.latitude + " " + parameters.longitude);
        Parse.Cloud.run("matchupFinder",parameters,{success: function() { counter +=1;}, error: function(error){ counter += 1;}});

    }

});

Parse.Cloud.job("findAllMatchups", function(request, status){

    var Bucket = Parse.Object.extend("Bucket");
    var query = new Parse.Query(Bucket);
    
    var counter = 0;

    query.each(function(result){
        var crumbs = result.get("crumbs");
        parameters = new Object();
        parameters.crumbs = crumbs;
        parameters.loc = result.get("bucketName");
       Parse.Cloud.run("findMatchupsForBucket",parameters, {success: function(){ counter +=1;}, error: function(error){ counter +=1;}}); 

    }).then(function(){

        status.success("success!");
       
    }, function(error){
        status.error("fail");
    });
               
});


Parse.Cloud.define("findNumMatches", function(request, response){
    var username = request.params.username;
    response.success(Math.floor(Math.random() * 5)); // todo: Just temporary of couse
});
