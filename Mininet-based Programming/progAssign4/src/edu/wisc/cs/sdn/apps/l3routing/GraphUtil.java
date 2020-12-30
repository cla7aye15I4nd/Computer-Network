package edu.wisc.cs.sdn.apps.l3routing;

import net.floodlightcontroller.routing.Link;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class GraphUtil {
    static class Edge {
        Long dist;
        Link link;

        Edge (Long dist, Link link) {
            this.dist = dist;
            this.link = link;
        }
    };

    static HashMap<Long, HashMap<Long, Edge>> makeTable(Collection<Long> nodes, Collection<Link> links) {
        HashMap<Long, HashMap<Long, Edge>> graph;
        HashMap<Long, HashMap<Long, Edge>> table;

        graph = new HashMap<Long, HashMap<Long, Edge>>();
        table = new HashMap<Long, HashMap<Long, Edge>>();
        for (Long from: nodes) {
            HashMap<Long, Edge> map = new HashMap<Long, Edge>();
            for (Long to: nodes) {
                if (from == to)
                    map.put(to, new Edge(0L, null));
                else
                    map.put(to, new Edge(Long.MAX_VALUE, null));
            }            
            graph.put(from, new HashMap<Long, Edge>());
            table.put(from, map);
        }

        for (Link link: links) {
            Long u = link.getSrc(), v = link.getDst();
            graph.get(u).put(v, new Edge(1L, link));
            graph.get(v).put(u, new Edge(1L, link));
        }

        for (Long node: nodes) {            
            HashSet<Long> set = new HashSet<Long>();
            HashMap<Long, Edge> dist = table.get(node);
            Queue<Long> q = new LinkedList<Long>();

            q.offer(node);
            set.add(node);
            while (!q.isEmpty()) {                
                Long u = q.poll();
                
                for (Map.Entry<Long, Edge> entry: graph.get(u).entrySet()) {
                    Long v = entry.getKey();
                    if (!set.contains(v)) {                        
                        if (dist.get(u).dist == 0) 
                            dist.put(v, new Edge(dist.get(u).dist + 1, entry.getValue().link));
                        else
                            dist.put(v, new Edge(dist.get(u).dist + 1, dist.get(u).link));
                        set.add(v);
                        q.offer(v);
                    }
                }
            }
        }

        return table;
    }
}
