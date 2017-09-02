/*global cordova, module*/
var serviceDiscovery =  {};

serviceDiscovery.getNetworkServices = function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "serviceDiscovery", "getNetworkServices", []);
    return true;
};

module.exports = serviceDiscovery;

