
#import "RNReactNativeEasemob.h"
#import <MediaPlayer/MediaPlayer.h>
#import <React/RCTConvert.h>
#import <React/RCTImageLoader.h>
@implementation RNReactNativeEasemob

//- (dispatch_queue_t)methodQueue
//{
//    return dispatch_get_main_queue();
//}
{
    bool hasListeners;
}
RCT_EXPORT_MODULE();
-(void)startObserving {
    hasListeners = YES;
    // Set up any upstream listeners or background tasks as necessary
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    hasListeners = NO;
    // Remove upstream listeners, stop unnecessary background tasks
}
- (NSArray<NSString *> *)supportedEvents
{
    return @[@"message",@"download",@"update",@"deliver"];
}
-(void)messagesDidDeliver:(NSArray *)aMessages{
    if (hasListeners) {
        [self sendEventWithName:@"deliver" body:@{@"data":@"deliver"}];
    }
}
- (void)messagesDidReceive:(NSArray *)aMessages {
    EMMessage *message=aMessages[0];
    NSDictionary *_msg=[self getContent:message];
    NSString *_from=message.from;
    NSString *_msgId=message.messageId;
    NSNumber *_time=[NSNumber numberWithLong:message.timestamp];
    NSDictionary *result=@{@"from":_from,@"msgId":_msgId,@"time":_time,@"msg":_msg};
    if (hasListeners) {
        [self sendEventWithName:@"message" body:result];
    }
}
-(void)messageStatusDidChange:(EMMessage *)aMessage error:(EMError *)aError{
    [self sendEventWithName:@"update" body:@{@"date":@"update"}];
}
//注册
RCT_EXPORT_METHOD(_create:(NSString *)user pwd:(NSString *)pwd callback:(RCTResponseSenderBlock)callback)
{
    EMError *error = [[EMClient sharedClient] registerWithUsername:user password:pwd];
    if (error==nil) {
        NSString *data=@"注册成功";
        callback(@[[NSNull null], data]);
    }
}
//登录
RCT_REMAP_METHOD(_login,  user:(NSString *)user pwd:(NSString *)pwd resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    BOOL isAutoLogin = [EMClient sharedClient].options.isAutoLogin;
    if (!isAutoLogin) {
        EMError *error = [[EMClient sharedClient] loginWithUsername:user password:pwd];
        if (error==nil) {
            NSString *data=@"登录成功";
            resolve(data);
        }else{
            NSString *err=@"登录失败";
            reject(err,error.errorDescription,nil);
        }
    }
}
//登出
RCT_REMAP_METHOD(_logout,resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject ){
    EMError *error = [[EMClient sharedClient] logout:YES];
    if (error==nil) {
        NSString *data=@"登出成功";
        resolve(data);
    }else{
        NSString *err=@"登出失败";
        reject(err,error.errorDescription,nil);
    }
}
//发送文本文件
RCT_REMAP_METHOD(_sendText ,  content:(NSString *)content user:(NSString *)user resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMTextMessageBody *body = [[EMTextMessageBody alloc] initWithText:content];

    NSString *from = [[EMClient sharedClient] currentUsername];
    
    //生成Message
    EMMessage *message = [[EMMessage alloc] initWithConversationID:user from:from to:user body:body ext:nil];
    message.chatType = EMChatTypeChat;// 设置为单聊消息
    [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
        if (aError==nil) {
//            NSString *data=@"发送成功";
            if(aMessage.body.type==EMMessageBodyTypeText){
                EMTextMessageBody *textBody = (EMTextMessageBody *)aMessage.body;
                NSString *txt = textBody.text;
                resolve(txt);
            }
//            resolve(data);
        }else{
            NSString *err=@"发送失败";
            reject(err,aError.errorDescription,nil);
        }
    }];
}
//发送语音
RCT_REMAP_METHOD(_sendVoice ,  path:(NSString *)path length:(int)length user:(NSString *)user  resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMVoiceMessageBody *body = [[EMVoiceMessageBody alloc] initWithLocalPath:path displayName:@"audio"];
    body.duration = length ;
    NSString *from = [[EMClient sharedClient] currentUsername];
    
    // 生成message
    EMMessage *message = [[EMMessage alloc] initWithConversationID:user from:from to:user body:body ext:nil];
    message.chatType = EMChatTypeChat;// 设置为单聊消息
    [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
        if (aError==nil) {
            resolve(@{@"url":body.remotePath});
        }else{
            NSString *err=@"发送失败";
            reject(err,aError.errorDescription,nil);
        }
    }];
}
//发送视频
RCT_REMAP_METHOD(_sendVideo , path:(NSString *)path thumbPath:(NSString *)thumbPath length:(int)length user:(NSString *)user resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMVideoMessageBody *body = [[EMVideoMessageBody alloc] initWithLocalPath:path displayName:@"video.mp4"];
    body.thumbnailLocalPath=thumbPath;
    NSString *from = [[EMClient sharedClient] currentUsername];
    
    // 生成message
    EMMessage *message = [[EMMessage alloc] initWithConversationID:user from:from to:user body:body ext:nil];
    message.chatType = EMChatTypeChat;// 设置为单聊消息
    [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
        if (aError==nil) {
    
            resolve(@{@"url":body.remotePath});
        }else{
            NSString *err=@"发送失败";
            reject(err,aError.errorDescription,nil);
        }
    }];
}
//发送图片
RCT_REMAP_METHOD(_sendImage ,  path:(NSString *)path user:(NSString *)user resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
                    EMImageMessageBody *body = [[EMImageMessageBody alloc] initWithLocalPath:path displayName:@"image.png"];
                    NSString *from = [[EMClient sharedClient] currentUsername];
                    
                    //生成Message
                    EMMessage *message = [[EMMessage alloc] initWithConversationID:user from:from to:user body:body ext:nil];
                    message.chatType = EMChatTypeChat;// 设置为单聊消息
                    [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                        if (aError==nil) {
                            resolve(@{@"url":body.remotePath,@"tUrl":body.thumbnailRemotePath});
                        }else{
                            NSString *err=@"发送失败";
                            reject(err,aError.errorDescription,nil);
                        }
                    }];
            

    }


