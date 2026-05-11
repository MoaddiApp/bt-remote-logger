#import "AppDelegate.h"
#import "EventWindow.h"

#import <React/RCTBundleURLProvider.h>

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.moduleName = @"BtRemoteLogger";
  self.initialProps = @{};

  BOOL result = [super application:application didFinishLaunchingWithOptions:launchOptions];

  UIWindow *oldWindow = self.window;
  if (oldWindow && ![oldWindow isKindOfClass:[EventWindow class]]) {
    EventWindow *ew = nil;
    if (@available(iOS 13.0, *)) {
      UIWindowScene *scene = oldWindow.windowScene;
      if (scene) {
        ew = [[EventWindow alloc] initWithWindowScene:scene];
      }
    }
    if (!ew) {
      ew = [[EventWindow alloc] initWithFrame:oldWindow.frame];
    }
    ew.rootViewController = oldWindow.rootViewController;
    ew.backgroundColor = oldWindow.backgroundColor;
    self.window = ew;
    [ew makeKeyAndVisible];
  }

  return result;
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
  return [self bundleURL];
}

- (NSURL *)bundleURL
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

@end
