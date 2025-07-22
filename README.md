
# About This Project

Minecraft faces a scalability issue: as the number of players increases, performance quickly degrades.

This is fundamentally a scaling problem. As more clients connect, the number of chunk loading requests rises accordingly. This forces the main game thread to manage an increasing number of Entities, BlockEntities, and BlockUpdates, while also handling more requests for generating Chunks that haven't yet been created.

The problem can be solved by distributing the load across multiple systems.

To address this, the system should follow these key principles:

- Each server should be solely responsible for managing a specific section of the world.

- The system must handle seamless player transfers between servers, without the client being aware of the change.

- The client should continue to receive all game information as if connected to a single server, even though that data may be distributed across multiple servers.

The Minecraft world is dividen in a ring configuration, each ring is handled by only one server

<img width="501" height="484" alt="anillos" src="https://github.com/user-attachments/assets/b82bb872-19be-4c9a-9b56-30bdfe7d4b5d" />

Player and entities can seamlesly transfer between servers.

//GiF

The next image shows a wide view of the architecture of the solution.

<img width="1692" height="754" alt="arquitectura-detallada" src="https://github.com/user-attachments/assets/07a3007f-21bc-41e7-ab1d-047b56b9d01a" />

# Performance improvements

The next graphs shows the mspt of 3 diferent loads with 3,5 and 11 load units in an ordinary server. Each load unit consists of an aproximation of a real player load on the server. (84 entities, 267 block updates y 5120 block entities). Notice how the 11 load excedes the 50ms and has TPS drop.

<img width="1461" height="805" alt="1-ms-1" src="https://github.com/user-attachments/assets/781b4675-906b-46ec-9a6a-725c6a83a044" />

The new distributed system handles the same load across 11 Workers with no TPS drop.

<img width="1401" height="805" alt="1-ms-11" src="https://github.com/user-attachments/assets/d58b67e2-198d-499a-913e-303abf9491be" />