//发送我的位置
RCT_REMAP_METHOD(_sendLocation , latitude:(float)latitude longitude:(float)longitude address:(NSString *)address user:(NSString *)user resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMLocationMessageBody *body = [[EMLocationMessageBody alloc] initWithLatitude:latitude longitude:longitude address:address];
    NSString *from = [[EMClient sharedClient] currentUsername];
    
    // 生成message
    EMMessage *message = [[EMMessage alloc] initWithConversationID:user from:from to:user body:body ext:nil];
    message.chatType = EMChatTypeChat;// 设置为单聊消息
    [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
        if (aError==nil) {
            NSString *data=@"发送成功";
            resolve(data);
        }else{
            NSString *err=@"发送失败";
            reject(err,aError.errorDescription,nil);}
    }];
}
//获取所有好友
RCT_REMAP_METHOD(_getAllContacts , resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMError *error = nil;
    NSArray *userlist = [[EMClient sharedClient].contactManager getContactsFromServerWithError:&error];
    if (!error) {
        resolve(userlist);
    }else{
        NSString *err=@"发送失败";
        reject(err,error.errorDescription,nil);}
}
//开启消息监听
RCT_EXPORT_METHOD(_addMessageListener){
    [[EMClient sharedClient].chatManager addDelegate:self delegateQueue:nil];
}
//获取所有对话
RCT_REMAP_METHOD(_getAllConversations,  resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    NSArray *conversations = [[EMClient sharedClient].chatManager getAllConversations];
    NSMutableArray *array=[[NSMutableArray alloc] init];
    for(id msg in conversations){
        NSNumber *_unRead=[NSNumber numberWithInt:[msg unreadMessagesCount]];
        NSString *_name=[msg conversationId];
        EMMessage *message=[msg latestMessage];
        if(message==nil){
            
        }else{
            NSDictionary *_msg=[self getContent:message];
            NSString *_from=message.from;
            NSString *_msgId=message.messageId;
            NSNumber *_time=[NSNumber numberWithLong:message.timestamp];
            NSDictionary *obj=@{@"unRead":_unRead,@"name":_name,@"msg":_msg,@"msgId":_msgId,@"time":_time,@"from":_from};
            [array addObject:obj];
        }
        
        
        
        
    }
    resolve(array);
}

