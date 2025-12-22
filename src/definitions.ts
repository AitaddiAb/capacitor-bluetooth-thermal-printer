export interface BtThPrinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
