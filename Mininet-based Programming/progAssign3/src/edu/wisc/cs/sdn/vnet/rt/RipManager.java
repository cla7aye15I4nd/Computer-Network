package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.RIPv2;
import net.floodlightcontroller.packet.RIPv2Entry;

public class RipManager {
    public final static long TIME_LIMIT = 30000;
    private Router router;

    RipManager (Router router) {
        this.router = router;

        // Add entries to the route table for the subnets 
        // that are directly reachable via the router’s interfaces
        RouteTable table = router.getRouteTable();
        for (Iface iface: router.getInterfaces().values()) {
            int mask = iface.getSubnetMask();
            table.insert(mask & iface.getIpAddress(), 0, mask, iface);
        }


        // Send a RIP request out all of the router’s interfaces when RIP is initialized
        for (Iface iface: router.getInterfaces().values()) 
            router.sendPacket(Wrapper.makeRipRequestPacket(iface), iface);

        // Send an unsolicited RIP response out all of the router’s interfaces 
        // every 10 seconds thereafter
        new Thread(new Runnable() {
            @Override
            public void run() {                                    
                while (true) {
                    boardcastRouteTable(table);
                    try { Thread.sleep(10 * 1000); } 
                    catch (InterruptedException e) { /* empty */ } 
                }
            }
        }).start();
    }

    void boardcastRouteTable(RouteTable table) {
        RIPv2 ripPacket = Wrapper.makeRipReponsePacketHook(table);
        for (Iface iface: router.getInterfaces().values()) 
            router.sendPacket(Wrapper.makeRipReponsePacket(iface, ripPacket, 
                                "FF:FF:FF:FF:FF:FF", IPv4.toIPv4Address("224.0.0.9")), iface);
    }

    void handlePacket(Ethernet etherPacket, Iface iface) {
        byte command = ((RIPv2) etherPacket.getPayload().getPayload().getPayload()).getCommand();
        switch (command) {
            case RIPv2.COMMAND_REQUEST:
                handleRequestPacket(etherPacket, iface);
                break;
            case RIPv2.COMMAND_RESPONSE:
                handleResponsePacket(etherPacket, iface);
                break;
        }
    }

    void handleRequestPacket(Ethernet etherPacket, Iface iface) {
        // When sending a RIP response for a specific RIP request, 
        // the destination IP address and destination Ethernet address should be 
        // the IP address and MAC address of the router interface that sent the request
        RouteTable table = router.getRouteTable();
        RIPv2 ripPacket = Wrapper.makeRipReponsePacketHook(table);
        router.sendPacket(Wrapper.makeRipReponsePacket(iface, ripPacket, 
                            etherPacket.getSourceMACAddress().toString(), 
                            ((IPv4)etherPacket.getPayload()).getSourceAddress()), iface);
    }

    void handleResponsePacket(Ethernet etherPacket, Iface iface) {
        boolean flag = false;
        RouteTable table = router.getRouteTable();

        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        RIPv2 ripPacket = (RIPv2) etherPacket.getPayload().getPayload().getPayload().getPayload();
        for (RIPv2Entry entry: ripPacket.getEntries()) {            
            int ip = entry.getAddress();
            int mask = entry.getSubnetMask();
            int gateway = ipPacket.getSourceAddress();
            
            int metric = entry.getMetric();
            RouteEntry routeEntry = table.lookup(ip & mask);

            if (routeEntry == null) {
                flag = true;
                table.ripInsert(ip, gateway, mask, iface, metric+1, System.currentTimeMillis());
            } else {
                if (routeEntry.metric > metric + 1) {
                    flag = true;
                    routeEntry.metric = metric + 1;
                    routeEntry.setGatewayAddress(gateway);
                    routeEntry.setInterface(iface);                    
                }
                routeEntry.timestamp = System.currentTimeMillis();
            }
        }

        if (flag) boardcastRouteTable(table);
    }
}