-(NSDictionary *) getContent:(EMMessage *) message{
    EMMessageBody *msgBody = message.body;
    NSString *_from=message.from;
    NSString *_msgId=message.messageId;
    NSNumber *_time=[NSNumber numberWithLong:message.timestamp];
    switch (msgBody.type) {
        case EMMessageBodyTypeText:
        {
            // 收到的文字消息
            EMTextMessageBody *textBody = (EMTextMessageBody *)msgBody;
            NSString *txt = textBody.text;
            return @{@"type":@"text",@"text":txt,@"from":_from,@"msgId":_msgId,@"time":_time};
        }
            break;
        case EMMessageBodyTypeImage:
        {
            // 得到一个图片消息body
            EMImageMessageBody *body = ((EMImageMessageBody *)msgBody);
            NSString *url=body.remotePath;
            NSString *tUrl=body.thumbnailRemotePath;
            return @{@"type":@"image",@"url":url,@"from":_from,@"msgId":_msgId,@"time":_time,@"tUrl":tUrl};
            
        }
            break;
        case EMMessageBodyTypeLocation:
        {
            EMLocationMessageBody *body = (EMLocationMessageBody *)msgBody;
            NSNumber* latitude=[NSNumber numberWithDouble:body.latitude];
            NSNumber* longitude=[NSNumber numberWithDouble:body.longitude];
            NSString *address=body.address;
            return @{@"type":@"location",@"latitude":latitude,@"longitude":longitude,@"address":address,@"from":_from,@"msgId":_msgId,@"time":_time};
        }
            break;
        case EMMessageBodyTypeVoice:
        {
            // 音频sdk会自动下载
            EMVoiceMessageBody *body = (EMVoiceMessageBody *)msgBody;
            NSString *localUrl=body.localPath;
            return @{@"type":@"voice",@"url":body.remotePath,@"from":_from,@"msgId":_msgId,@"time":_time,@"localUrl":localUrl};
        }
            break;
        case EMMessageBodyTypeVideo:
        {
            EMVideoMessageBody *body = (EMVideoMessageBody *)msgBody;
            if(body.downloadStatus==EMDownloadStatusSucceed){
                return @{@"type":@"video",@"url":body.remotePath,@"from":_from,@"msgId":_msgId,@"time":_time,@"tUrl":body.thumbnailRemotePath,@"localPath":body.localPath,@"success":@"1"};
            }
               return @{@"type":@"video",@"url":body.remotePath,@"from":_from,@"msgId":_msgId,@"time":_time,@"tUrl":body.thumbnailRemotePath,@"localPath":body.localPath};
        }
            break;
            
        default:
            return nil;
            break;
    }

}

