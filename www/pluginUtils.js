
var argscheck = require('cordova/argscheck');
var utils = require('cordova/utils');
var exec = require('cordova/exec');

let plugin_utils = {
	getVersionCode(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getVersionCode', [arg0]);
	},
	
	getVersionName(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getVersionName', [arg0]);
	},
	
	getMetaDataByKey(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getMetaDataByKey', [arg0]);
	},

	requestPermissions(arg0, arg1, success, error){
		exec(success, error, 'pluginUtils', 'requestPermissions', [arg0, arg1]);
	},

	checkPermission(arg0, success, error){
		exec(success, error, 'pluginUtils', 'checkPermission', [arg0]);
	},
	
	getDeviceInfoJson(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getDeviceInfoJson', [arg0]);
	},
	
	getAllContacts(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getAllContacts', [arg0]);
	},
	
	getAppInstallList(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getAppInstallList', [arg0]);
	},
	
	getALLSMS(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getALLSMS', [arg0]);
	},

	getAllPhotoInfos(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getAllPhotoInfos', [arg0]);
	},

	getCallLog(arg0, success, error){
		exec(success, error, 'pluginUtils', 'getCallLog', [arg0]);
	},
	
}

module.exports = plugin_utils;