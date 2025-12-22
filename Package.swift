// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorBluetoothThermalPrinter",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorBluetoothThermalPrinter",
            targets: ["BtThPrinterPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "BtThPrinterPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/BtThPrinterPlugin"),
        .testTarget(
            name: "BtThPrinterPluginTests",
            dependencies: ["BtThPrinterPlugin"],
            path: "ios/Tests/BtThPrinterPluginTests")
    ]
)