//获取当前联系人聊天
RCT_REMAP_METHOD(_getConversation, user:(NSString *)user msgId:(nullable NSString*)msgId resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMConversation *conversation = [[EMClient sharedClient].chatManager getConversation:user type:EMConversationTypeChat createIfNotExist:YES];
    [conversation markAllMessagesAsRead:nil];
        [conversation loadMessagesStartFromId:nil count:40 searchDirection:EMMessageSearchDirectionUp completion:^(NSArray *aMessages, EMError *aError) {
            if(aError==nil){
                NSMutableArray *array=[[NSMutableArray alloc] init];
                for(id msg in aMessages ){
                    NSDictionary *_msg=[self getContent:msg];
                    [array addObject:_msg];
                }
                resolve(array);
            }else{
                NSString *err=@"发送失败";
                reject(err,aError.errorDescription,nil);
            }
        }];
   
}
//下载附件
RCT_REMAP_METHOD(_download, user:(NSString *)user  msgId:(NSString *)msgId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    EMConversation *conversation = [[EMClient sharedClient].chatManager getConversation:user type:EMConversationTypeChat createIfNotExist:YES];
    EMMessage *message =[conversation loadMessageWithId:msgId error:nil];
    [[EMClient sharedClient].chatManager downloadMessageAttachment:message progress:^(int progress){
        NSNumber *pro=[NSNumber numberWithInt:progress];
        if (hasListeners) {
            NSDictionary *body=@{@"progress":pro};
            [self sendEventWithName:@"download" body:body];
        }
    }  completion:^(EMMessage *message, EMError *error) {
        if (!error) {
            //            NSLog(@"下载成功，下载后的message是 -- %@",aMessage);
            resolve(@"下载完成");
        }
    }];
}
//获取账户下所有图片
RCT_REMAP_METHOD(_loadPics, user:(NSString *)user resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    EMConversation *conversation = [[EMClient sharedClient].chatManager getConversation:user type:EMConversationTypeChat createIfNotExist:YES];
    [conversation loadMessagesWithType:EMMessageBodyTypeImage timestamp:-100 count:20 fromUser:nil searchDirection:EMMessageSearchDirectionUp completion:^(NSArray *aMessages, EMError *aError) {
        if(aError==nil){
            NSMutableArray *array=[[NSMutableArray alloc] init];
            for(EMMessage *msg in aMessages){
                NSDictionary *_msg=[self getContent:msg];
                [array addObject:_msg];
            }
            resolve(array);
        }else{
            NSString *err=@"发送失败";
            reject(err,aError.errorDescription,nil);
        }
    }];
}
//搜索聊天记录
RCT_REMAP_METHOD(_searchText, user:(NSString *)user keyword:(NSString *)keyword resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject){
    EMConversation *conversation = [[EMClient sharedClient].chatManager getConversation:user type:EMConversationTypeChat createIfNotExist:YES];
    NSString *data=keyword;
    if(keyword.length<1){
        data=nil;
    }
    [conversation loadMessagesWithKeyword:data timestamp:-1 count:999 fromUser:nil searchDirection:EMMessageSearchDirectionUp completion:^(NSArray *aMessages, EMError *aError) {
        dispatch_async(dispatch_get_main_queue(), ^(void) {
            if(aError==nil){
                
                NSMutableArray *array=[[NSMutableArray alloc] init];
                for(EMMessage *msg in aMessages){
                    NSDictionary *_msg=[self getContent:msg];
                    [array addObject:_msg];
                }
                resolve(array);
            }else{
                NSString *err=@"发送失败";
                reject(err,aError.errorDescription,nil);}
        });
 
    }];
    
}
    
