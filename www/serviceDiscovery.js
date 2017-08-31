/*global cordova, module*/
var exec = cordova.require('cordova/exec');
var serviceDiscovery = serviceDiscovery || {};
module.exports = serviceDiscovery;

serviceDiscovery.getNetworkServices = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "serviceDiscovery", "getNetworkServices", []);
    console.log(1);
    return true;
};

