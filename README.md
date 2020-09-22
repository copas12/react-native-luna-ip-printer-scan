# react-native-luna-ip-printer-scan

## Getting started

`$ npm install react-native-luna-ip-printer-scan --save`

### Mostly automatic installation

`$ react-native link react-native-luna-ip-printer-scan`

## Usage

```javascript
import LunaIpPrinterScan from '@luna/ip-printer-scan';

...
eventListener: any;
state = {
  ip: [];
}

componentWillMount() {
   this.eventListener = LunaIpPrinterScanner.listen((event) => {
      console.log({ event }); // "someValue"
      const currPrinters = this.state.printers;
      currPrinters.push(event.ip);
      this.setState({ printers: currPrinters });
    });
}

componentWillUnmount() {
    this.eventListener.remove(); //Removes the listener
}

...
// trigger scan
LunaIpPrinterScan.scan();
```
