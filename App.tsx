import React, {useEffect, useState, useRef} from 'react';
import {
  StyleSheet,
  Text,
  View,
  FlatList,
  TouchableOpacity,
  Platform,
  StatusBar,
  Alert,
  SafeAreaView,
  NativeModules,
  NativeEventEmitter,
} from 'react-native';

const {KeyEventListener} = NativeModules;
const keyEventEmitter = new NativeEventEmitter(KeyEventListener);

interface LogEntry {
  id: string;
  timestamp: number;
  keyCode: number;
  keyName: string;
  action: string;
  source: string;
  deviceName?: string;
  scanCode?: number;
}

export default function App() {
  const [events, setEvents] = useState<LogEntry[]>([]);
  const [isActive, setIsActive] = useState(false);
  const [lastButton, setLastButton] = useState<string | null>(null);
  const eventCounter = useRef(0);

  useEffect(() => {
    const subscription = keyEventEmitter.addListener('onKeyEvent', (event: any) => {
      if (event.action !== 'KEY_DOWN') return;

      eventCounter.current += 1;
      const entry: LogEntry = {
        id: `${eventCounter.current}`,
        timestamp: event.timestamp,
        keyCode: event.keyCode,
        keyName: event.keyName,
        action: event.action,
        source: event.source,
        deviceName: event.deviceName,
        scanCode: event.scanCode,
      };

      setEvents(prev => [entry, ...prev].slice(0, 200));
      setLastButton(event.keyName);

      Alert.alert(
        'Button Pressed!',
        `${getButtonLabel(event.keyName)}\n\nKey: ${event.keyName}\nCode: ${event.keyCode}\nSource: ${event.source}`,
        [{text: 'OK'}],
        {cancelable: true},
      );
    });

    return () => {
      subscription.remove();
      KeyEventListener?.stopListening();
    };
  }, []);

  const handleToggle = () => {
    if (isActive) {
      KeyEventListener.stopListening();
      setIsActive(false);
    } else {
      KeyEventListener.startListening();
      setIsActive(true);
    }
  };

  const handleClear = () => {
    setEvents([]);
    setLastButton(null);
    eventCounter.current = 0;
  };

  const getButtonLabel = (keyName: string): string => {
    const labels: Record<string, string> = {
      DPAD_UP: 'Arrow Up button pressed',
      DPAD_DOWN: 'Arrow Down button pressed',
      DPAD_LEFT: 'Arrow Left button pressed',
      DPAD_RIGHT: 'Arrow Right button pressed',
      DPAD_CENTER: 'D-Pad Center button pressed',
      ENTER: 'Enter button pressed',
      VOLUME_UP: 'Volume Up / Camera button pressed',
      VOLUME_DOWN: 'Volume Down button pressed',
      MEDIA_PLAY_PAUSE: 'Media Play/Pause button pressed',
      MEDIA_NEXT: 'Media Next button pressed',
      MEDIA_PREVIOUS: 'Media Previous button pressed',
      CAMERA: 'Camera button pressed',
      SPACE: 'Space button pressed',
      TAB: 'Tab button pressed',
      ESCAPE: 'Escape button pressed',
      PAGE_UP: 'Page Up button pressed',
      PAGE_DOWN: 'Page Down button pressed',
      BACK: 'Back button pressed',
      SEARCH: 'Search button pressed',
    };
    return labels[keyName] || `${keyName} button pressed`;
  };

  const getButtonColor = (keyName: string): string => {
    if (keyName.startsWith('DPAD')) return '#4CAF50';
    if (keyName.startsWith('VOLUME')) return '#FF9800';
    if (keyName.startsWith('MEDIA')) return '#9C27B0';
    if (keyName === 'ENTER') return '#2196F3';
    if (keyName === 'CAMERA') return '#FF5722';
    return '#607D8B';
  };

  const formatTime = (ts: number): string => {
    const d = new Date(ts);
    return `${d.getHours().toString().padStart(2, '0')}:${d
      .getMinutes()
      .toString()
      .padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}.${d
      .getMilliseconds()
      .toString()
      .padStart(3, '0')}`;
  };

  const renderEvent = ({item}: {item: LogEntry}) => (
    <View style={[styles.eventRow, {borderLeftColor: getButtonColor(item.keyName)}]}>
      <View style={styles.eventHeader}>
        <Text style={styles.eventKeyName}>{item.keyName}</Text>
        <Text style={styles.eventTime}>{formatTime(item.timestamp)}</Text>
      </View>
      <View style={styles.eventDetails}>
        <Text style={styles.eventDetail}>Code: {item.keyCode}</Text>
        {item.scanCode !== undefined && item.scanCode !== 0 && (
          <Text style={styles.eventDetail}>Scan: {item.scanCode}</Text>
        )}
        <Text style={styles.eventDetail}>Src: {item.source}</Text>
        {item.deviceName && item.deviceName !== 'unknown' && (
          <Text style={styles.eventDetail}>Dev: {item.deviceName}</Text>
        )}
      </View>
      <Text style={styles.eventLabel}>{getButtonLabel(item.keyName)}</Text>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      <View style={styles.header}>
        <Text style={styles.title}>BT Remote Logger</Text>
        <Text style={styles.subtitle}>
          {Platform.OS === 'ios' ? 'iOS' : 'Android'} | React Native
        </Text>
      </View>

      <View style={styles.statusBar}>
        <View style={[styles.statusDot, isActive ? styles.dotActive : styles.dotInactive]} />
        <Text style={styles.statusText}>
          {isActive ? 'Listening for key events...' : 'Tap Start to begin'}
        </Text>
        <Text style={styles.eventCount}>{events.length} events</Text>
      </View>

      {lastButton && (
        <View style={[styles.lastButtonBox, {backgroundColor: getButtonColor(lastButton) + '20', borderColor: getButtonColor(lastButton)}]}>
          <Text style={styles.lastButtonLabel}>Last Detected:</Text>
          <Text style={[styles.lastButtonName, {color: getButtonColor(lastButton)}]}>
            {getButtonLabel(lastButton)}
          </Text>
        </View>
      )}

      <View style={styles.controls}>
        <TouchableOpacity
          style={[styles.button, isActive ? styles.buttonStop : styles.buttonStart]}
          onPress={handleToggle}>
          <Text style={styles.buttonText}>{isActive ? 'Stop' : 'Start'}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.buttonClear} onPress={handleClear}>
          <Text style={styles.buttonText}>Clear Log</Text>
        </TouchableOpacity>
      </View>

      {events.length === 0 && isActive && (
        <View style={styles.instructions}>
          <Text style={styles.instructionTitle}>Ready to capture!</Text>
          <Text style={styles.instructionText}>
            1. Make sure your BT remote is paired{'\n'}
            2. Press any button on the remote{'\n'}
            3. Events will appear below with details{'\n'}
            4. An alert will pop up for each press
          </Text>
        </View>
      )}

      <FlatList
        data={events}
        renderItem={renderEvent}
        keyExtractor={item => item.id}
        style={styles.eventList}
        contentContainerStyle={styles.eventListContent}
        ListEmptyComponent={
          !isActive ? (
            <View style={styles.emptyState}>
              <Text style={styles.emptyText}>Press "Start" and use your Bluetooth remote</Text>
            </View>
          ) : null
        }
      />

      <View style={styles.footer}>
        <Text style={styles.footerText}>BT Remote Event Logger v1.0 | {Platform.OS.toUpperCase()}</Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {flex: 1, backgroundColor: '#1a1a2e'},
  header: {paddingHorizontal: 20, paddingTop: 16, paddingBottom: 12, borderBottomWidth: 1, borderBottomColor: '#2a2a4e'},
  title: {fontSize: 24, fontWeight: 'bold', color: '#e0e0ff'},
  subtitle: {fontSize: 13, color: '#8888aa', marginTop: 2},
  statusBar: {flexDirection: 'row', alignItems: 'center', paddingHorizontal: 20, paddingVertical: 10, backgroundColor: '#16162b'},
  statusDot: {width: 10, height: 10, borderRadius: 5, marginRight: 8},
  dotActive: {backgroundColor: '#4CAF50'},
  dotInactive: {backgroundColor: '#666'},
  statusText: {flex: 1, color: '#aaa', fontSize: 14},
  eventCount: {color: '#666', fontSize: 12},
  lastButtonBox: {marginHorizontal: 20, marginTop: 12, padding: 14, borderRadius: 10, borderWidth: 1},
  lastButtonLabel: {fontSize: 11, color: '#888', textTransform: 'uppercase', letterSpacing: 1},
  lastButtonName: {fontSize: 20, fontWeight: 'bold', marginTop: 4},
  controls: {flexDirection: 'row', paddingHorizontal: 20, paddingVertical: 12, gap: 10},
  button: {flex: 1, paddingVertical: 12, borderRadius: 8, alignItems: 'center'},
  buttonStart: {backgroundColor: '#4CAF50'},
  buttonStop: {backgroundColor: '#f44336'},
  buttonClear: {flex: 1, paddingVertical: 12, borderRadius: 8, alignItems: 'center', backgroundColor: '#333355'},
  buttonText: {color: '#fff', fontWeight: 'bold', fontSize: 16},
  instructions: {marginHorizontal: 20, padding: 14, backgroundColor: '#22224a', borderRadius: 10, marginBottom: 8},
  instructionTitle: {color: '#e0e0ff', fontWeight: 'bold', fontSize: 15, marginBottom: 6},
  instructionText: {color: '#999', fontSize: 13, lineHeight: 20},
  eventList: {flex: 1, paddingHorizontal: 20},
  eventListContent: {paddingBottom: 20},
  eventRow: {backgroundColor: '#22224a', borderRadius: 8, padding: 12, marginBottom: 8, borderLeftWidth: 4},
  eventHeader: {flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'},
  eventKeyName: {color: '#e0e0ff', fontWeight: 'bold', fontSize: 16, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace'},
  eventTime: {color: '#666', fontSize: 12, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace'},
  eventDetails: {flexDirection: 'row', flexWrap: 'wrap', marginTop: 4, gap: 8},
  eventDetail: {color: '#8888aa', fontSize: 12, fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace'},
  eventLabel: {color: '#aaaacc', fontSize: 13, marginTop: 6, fontStyle: 'italic'},
  emptyState: {paddingVertical: 40, alignItems: 'center'},
  emptyText: {color: '#555', fontSize: 14},
  footer: {paddingVertical: 8, alignItems: 'center', borderTopWidth: 1, borderTopColor: '#2a2a4e'},
  footerText: {color: '#444', fontSize: 11},
});
