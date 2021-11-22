# chordp2p

_Authors: [Caleb Carlson](https://github.com/inf0rmatiker), [Menuka Warushavithana](https://github.com/menuka94)

Team-based implementation of Chord's Peer-to-Peer Distributed Hashtable (DHT) written in Java, to complete
[CS555: Distributed Systems](https://www.cs.colostate.edu/~cs555/)' [Assignment 2](docs/CS555-Fall2021-HW2.pdf) *(click link to view description)*.
Implementation includes [Peers](#Peers), a [Discovery Node](#Discovery-Node), and a [Client](#Client).
Requests are managed using a thread-per-request threading model, and a custom Socket-based messaging implementation
with messages marshalled/unmarshalled into efficient byte structures.

## Peers

Decentralized and responsible for the actual storage of data items. Organized into a ring configuration, per Chord's specification.
Each Peer maintains information about the data stored, their successor Peer, and their predecessor Peer.
Also maintains a **Finger Table**, as discussed below:

### Data Structure: Finger Table

Contains exactly 16 entries. Each entry *i* in the Finger Table for Peer *p* is the ID and IP address of the _successor(2^(i-1) + p)_.
For example, entry _i = 3_ in the Finger Table for Peer *p = 16,000* would be the successor Peer of the *2^(2) + 16,000 = 16,004*th position
in the ring. This Finger Table is dynamically updated with new nodes joining and leaving the network.

### Operation: Finding `successor(k)`

A request for the true successor of ID _k_ is satisfied by either the current Peer, if it is the successor of _k_, or if it knows
the true successor of _k_. If it does not know the true successor of _k_ in the ring, it forwards the request to the closest Peer _p'_
to _k_ such that _p' < k_.

### Operation: Storing Data Item

Data items with an ID strictly less than or equal to our Peer ID can be stored locally.

## Discovery Node

Responsible for keeping track of Peer IP addresses and their identifiers, so that one can be used at random as an entry-point to the DHT.
Not a critical component of the system, but acts like trackers do in other distributed networks-- a discovery service.

### Operation: Peer Join

Every time a peer joins the network, it reaches out to the Discovery Node, and the Discovery Node returns the network information
of a random peer that is currently being tracked. The random selection of a node provides a basic load-balancing mechanism.
Once a random peer is 

## Client

Responsible for interacting with the Peers directly to store/retrieve data items.

## Identifiers

16 bits, allowing for up to 2^16 = 65,535 unique values. Represented as a 4-digit hex value.
Computed as a digest of each data item that is stored in the DHT. This digest determines where in the ring (which Peer) the data item
is stored. The first Peer in the ring that has an ID _p_ greater than or equal to the ID of the data item _k_ is the Peer responsible
for storing that data item.

## Usage

*Note:* The Discovery Server, Peer, and Client (store-data) are all interactive and take STDIN once spun up.

Using the `Makefile`:
- Clean the previous build: `make clean`
- Recompile and build the project: `make build`
- Run the Discovery Server: `make discovery`
- Run a Peer: `make peer DISCOVERY=<discovery_server_hostname> ID=<peer_id>`
- Store data: `make store-data DISCOVERY=<discovery_server_hostname>`

## Example Case Usage

### Start Discovery Server

- `make discovery` (on arkansas.cs.colostate.edu)

### Adding Peers

- `make peer DISCOVERY=arkansas ID=83d8`
- `make peer DISCOVERY=arkansas ID=f5c5`
- `make peer DISCOVERY=arkansas ID=1e4a`
- `make peer DISCOVERY=arkansas ID=fffe`
- `make peer DISCOVERY=arkansas ID=1539`

### Storing Data

- FileDir: `./test-data`

| FileName  | Digest |
|-----------|--------|
| img1.jpg  | 1714   |
| img2.jpg  | 608c   |
| img3.jpg  | 6c8e   |
| img4.jpg  | ec5d   |
| img5.jpg  | 9783   |
| img6.jpg  | 8640   |
| img7.jpg  | d687   |
| img8.jpg  | 505c   |
| img9.jpg  | 34dd   |
| img10.jpg | d0d1   |

- `make store-data DISCOVERY=arkansas`
- `> add-file test-data/img1.jpg`