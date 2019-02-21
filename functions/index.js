// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

const KEY_METADATA = 'key_metadata';
const DOCUMENTS = 'documents/';

// [START storage onFinalize Trigger]
exports.insertFileTrigger = functions.storage.object().onFinalize((object) => {
    const filePath = object.name; // File path in the bucket.

    var arrayPath = filePath.split('/');
    const filename = arrayPath[arrayPath.length - 1]; // nema of the file
    var path = ""; // absolute path of the file, filename excluded 
    for(var index = 0; index < (arrayPath.length - 1); index++) {
        path = path + arrayPath[index] + "/"; 
    }

    // add to the absolute path the encoding
    path = DOCUMENTS + path + object.metadata[KEY_METADATA] + '/';

    console.log('new file added: ', filePath);

    // object that describes the file, to add in the firebase database
    var fileObj = {
        filename : filename,
        key : object.metadata[KEY_METADATA],
		contentType : object.contentType,
		size : object.size,
		time : object.timeCreated
	};
    admin.database().ref(path).set(fileObj);
    return null;
});
// [END storage onFinalize Trigger]


// [START storage onDelete Trigger]
exports.deleteFileTrigger = functions.storage.object().onDelete((object, context) => {
    const filePath = object.name; // file path in the bucket

    console.log('new file removed: ', filePath);

    var arrayPath = filePath.split('/');
    var path = ""; // absolute path of the file, filename excluded 
    for(var index = 0; index < (arrayPath.length - 1); index++) {
        path = path + arrayPath[index] + "/"; 
    }

    // add to the absolute path the encoding
    path = DOCUMENTS + path + object.metadata[KEY_METADATA] + '/';

    // remove the corresponding file stored in the database
    admin.database().ref(path).set(null);
});
// [END storage onDelete Trigger]