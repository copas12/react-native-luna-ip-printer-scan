# react-native-luna-ip-printer-scan

## Getting started

`$ yarn add github:copas12/react-native-luna-ip-printer-scan`

### Mostly automatic installation

`$ npx react-native link @luna/ip-printer-scan`

## Usage

```javascript
import LunaIpPrinterScan from '@luna/ip-printer-scan';

...
eventListener: any;
state = {
  printers: [];
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
