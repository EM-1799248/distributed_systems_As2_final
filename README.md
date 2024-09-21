# Instructions to run
*Notes:*
- requires Gson to be installed
<br>
<br>
<br>

1. To compile all files, type the following command in a terminal.
   ``` Bash
   make
   ```

2. Start the Aggregation Server by typing the following command in the same terminal. To exit use ctrl+c.
   ``` Bash
   make run-server
   ```

3. Start the Content Server by typing the following command in a new terminal. To exit use ctrl+c.
   ``` Bash
   make run-content
   ```

4. Send a GET Request through a client by typing the following command in a new terminal. To exit use ctrl+c.
   ``` Bash
   make run-client
   ```

5. Clean compiled files by typing the following command.
   ``` Bash
   make clean
   ```