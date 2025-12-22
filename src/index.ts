import { registerPlugin } from '@capacitor/core';

import type { BtThPrinterPlugin } from './definitions';

const BtThPrinter = registerPlugin<BtThPrinterPlugin>('BtThPrinter', {
  web: () => import('./web').then((m) => new m.BtThPrinterWeb()),
});

export * from './definitions';
export { BtThPrinter };
