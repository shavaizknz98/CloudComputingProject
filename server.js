global.WebSocket = require('ws');
const compression = require('compression');
var fs = require('fs');
var cookieParser = require('cookie-parser');
const multer = require('multer');
const path = require('path');
const databaseServer = 'https://cbspdatabaseserver.azurewebsites.net/webapi'
var zlib = require('zlib');
const superagent = require('superagent');
const { BlobServiceClient } = require('@azure/storage-blob');
const uuidv1 = require('uuid/v1');
const AZURE_STORAGE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=cbspstorageaccount;AccountKey=wBGXyMM/shqzc2se6r7zECrt2sU8uhAZ0Ap/+t/38dYjI1mN0K/uXKVbORs2TNbZ/UhPSoiDxSXUJu3xuWCofg==;EndpointSuffix=core.windows.net";
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, './Uploads/');
    },

    filename: function (req, file, cb) {
        cb(null, file.originalname);
    }
});

var express = require('express');
var app = express();
app.use(compression());
app.use(cookieParser());
var bodyParser = require('body-parser');
var upload = multer({ storage: storage })
app.set('port', process.env.PORT || 3005);
// Create application/x-www-form-urlencoded parser
var urlencodedParser = bodyParser.urlencoded({ extended: false })
//default behavior
app.use(express.static(__dirname));
//GET the home page : index.html -> localhost:3005/
app.get('/', function (req, res) {
    res.sendFile(__dirname + '/index.html')
});

function logUserIn(email, pass, reqResponse, myReq) {
    superagent.get( databaseServer + '/Users/' + email + "/" + pass)
        .then(res => {
            console.log(res.body)
            if (res.body == "") {
                console.log(email + " does not exist")
                return reqResponse.redirect(303, 'index.html')
            } else {
                console.log(email + " can be logged in. Cookie data is: ")
                console.log(myReq.cookies.email);
                // let my cookie be (or not be) here
                let emailCookie = myReq.cookies.email;
                // if not make it
                if (!emailCookie) {
                    reqResponse.cookie('email', email);
                    console.log("Cookies Set");
                }
                return reqResponse.redirect(303, 'dashboard.html')
            }
        })
        .catch(err => {
            console.log(err.message)
            return reqResponse.redirect(303, '/')
        });
}

function signUserUp(email, name, pass, reqResponse, myReq) {
    superagent
        .post( databaseServer + '/Users/')
        .send({ email: email, name: name, pass: pass })
        .set('Accept', 'application/json')
        .then(res => {
            console.log(res.body)
            if (res.body == "") {
                console.log(email + " could not be signed up")
                return reqResponse.redirect(303, 'index.html')
            } else {
                console.log(email + " can be signed up")
                reqResponse.cookie('email', email);
                return reqResponse.redirect(303, 'dashboard.html')
            }
        });
}

function addVideoFromUser(email, vidName, vidPass, vidID, cDate, vidPath, analyzed) {
    superagent
        .post( databaseServer + '/Videos/')
        .send({ email: email, vidName: vidName, vidPass: "", vidID: 0, cDate: "", vidPath: vidPath, analyzed: false })
        .set('Accept', 'application/json')
        .then(res => {
            console.log(res.body)
            if (res.body == "") {
                console.log(email + " could not add video to database")
                //return reqResponse.redirect(303, 'dashboard.html')
            } else {
                console.log(email + " video added to database")
                //return reqResponse.redirect(303, 'dashboard.html')
            }
        });
}

function getVideoFromUser(email, reqResponse){
    superagent
    .get( databaseServer + '/Videos/' + email)
    .set('Accept', 'application/json')
    .then(res => {
        console.log("Sending video list for "+ email);
        return reqResponse.send(JSON.stringify(res.body));
    });
}

function getVidPathFromUser(email, name ,reqResponse){
    superagent
    .get( databaseServer + '/Videos/' + email)
    .set('Accept', 'application/json')
    .then(res => {
        
        var jsonData = JSON.parse(JSON.stringify(res.body));
        for( var index in res.body) {
            var vidName = jsonData[index]['vidName'];
            if(vidName == name)
            return reqResponse.redirect(jsonData[index]['vidPath']);

        }
        
    });
}

