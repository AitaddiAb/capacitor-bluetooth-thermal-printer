export interface BtThPrinterPlugin {
  connect(options: { address: string }): Promise<void>;
  printImage(options: { image: string; width: number; height: number; align: number }): Promise<void>;
  listBluetoothDevices(): Promise<{ devices: Array<{ name: string; address: string }> }>;
}
