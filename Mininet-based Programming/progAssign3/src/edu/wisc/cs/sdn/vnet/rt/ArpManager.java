package edu.wisc.cs.sdn.vnet.rt;

import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;

public class ArpManager {
    private Router router;
    private ArpCache arpCache;
    private HashMap<Integer, Monitor> table;

    class Monitor {
        Thread thread;
        public Queue<Request> requestQueue;      
        
        int ip;
        Iface outIface;     
        ArpManager arpManager;                     

        Router router;

        Monitor (int ip, Iface outIface, ArpManager arpManager, Router router) {
            this.requestQueue = new LinkedList<Request>();
            this.ip = ip;            
            this.outIface = outIface;
            this.arpManager = arpManager;
            this.router = router;

            thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 3; ++i) {
                            Wrapper.makeArpRequestPacket(outIface, ip);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) { return; }

                    for (Request req: requestQueue) 
                        router.sendPacket(Wrapper.makeICMPPacket(req.inIface, req.etherPacket, 3, 1), req.inIface);                        
                    arpManager.secureRemove(ip);
                }
            });
            thread.start();
        }        

        void handleReply(byte[] mac) {
            thread.interrupt();
            for (Request req: requestQueue) {
                router.sendPacket(req.etherPacket.setDestinationMACAddress(mac), outIface);
            }
            arpManager.secureRemove(ip);
        }
    };

    class Request{
        Ethernet etherPacket;
        Iface inIface;

        Request (Ethernet etherPacket, Iface inIface) {
            this.etherPacket = etherPacket;
            this.inIface = inIface;
        }
    };

    ArpManager (Router router, ArpCache arpCache) {
        this.router = router;
        this.arpCache = arpCache;
        this.table = new HashMap<>();
    }

    void handleReply(Ethernet etherPacket, Iface inIface) {
        ARP arpPacket = (ARP) etherPacket.getPayload();
        int ip = IPv4.toIPv4Address(arpPacket.getSenderProtocolAddress());
        MACAddress mac = new MACAddress(arpPacket.getSenderHardwareAddress());

        synchronized (table) {
            Monitor monitor = table.get(ip);
            if (monitor != null) {
                monitor.handleReply(mac.toBytes());
                table.remove(ip);
            }            
        }
        arpCache.insert(mac, ip);
    }

    void generateRequest(Ethernet etherPacket, Iface inIface, int ip, Iface outIface) {
        synchronized (table) {
            Monitor monitor = table.get(ip);
            if (monitor == null) {
                monitor = new Monitor(ip, outIface, this, router);
                table.put(ip, monitor);
            }
            monitor.requestQueue.add(new Request(etherPacket, inIface));
        }
    }

    void secureRemove(int ip) {
        synchronized (table) { table.remove(ip); }
    }
}