function deleteOriginalFile(filename, myReq) {
    console.log('Done compressing ' + filename);
    fs.unlink('./Uploads/' + filename, (err) => {
        if (err) {
            console.log("Could not delete file");
            return;
        } else {
            console.log("Deleted uncompressed file proceeding to upload to azure storage");
            uploadToContainer(filename, myReq);
        }
    })
}


function deleteRemainingFile(filename) {
    console.log('Deleting remaining user file ' + filename);
    fs.unlink('./Uploads/' + filename, (err) => {
        if (err) {
            console.log("Could not delete file");
            return;
        } else {
            console.log("Deleted remaining files");
        }
    })
}


async function uploadToContainer(filename, myReq) {
    const blobServiceClient = await BlobServiceClient.fromConnectionString(AZURE_STORAGE_CONNECTION_STRING);

    // Create a unique name for the container
    const containerName = 'container';

    console.log('\nCreating container...');
    console.log('\t', containerName);

    // Get a reference to a container
    const containerClient = await blobServiceClient.getContainerClient(containerName);

    // Create a unique name for the blob
    const blobName = myReq.cookies.email + filename + ".gz";

    // Get a block blob client
    const blockBlobClient = containerClient.getBlockBlobClient(blobName);

    console.log('\nUploading to Azure storage as blob:\n\t', blobName);

    const uploadBlobResponse = await blockBlobClient.uploadFile("Uploads/" + filename + ".gz");

    console.log("Blob was uploaded successfully. Proceeding to delete remaning local files.");
    deleteRemainingFile(filename + ".gz");
    console.log("Adding video to database " + "URL : " + blockBlobClient.url);
    console.log("Adding to database for " + myReq.cookies.email);
    addVideoFromUser(myReq.cookies.email, blobName, "", 0, "", blockBlobClient.url, false)

}

function compressVideoUploads(filename, myReq) {
    var fileContents = fs.createReadStream(`./Uploads/${filename}`);
    var writeStream = fs.createWriteStream(`./Uploads/${filename}.gz`);
    var zip = zlib.createGzip({
        level: 9 // Max compression level
    });

    fileContents.pipe(zip).pipe(writeStream).on('finish', (err) => {
        if (err) {
            console.log(err);
        }
        else {
            console.log("Proceeding to delete");
            deleteOriginalFile(filename, myReq);
        }
    });


}

app.post('/login', urlencodedParser, function (req, res) {
    username = req.body.uname; //requesting the username input value
    password = req.body.psw; //requesting the password input value

    console.log("Post request recieved to login " + username);
    //checking if username and password are in the database
    logUserIn(username, password, res, req);

});

app.post('/signup', urlencodedParser, function (req, res) {
    username = req.body.uname; //requesting the username input value
    name = req.body.name;
    password = req.body.psw; //requesting the password input value

    console.log("Post request recieved to signup " + username);
    signUserUp(username, name, password, res, req);
});


app.post('/fileupload', upload.single('file'), (req, res, next) => {
    console.log("Receiving file from client");
    console.log(req.file);
    if (req.file) {
        var file = req.file
        var filename = file.originalname
        console.log("Uploading file for " + req.cookies.email);
        console.log("File named " + filename + " Uploaded, Proceeding to compress.")
        compressVideoUploads(file.filename, req);

        res.redirect(303, 'dashboard.html');
    }
});

app.post('/filedownload', urlencodedParser , (req, res, next) => {
    console.log("Sending file to client");
    console.log(req.body.video);
    getVidPathFromUser(req.cookies.email, req.body.video, res);
    
});

app.post('/filedelete', urlencodedParser , (req, res, next) => {
    console.log("Deleting file from client");
    console.log(req.body.videoDelete);
    getVidPathFromUser(req.cookies.email, req.body.video, res);
    
});

app.get('/getVideos', function (req, res) { 
    console.log("getting videos for " + req.cookies.email)
    getVideoFromUser(req.cookies.email, res);
});


app.listen(app.get('port'), function () {
    console.log('Express started on http://localhost:' + app.get('port') + '; press Ctrl-C to terminate.');
    console.log(AZURE_STORAGE_CONNECTION_STRING);
    

});

