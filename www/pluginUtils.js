
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
	
	setUserKeyValue_JSON(arg0, success, error){
		exec(success, error, 'pluginUtils', 'setUserKeyValue_JSON', [arg0]);
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
}

module.exports = plugin_utils;