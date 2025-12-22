import { BtThPrinter } from 'capacitor-bluetooth-thermal-printer';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    BtThPrinter.echo({ value: inputValue })
}
