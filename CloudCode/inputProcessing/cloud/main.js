require('cloud/smshandler.js')
var bucketSize = .00005;

var buckets = {};


Parse.Cloud.define("storeLocation", function(request, response){

    // var parseUserId = Parse.User.current().id;
    // console.log(parseUserId);
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

Parse.Cloud.define("testQuery", function(request, response){

    var Bucket = Parse.Object.extend("Bucket");
    var query = new Parse.Query(Bucket);
    query.find({
        success: function(results){
                     console.log("success");
                     console.log(results);
                 },
        error: function(error){
                   console.log(error);
                   response.error(error);
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
                     for (var i = 0; i < results.length; i++){
                         var bucket = results[i];
                         var hits = bucket.get("crumbs");
                         console.log("hits " + hits);

                         for (var user in hits){
                             if (userId != user){

                                 var Connection = Parse.Object.extend("Connection");
                                 var connection = new Connection;

                                 connection.set("user", userId);
                                 connection.set("friend", user);

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

Parse.Cloud.define("findMatchesforUser", function(request, response){

    var userId = request.params.userId;

    var hitList;

    var fbUser = Parse.Object.extend("fbUser");
    var query = new Parse.Query(fbUser);
    query.equalTo("userId", userId);
    query.find({
        success: function(userMatch){

                     if(userMatch.length > 0){
                         userHit = userMatch[0];
                     }
                     else{
                         userHit = new fbUser;
                         userHit.set("userId",userId);
                     }


    if(userHit.get("hitList")!==undefined){
        hitList = userHit.get("hitList");
    }
    else{
        hitList = {};
    }

    var Connection = Parse.Object.extend("Connection");
    var query = new Parse.Query(Connection);
    query.equalTo("user",userId);
    query.find({
        success: function(results){
                     for(var i = 0; i < results.length; i++){
                         var curHit = results[i];
                         if(curHit.get("friend") in hitList){
                             hitList[curHit.get("friend")] += 1;
                         }
                         else{
                             hitList[curHit.get("friend")] = 1;
                         }
                     }

                 },
        error: function(error){
                   response.error();
               }
    });


    // set and save hitList

    userHit.set("hitList", hitList);
    userHit.save(null,{
        success: function(userHit){
                     console.log("saved.");
                     userHit.save();

                 },
        error: function(userHit, error){
                   alert("failed");
                   response.error();
               }
    }); 



                 },
            error: function(error){
                       response.error();
                   }
    });

});

Parse.Cloud.define("addPhonePair", function(request, response){

    var p1 = request.params.p1;
    var p2 = request.params.p2;

    Parse.Cloud.httpRequest({
        method: "GET",
        url: "http://testing.thomasteisberg.com/familiarfaces/addphonepair.php",
        body: {
            p1:p1,
        p2:p2,
        }
    });

});

Parse.Cloud.define("getMatchName", function(request, response){
    var userId = request.params.userId;
    var threshold = request.params.threshold;
    var hits = new Array();
    var fbUser = Parse.Object.extend("fbUser");
    var query = new Parse.Query(fbUser);
    query.equalTo("userId", userId);
    query.find({
        success: function(userMatch){
                     if(userMatch.length > 0){
                         userHit = userMatch[0];
                         hitList = userHit.get("hitList");
                         for(var friend in hitList){
                             if(hitList[friend] > threshold){
                                 hits.push(friend);
                             }
                         }
                         var randHit = hits[Math.floor(Math.random() * hits.length)];
                     }    
                 },
        error: function(error){
                   response.error();
               }
    });

    response.success(randHit);

});

Parse.Cloud.define("rejectMatch", function(request, response){
    // passing in the userId of the match (as matchUserId) and current userId
    // add each person to the other's exclusion list
    // no response
});

Parse.Cloud.define("inviteChat", function(request,response){
    // passing in userId of match (as matchUserId) and current userId
    // needs to send push notification to other user *****
});


Parse.Cloud.define("sms", function(request, response){

    var p1 = request.params.p1;
    var msg = request.params.msg;

    Parse.Cloud.httpRequest({
        method: "GET",
        url: "http://testing.thomasteisberg.com/familiarfaces/sms.php",
        body: {
            msisdn:p1,
        text:msg,
        }

    });
});

Parse.Cloud.define("findNumMatches", function(request, response){
    var userId = request.params.userId;
    var threshold = request.params.threshold;
    var fbFriends = request.params.friends;
    var counter = 0;

    var fbUser = Parse.Object.extend("fbUser");
    var query = new Parse.Query(fbUser);
    query.equalTo("userId", userId);
    
    console.log("about to query for user");
    query.find({
        success: function(result){
                     if(result.length > 0){
                        var curUser = results[0];
                        var hitList = curUser.get("hitlist");
                        for(var friend in hitList){
                            if(hitList[friend] > threshold){
                                counter++;
                            }
                        }
                     }
                     else{
                        curUser = new fbUser;
                        curUser.set("userId", userId);
                        counter = 0;
                        curUser.set("exclusionList", fbFriends);
                        
                        curUser.save(null,{
                            success:function(curUser){
                                        curUser.save();
                                    },
                            error: function(curUser, error){
                                   response.error(error);
                                   }
                        });
                     }

        
                     },
        error: function(error){
                   response.error(error);
               }

}).then(function(){
    response.success(counter);
}, function(error){
    response.error();
});
});
