
package com.easemob;
import android.support.annotation.Nullable;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.chat.adapter.message.EMAMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.facebook.react.bridge.Promise;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
public class RNReactNativeEasemobModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private EMMessageBody body;

    private void sendEvent(
            String eventName,
            @Nullable WritableMap params) {
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
    private void checkBody(WritableMap bodyObj,EMMessageBody body){
        if (body instanceof EMImageMessageBody) {
            String url = ((EMImageMessageBody) body).getRemoteUrl();
            String tUrl = ((EMImageMessageBody) body).getThumbnailUrl();
            bodyObj.putString("type","image");
            bodyObj.putString("url", url);
            bodyObj.putString("tUrl", tUrl);
        }
        if (body instanceof EMTextMessageBody) {
            String text = ((EMTextMessageBody) body).getMessage();
            bodyObj.putString("type","text");
            bodyObj.putString("text", text);
        }
        if (body instanceof EMLocationMessageBody) {
            double latitude = ((EMLocationMessageBody) body).getLatitude();
            double longitude = ((EMLocationMessageBody) body).getLongitude();
            String address = ((EMLocationMessageBody) body).getAddress();
            bodyObj.putDouble("latitude", latitude);
            bodyObj.putDouble("longitude", longitude);
            bodyObj.putString("address", address);
            bodyObj.putString("type","location");
        }
        if (body instanceof EMVoiceMessageBody) {
            String url = ((EMVoiceMessageBody) body).getRemoteUrl();
            bodyObj.putString("url", url);
            bodyObj.putString("type","voice");
        }
        if (body instanceof EMVideoMessageBody) {
            String url = ((EMVideoMessageBody) body).getRemoteUrl();
            String tUrl = ((EMVideoMessageBody) body).getThumbnailUrl();
            String localPath =((EMVideoMessageBody) body).getLocalUrl();
            if(((EMVideoMessageBody) body).downloadStatus()== EMFileMessageBody.EMDownloadStatus.SUCCESSED){
                bodyObj.putString("success","1");
            }
            bodyObj.putString("status",((EMVideoMessageBody) body).downloadStatus().toString());
            bodyObj.putString("url", url);
            bodyObj.putString("tUrl", tUrl);
            bodyObj.putString("type","video");
            bodyObj.putString("localPath",localPath);
        }
    }
    public RNReactNativeEasemobModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        EMOptions options = new EMOptions();
//    options.setAcceptInvitationAlways(false);
//    options.setNumberOfMessagesLoaded(1);
        EMClient.getInstance().init(reactContext, options);
        EMClient.getInstance().chatManager().loadAllConversations();
        EMClient.getInstance().setDebugMode(true);
    }

    @Override
    public String getName() {
        return "RNReactNativeEasemob";
    }

    //登录
    @ReactMethod
    public void _login(String user, String pwd,final Promise promise) {
        EMClient.getInstance().login(user, pwd, new EMCallBack() {//回调
            @Override
            public void onSuccess() {

                EMClient.getInstance().chatManager().loadAllConversations();
                promise.resolve("登录聊天服务器成功！");
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                promise.reject(message,"登录聊天服务器失败！");
            }
        });
    }

    //创建
    @ReactMethod
    public void _create(final String username, final String pwd, final Promise promise) {
        try {
            EMClient.getInstance().createAccount(username, pwd);
            promise.resolve("finish");
        } catch (HyphenateException e) {
            e.printStackTrace();
            promise.reject(e.getMessage());
        }

    }

    @ReactMethod
    public void _logout(final Promise promise) {
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                promise.resolve("success");
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub
                promise.reject(message,"err");
            }
        });
    }

    //发送文本
    @ReactMethod
    public void _sendText(String content, String user, final Promise promise) {
        try {
            EMMessage message = EMMessage.createTxtSendMessage(content, user);
            EMClient.getInstance().chatManager().sendMessage(message);
            message.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {
                    promise.resolve("success");
                }

                @Override
                public void onError(int i, String s) {
                    promise.reject(s,"err");
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } catch (Exception e) {

        }
    }

    //发送音频
    @ReactMethod
    public void _sendVoice(String path, int length, String user, final Promise promise) {
        try {
            final EMMessage message = EMMessage.createVoiceSendMessage(path, length, user);
            message.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {
                    WritableMap params = Arguments.createMap();
                    EMMessageBody body =message.getBody();
                    if(body instanceof EMVoiceMessageBody){
                        params.putString("url",((EMVoiceMessageBody) body).getRemoteUrl());
                        promise.resolve(params);
                    }


                }

                @Override
                public void onError(int i, String s) {
                    promise.reject(s,"失败");
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
            EMClient.getInstance().chatManager().sendMessage(message);

        } catch (Exception e) {

        }

    }

    //发送视频
    @ReactMethod
    public void _sendVideo(String path, String thumbPath, int length, String user, final Promise promise) {
        try {
            final EMMessage message = EMMessage.createVideoSendMessage(path, thumbPath, length, user);
            EMClient.getInstance().chatManager().sendMessage(message);
            message.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {

                    EMMessageBody body = message.getBody();
                    if(body instanceof EMVideoMessageBody){
                        WritableMap params = Arguments.createMap();
                        params.putString("url",((EMVideoMessageBody) body).getRemoteUrl());
                        promise.resolve(params);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    promise.reject(s,"发送失败");
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } catch (Exception e) {

        }

    }

    //发送图片
    @ReactMethod
    public void _sendImage(String path, String user, final Promise promise) {
        try {
            final EMMessage message = EMMessage.createImageSendMessage(path, false, user);
            EMClient.getInstance().chatManager().sendMessage(message);
            message.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {
                    EMMessageBody body=message.getBody();
                    if(body instanceof EMImageMessageBody){
                        WritableMap params = Arguments.createMap();
                        params.putString("url",((EMImageMessageBody) body).getRemoteUrl());
                        promise.resolve(params);
                    }
                }

                @Override
                public void onError(int i, String s) {
                    promise.reject(s,"失败");
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } catch (Exception e) {

        }

    }

    //发送本地地址
    @ReactMethod
    public void _sendLocation(Float latitude, Float longitude, String address, String user, final Promise promise) {
        try {
            EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, address, user);
            EMClient.getInstance().chatManager().sendMessage(message);
            message.setMessageStatusCallback(new EMCallBack() {
                @Override
                public void onSuccess() {
                    promise.resolve("success");
                }

                @Override
                public void onError(int i, String s) {
                    promise.reject(s,"失败");
                }

                @Override
                public void onProgress(int i, String s) {

                }
            });
        } catch (Exception e) {
        }
    }

    //  添加消息监听
    @ReactMethod
    public void _addMessageListener() {
        EMMessageListener msgListener = new EMMessageListener() {


            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                System.out.println(Arrays.toString(messages.toArray()));
                for (int i = 0; i < messages.size(); i++) {
                    EMMessage data = messages.get(i);
                    String from = data.getFrom();
                    String msgId = data.getMsgId();
                    long time = data.getMsgTime();
                    WritableMap params = Arguments.createMap();
                    RNReactNativeEasemobModule.this.checkBody(params,data.getBody());
                    params.putString("from", from);
                    params.putDouble("time", time);
                    params.putString("msgId", msgId);
                    sendEvent("message", params);
                }
//
            }


            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }


            public void onMessageRead(List<EMMessage> messages) {
                //收到已读回执
            }


            public void onMessageDelivered(List<EMMessage> message) {
                //收到已送达回执
                WritableMap params = Arguments.createMap();
                sendEvent("deliver", params);
            }

            public void onMessageRecalled(List<EMMessage> messages) {
                //消息被撤回
            }


            public void onMessageChanged(EMMessage message, Object change) {
                WritableMap obj = Arguments.createMap();
                obj.putString("data","update");
                sendEvent("update",obj);
                //消息状态变动
            }
        };
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }

    //获取好友列表
    @ReactMethod
    public void _getAllContacts(final Promise promise) {
        try {
            List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
            System.out.println(Arrays.toString(usernames.toArray()));
            WritableArray array = new WritableNativeArray();
            for (String item : usernames) {
                array.pushString(item);
            }
            ;
            promise.resolve(array);
        } catch (HyphenateException e) {
            promise.reject(e.getMessage(),"err");
        }
    }

    //获取所有对话
    @ReactMethod
    public void _getAllConversations(Promise promise) {
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        System.out.println("nima");
        WritableArray array = new WritableNativeArray();
        for (Map.Entry<String, EMConversation> item : conversations.entrySet()) {
            String name = item.getKey().toString();
            body = item.getValue().getLastMessage().getBody();
            WritableMap bodyObj = Arguments.createMap();
            this.checkBody(bodyObj,body);
            String msgId = item.getValue().getLastMessage().getMsgId();
            String msgTime =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(item.getValue().getLastMessage().getMsgTime()));
            int unRead = item.getValue().getUnreadMsgCount();
            String type = item.getValue().getType().toString();
            WritableMap obj = Arguments.createMap();
            obj.putString("name", name);
            obj.putMap("msg", bodyObj);
            obj.putString("msgId", msgId);
            obj.putString("time", msgTime);
            obj.putInt("unRead", unRead);
            array.pushMap(obj);
        }
        promise.resolve(array);
    }
    //获取该用户最近
    @ReactMethod
    public void _getConversation(String user,String startId,Promise promise){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
        WritableArray array = new WritableNativeArray();
        if(conversation != null){
        List<EMMessage> messages = conversation.getAllMessages();
        Log.v("msg","出来啊");

                Log.v("msg",messages.size()+"");
        for (EMMessage msg:messages) {
            Log.v("msg",msg.toString());
            String from=msg.getFrom();
            String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(msg.getMsgTime()));
            String msgId=msg.getMsgId();
            WritableMap bodyObj=Arguments.createMap();
            body=msg.getBody();
            bodyObj.putString("from",from);
            bodyObj.putString("time",time);
            bodyObj.putString("msgId",msgId);
            this.checkBody(bodyObj,body);
            array.pushMap(bodyObj);
        }
        String id =messages.get(0).getMsgId();
        List<EMMessage> dbMessages = conversation.loadMoreMsgFromDB(id, 50);
        for (EMMessage msg:dbMessages) {
            String from=msg.getFrom();
            WritableMap bodyObj=Arguments.createMap();
            String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(msg.getMsgTime()));
            String msgId=msg.getMsgId();
            body=msg.getBody();
            this.checkBody(bodyObj,body);
            bodyObj.putString("from",from);
            bodyObj.putString("time",time);
            bodyObj.putString("msgId",msgId);
            array.pushMap(bodyObj);
        }
        conversation.markAllMessagesAsRead();
        promise.resolve(array);
    }else {
            promise.resolve(array);
        }
    }
    //下载文件
    @ReactMethod
    public void _download(String user , String msgId , final Promise promise){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
        final EMMessage msg =conversation.getMessage(msgId,false);
        if(msg.getBody() instanceof EMVideoMessageBody){
            EMClient.getInstance().chatManager().downloadFile(((EMVideoMessageBody) msg.getBody()).getRemoteUrl(),
                    ((EMVideoMessageBody) msg.getBody()).getLocalUrl(), null,
                    new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            Log.v("msg","success");
                            promise.resolve("ok");
                            if(msg.getBody() instanceof EMVideoMessageBody){
                                ((EMVideoMessageBody) msg.getBody()).setDownloadStatus(EMFileMessageBody.EMDownloadStatus.SUCCESSED);
                            }
                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.v("msg",s);
                        }

                        @Override
                        public void onProgress(int i, String s) {
                            WritableMap params = Arguments.createMap();
                            params.putInt("progress",i);
                            sendEvent("download",params);
                        }
                    });
        }



    }
    //获取账户下所有图片
    @ReactMethod
    public  void _loadPics(String user,Promise promise){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
        List<EMMessage> messages=conversation.searchMsgFromDB(EMMessage.Type.IMAGE,0,999,null,EMConversation.EMSearchDirection.DOWN);
        WritableArray array = new WritableNativeArray();
        for(EMMessage msg :messages){
            WritableMap bodyObj=Arguments.createMap();
            body=msg.getBody();
            String from=msg.getFrom();
            String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(msg.getMsgTime()));
            String msgId=msg.getMsgId();
            this.checkBody(bodyObj,body);
            bodyObj.putString("from",from);
            bodyObj.putString("time",time);
            bodyObj.putString("msgId",msgId);
            array.pushMap(bodyObj);
        }
        promise.resolve(array);
    }
    //搜索
    @ReactMethod
    public  void _searchText(String user,String keyword,Promise promise){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
        List<EMMessage> messages=conversation.searchMsgFromDB(keyword,0,999,null,EMConversation.EMSearchDirection.DOWN);
        WritableArray array = new WritableNativeArray();
        for(EMMessage msg :messages){
            WritableMap bodyObj=Arguments.createMap();
            body=msg.getBody();
            String from=msg.getFrom();
            String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(msg.getMsgTime()));
            String msgId=msg.getMsgId();
            this.checkBody(bodyObj,body);
            bodyObj.putString("from",from);
            bodyObj.putString("time",time);
            bodyObj.putString("msgId",msgId);
            array.pushMap(bodyObj);
        }
        promise.resolve(array);
    }
    //删除消息
    @ReactMethod
    public  void _deleteMsg(String user,String msgId,final Promise promise){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
        conversation.removeMessage(msgId);
        promise.resolve("删除成功");
    }
    @ReactMethod
    public void _forwarding (final String user,final String msgId,final String toUser ,final Promise promise){
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(user);
        EMMessage _message=conversation.getMessage(msgId,false);
        final EMMessageBody body =_message.getBody();
        if(body instanceof EMTextMessageBody){
            try {
                String content=((EMTextMessageBody) body).getMessage();
                EMMessage message = EMMessage.createTxtSendMessage(content, toUser);
                EMClient.getInstance().chatManager().sendMessage(message);
                message.setMessageStatusCallback(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        promise.resolve("success");
                    }

                    @Override
                    public void onError(int i, String s) {
                        promise.reject(s,"err");
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            } catch (Exception e) {

            }
        }
        if(body instanceof EMImageMessageBody){
            final String path=((EMImageMessageBody) body).getLocalUrl();
            EMClient.getInstance().chatManager().downloadFile(((EMImageMessageBody) body).getRemoteUrl(),
                    ((EMImageMessageBody) body).getLocalUrl(), null,
                    new EMCallBack() {
                        @Override
                        public void onSuccess() {
                                ((EMImageMessageBody) body).setDownloadStatus(EMFileMessageBody.EMDownloadStatus.SUCCESSED);
                            try {
                                EMMessage message = EMMessage.createImageSendMessage(path, false, toUser);
                                EMClient.getInstance().chatManager().sendMessage(message);
                                message.setMessageStatusCallback(new EMCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        promise.resolve("success");
                                    }

                                    @Override
                                    public void onError(int i, String s) {
                                        promise.reject(s,"失败");
                                    }

                                    @Override
                                    public void onProgress(int i, String s) {

                                    }
                                });
                            } catch (Exception e) {

                            }

                        }

                        @Override
                        public void onError(int i, String s) {
                            Log.v("msg",s);
                        }

                        @Override
                        public void onProgress(int i, String s) {
                        }
                    });

        }
        if(body instanceof EMVoiceMessageBody){
            String path=((EMVoiceMessageBody) body).getLocalUrl();
            try {
                EMMessage message = EMMessage.createVoiceSendMessage(path, 1, toUser);
                message.setMessageStatusCallback(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        promise.resolve("success");
                    }

                    @Override
                    public void onError(int i, String s) {
                        promise.reject(s,"失败");
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
                EMClient.getInstance().chatManager().sendMessage(message);

            } catch (Exception e) {

            }
        }
        if(body instanceof EMLocationMessageBody){
            String address=((EMLocationMessageBody) body).getAddress();
            double latitude=((EMLocationMessageBody) body).getLatitude();
            double longitude=((EMLocationMessageBody) body).getLongitude();
            try {
                EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, address, toUser);
                EMClient.getInstance().chatManager().sendMessage(message);
                message.setMessageStatusCallback(new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        promise.resolve("success");
                    }

                    @Override
                    public void onError(int i, String s) {
                        promise.reject(s,"失败");
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            } catch (Exception e) {
            }
        }
        if(body instanceof  EMVideoMessageBody){
            final String path=((EMVideoMessageBody) body).getLocalUrl();
            final String thumbPath=((EMVideoMessageBody) body).getThumbnailUrl();
            if(((EMVideoMessageBody) body).downloadStatus()==EMFileMessageBody.EMDownloadStatus.SUCCESSED){
                try {
                    EMMessage message = EMMessage.createVideoSendMessage(path, thumbPath, 3, toUser);
                    EMClient.getInstance().chatManager().sendMessage(message);
                    message.setMessageStatusCallback(new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            promise.resolve("success");
                        }

                        @Override
                        public void onError(int i, String s) {
                            promise.reject(s,"发送失败");
                        }

                        @Override
                        public void onProgress(int i, String s) {

                        }
                    });
                } catch (Exception e) {

                }
            }else{

                    EMClient.getInstance().chatManager().downloadFile(((EMVideoMessageBody) body).getRemoteUrl(),
                            ((EMVideoMessageBody) body).getLocalUrl(), null,
                            new EMCallBack() {
                                @Override
                                public void onSuccess() {
                                    ((EMVideoMessageBody) body).setDownloadStatus(EMFileMessageBody.EMDownloadStatus.SUCCESSED);
                                    try {
                                        EMMessage message = EMMessage.createVideoSendMessage(path, thumbPath, 3, toUser);
                                        EMClient.getInstance().chatManager().sendMessage(message);
                                        message.setMessageStatusCallback(new EMCallBack() {
                                            @Override
                                            public void onSuccess() {
                                                promise.resolve("success");
                                            }

                                            @Override
                                            public void onError(int i, String s) {
                                                promise.reject(s,"发送失败");
                                            }

                                            @Override
                                            public void onProgress(int i, String s) {

                                            }
                                        });
                                    } catch (Exception e) {

                                    }

                                }

                                @Override
                                public void onError(int i, String s) {
                                    Log.v("msg",s);
                                }

                                @Override
                                public void onProgress(int i, String s) {
                                    WritableMap params = Arguments.createMap();
                                    params.putInt("progress",i);
                                    sendEvent("download",params);
                                }
                            });
            }

        }
    }
}