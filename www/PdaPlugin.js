var exec = require('cordova/exec');

var PdaPlugin = {
    setScanMode: function(arg0, success, error) {
        exec(success, error, 'PdaPlugin', 'setScanMode', [arg0]);
    },
    scan: function(success, error) {
        exec(success, error, 'PdaPlugin', 'scan');
    },
    createReader: function(power, success, error) {
        exec(success, error, 'PdaPlugin', 'createReader', [power]);
    }
}
module.exports = PdaPlugin;
