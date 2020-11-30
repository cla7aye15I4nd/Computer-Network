package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{	
    /**
     * Creates a router for a specific host.
     * @param host hostname for the router
     */
    public Switch(String host, DumpFile logfile)
    {
	super(host,logfile);
    }
    
    /**
     * Handle an Ethernet packet received on a specific interface.
     * @param etherPacket the Ethernet packet that was received
     * @param inIface the interface on which the packet was received
     */
    public void handlePacket(Ethernet etherPacket, Iface inIface)
    {
	System.out.println("*** -> Received packet: " +
			   etherPacket.toString().replace("\n", "\n\t"));
	

	for (Map.Entry<MACAddress, Entry> entry : new HashSet<>(switching_table.entrySet())) {
	    if (System.currentTimeMillis() - entry.getValue().timestamp > TIME_LIMIT)
		switching_table.remove(entry.getKey());
	}

	Entry src = switching_table.get(etherPacket.getSourceMAC());
	Entry dst = switching_table.get(etherPacket.getDestinationMAC());

	if (src == null) 
	    switching_table.put(etherPacket.getSourceMAC(), new Entry(inIface.getName()));
	else
	    src.timestamp = System.currentTimeMillis();

	if (dst == null) {
	    for (Iface iface: interfaces.values()) {
		if (!iface.getName().equals(inIface.getName()))
		    super.sendPacket(etherPacket, iface);
	    }
	} else
	    super.sendPacket(etherPacket, interfaces.get(dst.iface));
	    
    }
    
    private final long TIME_LIMIT = 15000;
    private HashMap<MACAddress, Entry> switching_table = new HashMap<MACAddress, Entry> ();

    private class Entry{
	String iface;
	long timestamp;
	
	Entry (String iface) {
	    this.iface = iface;
	    this.timestamp = System.currentTimeMillis();
	}

	@Override
	public String toString() {
	    return String.format("Entry(%s, %d)", iface, timestamp);
	}
    };
}
