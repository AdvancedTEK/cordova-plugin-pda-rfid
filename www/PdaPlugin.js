var exec = require('cordova/exec');

var PdaPlugin = {
    createReader: function(power, success, error) {
        exec(success, error, 'PdaPlugin', 'createReader', [power]);
    },
    destroyReader: function(arg0, success, error) {
        exec(success, error, 'PdaPlugin', 'destroyReader', [arg0]);
    },
    startReading: function(arg0, success, error) {
        exec(success, error, 'PdaPlugin', 'startReading', [arg0]);
    },
    readRfidTags: function(tagsToRead, timeout, success, error) {
        exec(success, error, 'PdaPlugin', 'readRfidTags', [tagsToRead, timeout]);
    },
    stopReading: function(arg0, success, error) {
        exec(success, error, 'PdaPlugin', 'stopReading', [arg0]);
    },
    setScanMode: function(keyMode, success, error) {
        exec(success, error, 'PdaPlugin', 'setScanMode', [keyMode]);
    }
}
module.exports = PdaPlugin;
