#import "EventWindow.h"
#import "KeyEventListener.h"

@implementation EventWindow

- (void)pressesBegan:(NSSet<UIPress *> *)presses withEvent:(UIPressesEvent *)event {
  KeyEventListener *listener = [KeyEventListener shared];
  if (listener) {
    for (UIPress *press in presses) {
      [listener handlePress:press action:@"DOWN"];
    }
  }
  [super pressesBegan:presses withEvent:event];
}

- (void)pressesEnded:(NSSet<UIPress *> *)presses withEvent:(UIPressesEvent *)event {
  KeyEventListener *listener = [KeyEventListener shared];
  if (listener) {
    for (UIPress *press in presses) {
      [listener handlePress:press action:@"UP"];
    }
  }
  [super pressesEnded:presses withEvent:event];
}

- (void)pressesCancelled:(NSSet<UIPress *> *)presses withEvent:(UIPressesEvent *)event {
  [super pressesCancelled:presses withEvent:event];
}

- (void)sendEvent:(UIEvent *)event {
  if (event.type == UIEventTypeTouches) {
    KeyEventListener *listener = [KeyEventListener shared];
    if (listener) {
      NSSet<UITouch *> *touches = [event allTouches];
      for (UITouch *touch in touches) {
        [listener handleTouch:touch];
      }
    }
  }
  [super sendEvent:event];
}

@end
