import { WebPlugin } from '@capacitor/core';

import type { BtThPrinterPlugin } from './definitions';

export class BtThPrinterWeb extends WebPlugin implements BtThPrinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
