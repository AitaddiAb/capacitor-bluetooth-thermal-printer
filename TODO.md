# TODO - Capacitor Bluetooth Thermal Printer

Future features and improvements for the Capacitor Bluetooth Thermal Printer plugin.

## 📊 Progress Tracking

### **Completed**: 5 / 28 (18%)

### **Remaining**: 23 / 28 (82%)

## 🔨 Currently working on:

### (Nothing Planned Yet)

## 📅 Next planned features:

### (Nothing Planned Yet)

---

## 🔌 Core Functionality

| 📋  | Description                                                                |
| --- | -------------------------------------------------------------------------- |
| ✅  | **Bluetooth Connection**: Connect to Bluetooth thermal printers            |
| ✅  | **Image Printing**: Print images to thermal printers                       |
| ✅  | **Device Listing**: List paired Bluetooth devices                          |
| ☑️  | **Text Printing**: Add support for printing text                           |
| ☑️  | **Automatic Image Processing**: Auto-clean base64 and calculate dimensions |

---

## 🛠️ SDK & Dependencies

| 📋  | Description                                                                         |
| --- | ----------------------------------------------------------------------------------- |
| ☑️  | **Remove Datecs SDK Dependency**: Investigate alternatives or native implementation |
| ☑️  | **SDK Optimization**: Optimize SDK usage and reduce dependencies                    |

---

## 🔐 Permissions & Security

| 📋  | Description                                                                  |
| --- | ---------------------------------------------------------------------------- |
| ☑️  | **Permission Manager**: Add permission checking and requesting functionality |
| ☑️  | **Runtime Permissions**: Handle Android runtime permission requests          |
| ☑️  | **Permission Status**: Check current permission status                       |

---

## 🖼️ Image Processing

| 📋  | Description                                                                      |
| --- | -------------------------------------------------------------------------------- |
| ☑️  | **Automatic Width Calculation**: Auto-detect optimal image width for printer     |
| ☑️  | **Automatic Height Calculation**: Auto-calculate height maintaining aspect ratio |
| ☑️  | **Base64 Auto-Clean**: Automatically clean base64 strings in printImage method   |

---

## 📝 Text Printing Features

| 📋  | Description                                               |
| --- | --------------------------------------------------------- |
| ☑️  | **Print Text**: Basic text printing functionality         |
| ☑️  | **Text Formatting**: Support for bold, italic, underline  |
| ☑️  | **Text Alignment**: Left, center, right alignment support |
| ☑️  | **Font Size**: Support for different font sizes           |
| ☑️  | **Line Breaks**: Handle line breaks and multi-line text   |

---

## 🎯 Platform Support

| 📋  | Description                                           |
| --- | ----------------------------------------------------- |
| ✅  | **Android Support**: Full Android implementation      |
| ☑️  | **iOS Support**: Add iOS implementation (if possible) |
| ☑️  | **Web Support**: Web fallback or mock implementation  |

---

## 🐛 Bug Fixes & Improvements

| 📋  | Description                               |
| --- | ----------------------------------------- |
| ☑️  | Monitor and fix any reported bugs         |
| ☑️  | Improve error handling and error messages |
| ☑️  | Add comprehensive logging for debugging   |
| ☑️  | Performance optimization                  |
| ☑️  | Code refactoring and cleanup              |

---

## 📚 Documentation

| 📋  | Description                                                              |
| --- | ------------------------------------------------------------------------ |
| ✅  | **README Documentation**: Complete usage documentation and API reference |
| ☑️  | **Code Documentation**: Inline code documentation and JSDoc comments     |

---

## Notes

- The plugin currently uses the Datecs SDK (`com.datecs.api.jar`) for printer communication
- Consider alternatives to Datecs SDK if possible to reduce dependencies
- Permission management is crucial for Android 12+ devices
- Automatic image processing will improve developer experience
- Text printing is a highly requested feature for thermal printers
