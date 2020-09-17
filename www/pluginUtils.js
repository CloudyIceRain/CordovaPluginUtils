
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
}

module.exports = plugin_utils;