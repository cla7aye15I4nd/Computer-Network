package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.RIPv2;
import net.floodlightcontroller.packet.RIPv2Entry;
import net.floodlightcontroller.packet.Ethernet;

public class Wrapper {
    static Ethernet makeICMPPacket(Iface iface, Ethernet etherPacket, int type, int code) {
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

    static Ethernet makeICMPEchoPacket(Iface iface, Ethernet etherPacket) {
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

    static Ethernet makeArpReplyPacket(Iface iface, Ethernet etherPacket) {
        ARP arpPacket = (ARP) etherPacket.getPayload();
        return 
            (Ethernet) new Ethernet()
            .setEtherType(Ethernet.TYPE_ARP)
            .setSourceMACAddress(iface.getMacAddress().toBytes())
            .setDestinationMACAddress(etherPacket.getSourceMACAddress())
            .setPayload(
                new ARP()
                .setHardwareType(ARP.HW_TYPE_ETHERNET)
                .setProtocolType(ARP.PROTO_TYPE_IP)
                .setHardwareAddressLength((byte) Ethernet.DATALAYER_ADDRESS_LENGTH)
                .setProtocolAddressLength((byte) 4)
                .setOpCode(ARP.OP_REPLY)
                .setSenderHardwareAddress(iface.getMacAddress().toBytes())
                .setSenderProtocolAddress(iface.getIpAddress())
                .setTargetHardwareAddress(arpPacket.getSenderHardwareAddress())
                .setTargetProtocolAddress(arpPacket.getSenderProtocolAddress())
            );
    }

    static Ethernet makeArpRequestPacket(Iface iface, int ip) {
        return 
            (Ethernet) new Ethernet()
            .setEtherType(Ethernet.TYPE_ARP)
            .setSourceMACAddress(iface.getMacAddress().toBytes())
            .setDestinationMACAddress("FF:FF:FF:FF:FF:FF")
            .setPayload(
                new ARP()
                .setHardwareType(ARP.HW_TYPE_ETHERNET)
                .setProtocolType(ARP.PROTO_TYPE_IP)
                .setHardwareAddressLength((byte) Ethernet.DATALAYER_ADDRESS_LENGTH)
                .setProtocolAddressLength((byte) 4)
                .setOpCode(ARP.OP_REQUEST)
                .setSenderHardwareAddress(iface.getMacAddress().toBytes())
                .setSenderProtocolAddress(iface.getIpAddress())
                .setTargetHardwareAddress(new byte[Ethernet.DATALAYER_ADDRESS_LENGTH])
                .setTargetProtocolAddress(ip)
            );
    }

    static Ethernet makeRipRequestPacket(Iface iface) {
        RIPv2 ripPacket = new RIPv2();
        ripPacket.setCommand(RIPv2.COMMAND_REQUEST);
        return 
            (Ethernet) new Ethernet()
            .setEtherType(Ethernet.TYPE_IPv4)
            .setSourceMACAddress(iface.getMacAddress().toBytes())
            .setDestinationMACAddress("FF:FF:FF:FF:FF:FF")
            .setPayload(
                (IPv4) new IPv4()
                .setTtl((byte) 15)
                .setProtocol(IPv4.PROTOCOL_UDP)
                .setDestinationAddress(IPv4.toIPv4Address("224.0.0.9"))
                .setSourceAddress(iface.getIpAddress())
                .setPayload(
                    (UDP) new UDP()
                    .setSourcePort(UDP.RIP_PORT)
                    .setDestinationPort(UDP.RIP_PORT)
                    .setPayload(ripPacket)
                )
            );
    }

    static RIPv2 makeRipReponsePacketHook(RouteTable table) {
        RIPv2 ripPacket = new RIPv2();
        for (RouteEntry entry: table.getEntries()) 
            ripPacket.addEntry(
                new RIPv2Entry(
                    entry.getDestinationAddress(),
                    entry.getMaskAddress(),
                    entry.getMetric()));
        return ripPacket;
    }

    static Ethernet makeRipReponsePacket(Iface iface, RIPv2 ripPacket, String mac, int ip) {
        return 
            (Ethernet) new Ethernet()
            .setEtherType(Ethernet.TYPE_IPv4)
            .setSourceMACAddress(iface.getMacAddress().toBytes())
            .setDestinationMACAddress(mac)
            .setPayload(
                (IPv4) new IPv4()
                .setTtl((byte) 15)
                .setProtocol(IPv4.PROTOCOL_UDP)
                .setDestinationAddress(ip)
                .setSourceAddress(iface.getIpAddress())
                .setPayload(
                    (UDP) new UDP()
                    .setSourcePort(UDP.RIP_PORT)
                    .setDestinationPort(UDP.RIP_PORT)
                    .setPayload(ripPacket)
                )
            );
    }
}
