package edu.wisc.cs.sdn.vnet.rt;

import java.nio.ByteBuffer;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.packet.ARP;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;

	/** ARP Manager */
	private ArpManager arpManager;
	
	/** RIP Manager */
	private RipManager ripManager;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
		this.arpManager = new ArpManager(this, arpCache);
		this.ripManager = null;
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
	}

	public void enableRip() 
	{ 
		System.out.println("RIP enabled");
		this.ripManager = new RipManager(this); 
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
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		switch(etherPacket.getEtherType())
		{
		case Ethernet.TYPE_IPv4:
			this.handleIpPacket(etherPacket, inIface);
			break;
		case Ethernet.TYPE_ARP:
			this.handleArpPacket(etherPacket, inIface);
		// Ignore all other packet types, for now
		}
		
		/********************************************************************/
	}
	
	private void handleIpPacket(Ethernet etherPacket, Iface inIface)
	{
		// Make sure it's an IP packet
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4)
		{ return; }
		
		// Get IP header
		IPv4 ipPacket = (IPv4)etherPacket.getPayload();
        System.out.println("Handle IP packet");

        // Verify checksum
        short origCksum = ipPacket.getChecksum();
        ipPacket.resetChecksum();
        byte[] serialized = ipPacket.serialize();
        ipPacket.deserialize(serialized, 0, serialized.length);
        short calcCksum = ipPacket.getChecksum();
        if (origCksum != calcCksum)
        { return; }
        
        // Check TTL
        ipPacket.setTtl((byte)(ipPacket.getTtl()-1));
        if (0 == ipPacket.getTtl())
		{ 
			this.sendPacket(Wrapper.makeICMPPacket(inIface, etherPacket, 11, 0), inIface);
			return; 
		}
        
        // Reset checksum now that TTL is decremented
		ipPacket.resetChecksum();
		
		// check if an arriving IP packet has a destination 224.0.0.9, 
		// a protocol type of UDP, 
		// and a UDP destination port of 520.
		Boolean ripFlag = ripManager != null && 			
			ipPacket.getProtocol() == IPv4.PROTOCOL_UDP &&
			((UDP)ipPacket.getPayload()).getDestinationPort() == UDP.RIP_PORT;
		
		if (ripFlag && ipPacket.getDestinationAddress() == IPv4.toIPv4Address("224.0.0.9")) {
			System.out.println("Recv RIP packet (1)");
			ripManager.handlePacket(etherPacket, inIface);
			return;
		}

        // Check if packet is destined for one of router's interfaces
        for (Iface iface : this.interfaces.values())
        {
        	if (ipPacket.getDestinationAddress() == iface.getIpAddress())
			{ 
				switch (ipPacket.getProtocol()) {
					case IPv4.PROTOCOL_UDP:
						if (ripFlag) {
							System.out.println("Recv RIP packet (2)");
							ripManager.handlePacket(etherPacket, inIface);
							return;
						}
					case IPv4.PROTOCOL_TCP: 
						this.sendPacket(Wrapper.makeICMPPacket(inIface, etherPacket, 3, 3), inIface);
						break;
					case IPv4.PROTOCOL_ICMP:
						ICMP icmp = (ICMP) ipPacket.getPayload();
						if (icmp.getIcmpType() == 8)
							this.sendPacket(Wrapper.makeICMPEchoPacket(inIface, etherPacket), inIface);							
				}
				return; 			
			}
        }
		
        // Do route lookup and forward
        this.forwardIpPacket(etherPacket, inIface);
	}

    private void forwardIpPacket(Ethernet etherPacket, Iface inIface)
    {
        // Make sure it's an IP packet
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4)
		{ return; }
        System.out.println("Forward IP packet");
		
		// Get IP header
		IPv4 ipPacket = (IPv4)etherPacket.getPayload();
        int dstAddr = ipPacket.getDestinationAddress();

        // Find matching route table entry 
        RouteEntry bestMatch = this.routeTable.lookup(dstAddr);

        // If no entry matched, do nothing
        if (null == bestMatch)
        { 
			this.sendPacket(Wrapper.makeICMPPacket(inIface, etherPacket, 3, 0), inIface);
			return; 
		}

        // Make sure we don't sent a packet back out the interface it came in
        Iface outIface = bestMatch.getInterface();
        if (outIface == inIface)
        { return; }

        // Set source MAC address in Ethernet header
        etherPacket.setSourceMACAddress(outIface.getMacAddress().toBytes());

        // If no gateway, then nextHop is IP destination
        int nextHop = bestMatch.getGatewayAddress();
        if (0 == nextHop)
        { nextHop = dstAddr; }

        // Set destination MAC address in Ethernet header
        ArpEntry arpEntry = this.arpCache.lookup(nextHop);
        if (null == arpEntry)
		{ 
			// this.sendPacket(Wrapper.makeICMPPacket(inIface, etherPacket, 3, 1), inIface);
			arpManager.generateRequest(etherPacket, inIface, nextHop, outIface);
			return; 
		}
        etherPacket.setDestinationMACAddress(arpEntry.getMac().toBytes());
        
        this.sendPacket(etherPacket, outIface);
    }

	private void handleArpPacket(Ethernet etherPacket, Iface inIface) 
	{
		ARP arpPacket = (ARP) etherPacket.getPayload();
		int targetIp = ByteBuffer.wrap(arpPacket.getTargetProtocolAddress()).getInt();
		switch (arpPacket.getOpCode()) {
			case ARP.OP_REQUEST:
				if (targetIp == inIface.getIpAddress())
					this.sendPacket(Wrapper.makeArpReplyPacket(inIface, etherPacket), inIface);
				break;
			case ARP.OP_REPLY:
				this.arpManager.handleReply(etherPacket, inIface);
		}
	}	
}
