import { NativeModules, NativeEventEmitter } from 'react-native';

const { LunaIpPrinterScan } = NativeModules;

class IpPrinterScanner {
  static listen(callback) {
    const eventEmitter = new NativeEventEmitter(NativeModules.LunaIpPrinterScan);
    return eventEmitter.addListener('IpPrinter', callback);
  }
  static scan() {
    LunaIpPrinterScan.scan(); 
  }
}

export default IpPrinterScanner;

