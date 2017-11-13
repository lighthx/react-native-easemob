# react-native-easemob

额，我太懒了，关于加Appkey和初始化，以及IOS需要添加额外的一些包请查看官方文档（晚些可能能补上）
http://docs.easemob.com/im/300iosclientintegration/140iosquickstart



## Installation

```javascript
npm install react-native-easemob --save
```
or

```javascript
yarn add react-native-easemob
```

### Automatic Install

`react-native link react-native-easemob`

## Usage

```javascript

import {login,sendText,sendVoice,sendVideo,sendImage,sendLocation,getAllContacts,getConversation,download,deleteMsg,forwarding} from 'react-native-easemob'
import {DeviceEventEmitter, NativeEventEmitter, NativeModules} from 'react-native';
const {RNReactNativeEasemob} = NativeModules;
const iosChatEmitter = new NativeEventEmitter(RNReactNativeEasemob);
//监听ios获取消息
iosChatEmitter.addListener(	'message',async(e) => {})

//监听ios对方收到消息
iosChatEmitter.addListener(	"deliver",async(e) => {})

//监听android获取消息
DeviceEventEmitter.addListener('message', async(e) => {});

//监听android对方获取消息
DeviceEventEmitter.addListener('deliver', async(e) => {});

//登录
Promise login(userId:string,pwd:string)

//发送文本
Promise sendText(content:string,userId:string)

//发送语音
Promise sendVoice(path:string,length:number,userId:string)

//发送视频
Promise sendVideo(path:string,thumbPath:string,length:number,userId:string)

//发送图片
Promise sendImage(path:string,userId:string)

//发送位置
Promise sendLocation(latitude:number,longitude:number,address:string,userId:string)

//获取当前用户的所有联系人
Promise getAllContacts()

//下载当前消息下的附件
Promise download(userId:string,msgId:string)

//获取回话列表包含最近一条的对话
Promise getAllConversations()

//获取当前用户的聊天记录
Promise getConversation(userId:string,msgId:string)

//删除消息
Promise deleteMsg(userId:string,msgId:string)

//转发消息
Promise forwarding(userId:string,msgId:string,toUserId:string)
```

