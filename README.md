# capacitor-bluetooth-thermal-printer

A Capacitor plugin for Bluetooth thermal printer communication on Android. This plugin provides a native interface to connect and print to thermal printers via Bluetooth using the Datecs SDK.

## Features

- 🔌 Connect to Bluetooth thermal printers
- 🖨️ Print images to thermal printers
- 📱 List paired Bluetooth devices
- ⚡ Fast and reliable printing with persistent connections
- 🔄 Automatic reconnection on print failures

## Installation

```bash
npm install capacitor-bluetooth-thermal-printer
npx cap sync
```

## Requirements

- **Android**: Minimum SDK 24 (Android 7.0)
- **Capacitor**: >= 8.0.0
- **Permissions**: The plugin requires Bluetooth permissions which are automatically added to your `AndroidManifest.xml`

## Usage

### Import the Plugin

```typescript
import { BtThPrinter } from 'capacitor-bluetooth-thermal-printer'
```

### Connect to a Printer

```typescript
try {
  await BtThPrinter.connect({ 
    address: '00:11:22:33:44:55' // Bluetooth MAC address
  })
  console.log('Connected successfully')
} catch (error) {
  console.error('Connection failed:', error)
}
```

### Print an Image

```typescript
try {
  // Base64 image string (with or without data URI prefix)
  const base64Image = 'data:image/png;base64,iVBORw0KGgo...'
  
  await BtThPrinter.printImage({
    image: base64Image,  // Base64 string (will be cleaned automatically)
    width: 384,         // Image width in pixels (384 = 48mm at 8px/mm)
    height: 0,          // Height (0 = auto, maintains aspect ratio)
    align: 1            // Alignment: 0=left, 1=center, 2=right
  })
  console.log('Print successful')
} catch (error) {
  console.error('Print failed:', error)
}
```

### List Bluetooth Devices

```typescript
try {
  const result = await BtThPrinter.listBluetoothDevices()
  const devices = result.devices || []
  
  devices.forEach(device => {
    console.log(`Device: ${device.name} - ${device.address}`)
  })
} catch (error) {
  console.error('Failed to list devices:', error)
}
```

### Complete Example: Print with Auto-Reconnect

```typescript
import { BtThPrinter } from 'capacitor-bluetooth-thermal-printer'

async function printToThermalPrinter(base64Images: string[], printerMac: string) {
  for (const image of base64Images) {
    try {
      // Try printing directly (if already connected)
      await BtThPrinter.printImage({
        image: image,
        width: 384,
        height: 0,
        align: 1
      })
    } catch (error) {
      // If print fails, try connecting first, then print again
      try {
        await BtThPrinter.connect({ address: printerMac })
        await BtThPrinter.printImage({
          image: image,
          width: 384,
          height: 0,
          align: 1
        })
      } catch (retryError) {
        console.error('Print failed after reconnect:', retryError)
        throw retryError
      }
    }
  }
}

// Usage
const images = ['data:image/png;base64,...', 'data:image/png;base64,...']
await printToThermalPrinter(images, '00:11:22:33:44:55')
```

## API Reference

### `connect(options)`

Connect to a Bluetooth thermal printer.

**Parameters:**
- `options.address` (string, required): Bluetooth MAC address of the printer (format: `XX:XX:XX:XX:XX:XX`)

**Returns:** `Promise<void>`

**Throws:** Error if connection fails

---

### `printImage(options)`

Print an image to the connected thermal printer.

**Parameters:**
- `options.image` (string, required): Base64 encoded image string (with or without `data:image/...;base64,` prefix)
- `options.width` (number, default: 384): Image width in pixels
  - `384` = 48mm at 8 pixels/mm (standard thermal printer width)
  - `288` = 36mm
  - `576` = 72mm
- `options.height` (number, default: 0): Image height in pixels (0 = auto, maintains aspect ratio)
- `options.align` (number, default: 1): Text alignment
  - `0` = Left
  - `1` = Center
  - `2` = Right

**Returns:** `Promise<void>`

**Throws:** Error if print fails

**Note:** The plugin automatically:
- Cleans the base64 string (removes data URI prefix if present)
- Scales images to fit the printer width if needed
- Maintains aspect ratio when height is 0

---

### `listBluetoothDevices()`

List all paired Bluetooth devices.

**Returns:** `Promise<{ devices: Array<{ name: string; address: string; type?: number; aliasName?: string }> }>`

**Example Response:**
```json
{
  "devices": [
    {
      "name": "InnerPrinter",
      "address": "00:11:22:33:44:55",
      "type": 0,
      "aliasName": "InnerPrinter"
    }
  ]
}
```

---

## Technical Details

### Datecs SDK

This plugin uses the **Datecs SDK** (`com.datecs.api.jar`) for Bluetooth thermal printer communication. The SDK provides low-level access to Datecs thermal printers and is wrapped in a Capacitor-compatible interface.

**Source Code Reference:**
- **Datecs SDK Wrapper**: `android/src/main/java/dev/abdrahim/cbtp/DatecsSDKWrapper.java`
- **Datecs Utilities**: `android/src/main/java/dev/abdrahim/cbtp/DatecsUtil.java`
- **Printer Interface**: `android/src/main/java/dev/abdrahim/cbtp/Printer.java`
- **SDK JAR**: `android/libs/com.datecs.api.jar`

The plugin implementation is based on the Datecs SDK API and handles:
- Bluetooth socket communication
- Image processing and scaling
- ESC/POS command generation
- Error handling and reconnection logic

### Image Processing

Images are automatically processed before printing:
1. Base64 string is cleaned (data URI prefix removed if present)
2. Image is decoded to a bitmap
3. Image is scaled to fit the printer width (max 384px, rounded to multiple of 8)
4. Aspect ratio is maintained
5. Image is converted to grayscale and dithered using Floyd-Steinberg algorithm
6. Bitmap is compressed and sent to the printer

### Connection Management

The plugin maintains a persistent connection to the printer for faster subsequent prints. If a print fails, the plugin will automatically attempt to reconnect before retrying.

## Permissions

The following permissions are automatically added to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
```

## Error Handling

The plugin throws errors with descriptive messages. Common error scenarios:

- **Connection failures**: Invalid MAC address, device not found, or connection timeout
- **Print failures**: Image processing errors, printer not connected, or communication errors
- **Device listing failures**: Bluetooth not available or permission denied

Always wrap plugin calls in try-catch blocks for proper error handling.

## Troubleshooting

### Devices not appearing in list
- Ensure Bluetooth is enabled on the device
- Make sure the printer is paired with the Android device
- Check that Bluetooth permissions are granted

### Print fails immediately
- Verify the printer MAC address is correct
- Ensure the printer is powered on and in range
- Try connecting manually first, then print

### Image appears garbled or incorrect
- Ensure image width is appropriate for your printer (384px for 48mm printers)
- Check that the base64 image is valid
- Verify image format is supported (PNG, JPEG)

## License

MIT

## Author

AitaddiAb

## Repository

[GitHub Repository](https://github.com/AitaddiAb/capacitor-bluetooth-thermal-printer)
