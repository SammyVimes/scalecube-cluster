package io.scalecube.examples;

import static java.util.stream.Collectors.joining;

import io.scalecube.cluster.Cluster;
import io.scalecube.cluster.ClusterConfig;
import io.scalecube.cluster.ClusterImpl;
import io.scalecube.cluster.Member;
import io.scalecube.transport.netty.tcp.TcpTransportFactory;
import java.util.Collections;
import java.util.Map;

/**
 * Example how to create {@link Cluster} instances and join them to cluster.
 *
 * @author Anton Kharenko
 */
public class ClusterJoinExamples {

  /** Main method. */
  public static void main(String[] args) {
    // Start seed member Alice
    Cluster alice =
        new ClusterImpl()
            .config(opts -> opts.memberAlias("Alice"))
            .transportFactory(TcpTransportFactory::new)
            .startAwait();

    // Join Bob to cluster with Alice
    Cluster bob =
        new ClusterImpl()
            .config(opts -> opts.memberAlias("Bob"))
            .membership(opts -> opts.seedMembers(alice.address()))
            .transportFactory(TcpTransportFactory::new)
            .startAwait();

    // Join Carol to cluster with metadata
    Map<String, String> metadata = Collections.singletonMap("name", "Carol");
    Cluster carol =
        new ClusterImpl()
            .config(opts -> opts.memberAlias("Carol").metadata(metadata))
            .membership(opts -> opts.seedMembers(alice.address()))
            .transportFactory(TcpTransportFactory::new)
            .startAwait();

    // Start Dan on port 3000
    ClusterConfig configWithFixedPort =
        new ClusterConfig()
            .memberAlias("Dan")
            .membership(opts -> opts.seedMembers(alice.address()))
            .transport(opts -> opts.port(3000));
    Cluster dan =
        new ClusterImpl(configWithFixedPort)
            .transportFactory(TcpTransportFactory::new)
            .startAwait();

    // Start Eve in separate cluster (separate sync group)
    ClusterConfig configWithSyncGroup =
        new ClusterConfig()
            .memberAlias("Eve")
            .membership(
                opts ->
                    opts.seedMembers(
                            alice.address(),
                            bob.address(),
                            carol.address(),
                            dan.address()) // won't join anyway
                        .namespace("another-cluster"));
    Cluster eve =
        new ClusterImpl(configWithSyncGroup)
            .transportFactory(TcpTransportFactory::new)
            .startAwait();

    // Print cluster members of each node

    System.out.println(
        "Alice ("
            + alice.address()
            + ") cluster: "
            + alice.members().stream().map(Member::toString).collect(joining("\n", "\n", "\n")));

    System.out.println(
        "Bob ("
            + bob.address()
            + ") cluster: "
            + bob.members().stream().map(Member::toString).collect(joining("\n", "\n", "\n")));

    System.out.println(
        "Carol ("
            + carol.address()
            + ") cluster: "
            + carol.members().stream().map(Member::toString).collect(joining("\n", "\n", "\n")));

    System.out.println(
        "Dan ("
            + dan.address()
            + ") cluster: "
            + dan.members().stream().map(Member::toString).collect(joining("\n", "\n", "\n")));

    System.out.println(
        "Eve ("
            + eve.address()
            + ") cluster: " // alone in cluster
            + eve.members().stream().map(Member::toString).collect(joining("\n", "\n", "\n")));
  }
}
