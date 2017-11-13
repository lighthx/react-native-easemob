
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif
#import <HyphenateLite/HyphenateLite.h>
#import <React/RCTEventEmitter.h>
@interface RNReactNativeEasemob : RCTEventEmitter <RCTBridgeModule,EMChatManagerDelegate>

@end
  
