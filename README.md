# SimpliStash
A Stupid Simple In-Memory Key-Value Database. For more details and usage notes see the [wiki](https://github.com/youngbryanyu/SimpliStash/wiki).

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
To start the server, run `sstash`.

## Running the CLI
To run the CLI, run `sstash-cli <ip> <port>`. If running locally this is just `sstash-cli localhost 3000` by default.