//删除消息
RCT_REMAP_METHOD(_deleteMsg, user:(NSString *)user msgId:(NSString *)msgId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    EMConversation *conversation = [[EMClient sharedClient].chatManager getConversation:user type:EMConversationTypeChat createIfNotExist:YES];
    [conversation deleteMessageWithId:msgId error:nil];
    resolve(@"删除成功");
}
//转发消息
RCT_REMAP_METHOD(_forwarding, user:(NSString *)user msgId:(NSString*)msgId toUser:(NSString*)toUser resolve:(RCTPromiseResolveBlock)resolve rejecte:(RCTPromiseRejectBlock)reject){
    EMConversation *conversation = [[EMClient sharedClient].chatManager getConversation:user type:EMConversationTypeChat createIfNotExist:YES];
    EMMessage *_message=[conversation loadMessageWithId:msgId error:nil];
     NSString *from = [[EMClient sharedClient] currentUsername];
    EMMessageBody *body =_message.body;
    switch (body.type) {
        case EMMessageBodyTypeText:
        {
            // 转发文字消息
            EMTextMessageBody *textBody = (EMTextMessageBody *)body;
            EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:textBody ext:nil];
            message.chatType = EMChatTypeChat;// 设置为单聊消息
            [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                if (aError==nil) {
                    NSString *data=@"发送成功";
                    resolve(data);
                }else{
                    NSString *err=@"发送失败";
                    reject(err,aError.errorDescription,nil);
                }
            }];
        }
            break;
        case EMMessageBodyTypeImage:
        {
            // 得到一个图片消息body
           
            EMImageMessageBody * imgbody=((EMImageMessageBody *) body);
            EMMessageStatus status=[_message status];
            if(status != EMDownloadStatusSucceed){
                [[EMClient sharedClient].chatManager downloadMessageAttachment:_message progress:^(int progress){
                }  completion:^(EMMessage *message, EMError *error) {
                    if (!error) {
                        
                        EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:imgbody ext:nil];
                        message.chatType = EMChatTypeChat;//
                        [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                            if (aError==nil) {
                                NSString *data=@"发送成功";
                                resolve(data);
                            }else{
                                NSString *err=@"发送失败";
                                reject(err,aError.errorDescription,nil);
                            }
                        }];
                    }
                }];
            }else{
                EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:imgbody ext:nil];
                message.chatType = EMChatTypeChat;//
                [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                    if (aError==nil) {
                        NSString *data=@"发送成功";
                        resolve(data);
                    }else{
                        NSString *err=@"发送失败";
                        reject(err,aError.errorDescription,nil);
                    }
                }];
            
            }
           
            
        }
            break;
        case EMMessageBodyTypeLocation:
        {
            EMLocationMessageBody *localBody = (EMLocationMessageBody *)body;
            EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:localBody ext:nil];
            message.chatType = EMChatTypeChat;//
            [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                if (aError==nil) {
                    NSString *data=@"发送成功";
                    resolve(data);
                }else{
                    NSString *err=@"发送失败";
                    reject(err,aError.errorDescription,nil);
                }
            }];

        }
            break;
        case EMMessageBodyTypeVoice:
        {
            // 音频sdk会自动下载
            EMVoiceMessageBody *voiceBody = (EMVoiceMessageBody *)body;
            EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:voiceBody ext:nil];
            message.chatType = EMChatTypeChat;//
            [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                if (aError==nil) {
                    NSString *data=@"发送成功";
                    resolve(data);
                }else{
                    NSString *err=@"发送失败";
                    reject(err,aError.errorDescription,nil);
                }
            }];
            
        }
            break;
        case EMMessageBodyTypeVideo:
        {
            EMVideoMessageBody *videoBody = (EMVideoMessageBody *)body;
            
            if(_message.status==EMDownloadStatusSucceed){
                EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:videoBody ext:nil];
                message.chatType = EMChatTypeChat;//
                [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                    if (aError==nil) {
                        NSString *data=@"发送成功";
                        resolve(data);
                    }else{
                        NSString *err=@"发送失败";
                        reject(err,aError.errorDescription,nil);
                    }
                }];
                
            }else{
                [[EMClient sharedClient].chatManager downloadMessageAttachment:_message progress:^(int progress){
                }  completion:^(EMMessage *message, EMError *error) {
                    if (!error) {
                        
                        EMMessage *message = [[EMMessage alloc] initWithConversationID:toUser from:from to:toUser body:videoBody ext:nil];
                        message.chatType = EMChatTypeChat;//
                        [[EMClient sharedClient].chatManager sendMessage:message progress:nil completion:^(EMMessage *aMessage, EMError *aError) {
                            if (aError==nil) {
                                NSString *data=@"发送成功";
                                resolve(data);
                            }else{
                                NSString *err=@"发送失败";
                                reject(err,aError.errorDescription,nil);
                            }
                        }];
                    }
                }];

            }
        }
            break;
            
        default:
   
            break;

    }
}
@end
