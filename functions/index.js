// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

const SECRET_FILE = '.010secret.txt'
const KEY_METADATA = 'key_metadata';
const DOCUMENTS = 'documents/';

// [START storage onFinalize Trigger]
exports.insertFileTrigger = functions.storage.object().onFinalize((object) => {
    const filePath = object.name; // File path in the bucket.
    const filename = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length);
    var path = filePath.substring(0, filePath.lastIndexOf('/'));

    // add to the absolute path the encoding
    path = DOCUMENTS + path + '/' + object.metadata[KEY_METADATA] + '/';

    var d = new Date();
    const lastAccess = d.getTime();

    if (filename === SECRET_FILE) {
        // empty file used for creating a new directory
        console.log('secret file: ', filePath);
        var secretFileObj = {
            filename : filename,
            key : "",
            contentType : "",
            size : "",
            time : "",
            lastAccess : lastAccess,
            offline : false
        };
        admin.database().ref(path).set(secretFileObj);
    }
    else {
        // object that describes the file, to add in the firebase database
        console.log('new file added: ', filePath);
        var fileObj = {
            filename : filename,
            key : object.metadata[KEY_METADATA],
            contentType : object.contentType,
            size : object.size,
            time : object.timeCreated,
            lastAccess : lastAccess,
            offline : false
        };
        admin.database().ref(path).set(fileObj);
    }
    return null;
});
// [END storage onFinalize Trigger]


// [START storage onDelete Trigger]
exports.deleteFileTrigger = functions.storage.object().onDelete((object, context) => {
    const filePath = object.name; // file path in the bucket
    var path = filePath.substring(0, filePath.lastIndexOf('/'));

    console.log('new file removed: ', filePath);

    // add to the absolute path the encoding
    path = DOCUMENTS + path + '/' + object.metadata[KEY_METADATA];

    // remove the corresponding file stored in the database
    admin.database().ref(path).set(null);

    return null;
});
// [END storage onDelete Trigger]