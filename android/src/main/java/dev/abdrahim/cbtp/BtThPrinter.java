package dev.abdrahim.cbtp;

import com.getcapacitor.Logger;

public class BtThPrinter {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
