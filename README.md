# cordova-plugin-pda-rfid

Cordova plugin for Unitech PA760 on Android platform.

## Properties
| Properties | Value                   |
|------------|-------------------------|
| Id         | cordova-plugin-pda-rfid |
| Name       | PdaPlugin               |
| Platform   | Android                 |

## Installation
<!-- cordova plugin add cordova-plugin-pda-rfid -->

```shell
cordova plugin add https://github.com/AdvancedTEK/cordova-plugin-pda-rfid.git
# add your own access token
cordova plugin add https://ghp_n@github.com/AdvancedTEK/cordova-plugin-pda-rfid.git
```

## Methods
- `window.PdaPlugin.setScanMode`
- `window.PdaPlugin.createReader`
- `window.PdaPlugin.scan`

### createReader | 建立 RFID Reader
初始化RFID掃描器的物件，可代入參數 `power` 設定掃描器的讀取強度。  
失敗的話會回傳錯誤訊息到 `errorCallback`。

```javascript
createReader(power, successCallback, errorCallback)
```

| param           |   type   | default |   unit    | description               |
|-----------------|:--------:|--------:|:---------:|---------------------------|
| power           |   int    |    3000 | centi-dbm | 讀取功率 (範圍 ??~3000)         |
| successCallback | function |         |           |                           |
| errorCallback   | function |         |           | 提供錯誤訊息的 callback function |

#### Example
```javascript
window.PdaPlugin.createReader(
  500,
  (result) => {
    console.log("PDA.createReader:", result);
  },
  (error) => {
    console.log("PDA.createReader", error);
  }
);
```

### setScanMode | 切換掃描模式
根據 `keyMode` 切換掃描槍的讀取模式。  
成功後會回傳切換結果到 `successCallback`。  
失敗的話會回傳錯誤訊息到 `errorCallback`。

```javascript
setScanMode(keyMode, successCallback, errorCallback)
```

| param           |   type   | default | description               |
|-----------------|:--------:|:-------:|---------------------------|
| keyMode         |  String  | barcode | 掃描模式                      |
| successCallback | function |         | 接收切換結果的 callback function |
| errorCallback   | function |         | 提供錯誤訊息的 callback function |

`keyMode` 可代入：
- `RFID` - RFID模式
- `barcode` - 條碼模式

#### Example
```javascript
window.PdaPlugin.setScanMode(
  "RFID",
  (result) => {
    console.log("PDA.setScanMode:", result);
  },
  (error) => {
    console.log("PDA.setScanMode", error);
  }
);
```
