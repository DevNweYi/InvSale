package com.bosictsolution.invsale.bluetooth.print;

/**
 * Created by yefeng on 6/1/15.
 * github:yefengfreedom
 * <p/>
 * printNetworkPrinter message event
 */
public class PrintMsgEvent {
    public int type;
    public String msg;

    public PrintMsgEvent(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }
}
