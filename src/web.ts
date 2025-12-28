import { WebPlugin } from '@capacitor/core';

import type { BtThPrinterPlugin } from './definitions';

export class BtThPrinterWeb extends WebPlugin implements BtThPrinterPlugin {
  async connect(_options: { address: string }): Promise<void> {
    throw this.unimplemented('connect is not implemented on web');
  }

  async printImage(_options: { image: string; width: number; height: number; align: number }): Promise<void> {
    throw this.unimplemented('printImage is not implemented on web');
  }

  async listBluetoothDevices(): Promise<{ devices: Array<{ name: string; address: string }> }> {
    throw this.unimplemented('listBluetoothDevices is not implemented on web');
  }
}
