package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.Ethernet;

public class ICMPWrapper {
    static Ethernet makePacket(Iface iface, Ethernet etherPacket, int type, int code) {
        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        byte[] data = new byte[ipPacket.getHeaderLength()*4 + 12];
        System.arraycopy(ipPacket.serialize(), 0, data, 4, data.length - 4);

        return 
            (Ethernet) new Ethernet()
            .setEtherType(Ethernet.TYPE_IPv4)
            .setSourceMACAddress(iface.getMacAddress().toBytes())
            .setDestinationMACAddress(etherPacket.getSourceMACAddress())
            .setPayload(
                (IPv4) new IPv4()
                .setTtl((byte) 64)
                .setProtocol(IPv4.PROTOCOL_ICMP)
                .setDestinationAddress(ipPacket.getSourceAddress())
                .setSourceAddress(iface.getIpAddress())
                .setPayload(
                    (ICMP) new ICMP()
                    .setIcmpType((byte) type)
                    .setIcmpCode((byte) code)
                    .setPayload(
                        new Data(data)
                    )
                )
            );
    }

    static Ethernet makeEchoPacket(Iface iface, Ethernet etherPacket) {
        IPv4 ipPacket = (IPv4) etherPacket.getPayload();
        return 
            (Ethernet) new Ethernet()
            .setEtherType(Ethernet.TYPE_IPv4)
            .setSourceMACAddress(iface.getMacAddress().toBytes())
            .setDestinationMACAddress(etherPacket.getSourceMACAddress())
            .setPayload(
                (IPv4) new IPv4()
                .setTtl((byte) 64)
                .setProtocol(IPv4.PROTOCOL_ICMP)
                .setDestinationAddress(ipPacket.getSourceAddress())
                .setSourceAddress(ipPacket.getDestinationAddress())
                .setPayload(
                    (ICMP) new ICMP()
                    .setIcmpType((byte) 0)
                    .setIcmpCode((byte) 0)
                    .setPayload(
                        new Data(ipPacket.getPayload().getPayload().serialize())
                    )
                )
            );
    }
}
