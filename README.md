# SimpliStash
A simple in-memory key-value database. For details and usage notes see the [wiki](https://github.com/youngbryanyu/SimpliStash/wiki).

## Installation
Run the following to clone the repository and run the setup script. 
```
git clone https://github.com/youngbryanyu/SimpliStash.git
cd SimpliStash
chmod +x setup.sh
./setup.sh
```

The setup script does the following:
- Compiles the source code
- Creates symbolic links to executables

## Running SimpliStash
To start the server, run `sstash <primaryPort> <readOnlyPort> [masterIp] [masterPort]`. See [Example](#example) for an example.

## Running the CLI
To run the CLI, run `sstash-cli <ip> <port>`. See [Example](#example) for an example.

## Example
1. `sstash 3000 3001`: Starts a master node with its main server on port 3000 and read-only server on port 3001 using.
2. `sstash-cli localhost 3000`: Connects to the master node's primary port using the CLI.
3. `sstash 4000 4001 localhost 3000`: Spins up a read-replica of the master node listening on port 3000. The read-replica's read-only server listens on port 4001.
4. `sstash-cli localhost 4001`: Connects to the read-replica's read-only port using the CLI.

For more details, see the [wiki](https://github.com/youngbryanyu/SimpliStash/wiki).

## Contributing
This project has currently been paused. If you are interested in running it or extending it, you will need to fork the repo. Reach out to `youngyu2002@gmail.com` for any questions.
