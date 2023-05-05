# cordova-plugin-pda-rfid

Cordova plugin for Unitech PA760 on Android platform.

## Properties
| Properties | Value                   |
|------------|-------------------------|
| Id         | cordova-plugin-pda-rfid |
| Name       | PdaPlugin               |
| Platform   | Android                 |

## Installation
```shell
cordova plugin add cordova-plugin-pda-rfid
```

## Methods
- `window.PdaPlugin.createReader`
- `window.PdaPlugin.destroyReader`
- `window.PdaPlugin.setScanMode`
- `window.PdaPlugin.readRfidTags`
- `window.PdaPlugin.startReading`
- `window.PdaPlugin.stopReading`

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
```javascript=
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


### destroyReader | 銷毀 RFID reader
關閉 Reader 物件。  
失敗的話會回傳錯誤訊息到 `errorCallback`。

```javascript
destroyReader(successCallback, errorCallback)
```

| param           | type     | description               |
|-----------------|----------|---------------------------|
| successCallback | function |                           |
| errorCallback   | function | 提供錯誤訊息的 callback function |

#### Example
```javascript=
window.PdaPlugin.destroyReader(
  (result) => {
    console.log("destroyReader" + result);
  },
  (error) => {
    console.log(error);
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
```javascript=
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


### readRfidTags | 有時間限制的掃描
啟動 Reader 開始掃描，並根據參數 `tagsToRead` 和 `timeout` 來設定掃描結束的時機。

- 掃描結束時機：
  1. 讀取到的 tag 數量大於等於 `tagsToRead`
  2. 讀取時間超過 `timeout` 的限制


- 讀取結果回傳格式：
  - 型態為 `String`
  - ex: `"[510722082400200000002265, 303515BDC012EB90602DCB20]"`


- 超過 `timeout` 時間後：
  - 一個 tag 都沒讀到
    - 回傳錯誤訊息「沒辦法在 `timeout/1000` 秒內掃描到 RFID tag」到 `errorCallback`
  - 有讀到，但數量小於 `tagsToRead`
    - 把讀到的結果回傳到 `successCallback`


```javascript
readRfidTags(tagsToRead, timeout, successCallback, errorCallback)
```

| param           |   type   | default |    unit     | description               |
|-----------------|:--------:|--------:|:-----------:|---------------------------|
| tagsToRead      |   int    |       5 |      -      | 要讀幾個 tag                  |
| timeout         |   int    |    3000 | millisecond | 讀取時間                      |
| successCallback | function |         |             | 回傳讀取到的 RFID tags          |
| errorCallback   | function |         |             | 提供錯誤訊息的 callback function |

#### Example
```javascript=
window.PdaPlugin.readRfidTags(
  3,
  2000,
  (result) => {
    console.log("readRfidTags", result);
  },
  (error) => {
    console.log(error);
  }
);
```


### startReading | 開始掃描
啟動 Reader 開始掃描，不會自己結束。  
需要呼叫 [stopReading](#stopReading) 才會停止。  
失敗的話會回傳錯誤訊息到 `errorCallback`。

```javascript
startReading(successCallback, errorCallback)
```

| param           | type     | description               |
|-----------------|----------|---------------------------|
| successCallback | function |                           |
| errorCallback   | function | 提供錯誤訊息的 callback function |

#### Example
```javascript=
window.PdaPlugin.startReading(
  (result) => {
    console.log("startReading", result);
  },
  (error) => {
    console.log(error);
  }
);
```

### stopReading | 停止掃描
關閉 Reader 停止掃描，並回傳讀取結果。  
失敗的話會回傳錯誤訊息到 `errorCallback`。

讀取結果回傳格式：
- 型態為 `String`
- ex: `[510722082400200000002265, 303515BDC012EB90602DCB20]`, `[]`

```javascript
stopReading(successCallback, errorCallback)
```

| param           | type     | description               |
|-----------------|----------|---------------------------|
| successCallback | function | 回傳讀取到的 RFID tags          |
| errorCallback   | function | 提供錯誤訊息的 callback function |

#### Example
```javascript=
window.PdaPlugin.stopReading(
  (result) => {
    console.log("stopReading" + result);
  },
  (error) => {
    console.log(error);
  }
);
```