Hello,
execute these commands one by one:
commands are lines starting with a '>'

0. if AutoCompile doesn't work as expected, execute make again
> make clean && make

1. start an AggregationServer (u can stay in current directory where 'makefile' is)
> make AggregationServer

2. start a ContentServer
> make ContentServer

3. start a GETClient
> make GETClient

4. u can type "put" in ContentServer window, then it will read local file, generate a PUT message and send it to AggregationServer
> put

5. u can type "get" in GETClient window, then it will generate a GET message, send it to AggregationServer and parse received message from AggregationServer. It could be a correct weather info or error code
> get

6. Now, u can type "put" or "get" in their own windows in any order u want.
# you can see output details in server's window or clients' windows.

**Explanation**:
   a. Once AS received a PUT request and executed successfully, AS will send a Heartbeat Check to this CS every 15s
   meanwhile attach its Lamport Clock with this messaage to CS for syncronization.
   b. AS has a PriorityQueue for save the weather feed temporarily. Everytime it add a new entry it will save the info +
   Lamport clock.
   c. AS also has a cache.txt for storing these entries in PriorityQueue in case of crash.
   d. Clients will always get the weather entry with the maximum Lamport Clock number, which is type of Long.
   e. If PriorityQueue's size>20, it will move the older entries into a backup.txt and only keep the 20 newest ones in
   the queue.